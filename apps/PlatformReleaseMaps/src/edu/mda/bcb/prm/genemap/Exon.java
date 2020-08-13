// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 University of Texas MD Anderson Cancer Center
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Tod-Casasent
 */
public class Exon implements Comparable<Exon>
{
	public String mExonType = null;			// col 2
	public String mChromosome = null;		// col 1	chromosome
	public long mStart = 0;					// col 4	start
	public long mEnd = 0;					// col 5	stop
	public String mStrand = null;			// col 7	strand
	public String mEnsemblId = null;		// col 9	"gene_id" (Ensembl id)
	public String mGeneSymbol = null;		// col 9	"gene_name" (Hugo name)
	public String mGeneSource = null;		// col 9	"gene_source" (such as, ensembl_havana)
	public String mTranscriptId = null;		// col 9	"transcript_id" (Ensembl id)
	public String mTranscriptSymbol = null;	// col 9	"transcript_name" (Hugo name?)
	public int mExonNumber = 0;				// col 9	"exon_number" (integer)
	public String mExonId = null;			// col 9	"exon_id" (Ensembl id)
	
	public Exon(String theGeneSymbol, String theEnsemblId, String theExonType, String theGeneSource,
			String theChromosome, long theStart, long theEnd, String theStrand,
			String theTranscriptId, String theTranscriptSymbol,
			int theExonNumber, String theExonId)
	{
		mGeneSource = theGeneSource;
		mGeneSymbol = theGeneSymbol;
		mEnsemblId = theEnsemblId;
		mExonType = theExonType;
		mChromosome = theChromosome;
		mStart = theStart;
		mEnd = theEnd;
		mStrand = theStrand;
		mTranscriptId = theTranscriptId;
		mTranscriptSymbol = theTranscriptSymbol;
		mExonNumber = theExonNumber;
		mExonId = theExonId;
	}

	@Override
	public int compareTo(Exon o)
	{
		int ret = this.mGeneSymbol.compareTo(o.mGeneSymbol);
		if (0==ret)
		{
			ret = this.mEnsemblId.compareTo(o.mEnsemblId);
			if (0==ret)
			{
				ret = this.mExonType.compareTo(o.mExonType);
				if (0==ret)
				{
					ret = this.mTranscriptId.compareTo(o.mTranscriptId);
					if (0==ret)
					{
						ret = Integer.compare(this.mExonNumber, o.mExonNumber);
						if (0==ret)
						{
							ret = this.mExonId.compareTo(o.mExonId);
						}
					}
				}
			}
		}
		return ret;
	}

