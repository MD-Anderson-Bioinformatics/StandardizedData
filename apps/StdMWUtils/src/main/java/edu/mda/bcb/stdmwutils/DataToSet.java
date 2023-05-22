/*
 *  Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
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
package edu.mda.bcb.stdmwutils;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author TDCasasent
 * @param <T>
 */
public class DataToSet<T>
{
	public DataToSet()
	{
		mDataToSet = new TreeMap<>();
	}

	private TreeMap<String, TreeSet<T>> mDataToSet = null;

	public void put(String theHash, T theData) throws StdMwException
	{
		TreeSet<T> set = mDataToSet.get(theHash);
		if (null==set)
		{
			set = new TreeSet<>();
		}
		boolean exists = !set.add(theData);
		mDataToSet.put(theHash, set);
		if (true==exists)
		{
			String errMsg = "Duplicate Objects Found for key " + theHash;
			StdMwDownload.printErr(errMsg);
			throw new StdMwException(errMsg);
		}
	}

	public TreeSet<T> getAll()
	{
		TreeSet<T> set = new TreeSet<>();
		for (TreeSet<T> subSet : mDataToSet.values())
		{
			set.addAll(subSet);
		}
		return set;
	}

	public TreeSet<T> get(String theHash)
	{
		return mDataToSet.get(theHash);
	}
	
	public int size()
	{
		return mDataToSet.size();
	}
}

