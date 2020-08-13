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

import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class DataArchive implements Comparable<DataArchive>
{
	public String mPath = null;
	public String mID = null;
	public JsonDataset mJD = null;
	public TreeSet<String>  mFiles = null;
	
	public DataArchive(String thePath, String theID, TreeSet<String> theFiles, JsonDataset theJD)
	{
		mPath = thePath;
		mID = theID;
		mJD = theJD;
		mFiles = new TreeSet<>();
		mFiles.addAll(theFiles);
	}
	
	public String getFilesAsString()
	{
		String files = null;
		for (String file : mFiles)
		{
			if (null==files)
			{
				files = file;
			}
			else
			{
				files = files + "|" + file;
			}
		}
		return files;
	}
	
	public String getLine()
	{
		return mPath + "\t" +
				mID + "\t" +
				getFilesAsString() + "\t" +
				mJD.getLine();
	}
	
	public String get(String theHeader) throws Exception
	{
		String result = null;
		if ("ID".equals(theHeader))
		{
			result = mID;
		}
		else if ("Path".equals(theHeader))
		{
			result = mPath;
		}
		else if ("Files".equals(theHeader))
		{
			result = getFilesAsString();
		}
		else if ("Source".equals(theHeader))
		{
			result = mJD.source;
		}
		else if ("Variant".equals(theHeader))
		{
			result = mJD.variant;
		}
		else if ("Version".equals(theHeader))
		{
			result = mJD.version;
		}
		else if ("Project".equals(theHeader))
		{
			result = mJD.project;
		}
		else if ("Sub-Project".equals(theHeader))
		{
			result = mJD.subProject;
		}
		else if ("Category".equals(theHeader))
		{
			result = mJD.category;
		}
		else if ("Platform".equals(theHeader))
		{
			result = mJD.platform;
		}
		else if ("Data".equals(theHeader))
		{
			result = mJD.data;
		}
		else if ("Algorithm".equals(theHeader))
		{
			result = mJD.algorithm;
		}
		else if ("Details".equals(theHeader))
		{
			result = mJD.details;
		}
		else 
		{
			throw new Exception("Unknown header '" + theHeader + "'");
		}
		return result;
	}
	
	static public DataArchive setLine(String theLine)
	{
		// path
		int index = theLine.indexOf("\t");
		String path = theLine.substring(0, index);
		theLine = theLine.substring(index+1);
		// id
		index = theLine.indexOf("\t");
		String id = theLine.substring(0, index);
		theLine = theLine.substring(index+1);
		// files
		index = theLine.indexOf("\t");
		String myFiles = theLine.substring(0, index);
		theLine = theLine.substring(index+1);
		TreeSet<String> files = new TreeSet<>();
		for (String file : myFiles.split("\\|", -1))
		{
			files.add(file);
		}
		// JsonDataset
		JsonDataset jd = JsonDataset.setLine(theLine);
		return new DataArchive(path, id, files, jd);
	}

	@Override
	public int compareTo(DataArchive o)
	{
		return mPath.compareTo(o.mPath);
	}
}
