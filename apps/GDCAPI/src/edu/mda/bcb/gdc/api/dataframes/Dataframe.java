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

package edu.mda.bcb.gdc.api.dataframes;

import edu.mda.bcb.gdc.api.GDCAPI;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Tod-Casasent
 */
public class Dataframe
{
	public ArrayList<String> mHeaders = null;
	public ArrayList<ArrayList<String>> mData = null;
	
	
	public Dataframe()
	{
		mHeaders = null;
		mData = new ArrayList<>();
	}
	
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
