/*
 *  Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 University of Texas MD Anderson Cancer Center
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
package edu.mda.bioinfo.stdmwutils;

import edu.mda.bioinfo.stdmwutils.mwdata.MWUrls;
import edu.mda.bioinfo.stdmwutils.utils.AnalysisUtil;
import edu.mda.bioinfo.stdmwutils.utils.SummaryUtil;
import edu.mda.bioinfo.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bioinfo.stdmwutils.utils.RefMetUtil;
import edu.mda.bioinfo.stdmwutils.utils.OtherIdsUtil;
import edu.mda.bioinfo.stdmwutils.validate.ValidateUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author TDCasasent
 */
public class StdMwDownload
{

	static private File M_OUTPUT_FILE = null;

	public static void main(String[] args)
	{
		try
		{
			StdMwDownload.printLn("StdMwDownload Starting");
			StdMwDownload.printLn(getVersion());
			// use normal download for normal runs (check this one in)
			//normalDownload();
			// use test read for new development (adding to existing tables, without having to download everything)
			testRead();
			StdMwDownload.printLn("StdMwDownload Complete");
		}
		catch (Exception exp)
		{
			StdMwDownload.printErr("Error in main", exp);
		}
	}

	static private void normalDownload() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		String timeStamp = StdMwDownload.getTimestamp();
		SummaryUtil su = SummaryUtil.updateSummaryUtil(timeStamp);
		AnalysisUtil au = AnalysisUtil.updateAnalysisUtil(timeStamp, su);
		MetaboliteUtil mu = MetaboliteUtil.updateMetaboliteUtil(timeStamp, au);
		RefMetUtil ru = RefMetUtil.updateRefMetUtil(timeStamp);
		OtherIdsUtil ou = OtherIdsUtil.updateOtherIdsUtil(timeStamp, ru, mu);
		ValidateUtil vu = new ValidateUtil(mu, ru, ou);
		vu.validate(timeStamp, au.getRandomIds());
	}

	static private void testRead() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		SummaryUtil su = SummaryUtil.readNewestSummaryFile();
		AnalysisUtil au = AnalysisUtil.readNewestAnalysisFile();
		MetaboliteUtil mu = MetaboliteUtil.readNewestMetaboliteFile();
		RefMetUtil ru = RefMetUtil.readNewestRefMetFile();
		OtherIdsUtil ou = OtherIdsUtil.readNewestOtherIdsFile();
		//
		File timestampDir = MWUrls.findNewestDir(new File(MWUrls.M_MW_CACHE));
		ValidateUtil vu = new ValidateUtil(mu, ru, ou);
		vu.validate(timestampDir.getName(), au.getRandomIds());
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
		return "StdMWUtils 2020-09-11-1000";
	}

	static public ArrayList<String> cleanCSVline(String theLine)
	{
		ArrayList<String> tokens = new ArrayList<>();
		// first split string on comma
		return tokens;
	}
}