	static public TreeMap<String, Exon> processEnsembleFileList37(String theZipFile) throws IOException, Exception
	{
		System.out.println("input file 37: " + theZipFile);
		TreeMap<String, ArrayList<Exon>> exons = new TreeMap<>();
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
						if ("exon".equals(type))
						{
							String start = splitted[3];
							String end = splitted[4];
							String strand = splitted[6];
							String geneSource = EnsemblGeneMap.extractFromLastColumn("gene_source", splitted[8]);
							String geneName = EnsemblGeneMap.extractGeneName(splitted[8]);
							String [] splitName = geneName.split("\\|", -1);
							String geneSymbol = splitName[0];
							String ensemblId = splitName[1];
							String transcriptId = EnsemblGeneMap.extractFromLastColumn("transcript_id", splitted[8]);
							String transcriptSymbol = EnsemblGeneMap.extractFromLastColumn("transcript_name", splitted[8]);
							int exonNumber = Integer.decode(EnsemblGeneMap.extractFromLastColumn("exon_number", splitted[8]));
							String exonId = EnsemblGeneMap.extractFromLastColumn("exon_id", splitted[8]);
							// this is type as in protein gene, pseudogene, lincrna, etc.
							String exonType = splitted[1];
							ArrayList<Exon> locs = exons.get(geneName);
							if (null==locs)
							{
								locs = new ArrayList<>();
							}
							locs.add(new Exon(geneSymbol, ensemblId, exonType, geneSource,
									chromosome, Long.valueOf(start), Long.valueOf(end), strand, 
									transcriptId, transcriptSymbol, 
									exonNumber, exonId));
							exons.put(geneName, locs);
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
		System.out.println("process exons names");
		TreeMap<String, Exon> finalSelections = new TreeMap<>();
		count = 0;
		try
		{
			System.out.print("Exon");
			for(Map.Entry<String, ArrayList<Exon>> entry : exons.entrySet())
			{
				if (0== (count%2000))
				{
					System.out.print(" " + count);
				}
				ArrayList<Exon> locs = entry.getValue();
				String geneSymbol = entry.getKey();
				for(Exon myLoc : locs)
				{
					// only one entry, use it
					finalSelections.put(geneSymbol + "|" + myLoc.mTranscriptId + "|" + EnsemblGeneMap.padToString(myLoc.mExonNumber), myLoc);
				}
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		System.out.println("return final Exons");
		return finalSelections;
	}

	static public TreeMap<String, Exon> processEnsembleFileList38(String theZipFile) throws IOException, Exception
	{
		System.out.println("input file 37: " + theZipFile);
		TreeMap<String, ArrayList<Exon>> exons = new TreeMap<>();
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
						if ("exon".equals(type))
						{
							String start = splitted[3];
							String end = splitted[4];
							String strand = splitted[6];
							String geneSource = EnsemblGeneMap.extractFromLastColumn("gene_source", splitted[8]);
							String geneName = EnsemblGeneMap.extractGeneName(splitted[8]);
							String [] splitName = geneName.split("\\|", -1);
							String geneSymbol = splitName[0];
							String ensemblId = splitName[1];
							String transcriptId = EnsemblGeneMap.extractFromLastColumn("transcript_id", splitted[8]);
							String transcriptSymbol = EnsemblGeneMap.extractFromLastColumn("transcript_name", splitted[8]);
							int exonNumber = Integer.decode(EnsemblGeneMap.extractFromLastColumn("exon_number", splitted[8]));
							String exonId = EnsemblGeneMap.extractFromLastColumn("exon_id", splitted[8]);
							// this is type as in protein gene, pseudogene, lincrna, etc.
							String exonType = EnsemblGeneMap.extractFromLastColumn("gene_biotype", splitted[8]);
							ArrayList<Exon> locs = exons.get(geneName);
							if (null==locs)
							{
								locs = new ArrayList<>();
							}
							locs.add(new Exon(geneSymbol, ensemblId, exonType, geneSource,
									chromosome, Long.valueOf(start), Long.valueOf(end), strand, 
									transcriptId, transcriptSymbol, 
									exonNumber, exonId));
							exons.put(geneName, locs);
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
		System.out.println("process exons names");
		TreeMap<String, Exon> finalSelections = new TreeMap<>();
		count = 0;
		try
		{
			System.out.print("Exon");
			for(Map.Entry<String, ArrayList<Exon>> entry : exons.entrySet())
			{
				if (0== (count%2000))
				{
					System.out.print(" " + count);
				}
				ArrayList<Exon> locs = entry.getValue();
				String geneSymbol = entry.getKey();
				for(Exon myLoc : locs)
				{
					// only one entry, use it
					finalSelections.put(geneSymbol + "|" + myLoc.mTranscriptId + "|" + EnsemblGeneMap.padToString(myLoc.mExonNumber), myLoc);
				}
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		System.out.println("return final Exons");
		return finalSelections;
	}

	static public TreeMap<String, Exon> processEnsembleFileListGDC38(String theFile) throws IOException, Exception
	{
		System.out.println("input file GDC38: " + theFile);
		TreeMap<String, ArrayList<Exon>> exons = new TreeMap<>();
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
						if ("exon".equals(type))
						{
							String start = splitted[3];
							String end = splitted[4];
							String strand = splitted[6];
							String geneSource = splitted[1].toLowerCase();
							String geneName = EnsemblGeneMap.extractGeneName(splitted[8]);
							String [] splitName = geneName.split("\\|", -1);
							String geneSymbol = splitName[0];
							String ensemblId = splitName[1];
							String transcriptId = EnsemblGeneMap.extractFromLastColumn("transcript_id", splitted[8]);
							String transcriptSymbol = EnsemblGeneMap.extractFromLastColumn("transcript_name", splitted[8]);
							int exonNumber = Integer.decode(EnsemblGeneMap.extractFromLastColumn("exon_number", splitted[8]));
							String exonId = EnsemblGeneMap.extractFromLastColumn("exon_id", splitted[8]);
							// this is type as in protein gene, pseudogene, lincrna, etc.
							String exonType = EnsemblGeneMap.extractFromLastColumn("gene_type", splitted[8]);
							ArrayList<Exon> locs = exons.get(geneName);
							if (null==locs)
							{
								locs = new ArrayList<>();
							}
							locs.add(new Exon(geneSymbol, ensemblId, exonType, geneSource,
									chromosome, Long.valueOf(start), Long.valueOf(end), strand, 
									transcriptId, transcriptSymbol, 
									exonNumber, exonId));
							exons.put(geneName, locs);
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
		System.out.println("process exons names");
		TreeMap<String, Exon> finalSelections = new TreeMap<>();
		count = 0;
		try
		{
			System.out.print("Exon");
			for(Map.Entry<String, ArrayList<Exon>> entry : exons.entrySet())
			{
				if (0== (count%2000))
				{
					System.out.print(" " + count);
				}
				ArrayList<Exon> locs = entry.getValue();
				String geneSymbol = entry.getKey();
				for(Exon myLoc : locs)
				{
					// only one entry, use it
					finalSelections.put(geneSymbol + "|" + myLoc.mTranscriptId + "|" + EnsemblGeneMap.padToString(myLoc.mExonNumber), myLoc);
				}
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		System.out.println("return final Exons");
		return finalSelections;
	}

	static public TreeMap<String, Exon> processEnsembleFileList36(String theZipFile) throws IOException, Exception
	{
		System.out.println("input file 36: " + theZipFile);
		TreeMap<String, ArrayList<Exon>> exons = new TreeMap<>();
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
					if (true=="exon".equals(splitted[2]))
					{
						long start = Long.valueOf(splitted[3]);
						long end = Long.valueOf(splitted[4]);
						String strand = splitted[6];
						String geneName = EnsemblGeneMap.extractGeneName(splitted[8]);
						String [] splitName = geneName.split("\\|", -1);
						String geneSymbol = splitName[0];
						String ensemblId = splitName[1];
						String transcriptId = EnsemblGeneMap.extractFromLastColumn("transcript_id", splitted[8]);
						String transcriptSymbol = EnsemblGeneMap.extractFromLastColumn("transcript_name", splitted[8]);
						String exonId = EnsemblGeneMap.extractFromLastColumn("exon_number", splitted[8]);
						int exonNumber = Integer.decode(exonId);
						// this is type as in protein gene, pseudogene, lincrna, etc.
						String exonType = splitted[1];
						ArrayList<Exon> locs = exons.get(geneName);
						if (null==locs)
						{
							locs = new ArrayList<>();
						}
						locs.add(new Exon(geneSymbol, ensemblId, exonType, "HG18",
								".", Long.valueOf(start), Long.valueOf(end), strand, 
								transcriptId, transcriptSymbol, 
								exonNumber, exonId));
						exons.put(geneName, locs);
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
		System.out.println("process exons names");
		TreeMap<String, Exon> finalSelections = new TreeMap<>();
		count = 0;
		try
		{
			System.out.print("Exon");
			for(Map.Entry<String, ArrayList<Exon>> entry : exons.entrySet())
			{
				if (0== (count%2000))
				{
					System.out.print(" " + count);
				}
				ArrayList<Exon> locs = entry.getValue();
				String geneSymbol = entry.getKey();
				for(Exon myLoc : locs)
				{
					// only one entry, use it
					finalSelections.put(geneSymbol + "|" + myLoc.mTranscriptId + "|" + EnsemblGeneMap.padToString(myLoc.mExonNumber), myLoc);
				}
				count = count + 1;
			}
		}
		finally
		{
			System.out.println(" " + count);
		}
		return finalSelections;
	}

}
