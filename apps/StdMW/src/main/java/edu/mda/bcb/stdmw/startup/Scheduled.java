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
package edu.mda.bcb.stdmw.startup;

import edu.mda.bcb.stdmw.utils.FIFOQueue;
import edu.mda.bcb.stdmwutils.StdMwException;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.utils.AnalysisUtil;
import edu.mda.bcb.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bcb.stdmwutils.utils.SummaryUtil;
import edu.mda.bcb.stdmwutils.utils.RefMetUtil;
import edu.mda.bcb.stdmwutils.utils.OtherIdsUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import javax.servlet.ServletContext;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author tdcasasent
 */
public class Scheduled implements Runnable
{
	private ServletContext mSC = null;
	public Scheduled(ServletContext theSC)
	{
		mSC = theSC;
	}
	
	@Override
	public void run()
	{
		try
		{
			mSC.log("Scheduled::Run Running scheduled updates " + MWUrls.M_VERSION);
			// clear expired jobs
			if (null==Load.mQueue)
			{
				Load.mQueue = new FIFOQueue();
			}
			Load.mQueue.clearExpired();
			// remove ZIPs if present
			File dataDir = new File(MWUrls.M_MWB_TEMP);
			if (dataDir.exists())
			{
				mSC.log("Scheduled::Run clean old data " + MWUrls.M_VERSION);
				File [] del = dataDir.listFiles();
				for (File rm : del)
				{
					long lastMod = rm.lastModified();
					// if over an hour old (60 min * 60 sec * 1000 ms)
					if ((System.currentTimeMillis() - lastMod) > (60*60*1000))
					{
						FileUtils.deleteQuietly(rm);
					}
				}
			}
			// update live dir
			// remove live, unstable - mSC.log("Scheduled::Run updateLiveDir " + MWUrls.M_VERSION);
			// remove live, unstable - updateLiveDir();
			// updated used Util objects
			mSC.log("Scheduled::Run updateUtilObjects " + MWUrls.M_VERSION);
			Scheduled.updateUtilObjects(mSC);
		}
		catch (Exception exp)
		{
			mSC.log("Error during Scheduled", exp);
		}
	}
	
//	protected void updateLiveDir() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
//	{
//		File currentDir = MWUrls.findNewestDir(new File(MWUrls.M_MW_CACHE));
//		// remove live dir if present
//		File liveDir = new File(MWUrls.M_MW_CACHE, "live");
//		if (liveDir.exists())
//		{
//			mSC.log("Scheduled::updateLiveDir remove livedir" + liveDir.getAbsolutePath());
//			FileUtils.deleteQuietly(liveDir);
//		}
//		liveDir.mkdir();
//		liveDir.list();
//		// TODO: this is a hack - sometimes mkdir seems to fail "magically" on network mounted drives
//		if (liveDir.exists())
//		{
//			mSC.log("Scheduled::updateLiveDir confirmed " + liveDir.getAbsolutePath());
//		}
//		else
//		{
//			mSC.log("Scheduled::updateLiveDir dir missing " + liveDir.getAbsolutePath());
//			throw new StdMwException("Scheduled::updateLiveDir dir missing " + liveDir.getAbsolutePath());
//		}
//		FileUtils.copyDirectory(currentDir, liveDir);
//		mSC.log("Scheduled::updateLiveDir Build New Utils " + liveDir.getAbsolutePath());
//		SummaryUtil su = SummaryUtil.updateSummaryUtil("live");
//		AnalysisUtil au = AnalysisUtil.updateAnalysisUtil("live", su);
//		MetaboliteUtil mu = MetaboliteUtil.updateMetaboliteUtil("live", au);
//		RefMetUtil ru = RefMetUtil.updateRefMetUtil("live");
//		OtherIdsUtil ou = OtherIdsUtil.updateOtherIdsUtil("live", ru, mu);
//		ValidateUtil vu = new ValidateUtil(mu, ru, ou);
//		if (false==vu.validate("live", au.getRandomIds()))
//		{
//			mSC.log("Scheduled::updateLiveDir LiveDir run failed to pass validate");
//			FileUtils.deleteQuietly(liveDir);
//		}
//		else
//		{
//			mSC.log("Scheduled::updateLiveDir LiveDir run passed validate");
//		}
//	}
	
	static private SummaryUtil mSummary = null;
	static private AnalysisUtil mAnalysis = null;
	static private MetaboliteUtil mMetaUtil = null;
	static private RefMetUtil mRefmetUtil = null;
	static private OtherIdsUtil mOtherIdsUtil = null;
	
	static synchronized public void updateUtilObjects(ServletContext theSC) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		mSummary = SummaryUtil.readNewestSummaryFile();
		mAnalysis = AnalysisUtil.readNewestAnalysisFile();
		mMetaUtil = MetaboliteUtil.readNewestMetaboliteFile();
		mRefmetUtil = RefMetUtil.readNewestRefMetFile();
		mOtherIdsUtil = OtherIdsUtil.readNewestOtherIdsFile();
	}
	
	static synchronized public SummaryUtil getSummary()
	{
		return mSummary;
	}
	
	static synchronized public AnalysisUtil getAnalysis()
	{
		return mAnalysis;
	}
	
	static synchronized public MetaboliteUtil getMetaUtil()
	{
		return mMetaUtil;
	}
	
	static synchronized public RefMetUtil getRefmetUtil()
	{
		return mRefmetUtil;
	}
	
	static synchronized public OtherIdsUtil getOtherIdsUtil()
	{
		return mOtherIdsUtil;
	}
}
