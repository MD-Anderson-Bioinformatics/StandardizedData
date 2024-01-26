// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.stdmwutils.indexes;

import java.util.TreeSet;

/**
 * Class for representing datasets in the DataIndex
 * 
 * @author Tod-Casasent
 */
public class DataArchive implements Comparable<DataArchive>
{
	/**
	 * Full path to ZIP file for data
	 */
	public String mPath = null;
	
	/**
	 * Unique ID for data
	 */
	public String mID = null;
	
	/**
	 * JsonDataset class containing other dataset information. Class is 
	 * converted to JSON using GSON when needed.
	 */
	public JsonDataset mJD = null;
	
	/**
	 * List of files stored in the ZIP file.
	 */
	public TreeSet<String>  mFiles = null;
	
	/**
	 * Constructor
	 * 
	 * @param thePath Full path to ZIP file for data
	 * @param theID Unique ID for data
	 * @param theFiles List of files stored in the ZIP file.
	 * @param theJD JsonDataset class containing other dataset information. 
	 */
	public DataArchive(String thePath, String theID, TreeSet<String> theFiles, JsonDataset theJD)
	{
		mPath = thePath;
		mID = theID;
		mJD = theJD;
		mFiles = new TreeSet<>();
		mFiles.addAll(theFiles);
	}
	
	/**
	 * Get pipe delimited string of files.
	 * 
	 * @return pipe delimited string of files
	 */
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
	
	/**
	 * Get string of line for index file (tab delimited)
	 * 
	 * @return Get string of line for index file (tab delimited)
	 */
	public String getLine()
	{
		return mPath + "\t" +
				mID + "\t" +
				getFilesAsString() + "\t" +
				mJD.getLine();
	}
	
	/**
	 * Get a value from the object based on header label
	 * 
	 * @param theHeader String of header label to retrieve
	 * @return String given value for that header label
	 * @throws Exception 
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
		else if ("Program".equals(theHeader))
		{
			result = mJD.program;
		}
		else if ("Project".equals(theHeader))
		{
			result = mJD.project;
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
	 */
	
	/**
	 * Create a DataArchive object using a line from the index file
	 * 
	 * @param theLine A line from the index file (should be in standard order)
	 * @return  A DataArchive object with that line's data
	 */
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

	/**
	 * Compare DataArchive using path (which should be unique)
	 * 
	 * @param o DataArchive to compare this to
	 * @return 0 is paths are the same (should not occur). Less then 0 if this.mPath is less than argument o's path. Greater than 0 otherwise.
	 */
	@Override
	public int compareTo(DataArchive o)
	{
		return mPath.compareTo(o.mPath);
	}
}
