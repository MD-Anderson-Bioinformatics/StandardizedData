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

package edu.mda.bcb.prm.gdcidentifiers;

import edu.mda.bcb.prm.PlatformReleaseMaps;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author tdcasasent
 */
public class PrepFiles
{
	public PrepFiles()
	{
	}

	public TreeSet<String> run(String theDataInDir, String theDataOutDir) throws IOException, Exception
	{
		TreeSet<String> resultFiles = new TreeSet<>();
		{
			// mimat TO mirbase
			String rawFile = new File(theDataInDir, "aliases.txt.zip").getAbsolutePath();
			String preppedFile = new File(theDataOutDir, "mimatTOmirbase.tsv").getAbsolutePath();
			prep_mimatTOmirbase(rawFile, preppedFile);
			resultFiles.add(preppedFile);
		}
		{
			// mature mirs
			String rawFile = new File(theDataInDir, "mature.fa.zip").getAbsolutePath();
			String preppedFile = new File(theDataOutDir, "mature_mirs.tsv").getAbsolutePath();
			prep_mature_mirs(rawFile, preppedFile);
			resultFiles.add(preppedFile);
		}
		{
			// mirbase to gene symbol
			// Entrez num to gene symbol
			String rawFile = new File(theDataInDir, "hgnc_complete_set.txt.zip").getAbsolutePath();
			String preppedFile = new File(theDataOutDir, "entreznumTOgenesymbol.tsv").getAbsolutePath();
			prep_entreznumTOgenesymbol(rawFile, preppedFile);
			resultFiles.add(preppedFile);
		}
		{
			// UC ID to gene symbol
			String rawFile = new File(theDataInDir, "kgXref.txt.zip").getAbsolutePath();
			String preppedFile = new File(theDataOutDir, "ucidTOgenesymbol.tsv").getAbsolutePath();
			prep_ucidTOgenesymbol(rawFile, preppedFile);
			resultFiles.add(preppedFile);
		}
		{
			// one to one ucsc and hgnc (ucid and gene symbol) mapping
			String rawHgncFile = new File(theDataInDir, "hgnc_complete_set.txt.zip").getAbsolutePath();
			String rawUcscFile = new File(theDataInDir, "kgXref.txt.zip").getAbsolutePath();
			String preppedFile = new File(theDataOutDir, "oneToOneUcscHgnc.tsv").getAbsolutePath();
			prep_oneToOneUcscHgnc(rawHgncFile, rawUcscFile, preppedFile);
			resultFiles.add(preppedFile);
		}
		{
			// one to one ucsc and hgnc (ucid and gene symbol) mapping
			String rawHgncFile = new File(theDataInDir, "hgnc_complete_set.txt.zip").getAbsolutePath();
			String preppedFile = new File(theDataOutDir, "geneSynonyms.tsv").getAbsolutePath();
			prep_geneSynonyms(rawHgncFile, preppedFile);
			resultFiles.add(preppedFile);
		}
		return resultFiles;
	}

