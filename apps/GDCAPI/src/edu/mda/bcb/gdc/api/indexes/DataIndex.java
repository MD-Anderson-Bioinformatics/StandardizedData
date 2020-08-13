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

package edu.mda.bcb.gdc.api.indexes;

import edu.mda.bcb.gdc.api.GDCAPI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class DataIndex
{
	private File mIndexFile = null;
	private ArrayList<String> mHeaders = null;
	private TreeSet<DataArchive> mData = null;
	
	public DataIndex(File theIndexFile)
	{
		mIndexFile = theIndexFile;
		mHeaders = getHeaders();
		mData = new TreeSet<>();
	}
	
	synchronized public void updateIndex(File theZip, String theID, JsonDataset theJD) throws IOException, Exception
	{
		if (mIndexFile.exists())
		{
			readDataFrame(mIndexFile);
		}
		// get files list from Zip
		TreeSet<String> myFiles = ZipData.getListOfFiles(theZip);
		DataArchive myRow = new DataArchive(theZip.getAbsolutePath(), theID, myFiles, theJD);
		mData.add(myRow);
		writeDataFrame(mIndexFile);
	}
	
	private void writeDataFrame(File theFile) throws IOException
	{
		OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
		try (BufferedWriter bw = java.nio.file.Files.newBufferedWriter(theFile.toPath(), Charset.availableCharsets().get("UTF-8"), options))
		{
			boolean addTab = false;
			// write header
			for(String hdr : mHeaders)
			{
				if (true==addTab)
				{
					bw.write("\t");
				}
				else
				{
					addTab = true;
				}
				bw.write(hdr);
			}
			bw.newLine();
			// write rows
			for (DataArchive da : mData)
			{
				bw.write(da.getLine());
				bw.newLine();
			}
		}
	}
	
	private void readDataFrame(File theFile) throws IOException, Exception
	{
		// TODO: add data validation to this readDataFrame
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theFile.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			String [] headers = null;
			String line = br.readLine();
			while (null != line)
			{
				if (null == headers)
				{
					headers = line.split("\t", -1);
					if (headers.length!=mHeaders.size())
					{
						throw new Exception("readDataFrame - incorrect number of headers");
					}
					for (int index=0; index<headers.length; index++)
					{
						String hdr = headers[index];
						if (!mHeaders.get(index).equals(hdr))
						{
							throw new Exception("readDataFrame - headers in wrong order or incorrectly spelled");
						}
					}
				}
				else
				{
					mData.add(DataArchive.setLine(line));
				}
				line = br.readLine();
			}
		}
	}
	
	private void addRow(DataArchive theRow)
	{
		mData.add(theRow);
	}
	
	private String getColumnFromColumnValue(String theDesiredColumn, String theMatchColumn, String theMatchValue) throws Exception
	{
		String value = null;
		for (DataArchive data : mData)
		{
			if (theMatchValue.equalsIgnoreCase(data.get(theMatchColumn)))
			{
				value = data.get(theDesiredColumn);
				return value;
			}
		}
		return value;
	}
	
	private DataArchive getRowFromColumnValue(String theMatchColumn, String theMatchValue) throws Exception
	{
		for (DataArchive data : mData)
		{
			if (theMatchValue.equals(data.get(theMatchColumn)))
			{
				return data;
			}
		}
		GDCAPI.printWarn("'" + theMatchColumn + "' not found for '" + theMatchValue + "'");
		return null;
	}
	
	static private ArrayList<String> getHeaders()
	{
		ArrayList<String> headers = new ArrayList<>();
		headers.add("Path");
		headers.add("ID");
		headers.add("Files");
		// external
		headers.add("Source");
		headers.add("Variant");
		headers.add("Project");
		headers.add("Sub-Project");
		headers.add("Category");
		headers.add("Platform");
		headers.add("Data"); // dataset first half: Standardized, Analyzed, AutoCorrected, or Corrected
		headers.add("Algorithm"); // dataset second half: Discrete, Continuous, or the correction algorithm
		headers.add("Details"); // underscore specialization
		headers.add("Version");
		return headers;
	}
}
