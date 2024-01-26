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
package edu.mda.bcb.stdmwutils;

import static edu.mda.bcb.stdmwutils.StdMwDownload.getVersion;
import edu.mda.bcb.stdmwutils.mwdata.Analysis;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.std.ZipData;
import edu.mda.bcb.stdmwutils.utils.AnalysisUtil;
import edu.mda.bcb.stdmwutils.utils.DownloadConvertSingle;
import edu.mda.bcb.stdmwutils.utils.DownloadJsonToString;
import edu.mda.bcb.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bcb.stdmwutils.utils.OtherIdsUtil;
import edu.mda.bcb.stdmwutils.utils.RefMetUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;


/**
 * Used in Batch Effects Interface to download a particular dataset from the MW
 * within a BEI docker container. Checks with BEI webapp for pending requests.
 * Call in a loop from a Bash script.
 * 
 * @author Tod-Casasent
 */
public class MWStack
{
	// This class is used to check for MW download requests part of BEI
	public static void main(String[] args)
	{
		System.out.println(StdMwDownload.getVersion());
		String outputDir = args[0];
		File outputFile = new File(outputDir);
		String urlBase = args[1];
		String once = args[2];
		//String outputDir = "/BEI/OUTPUT/";
		//String urlBase = "http://localhost:8084/";
		//String once = "once";
		while(true)
		{
			if (!outputFile.exists())
			{
				System.out.println("Directory does not exist " + outputDir);
				System.exit(0);
			}
			else
			{
				if (!"once".equals(once))
				{
					System.out.println("Wait 2 minutes");
					try
					{
						Thread.sleep(2*60*1000);
					}
					catch(Exception ignore)
					{
						System.out.println("Wake from sleep");
					}
				}
				try
				{
					System.out.println("check for jobs");
					String jobId = getNext(urlBase);
					if (jobId.equals("none"))
					{
						System.out.println("no job to process");
					}
					else
					{
						System.out.println("received job " + jobId);
						String basedir = new File(outputDir, jobId).getAbsolutePath();
						File primaryCurrentDir = new File(basedir, "PRI");
						File secondaryCurrentDir = new File(basedir, "SEC");
						if (new File(primaryCurrentDir, "PROCESS.TXT").exists())
						{
							StdMwDownload.setLogDir(primaryCurrentDir);
							// if it exists, process the directory
							// this will remove the PROCESS.TXT
							//String theDatasetConfig = "DatasetConfig_PRI-current.log";
							String theMWSuccess = "MW_SUCCESS.txt";
							String theMWFailure = "MW_FAILURE.txt";
							String theInProcessStatus = "NEWJOB_PRIMARY_MWRUN_WAIT";
							String theSuccessStatus = "NEWJOB_DONE";
							if (new File(secondaryCurrentDir, "PROCESS.TXT").exists())
							{
								theSuccessStatus = "NEWJOB_PRIMARY_DONE";
							}
							String theFailureStatus = "NEWJOB_FAILURE";
							File finalDataDir = new File(new File(basedir, "ZIP-DATA"), "original");
							finalDataDir.mkdirs();
							File finalMatrix1 = new File(finalDataDir, "matrix.tsv");
							File finalBatchs1 = new File(finalDataDir, "batches.tsv");
							File finalLinkmp1 = new File(finalDataDir, "ngchm_link_map.tsv");
							File finalRowcol1 = new File(finalDataDir, "row_col_types.tsv");
							// process directory
							processDir(urlBase, jobId, primaryCurrentDir, theMWSuccess, theMWFailure, theInProcessStatus,
									theSuccessStatus, theFailureStatus, finalMatrix1, finalBatchs1, finalLinkmp1, finalRowcol1);
						}
						if (new File(secondaryCurrentDir, "PROCESS.TXT").exists())
						{
							StdMwDownload.setLogDir(secondaryCurrentDir);
							// if it exists, process the directory
							// this will remove the PROCESS.TXT
							//String theDatasetConfig = "DatasetConfig_SEC-current.log";
							String theMWSuccess = "MW_SUCCESS2.txt";
							String theMWFailure = "MW_FAILURE2.txt";
							String theInProcessStatus = "NEWJOB_SECONDARY_MWRUN_WAIT";
							String theSuccessStatus = "NEWJOB_SECONDARY_DONE";
							String theFailureStatus = "NEWJOB_FAILURE";
							File finalDataDir = new File(new File(basedir, "ZIP-DATA"), "original");
							finalDataDir.mkdirs();
							File finalMatrix2 = new File(finalDataDir, "matrix_data2.tsv");
							File finalBatchs2 = new File(finalDataDir, "batches2.tsv");
							File finalLinkmp2 = new File(finalDataDir, "ngchm_link_map2.tsv");
							File finalRowcol2 = new File(finalDataDir, "row_col_types2.tsv");
							// process directory
							processDir(urlBase, jobId, secondaryCurrentDir, theMWSuccess, theMWFailure, theInProcessStatus,
									theSuccessStatus, theFailureStatus, finalMatrix2, finalBatchs2, finalLinkmp2, finalRowcol2);
						}
					}

				}
				catch(Exception exp)
				{
					StdMwDownload.printErr("Error in run", exp);
					exp.printStackTrace(System.err);
				}
				finally
				{
					StdMwDownload.setLogDir(null);
				}
				if ("once".equals(once))
				{
					System.exit(0);
				}
			}
		}
	}
	
