/*
 *  Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
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
public class DataMap<T>
{
	public DataMap()
	{
		mDataMap = new TreeMap<>();
	}

	private TreeMap<String, T> mDataMap = null;

	public void put(String theHash, T theData) throws StdMwException
	{
		T old = mDataMap.put(theHash, theData);
		if (null != old)
		{
			String errMsg = "Duplicate Objects Found for key " + theHash;
			StdMwDownload.printErr(errMsg);
			throw new StdMwException(errMsg);
		}
	}

	public TreeSet<T> getAll()
	{
		TreeSet<T> set = new TreeSet<>();
		set.addAll(mDataMap.values());
		return set;
	}

	public T get(String theHash)
	{
		return mDataMap.get(theHash);
	}
	
	public int size()
	{
		return mDataMap.size();
	}
}

