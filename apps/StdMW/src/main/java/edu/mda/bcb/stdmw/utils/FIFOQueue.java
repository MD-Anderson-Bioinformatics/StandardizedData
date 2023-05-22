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
package edu.mda.bcb.stdmw.utils;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author tdcasasent
 */
public class FIFOQueue
{
	private TreeMap<Long, FIFOJob> mQueue = null;
	
	public FIFOQueue()
	{
		mQueue = new TreeMap<>();
	}
	
	synchronized public long newJob()
	{
		long id = -1;
		if (mQueue.size()<10)
		{
			FIFOJob job = new FIFOJob();
			id = job.mID;
			mQueue.put(id, job);
		}
		internalClearExpired();
		return id;
	}
	
	synchronized public boolean isGood(long theId)
	{
		boolean good = false;
		FIFOJob job = mQueue.get(theId);
		if (job!=null)
		{
			job.reset();
			good = true;
		}
		internalClearExpired();
		return good;
	}
	
	synchronized public String getStatus(long theId)
	{
		String status = "unknown";
		FIFOJob job = mQueue.get(theId);
		if (job!=null)
		{
			job.reset();
			status = job.mResult;
			if (null==status)
			{
				status = "pending";
			}
		}
		internalClearExpired();
		return status;
	}
	
	synchronized public void finish(long theId, String theResult)
	{
		FIFOJob job = mQueue.get(theId);
		if (job!=null)
		{
			job.mResult = theResult;
			job.reset();
		}
		internalClearExpired();
	}
	
	private void internalClearExpired()
	{
		long now = System.currentTimeMillis();
		TreeSet<Long> keys = new TreeSet<>();
		keys.addAll(mQueue.keySet());
		for (long key : keys)
		{
			FIFOJob job = mQueue.get(key);
			if (null!=job)
			{
				// if more than a minute stale
				if ((now-job.mLastCheck)>(60*1000))
				{
					mQueue.remove(job.mID);
				}
			}
		}
	}
	
	synchronized public void clearExpired()
	{
		internalClearExpired();
	}
}