	/**
	 * For a given directory (there are primary and secondary data dirs),
	 * download the data, convert it, and copy to needed locations.
	 * 
	 * @param theUrlBase URL base, such as http://127.0.0.1:8080 or http://bei
	 * @param theJobId Job Id for data being processed
	 * @param theBaseDir Base directory for data
	 * @param theMWSuccess File to write to indicate success
	 * @param theMWFailure File to write to indicate failure
	 * @param theInProcessStatus Status to set job in web-app to indicate in-process
	 * @param theSuccessStatus Status to set job in web-app to indicate success
	 * @param theFailureStatus Status to set job in web-app to indicate failure
	 * @param theFinalMatrix Place to write final Matrix file
	 * @param theFinalBatchs Place to write final batch file
	 * @param theFinalLinkmp  Place to write final link map file
	 * @param theFinalRowcol  Place to write final row col type file
	 */
	public static void processDir(String theUrlBase, String theJobId,
			File theBaseDir, String theMWSuccess, String theMWFailure, 
			String theInProcessStatus, String theSuccessStatus, String theFailureStatus,
			File theFinalMatrix, File theFinalBatchs, File theFinalLinkmp, File theFinalRowcol)
	{
		try
		{
			postStatus(theUrlBase, theJobId, theInProcessStatus);
			StdMwDownload.printLn("main start");
			// process download and convert
			File zipFile = processDirectory(theBaseDir, theJobId);
			// copy matrix and batch files
			StdMwDownload.printLn("copy files");
			ZipData.extractFile(zipFile, "matrix.tsv", theFinalMatrix);
			ZipData.extractFile(zipFile, "batches.tsv", theFinalBatchs);
			ZipData.extractFile(zipFile, "ngchm_link_map.tsv", theFinalLinkmp);
			ZipData.extractFile(zipFile, "row_col_types.tsv", theFinalRowcol);
			// mark as complete
			StdMwDownload.printLn("mark as complete");
			StdMwDownload.printLn("new File(theBaseDir, theMWSuccess)=" + new File(theBaseDir, theMWSuccess).getAbsolutePath());
			new File(theBaseDir, theMWSuccess).createNewFile();
			StdMwDownload.printLn("theSuccessStatus=" + theSuccessStatus);
			postStatus(theUrlBase, theJobId, theSuccessStatus);
		}
		catch(Exception exp)
		{
			StdMwDownload.printErr("Error exception", exp);
			exp.printStackTrace(System.err);
			try
			{
				if ((null!=theJobId)&&(!"none".equals(theJobId)))
				{
					postStatus(theUrlBase, theJobId, theFailureStatus);
				}
			}
			catch(Exception exp2)
			{
				exp2.printStackTrace(System.err);
			}
			try
			{
				if (null!=theBaseDir)
				{
					new File(theBaseDir, theMWFailure).createNewFile();
				}
			}
			catch(Exception exp3)
			{
				exp3.printStackTrace(System.err);
			}
		}
	}
	
