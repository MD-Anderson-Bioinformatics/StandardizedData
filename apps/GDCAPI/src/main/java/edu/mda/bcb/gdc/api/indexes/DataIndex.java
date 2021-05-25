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

package edu.mda.bcb.gdc.api.indexes;

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
 * DataIndex class has the headers and information from an index file.
 * 
 * @author Tod-Casasent
 */
public class DataIndex
{
	/**
	 * Path to index file this object represents.
	 */
	private File mIndexFile = null;
	
	/**
	 * Header strings for this index file
	 */
	private ArrayList<String> mHeaders = null;
	
	/**
	 * Objects representing each row in the index file.
	 */
	private TreeSet<DataArchive> mData = null;
	
	/**
	 * Constructor which takes a path to the file.
	 * 
	 * @param theIndexFile Path to index file this object represents.
	 */
	public DataIndex(File theIndexFile)
	{
		mIndexFile = theIndexFile;
		mHeaders = getHeaders();
		mData = new TreeSet<>();
	}
	
	/**
	 * Add a row to an index -- adds to object and updates (or creates) file on disk.
	 * 
	 * @param theZip Path to ZIP for this row
	 * @param theID Unique String ID for this row
	 * @param theJD JsonDataset object for this row
	 * @throws IOException
	 * @throws Exception 
	 */
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
	
	/**
	 * Write the index file
	 * 
	 * @param theFile file to which to write this index
	 * @throws IOException 
	 */
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
	
	/**
	 * Read index file
	 * 
	 * @param theFile File to read for this index
	 * @throws IOException
	 * @throws Exception 
	 */
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
	
	/**
	 * Add a row to the list of rows
	 * 
	 * @param theRow DataArchive object to add
	 */
	private void addRow(DataArchive theRow)
	{
		mData.add(theRow);
	}
	
	/**
	 * ArrayList of headers for an index file
	 * 
	 * @return ArrayList of headers for an index file
	 */
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
