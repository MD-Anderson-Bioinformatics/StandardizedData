/*
 *  Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
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
package edu.mda.bcb.stdmwutils.std;

import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.StdMwException;
import edu.mda.bcb.stdmwutils.mwdata.Analysis;
import edu.mda.bcb.stdmwutils.mwdata.MwTable;
import edu.mda.bcb.stdmwutils.mwdata.Summary;
import edu.mda.bcb.stdmwutils.utils.AnalysisUtil;
import edu.mda.bcb.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bcb.stdmwutils.utils.OtherIdsUtil;
import edu.mda.bcb.stdmwutils.utils.RefMetUtil;
import edu.mda.bcb.stdmwutils.utils.SummaryUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class MWAPI
{
	public SummaryUtil mSu = null;
	public AnalysisUtil mAu = null;
	public MetaboliteUtil mMu = null;
	public RefMetUtil mRu = null;
	public OtherIdsUtil mOu = null;
	public File mTimestampDir = null;
	
	public MWAPI(SummaryUtil theSu, AnalysisUtil theAu, 
			MetaboliteUtil theMu, RefMetUtil theRu, OtherIdsUtil theOu, File theTimestampDir)
	{
		mSu = theSu;
		mAu = theAu;
		mMu = theMu;
		mRu = theRu;
		mOu = theOu;
		mTimestampDir = theTimestampDir;
	}
	
	public static Comparator SAMPLE_SORTED_ORDER = new Comparator<ProcessEntry>()
	{
		@Override
		public int compare(ProcessEntry p1, ProcessEntry p2)
		{
			// note comparison is backwards, to sort largest first
			int comp = Integer.compare(p2.mSu.sample_count, p1.mSu.sample_count);
			if (0==comp)
			{
				comp = p1.compareTo(p2);
			}
			return comp;
		}
	};
	
	protected ArrayList<ProcessEntry> processableCacheEntries(String theTimestamp, ProcessUtil thePU) throws NoSuchAlgorithmException
	{
		// get list of processable datasets from exising MW CACHE
		ArrayList<ProcessEntry> peList = new ArrayList<>();
		for (Summary su : mSu.getAll())
		{
			//StdMwDownload.printLn("MWAPI::processableCacheEntries - check " + su.study_id);
			if (su.sample_count > 0)
			{
				//StdMwDownload.printLn("MWAPI::processableCacheEntries - sample count = " + su.sample_count);
				TreeSet<Analysis> ts = mAu.getAnalysisFromStudyHash(su.hash);
				if (null!=ts)
				{
					for (Analysis an : ts)
					{
						MwTable mt = new MwTable(an, su);
						mt.init(mMu, mRu);
						// remove elements that do not have downloads
						if (mt.metabolite_count > 0)
						{
							// check to see if dataset NEEDS processing
							if(false==thePU.doesThisExist(mt))
							{
								StdMwDownload.printLn("MWAPI::processableCacheEntries - usable analysis=" + mt.analysis.analysis_id + " study=" + mt.analysis.study_id);
								peList.add(new ProcessEntry(an, su, theTimestamp, ProcessUtil.M_STATUS_NEW, null));
							}
							else
							{
								StdMwDownload.printLn("MWAPI::processableCacheEntries - skip analysis=" + mt.analysis.analysis_id + " study=" + mt.analysis.study_id);
							}
						}
					}
				}
				else
				{
					StdMwDownload.printLn("SMWPipeline::processableCacheEntries - no analysis " + su.study_id);
				}
			}
		}
		// sort list from largest to smallest sample count
		Collections.sort(peList, SAMPLE_SORTED_ORDER);
		StdMwDownload.printLn("SMWPipeline::processableCacheEntries - eligible studies = " + peList.size());
		return peList;
	}
	
	public void processPipeline(int theSize, String theTimestamp) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException, Exception
	{
		// check list to see if processing is done, if so, add the next theSize elements from sumList that are not in the processList
		ProcessUtil pu = ProcessUtil.readNewestProcessFile(mMu, mRu, mOu);
		// get list of processable datasets from existing MW CACHE
		ArrayList<ProcessEntry> peList = processableCacheEntries(theTimestamp, pu);
		// add theSize number of entries to pu and update process index on disk
		pu.addNewEntries(peList, theSize);
		pu.writeProcesses();
		// process entries currently in list
		pu.processPending(theTimestamp);
		// update the "completed" standardized MWB index
		pu.updateStandardizedIndex();
	}
	
	public void listProcessable(String theTimestamp) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException, Exception
	{
		// check list to see if processing is done, if so, add the next theSize elements from sumList that are not in the processList
		ProcessUtil pu = ProcessUtil.readNewestProcessFile(mMu, mRu, mOu);
		// get list of processable datasets from existing MW CACHE
		ArrayList<ProcessEntry> peList = processableCacheEntries(theTimestamp, pu);
		// add theSize number of entries to pu and update process index on disk
		long count = pu.addNewEntries(peList, 0);
		StdMwDownload.printLn(count + " new entries found");
	}
}