	/**
	 * Get the next job id which is requesting download from the BEI webapp.
	 * 
	 * @param theURLbase URL base, such as http://127.0.0.1:8080 or http://bei
	 * @return String giving job id to be processed or string "none"
	 * @throws Exception 
	 */
	public static String getNext(String theURLbase) throws Exception
	{
		System.out.println("get next");
		//String myUrl = "http://127.0.0.1:8080/BEI/BEI/JOBnext?jobType=MWDLD";
		String myUrl = theURLbase + "/MBA/MBA/JOBnext?jobType=MWDDLD";
		String result = DownloadJsonToString.downloadTextToString(myUrl);
		return result;
	}
	
	/**
	 * Update the job id inthe BEI webapp with a new status
	 * 
	 * @param theURLbase URL base, such as http://127.0.0.1:8080 or http://bei
	 * @param theJob JobId string
	 * @param theStatus Status string to which to set job
	 * @throws IOException
	 * @throws Exception 
	 */
	public static void postStatus(String theURLbase, String theJob, String theStatus) throws IOException, Exception
	{
		System.out.println("post job " + theJob + " for status " + theStatus);
		//String myUrl = "http://127.0.0.1:8080/BEI/BEI/JOBupdate?jobId=" + theJob + "&status=" + theStatus;
		String myUrl = theURLbase + "/MBA/MBA/JOBupdate?jobId=" + theJob + "&status=" + theStatus;
		String result = DownloadJsonToString.downloadTextToString(myUrl);
		if (!theJob.equalsIgnoreCase(result))
		{
			throw new Exception("Error updating status");
		}
	}

	/**
	 * Perform download and convert using the given job id and base directory
	 * (there are primary and secondary data dirs). Read the dataset.txt
	 * file and download it using downloadDataset.
	 * 
	 * @param theBaseDir Base directory (there are primary and secondary data dirs)
	 * @param theJobId job id string from BEI webapp
	 * @return File pointing to data download/convert location -- null indicates nothing downloaded.
	 * @throws IOException
	 * @throws Exception 
	 */
	public static File processDirectory(File theBaseDir, String theJobId) throws IOException, Exception
	{
		StdMwDownload.printLn("baseDir ='" + theBaseDir + "'");
		StdMwDownload.printLn("jobid   ='" + theJobId + "'");
		String analysisId = null;
		StdMwDownload.printLn("read analysis id from   ='" + new File(theBaseDir, "dataset.txt") + "'");
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(new File(theBaseDir, "dataset.txt").toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			// Study id
			br.readLine();
			// analysis id
			analysisId = br.readLine();
			// title
			br.readLine();
		}
		StdMwDownload.printLn("analysisId ='" + analysisId + "'");
		return downloadDataset(theBaseDir, analysisId, theJobId);
	}

	public static File downloadDataset(File theBaseDir, String theAnalysisId, String theJobId) throws Exception
	{
		StdMwDownload.printLn(getVersion());
		AnalysisUtil analysisUtil = AnalysisUtil.readNewestAnalysisFile();
		Analysis analysis = analysisUtil.getAnalysisFromId(theAnalysisId);
		if (null!=analysis)
		{
			MetaboliteUtil metaUtil = MetaboliteUtil.readNewestMetaboliteFile();
			RefMetUtil refmetUtil = RefMetUtil.readNewestRefMetFile();
			OtherIdsUtil otherIdsUtil = OtherIdsUtil.readNewestOtherIdsFile();
			DownloadConvertSingle dcs = new DownloadConvertSingle(analysis, analysisUtil, refmetUtil, otherIdsUtil, metaUtil);
			String zip = dcs.dAndC();
			zip = dcs.mAnalysis.study_hash + "/" + dcs.mAnalysis.hash + "/" + dcs.mAnalysis.hash + ".zip";
			return new File(MWUrls.M_MWB_TEMP, zip);
		}
		return null;
	}
}
