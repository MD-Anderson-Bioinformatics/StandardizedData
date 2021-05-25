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

package edu.mda.bcb.prm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public abstract class IterateFile_Mixin
{
	public String mHeaderLine = null;
	public ArrayList<String> mHeaders = null;
	public String mOutputFile = null;
	public String mOutputHeaders = null;
	public TreeSet<String> mOutputLines = null;
	public String mSkipComments = null;
	protected String mInputFileName = null;
	
	public IterateFile_Mixin(String theOutputFile, String theOutputHeaders, String theSkipComments)
	{
		mHeaderLine = null;
		mHeaders = null;
		mOutputFile = theOutputFile;
		mOutputHeaders = theOutputHeaders;
		// TreeSet sorts the output and removes duplicates
		mOutputLines = new TreeSet<>();
		mSkipComments = theSkipComments;
		mInputFileName = null;
	}
	
	public String run(String theZipFile) throws Exception
	{
		mInputFileName = new File(theZipFile).getName();
		System.out.println(mInputFileName);
		// reset for new file
		mHeaders = null;
		long count = 0;
		String inLine = null;
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theZipFile))
		{
			inLine = br.readLine();
			while(null!=inLine)
			{
				if ((null==mSkipComments)||(!inLine.startsWith(mSkipComments)))
				{
					if (0== (count%100000))
					{
						System.out.print(" " + count);
					}
					String [] splitted = inLine.split("\t", -1);
					if (null==mHeaders)
					{
						mHeaderLine = inLine;
						mHeaders = new ArrayList<>();
						mHeaders.addAll(Arrays.asList(splitted));
					}
					else
					{
						processLine(splitted);
					}
				}
				inLine = br.readLine();
				count = count + 1;
			}
			System.out.println(" " + count);
		}
		catch(Exception exp)
		{
			System.out.println(" Last line processed " + count);
			System.out.println("mHeaderLine=" + mHeaderLine);
			System.out.println("inLine=" + inLine);
			throw exp;
		}
		postRun();
		return mOutputFile;
	}
	
	public String getColumnValue(String theColumn, String [] theSplittedLine) throws Exception
	{
		String value = null;
		int index = mHeaders.indexOf(theColumn);
		if (index<0)
		{
			throw new Exception("Column '" + theColumn + "' not found.");
		}
		else
		{
			value = theSplittedLine[index];
		}
		return value;
	}
	
	public String extractValueGTF(String theMarker, String theString)
	{
		String value = null;
		String [] splitted = theString.split(";", -1);
		for(String token : splitted)
		{
			token = token.trim();
			if (token.startsWith(theMarker + " "))
			{
				String [] spSplit = token.split(" ", -1);
				value = spSplit[1];
				value = value.replace("\"", "");
			}
		}
		return value;
	}
	
	abstract public void processLine(String [] theSplittedLine) throws Exception;
	
	public void postRun() throws Exception
	{
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(mOutputFile)))
		{
			if (null!=mOutputHeaders)
			{
				bw.write(mOutputHeaders);
				bw.newLine();
			}
			for(String line : mOutputLines)
			{
				bw.write(line);
				bw.newLine();
			}
		}
	}
	
	protected String getLocation(String theCompositeLocation, boolean theFirst)
	{
		String value = "";
		String addressPair = theCompositeLocation;
		if (theCompositeLocation.contains(","))
		{
			String [] commaSplit = theCompositeLocation.split(",", -1);
			if (theFirst)
			{
				addressPair = commaSplit[0];
			}
			else
			{
				addressPair = commaSplit[commaSplit.length-1];
			}
		}
		String [] splitted = addressPair.split("-", -1);
		if (theFirst)
		{
			value = splitted[0];
		}
		else
		{
			value = splitted[1];
		}
		return value;
	}
}
