/*
 *  Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
 *  
 *  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
 *  MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>
 */
package edu.mda.bcb.stdmwutils.utils;

import edu.mda.bcb.stdmwutils.DataMap;
import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.StdMwException;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.mwdata.RefMet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author TDCasasent
 */
public class RefMetUtil
{
	static public RefMetUtil updateRefMetUtil(String theTimeStamp, boolean theWrite) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		// TODO: check for newest refMet file, and download new and compare to old
		File outDir = new File(MWUrls.M_MWB_CACHE, theTimeStamp);
		File inCSV = new File(MWUrls.M_MWB_CACHE, "refmet.csv");
		RefMetUtil ru = new RefMetUtil();
		File existing = new File(outDir, MWUrls.M_REFMET);
		if (existing.exists())
		{
			ru.readRefMet(outDir);
		}
		else
		{
			ru.fetchRefMet(inCSV);
		}
		if (theWrite)
		{
			ru.writeRefMet(outDir);
		}
		return ru;
	}

	static public RefMetUtil readNewestRefMetFile() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		File timestampDir = MWUrls.findNewestDir(new File(MWUrls.M_MWB_CACHE));
		RefMetUtil ru = new RefMetUtil();
		ru.readRefMet(timestampDir);
		return ru;
	}

	public DataMap<RefMet> mDataMap = null;

	private RefMetUtil()
	{
		mDataMap = new DataMap<>();
	}

	public RefMet getRefMet(String theName)
	{
		return mDataMap.get(theName);
	}

	public TreeSet<RefMet> getAll()
	{
		return mDataMap.getAll();
	}
	
	public void fetchRefMet(File theIn) throws FileNotFoundException, IOException, StdMwException
	{
		StdMwDownload.printLn("fetchRefMet - iterate file");
		//" refmet_name",super_class,main_class,sub_class,formula,exactmass,inchi_key,pubchem_cid
		try(Reader reader = new FileReader(theIn))
		{
			CSVFormat format = CSVFormat.DEFAULT.builder()
					.setDelimiter(',')
					.setQuote('"')
					.setIgnoreEmptyLines(true)
					.setHeader()
					.build();
			CSVParser parser = new CSVParser(reader, format);
			List<CSVRecord> records = parser.getRecords();
			int counter = 0;
			try
			{
				for (CSVRecord record : records)
				{
					counter += 1;
					RefMet refMet = new RefMet();
					refMet.name = record.get(" refmet_name").trim();
					refMet.super_class = record.get("super_class").trim();
					refMet.main_class = record.get("main_class").trim();
					refMet.sub_class = record.get("sub_class").trim();
					refMet.formula = record.get("formula").trim();
					refMet.exactmass = record.get("exactmass").trim();
					refMet.inchi_key = record.get("inchi_key").trim();
					refMet.pubchem_cid = record.get("pubchem_cid").trim();
					RefMet old = mDataMap.get(refMet.name);
					if ((null!=old)&&(refMet.exactMatch(old)))
					{
						StdMwDownload.printLn("Ignoring exact RefMet match for " + refMet.name);
					}
					else
					{
						mDataMap.put(refMet.name, refMet);
					}
				}
			}
			finally
			{
				StdMwDownload.printLn("RefMet exited at record " + counter);
			}
		}
		StdMwDownload.printLn("fetchRefMet - finished iterating");
	}

	public void writeRefMet(File theDir) throws IOException
	{
		if (mDataMap.size() > 0)
		{
			theDir.mkdir();
			File output = new File(theDir, MWUrls.M_REFMET);
			StdMwDownload.printLn("writeRefMet - write to " + output.getAbsolutePath());
			OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
			try (BufferedWriter bw = java.nio.file.Files.newBufferedWriter(output.toPath(), Charset.availableCharsets().get("UTF-8"), options))
			{
				bw.write(RefMet.getHeaderString());
				bw.newLine();
				StdMwDownload.printLn("writeRefMet - iterate refmets");
				TreeSet<RefMet> fullSet = mDataMap.getAll();
				int cnt = 0;
				for (RefMet refMet : fullSet)
				{
					if (0 == cnt % 1000)
					{
						System.out.print(".");
					}
					bw.write(refMet.getRowString());
					bw.newLine();
					bw.flush();
					cnt += 1;
					if (cnt > 100000)
					{
						System.out.println(".");
						cnt = 0;
					}
				}
				System.out.println(".");
				StdMwDownload.printLn("writeRefMet - finished iterating");
			}
		}
	}

	public void readRefMet(File theDir) throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		mDataMap = new DataMap<>();
		File input = new File(theDir, MWUrls.M_REFMET);
		StdMwDownload.printLn("readRefMet input = " + input.getAbsolutePath());
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(input.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			// headers
			String line = br.readLine();
			ArrayList<String> headers = new ArrayList<>();
			headers.addAll(Arrays.asList(line.split("\t", -1)));

			line = br.readLine();
			while (null != line)
			{
				RefMet refmet = RefMet.getFromRowString(headers, line);
				mDataMap.put(refmet.name, refmet);
				line = br.readLine();
			}
		}
	}
	
	public TreeSet<String> getPubChemId()
	{
		TreeSet<String> names = new TreeSet<>();
		TreeSet<RefMet> fullSet = mDataMap.getAll();
		for (RefMet rm : fullSet)
		{
			if ((!"".equals(rm.pubchem_cid))&&(!"NA".equals(rm.pubchem_cid))&&(null!=rm.pubchem_cid))
			{
				names.add(rm.pubchem_cid);
			}
		}
		return names;
	}
}
