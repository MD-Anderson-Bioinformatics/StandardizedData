// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.prm.genemap;

import edu.mda.bcb.prm.PlatformReleaseMaps;
import static edu.mda.bcb.prm.genemap.EnsemblGeneMap.M_VALID_CHROMOSOMES;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Tod-Casasent
 */
public class Gene implements Comparable<Gene>
{
	public String mGeneSymbol = null;
	public String mEnsemblId = null;
	public String mGeneType = null;
	public String mGeneSource = null;
	public String mChromosome = null;
	public long mStart = 0;
	public long mEnd = 0;
	public String mStrand = null;
	
	public Gene(String theGeneSymbol, String theEnsemblId, String theGeneType, String theGeneSource,
			String theChromosome, long theStart, long theEnd, String theStrand)
	{
		mGeneSource = theGeneSource;
		mGeneSymbol = theGeneSymbol;
		mEnsemblId = theEnsemblId;
		mGeneType = theGeneType;
		mChromosome = theChromosome;
		mStart = theStart;
		mEnd = theEnd;
		mStrand = theStrand;
	}

	@Override
	public int compareTo(Gene o)
	{
		int ret = this.mChromosome.compareTo(o.mChromosome);
		if (0==ret)
		{
			ret = this.mStrand.compareTo(o.mStrand);
			if (0==ret)
			{
				ret = Long.compare(this.mStart, o.mStart);
				if (0==ret)
				{
					ret = Long.compare(this.mEnd, o.mEnd);
				}
			}
		}
		return ret;
	}