	protected void prep_geneSynonyms(String theRawHgncFile, String thePreppedFile) throws FileNotFoundException, IOException, Exception
	{
		TreeMap<String, TreeSet<String>> symbolToSynonyms = new TreeMap<>();
		// first read the HGNC which has one to one maps
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theRawHgncFile))
		{
			// index 1 = Approved Symbol
			// index 4 = Status
			// index 6 = Previous Symbols
			// index 8 = Synonyms
			String inLine = br.readLine();
			String hgncIDcolText = "Approved Symbol";
			String previousSym = "Previous Symbols";
			String synonym = "Synonyms";
			ArrayList<String> columns = new ArrayList<>(Arrays.asList(inLine.split("\t", -1)));
			int hgncIndex = columns.indexOf(hgncIDcolText);
			if (-1==hgncIndex)
			{
				throw new Exception("Index for '" + hgncIDcolText + "' not found");
			}
			int previousSymIndex = columns.indexOf(previousSym);
			if (-1==previousSymIndex)
			{
				throw new Exception("Index for '" + previousSym + "' not found");
			}
			int synonymIndex = columns.indexOf(synonym);
			if (-1==synonymIndex)
			{
				throw new Exception("Index for '" + synonym + "' not found");
			}
			// 
			inLine = br.readLine();
			while(null!=inLine)
			{
				// index 1 = Approved Symbol
				// index 4 = Status
				// index 6 = Previous Symbols
				// index 8 = Synonyms
				String [] tabSplit = inLine.split("\t", -1);
				String symbol = tabSplit[hgncIndex];
				String prevSymbols = tabSplit[previousSymIndex];
				String synonyms = tabSplit[synonymIndex];
				if (symbol.endsWith("~withdrawn"))
				{
					symbol = symbol.replaceFirst("~withdrawn", "");
				}
				TreeSet<String> synList = symbolToSynonyms.get(symbol);
				if (null==synList)
				{
					synList = new TreeSet<>();
				}
				for(String sym : prevSymbols.split(",", -1))
				{
					sym = sym.trim();
					synList.add(sym);
				}
				for(String sym : synonyms.split(",", -1))
				{
					sym = sym.trim();
					synList.add(sym);
				}
				symbolToSynonyms.put(symbol, synList);
				inLine = br.readLine();
			}
		}
		// write file
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
		{
			for(Entry<String, TreeSet<String>> entry : symbolToSynonyms.entrySet())
			{
				bw.write(entry.getKey());
				for(String syn : entry.getValue())
				{
					if (false=="".equals(syn))
					{
						bw.write("\t" + syn);
					}
				}
				bw.newLine();
			}
		}
	}
	
	protected void prep_entreznumTOgenesymbol(String theRawFile, String thePreppedFile) throws FileNotFoundException, IOException, Exception
	{
		System.out.println("prep_entreznumTOgenesymbol theRawFile=" + theRawFile);
		System.out.println("prep_entreznumTOgenesymbol thePreppedFile=" + thePreppedFile);
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theRawFile))
		{
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
			{
				String inLine = br.readLine();
				// Approved Symbol
				// Synonyms
				// Entrez Gene ID
				String hgncIDcolText = "Approved Symbol";
				String entrezId = "Entrez Gene ID";
				ArrayList<String> columns = new ArrayList<>(Arrays.asList(inLine.split("\t", -1)));
				int hgncIndex = columns.indexOf(hgncIDcolText);
				if (-1==hgncIndex)
				{
					throw new Exception("Index for '" + hgncIDcolText + "' not found");
				}
				int entrezIdIndex = columns.indexOf(entrezId);
				if (-1==entrezIdIndex)
				{
					throw new Exception("Index for '" + entrezId + "' not found");
				}
				// skip header line
				inLine = br.readLine();
				while(null!=inLine)
				{
					String [] tabSplit = inLine.split("\t", -1);
					String entrezGeneNum = tabSplit[entrezIdIndex];
					String symbol = tabSplit[hgncIndex];
					String withdrawn = "";
					if (symbol.endsWith("~withdrawn"))
					{
						symbol = symbol.replaceFirst("~withdrawn", "");
						withdrawn = "withdrawn";
					}
					String output = symbol + "\t" + entrezGeneNum + "\t" + withdrawn;
					bw.write(output);
					//
					inLine = br.readLine();
					if (null!=inLine)
					{
						bw.newLine();
					}
				}
			}
		}
	}

	protected void prep_oneToOneUcscHgnc(String theRawHgncFile, String theRawUcscFile, String thePreppedFile) throws FileNotFoundException, IOException, Exception
	{
		System.out.println("prep_oneToOneUcscHgnc theRawHgncFile=" + theRawHgncFile);
		System.out.println("prep_oneToOneUcscHgnc theRawUcscFile=" + theRawUcscFile);
		System.out.println("prep_oneToOneUcscHgnc thePreppedFile=" + thePreppedFile);
		TreeMap<String, String> ucscToHgnc = new TreeMap<>();
		TreeMap<String, String> hgncToUcsc = new TreeMap<>();
		// first read the HGNC which has one to one maps
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theRawHgncFile))
		{
			String inLine = br.readLine();
			String hgncIDcolText = "Approved Symbol";
			String ucscIDcolText = "UCSC ID (supplied by UCSC)";
			ArrayList<String> columns = new ArrayList<>(Arrays.asList(inLine.split("\t", -1)));
			int hgncIndex = columns.indexOf(hgncIDcolText);
			int ucscIndex = columns.indexOf(ucscIDcolText);
			if (-1==hgncIndex)
			{
				throw new Exception("Index for '" + hgncIDcolText + "' not found");
			}
			if (-1==ucscIndex)
			{
				throw new Exception("Index for '" + hgncIDcolText + "' not found");
			}
			// skip header line
			inLine = br.readLine();
			while(null!=inLine)
			{
				String [] tabSplit = inLine.split("\t", -1);
				String hgnc = tabSplit[hgncIndex];
				String ucsc = tabSplit[ucscIndex];
				if (false=="".equals(ucsc))
				{
					if (hgnc.endsWith("~withdrawn"))
					{
						hgnc = hgnc.replaceFirst("~withdrawn", "");
					}
					String coll = ucscToHgnc.put(ucsc, hgnc);
					if(null!=coll)
					{
						throw new Exception("Collision from HGNC ucscToHgnc " + ucsc + ", " + hgnc + " & " + coll);
					}
					coll = hgncToUcsc.put(hgnc, ucsc);
					if(null!=coll)
					{
						throw new Exception("Collision from HGNC hgncToUcsc " + hgnc + ", " + ucsc + " & " + coll);
					}
				}
				inLine = br.readLine();
			}
		}
		// second read the UCSC which has one to many maps, and only process ones with N on second column
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theRawUcscFile))
		{
			TreeSet<String> replimatchOld = new TreeSet<>();
			replimatchOld.addAll(ucscToHgnc.keySet());
			replimatchOld.addAll(hgncToUcsc.keySet());
			String inLine = br.readLine();
			while(null!=inLine)
			{
				String [] tabSplit = inLine.split("\t", -1);
				String ucsc = tabSplit[0];
				//String otherid = tabSplit[1];
				String hgnc = tabSplit[4];
				// file contains non-gene symbols that can be detected with lower case letters
				if (hgnc.equals(hgnc.toUpperCase()))
				{
					if (false==ucscToHgnc.keySet().contains(ucsc))
					{
						if (false==hgncToUcsc.keySet().contains(hgnc))
						{
							if(null!=ucscToHgnc.put(ucsc, hgnc))
							{
								throw new Exception("Collision from UCSC ucscToHgnc " + ucsc + ", " + hgnc);
							}
							if(null!=hgncToUcsc.put(hgnc, ucsc))
							{
								throw new Exception("Collision from UCSC hgncToUcsc " + hgnc + ", " + ucsc);
							}
						}
					}
				}
				//
				inLine = br.readLine();
			}
		}
		// write one to one file oneToOneUcscHgnc
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
		{
			for(Entry<String, String> entry : ucscToHgnc.entrySet())
			{
				bw.write(entry.getKey() + "\t" + entry.getValue());
				bw.newLine();
			}
		}
	}
	
	protected void prep_mimatTOmirbase(String theRawFile, String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theRawFile))
		{
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
			{
				String inLine = br.readLine();
				while(null!=inLine)
				{
					String [] tabSplit = inLine.split("\t", -1);
					String mimat = tabSplit[0];
					String mirbases = tabSplit[1];
					String [] semicolonSplit = mirbases.split(";", -1);
					String longest = "";
					int length = 0;
					String fivepThreep = "";
					String star = "";
					String output = null;
					mirbases = mirbases.toLowerCase();
					if (mimat.startsWith("MIMAT"))
					{
						if (mirbases.startsWith("hsa"))
						{
							for(String mir : semicolonSplit)
							{
								if ((mir.contains("5p"))||(mir.contains("5P")))
								{
									fivepThreep = fivepThreep + mir + ";";
								}
								if ((mir.contains("3p"))||(mir.contains("3P")))
								{
									fivepThreep = fivepThreep + mir + ";";
								}
								if (mir.contains("*"))
								{
									star = star + mir + ";";
								}
								if (mir.length()>length)
								{
									longest = mir + ";";
									length = mir.length();
								}
								else if (mir.length()==length)
								{
									longest = longest + mir + ";";
								}
							}
							//
							output = mimat + "\t";
							if(false=="".equalsIgnoreCase(fivepThreep))
							{
								output = output + fivepThreep;
							}
							else if(false=="".equalsIgnoreCase(star))
							{
								output = output + star;
							}
							else
							{
								output = output + longest;
							}
							bw.write(output);
						}
					}
					//
					inLine = br.readLine();
					if ((null!=output)&&(null!=inLine))
					{
						bw.newLine();
					}
				}
			}
		}
	}

	protected void prep_mature_mirs(String theRawFile, String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theRawFile))
		{
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
			{
				String inLine = br.readLine();
				while(null!=inLine)
				{
					String mir = null;
					if (inLine.contains("Homo sapiens"))
					{
						String [] spaceSplit = inLine.split(" ", -1);
						mir = spaceSplit[0];
						mir = mir.replaceFirst(">", "");
						mir = mir.toLowerCase();
						bw.write(mir);
					}
					inLine = br.readLine();
					if ((null!=inLine)&&(null!=mir))
					{
						bw.newLine();
					}
				}
			}
		}
	}

	protected void prep_ucidTOgenesymbol(String theRawFile, String thePreppedFile) throws FileNotFoundException, IOException
	{
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theRawFile))
		{
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(thePreppedFile)))
			{
				String inLine = br.readLine();
				while(null!=inLine)
				{
					String [] tabSplit = inLine.split("\t", -1);
					String ucid = tabSplit[0];
					String symbol = tabSplit[4];
					String output = ucid + "\t" + symbol;
					bw.write(output);
					//
					inLine = br.readLine();
					if (null!=inLine)
					{
						bw.newLine();
					}
				}
			}
		}
	}

}
