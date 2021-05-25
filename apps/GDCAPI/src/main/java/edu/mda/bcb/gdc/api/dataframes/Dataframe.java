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

package edu.mda.bcb.gdc.api.dataframes;

import edu.mda.bcb.gdc.api.GDCAPI;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Dataframe class to read, write, and populate a table of strings.
 * 
 * @author Tod-Casasent
 */
public class Dataframe
{
	/**
	 * Array of strings for header columns.
	 */
	public ArrayList<String> mHeaders = null;
	
	/**
	 * ArrayList of String Arrays -- each String Array represents a row in the dataframe TSV.
	 */
	public ArrayList<ArrayList<String>> mData = null;
	
	/**
	 * Constructor sets headers to null but mData to an unpopulated object.
	 */
	public Dataframe()
	{
		mHeaders = null;
		mData = new ArrayList<>();
	}
	
	/**
	 * Populate this dataframe from the file.
	 * 
	 * @param theFile A TSV file to read into dataframe.
	 * @throws IOException 
	 */
	public void readDataFrame(File theFile) throws IOException
	{
		mHeaders = null;
		mData = new ArrayList<>();
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theFile.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			String line = br.readLine();
			while (null != line)
			{
				if (null == mHeaders)
				{
					mHeaders = new ArrayList<>();
					mHeaders.addAll(Arrays.asList(line.split("\t", -1)));
				}
				else
				{
					String[] splitted = line.split("\t", -1);
					ArrayList<String> myLine = new ArrayList<>();
					myLine.addAll(Arrays.asList(line.split("\t", -1)));
					mData.add(myLine);
				}
				line = br.readLine();
			}
		}
	}
	
	/**
	 * Find the row where theMatchColumn header is theMatchValue string, and 
	 * return the value found in theDesiredColumn for that row. For example,
	 * find "ID" of "TCGA-45-4563" and return "Disease-Type".
	 * 
	 * @param theDesiredColumn Column header to return value from.
	 * @param theMatchColumn Column header to search for matching value.
	 * @param theMatchValue Matching value to search for.
	 * @return A string value (may be of length 0) if match found or null.
	 */
	public String getColumnFromColumnValue(String theDesiredColumn, String theMatchColumn, String theMatchValue)
	{
		int indexMatch = mHeaders.indexOf(theMatchColumn);
		int indexDesired = mHeaders.indexOf(theDesiredColumn);
		String value = null;
		for (ArrayList<String> data : mData)
		{
			if (theMatchValue.equalsIgnoreCase(data.get(indexMatch)))
			{
				value = data.get(indexDesired);
				return value;
			}
		}
		return value;
	}
	
	/**
	 * Get the row where a column matches the requested value. Prints a warning if
	 * no match found. Used when we expect a match.
	 * 
	 * @param theMatchColumn Column header to search for matching value.
	 * @param theMatchValue Matching value to search for.
	 * @return ArrayList with matching value or an empty ArrayList if no match.
	 */
	public ArrayList<String> getRowFromColumnValue(String theMatchColumn, String theMatchValue)
	{
		int indexMatch = mHeaders.indexOf(theMatchColumn);
		for (ArrayList<String> data : mData)
		{
			if (theMatchValue.equals(data.get(indexMatch)))
			{
				return data;
			}
		}
		GDCAPI.printWarn("'" + theMatchColumn + "' not found for '" + theMatchValue + "'");
		return new ArrayList<String>();
	}
	
	/**
	 * Get the row where a column matches the requested value. Does NOT print a 
	 * warning if no match found. Used when we do not necessarily expect a match.
	 * 
	 * @param theMatchColumn Column header to search for matching value.
	 * @param theMatchValue Matching value to search for.
	 * @return ArrayList with matching value or an empty ArrayList if no match.
	 */
	public ArrayList<String> getRowFromColumnValueNoWarn(String theMatchColumn, String theMatchValue)
	{
		int indexMatch = mHeaders.indexOf(theMatchColumn);
		for (ArrayList<String> data : mData)
		{
			if (theMatchValue.equals(data.get(indexMatch)))
			{
				return data;
			}
		}
		return new ArrayList<String>();
	}
}
