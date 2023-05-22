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

import edu.mda.bcb.stdmwutils.indexes.DataIndex;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.std.MWAPI;
import edu.mda.bcb.stdmwutils.utils.AnalysisUtil;
import edu.mda.bcb.stdmwutils.utils.SummaryUtil;
import edu.mda.bcb.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bcb.stdmwutils.utils.RefMetUtil;
import edu.mda.bcb.stdmwutils.utils.OtherIdsUtil;
import edu.mda.bcb.stdmwutils.validate.ValidateUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.servlet.ServletContext;

/**
 *
 * @author TDCasasent
 */
public class StdMwDownload
{
	static private File M_OUTPUT_FILE = null;
	static private ServletContext M_SERVLET_CONTEXT = null;
	public static DataIndex M_QUERY_INDEX = null;
	
	public static void main(String[] args)
	{
		// uses directories specified in MWUrls
		String myDataVersion = "2023-03-28";
		try
		{
			RefMetUtil.updateRefMetUtil(myDataVersion);
			StdMwDownload.printLn("StdMwDownload Starting");
			StdMwDownload.printLn(getVersion());
			// use this for testing/development
			//StdMwDownload.printLn("testCacheRead");
			//testCacheRead(true);
			// use normal download for normal runs (check this one in)
			StdMwDownload.printLn("normalCacheDownload");
			normalCacheDownload(myDataVersion);
			// process analysis data for pipeline
			M_QUERY_INDEX = new DataIndex(new File(MWUrls.M_MWB_INDEXES, "mwb_index.tsv"));
			processCacheForPipeline();
			StdMwDownload.printLn("StdMwDownload Complete");
		}
		catch (Exception exp)
		{
			StdMwDownload.printErr("Error in main", exp);
		}
	}

	static private void normalCacheDownload(String theDataVersion) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		StdMwDownload.printLn("#############################################################################################");
		StdMwDownload.printLn("##################################normalCacheDownload########################################");
		StdMwDownload.printLn("#############################################################################################");
		SummaryUtil su = SummaryUtil.updateSummaryUtil(theDataVersion);
		AnalysisUtil au = AnalysisUtil.updateAnalysisUtil(theDataVersion, su);
		MetaboliteUtil mu = MetaboliteUtil.updateMetaboliteUtil(theDataVersion, au);
		RefMetUtil ru = RefMetUtil.updateRefMetUtil(theDataVersion);
		OtherIdsUtil ou = OtherIdsUtil.updateOtherIdsUtil(theDataVersion, ru, mu);
		ValidateUtil vu = new ValidateUtil(mu, ru, ou);
		vu.validate(theDataVersion, au.getRandomIds());
	}

	static private void testCacheRead(boolean theAllFlag) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		StdMwDownload.printLn("#############################################################################################");
		StdMwDownload.printLn("####################################testCacheRead############################################");
		StdMwDownload.printLn("#############################################################################################");
		SummaryUtil su = SummaryUtil.readNewestSummaryFile();
		AnalysisUtil au = AnalysisUtil.readNewestAnalysisFile();
		MetaboliteUtil mu = MetaboliteUtil.readNewestMetaboliteFile();
		RefMetUtil ru = RefMetUtil.readNewestRefMetFile();
		OtherIdsUtil ou = OtherIdsUtil.readNewestOtherIdsFile();
		//
		File timestampDir = MWUrls.findNewestDir(new File(MWUrls.M_MWB_CACHE));
		ValidateUtil vu = new ValidateUtil(mu, ru, ou);
		if (theAllFlag)
		{
			vu.validate(timestampDir.getName(), au.getAllIds());
		}
		else
		{
			vu.validate(timestampDir.getName(), au.getRandomIds());
		}
	}
	
	static private void processCacheForPipeline() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException, Exception
	{
		StdMwDownload.printLn("#############################################################################################");
		StdMwDownload.printLn("####################################processCacheForPipeline############################################");
		StdMwDownload.printLn("#############################################################################################");
		SummaryUtil su = SummaryUtil.readNewestSummaryFile();
		AnalysisUtil au = AnalysisUtil.readNewestAnalysisFile();
		MetaboliteUtil mu = MetaboliteUtil.readNewestMetaboliteFile();
		RefMetUtil ru = RefMetUtil.readNewestRefMetFile();
		OtherIdsUtil ou = OtherIdsUtil.readNewestOtherIdsFile();
		File timestampDir = MWUrls.findNewestDir(new File(MWUrls.M_MWB_CACHE));
		//
		MWAPI sp = new MWAPI(su, au, mu, ru, ou, timestampDir);
		sp.processPipeline(2000, timestampDir.getName());
	}

	static public String getTimestamp()
	{
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm");
		return dateFormat.format(calendar.getTime());
	}

	static public void setLogDir(File theDir)
	{
		if (null == theDir)
		{
			M_OUTPUT_FILE = null;
		}
		else
		{
			M_OUTPUT_FILE = new File(theDir, "std_mw_download.log");
		}
	}

	static public void setLogContext(ServletContext theContext)
	{
		if (null == theContext)
		{
			M_SERVLET_CONTEXT = null;
		}
		else
		{
			M_SERVLET_CONTEXT = theContext;
		}
	}

	static public void logIfNotNull(String theLog)
	{
		if (null != M_OUTPUT_FILE)
		{
			try
			{
				Files.write(M_OUTPUT_FILE.toPath(), (theLog + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
			catch (Exception theExp)
			{
				System.err.println("Failed writing to " + M_OUTPUT_FILE.getAbsolutePath());
				theExp.printStackTrace(System.err);
			}
		}
		if (null != M_SERVLET_CONTEXT)
		{
			try
			{
				M_SERVLET_CONTEXT.log(theLog);
			}
			catch (Exception theExp)
			{
				System.err.println("Failed writing to servlet context");
				theExp.printStackTrace(System.err);
			}
		}
	}

	static public void printWarn(String theLine)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --WRN-- " + theLine;
		System.err.println(out);
		System.err.flush();
		logIfNotNull(out);
	}

	static public void printErr(String theLine, Exception theExp)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --ERR-- " + theLine;
		System.err.println(out);
		System.err.flush();
		logIfNotNull(out);
		if (null != theExp)
		{
			theExp.printStackTrace(System.err);
			System.err.flush();
			theExp.printStackTrace(System.out);
			System.out.flush();
			logIfNotNull(theExp.getMessage());
			logIfNotNull(theExp.toString());
			Throwable cause = theExp.getCause();
			if (null != cause)
			{
				cause.printStackTrace(System.err);
				System.err.flush();
				logIfNotNull(cause.getMessage());
				logIfNotNull(cause.toString());
			}
		}
		else
		{
			printWarn("No exception included in call");
		}
	}

	static public void printErr(String theLine)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --ERR-- " + theLine;
		System.err.println(out);
		System.err.flush();
		logIfNotNull(out);
	}

	static public void printLn(String theLine)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --LOG-- " + theLine;
		System.out.println(out);
		System.out.flush();
		logIfNotNull(out);
	}

	static public String getVersion()
	{
		return "StdMWUtils BEA_VERSION_TIMESTAMP";
	}

	static public ArrayList<String> cleanCSVline(String theLine)
	{
		ArrayList<String> tokens = new ArrayList<>();
		// first split string on comma
		return tokens;
	}
}