	static public TreeMap<String, Gene> processEnsembleFileList37(String theZipFile) throws IOException, Exception
	{
		System.out.println("input file 37: " + theZipFile);
		TreeMap<String, ArrayList<Gene>> genes = new TreeMap<>();
		long count = 0;
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theZipFile))
		{
			System.out.print("line");
			String line = br.readLine();
			while(null!=line)
			{
				if (0== (count%100000))
				{
					System.out.print(" " + count);
				}
				if (false==line.startsWith("#"))
				{
					String [] splitted = line.split("\t", -1);
					String chromosome = splitted[0];
					if (true==M_VALID_CHROMOSOMES.contains(chromosome))
					{
						// this is type as in gene, exon, etc
						String type = splitted[2];
						if ("gene".equals(type))
						{
							String start = splitted[3];
							String end = splitted[4];
							String strand = splitted[6];
							String geneSource = EnsemblGeneMap.extractFromLastColumn("gene_source", splitted[8]);
							String geneName = EnsemblGeneMap.extractGeneName(splitted[8]);
							String [] splitName = geneName.split("\\|", -1);
							String geneSymbol = splitName[0];
							String ensemblId = splitName[1];
							// this is type as in protein gene, pseudogene, lincrna, etc.
							String geneType = splitted[1];
							ArrayList<Gene> locs = genes.get(geneSymbol);
							if (null==locs)
							{
								locs = new ArrayList<>();
							}
							locs.add(new Gene(geneSymbol, ensemblId, geneType, geneSource,
									chromosome, Long.valueOf(start), Long.valueOf(end), strand));
							genes.put(geneSymbol, locs);
						}
					}
				}
				line = br.readLine();
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		System.out.println("process gene names");
		TreeMap<String, Gene> finalGenes = new TreeMap<>();
		count = 0;
		try
		{
			System.out.print("gene");
			for(Map.Entry<String, ArrayList<Gene>> entry : genes.entrySet())
			{
				if (0== (count%2000))
				{
					System.out.print(" " + count);
				}
				ArrayList<Gene> locs = entry.getValue();
				String geneSymbol = entry.getKey();
				Gene loc = locs.get(0);
				if (1==locs.size())
				{
					// only one entry, use it
					finalGenes.put(loc.mGeneSymbol + "|" + loc.mEnsemblId, loc);
				}
				else if (loc.mGeneType.toLowerCase().contains("rna"))
				{
					// multiple entries for RNA is OK
					for (Gene myLoc : locs)
					{
						finalGenes.put(myLoc.mGeneSymbol + "|" + myLoc.mEnsemblId, myLoc);
					}
				}
				else
				{
					// pick "both" or ensembl, then havana, then insdc (mitochondrial) if needed
					// ensembl_havana ensembl havana insdc
					String [] callers = { "ensembl_havana" ,"ensembl" ,"havana" ,"insdc" };
					Gene useMe = null;
					for (String calledBy : callers)
					{
						if(null==useMe)
						{
							for (Gene myLoc : locs)
							{
								if(null==useMe)
								{
									if(calledBy.equals(myLoc.mGeneSource))
									{
										useMe = myLoc;
									}
								}
							}
						}
					}
					if (null!=useMe)
					{
						finalGenes.put(useMe.mGeneSymbol + "|" + useMe.mEnsemblId, useMe);
					}
					else
					{
						throw new Exception("No gene selected from " + geneSymbol);
					}
				}
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		System.out.println("return final genes");
		return finalGenes;
	}

	static public TreeMap<String, Gene> processEnsembleFileList38(String theZipFile) throws IOException, Exception
	{
		System.out.println("input file 38: " + theZipFile);
		TreeMap<String, ArrayList<Gene>> genes = new TreeMap<>();
		long count = 0;
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theZipFile))
		{
			System.out.print("line");
			String line = br.readLine();
			while(null!=line)
			{
				if (0== (count%100000))
				{
					System.out.print(" " + count);
				}
				if (false==line.startsWith("#"))
				{
					String [] splitted = line.split("\t", -1);
					String chromosome = splitted[0];
					if (true==M_VALID_CHROMOSOMES.contains(chromosome))
					{
						// this is type as in gene, exon, etc
						String type = splitted[2];
						if ("gene".equals(type))
						{
							String start = splitted[3];
							String end = splitted[4];
							String strand = splitted[6];
							String geneSource = EnsemblGeneMap.extractFromLastColumn("gene_source", splitted[8]);
							String geneName = EnsemblGeneMap.extractGeneName(splitted[8]);
							String [] splitName = geneName.split("\\|", -1);
							String geneSymbol = splitName[0];
							String ensemblId = splitName[1];
							// this is type as in protein gene, pseudogene, lincrna, etc.
							String geneType = EnsemblGeneMap.extractFromLastColumn("gene_biotype", splitted[8]);
							ArrayList<Gene> locs = genes.get(geneSymbol);
							if (null==locs)
							{
								locs = new ArrayList<>();
							}
							locs.add(new Gene(geneSymbol, ensemblId, geneType, geneSource,
									chromosome, Long.valueOf(start), Long.valueOf(end), strand));
							genes.put(geneSymbol, locs);
						}
					}
				}
				line = br.readLine();
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		System.out.println("process gene names");
		TreeMap<String, Gene> finalGenes = new TreeMap<>();
		count = 0;
		try
		{
			System.out.print("gene");
			for(Map.Entry<String, ArrayList<Gene>> entry : genes.entrySet())
			{
				if (0== (count%2000))
				{
					System.out.print(" " + count);
				}
				ArrayList<Gene> locs = entry.getValue();
				String geneSymbol = entry.getKey();
				Gene loc = locs.get(0);
				if (1==locs.size())
				{
					// only one entry, use it
					finalGenes.put(loc.mGeneSymbol + "|" + loc.mEnsemblId, loc);
				}
				else if (loc.mGeneType.toLowerCase().contains("rna"))
				{
					// multiple entries for RNA is OK
					for (Gene myLoc : locs)
					{
						finalGenes.put(myLoc.mGeneSymbol + "|" + myLoc.mEnsemblId, myLoc);
					}
				}
				else
				{
					// pick "both" or ensembl, then havana, then insdc (mitochondrial) if needed
					// ensembl_havana ensembl havana insdc
					String [] callers = { "ensembl_havana" ,"ensembl" ,"havana" ,"insdc" };
					Gene useMe = null;
					for (String calledBy : callers)
					{
						if(null==useMe)
						{
							for (Gene myLoc : locs)
							{
								if(null==useMe)
								{
									if(calledBy.equals(myLoc.mGeneSource))
									{
										useMe = myLoc;
									}
								}
							}
						}
					}
					if (null!=useMe)
					{
						finalGenes.put(useMe.mGeneSymbol + "|" + useMe.mEnsemblId, useMe);
					}
					else
					{
						throw new Exception("No gene selected from " + geneSymbol);
					}
				}
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		System.out.println("return final genes");
		return finalGenes;
	}


	static public TreeMap<String, Gene> processEnsembleFileListGDC38(String theFile) throws IOException, Exception
	{
		System.out.println("input file GDC38: " + theFile);
		TreeMap<String, ArrayList<Gene>> genes = new TreeMap<>();
		long count = 0;
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theFile))
		{
			System.out.print("line");
			String line = br.readLine();
			while(null!=line)
			{
				if (0== (count%100000))
				{
					System.out.print(" " + count);
				}
				if (false==line.startsWith("#"))
				{
					String [] splitted = line.split("\t", -1);
					String chromosome = splitted[0].replace("chr", "");
					if (true==M_VALID_CHROMOSOMES.contains(chromosome))
					{
						// this is type as in gene, exon, etc
						String type = splitted[2];
						if ("gene".equals(type))
						{
							String start = splitted[3];
							String end = splitted[4];
							String strand = splitted[6];
							String geneSource = splitted[1].toLowerCase();
							String geneName = EnsemblGeneMap.extractGeneName(splitted[8]);
							String [] splitName = geneName.split("\\|", -1);
							String geneSymbol = splitName[0];
							String ensemblId = splitName[1];
							// this is type as in protein gene, pseudogene, lincrna, etc.
							String geneType = EnsemblGeneMap.extractFromLastColumn("gene_type", splitted[8]);
							ArrayList<Gene> locs = genes.get(geneSymbol);
							if (null==locs)
							{
								locs = new ArrayList<>();
							}
							locs.add(new Gene(geneSymbol, ensemblId, geneType, geneSource,
									chromosome, Long.valueOf(start), Long.valueOf(end), strand));
							genes.put(geneSymbol, locs);
						}
					}
				}
				line = br.readLine();
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		System.out.println("process gene names");
		TreeMap<String, Gene> finalGenes = new TreeMap<>();
		count = 0;
		try
		{
			System.out.print("gene");
			for(Map.Entry<String, ArrayList<Gene>> entry : genes.entrySet())
			{
				if (0== (count%2000))
				{
					System.out.print(" " + count);
				}
				ArrayList<Gene> locs = entry.getValue();
				String geneSymbol = entry.getKey();
				Gene loc = locs.get(0);
				if (1==locs.size())
				{
					// only one entry, use it
					finalGenes.put(loc.mGeneSymbol + "|" + loc.mEnsemblId, loc);
				}
				else if (loc.mGeneType.toLowerCase().contains("rna"))
				{
					// multiple entries for RNA is OK
					for (Gene myLoc : locs)
					{
						finalGenes.put(myLoc.mGeneSymbol + "|" + myLoc.mEnsemblId, myLoc);
					}
				}
				else
				{
					// pick "both" or ensembl, then havana, then insdc (mitochondrial) if needed
					// ensembl_havana ensembl havana insdc
					String [] callers = { "ensembl_havana" ,"ensembl" ,"havana" ,"insdc" };
					Gene useMe = null;
					for (String calledBy : callers)
					{
						if(null==useMe)
						{
							for (Gene myLoc : locs)
							{
								if(null==useMe)
								{
									if(calledBy.equals(myLoc.mGeneSource))
									{
										useMe = myLoc;
									}
								}
							}
						}
					}
					if (null!=useMe)
					{
						finalGenes.put(useMe.mGeneSymbol + "|" + useMe.mEnsemblId, useMe);
					}
					else
					{
						throw new Exception("No gene selected from " + geneSymbol);
					}
				}
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		System.out.println("return final genes");
		return finalGenes;
	}

	static public TreeMap<String, Gene> processEnsembleFileList36(String theZipFile) throws IOException, Exception
	{
		System.out.println("input file 36: " + theZipFile);
		TreeMap<String, Gene> genes = new TreeMap<>();
		long count = 0;
		System.out.print("line");
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theZipFile))
		{
			String line = br.readLine();
			while(null!=line)
			{
				if (0== (count%100000))
				{
					System.out.print(" " + count);
				}
				if (false==line.startsWith("#"))
				{
					String [] splitted = line.split("\t", -1);
					String chromosome = splitted[0];
					if (true==M_VALID_CHROMOSOMES.contains(chromosome))
					{
						long start = Long.valueOf(splitted[3]);
						long end = Long.valueOf(splitted[4]);
						String strand = splitted[6];
						String geneName = EnsemblGeneMap.extractGeneName(splitted[8]);
						String [] splitName = geneName.split("\\|", -1);
						String geneSymbol = splitName[0];
						String ensemblId = splitName[1];
						Gene location = genes.get(geneName);
						// this file does not have "gene" level entries.
						// so iterate through finding the largest end and smallest start locations
						// and verify same chromosome
						if (null==location)
						{
							location = new Gene(geneSymbol, ensemblId, "NA", "HG18",
									chromosome, start, end, strand);
						}
						if (false==chromosome.equals(location.mChromosome))
						{
							throw new Exception("Chromosome does not match " + geneName);
						}
						if (false==strand.equals(location.mStrand))
						{
							throw new Exception("Strand does not match " + geneName);
						}
						if (start<location.mStart)
						{
							location.mStart = start;
						}
						if (location.mEnd<end)
						{
							location.mEnd = end;
						}
						genes.put(geneName, location);
					}
				}
				line = br.readLine();
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		TreeMap<String, Gene> finalGenes = new TreeMap<>();
		count = 0;
		System.out.print("gene");
		try
		{
			for(Map.Entry<String, Gene> entry : genes.entrySet())
			{
				if (0== (count%2000))
				{
					System.out.print(" " + count);
				}
				Gene loc = entry.getValue();
				String geneName = entry.getKey();
				finalGenes.put(geneName, loc);
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		return finalGenes;
	}

}
