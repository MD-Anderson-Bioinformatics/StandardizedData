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
package edu.mda.bcb.gdc.api;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Tod-Casasent
 */
public class GDCStack
{
	public static void main(String[] args)
	{
		System.out.println(GDCAPI.getVersion());
		String outputDir = args[0];
		String urlBase = args[1];
		String once = args[2];
		//String outputDir = "/BEI/OUTPUT/";
		//String urlBase = "http://localhost:8084/";
		//String once = "once";
		while(true)
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
						GDCAPI.setLogDir(primaryCurrentDir);
						// if it exists, process the directory
						// this will remove the PROCESS.TXT
						String theDatasetConfig = "DatasetConfig_PRI-current.log";
						String theGDCSuccess = "GDC_SUCCESS.txt";
						String theGDCFailure = "GDC_FAILURE.txt";
						String theInProcessStatus = "NEWJOB_PRIMARY_GDCRUN_WAIT";
						String theSuccessStatus = "NEWJOB_DONE";
						if (new File(secondaryCurrentDir, "PROCESS.TXT").exists())
						{
							theSuccessStatus = "NEWJOB_PRIMARY_DONE";
						}
						String theFailureStatus = "NEWJOB_FAILURE";
						File finalMatrix1 = new File(basedir, "matrix_data.tsv");
						File finalBatches1 = new File(basedir, "batches.tsv");
						File finalMutations1 = new File(basedir, "mutations.tsv");
						// process directory
						processDir(urlBase, jobId, primaryCurrentDir, theDatasetConfig, theGDCSuccess, theGDCFailure, theInProcessStatus,
								theSuccessStatus, theFailureStatus, finalMatrix1, finalBatches1, finalMutations1);
					}
					if (new File(secondaryCurrentDir, "PROCESS.TXT").exists())
					{
						GDCAPI.setLogDir(secondaryCurrentDir);
						// if it exists, process the directory
						// this will remove the PROCESS.TXT
						String theDatasetConfig = "DatasetConfig_SEC-current.log";
						String theGDCSuccess = "GDC_SUCCESS2.txt";
						String theGDCFailure = "GDC_FAILURE2.txt";
						String theInProcessStatus = "NEWJOB_SECONDARY_GDCRUN_WAIT";
						String theSuccessStatus = "NEWJOB_SECONDARY_DONE";
						String theFailureStatus = "NEWJOB_FAILURE";
						File finalMatrix2 = new File(basedir, "matrix_data2.tsv");
						File finalBatches2 = new File(basedir, "batches2.tsv");
						File finalMutations2 = new File(basedir, "mutations2.tsv");
						// process directory
						processDir(urlBase, jobId, secondaryCurrentDir, theDatasetConfig, theGDCSuccess, theGDCFailure, theInProcessStatus,
								theSuccessStatus, theFailureStatus, finalMatrix2, finalBatches2, finalMutations2);
					}
				}

			}
			catch(Exception exp)
			{
				GDCAPI.printErr("Error in run", exp);
				exp.printStackTrace(System.err);
			}
			finally
			{
				GDCAPI.setLogDir(null);
			}
			if ("once".equals(once))
			{
				System.exit(0);
			}
		}
	}
	
	public static void processDir(String theUrlBase, String theJobId,
			File theBaseDir, String theDatasetConfig, String theGDCSuccess, String theGDCFailure, 
			String theInProcessStatus, String theSuccessStatus, String theFailureStatus,
			File theFinalMatrix, File theFinalBatches, File theFinalMutations)
	{
		try
		{
			postStatus(theUrlBase, theJobId, theInProcessStatus);
			GDCAPI.printLn("main start");
			// process download and convert
			File convertDir = SingleDataset.processDirectory(theBaseDir, theJobId);
			// copy matrix and batch files
			GDCAPI.printLn("copy mutations, matrix and batch files");
			File mutation = new File(convertDir, "mutations.tsv");
			File matrix = new File(convertDir, "matrix_data.tsv");
			File batchs = new File(convertDir, "batches.tsv");
			GDCAPI.printLn("matrix=" + matrix.getAbsolutePath());
			GDCAPI.printLn("theFinalMatrix=" + theFinalMatrix.getAbsolutePath());
			GDCAPI.printLn("batchs=" + batchs.getAbsolutePath());
			GDCAPI.printLn("theFinalBatches=" + theFinalBatches.getAbsolutePath());
			FileUtils.copyFile(matrix, theFinalMatrix);
			FileUtils.copyFile(batchs, theFinalBatches);
			if (mutation.exists())
			{
				GDCAPI.printLn("mutation=" + mutation.getAbsolutePath());
				GDCAPI.printLn("theFinalMutations=" + theFinalMutations.getAbsolutePath());
				FileUtils.copyFile(mutation, theFinalMutations);
			}
			// mark as complete
			GDCAPI.printLn("mark as complete");
			GDCAPI.printLn("new File(theBaseDir, theGDCSuccess)=" + new File(theBaseDir, theGDCSuccess).getAbsolutePath());
			new File(theBaseDir, theGDCSuccess).createNewFile();
			GDCAPI.printLn("theSuccessStatus=" + theSuccessStatus);
			postStatus(theUrlBase, theJobId, theSuccessStatus);
		}
		catch(Exception exp)
		{
			GDCAPI.printErr("Error exception", exp);
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
					new File(theBaseDir, theGDCFailure).createNewFile();
				}
			}
			catch(Exception exp3)
			{
				exp3.printStackTrace(System.err);
			}
		}
	}
	
	public static String getNext(String theURLbase) throws Exception
	{
		System.out.println("get next");
		//String myUrl = "http://127.0.0.1:8080/BEI/BEI/JOBnext?jobType=GDCDLD";
		String myUrl = theURLbase + "/BEI/BEI/JOBnext?jobType=GDCDLD";
		String result = DownloadJsonToString.downloadTextToString(myUrl);
		return result;
	}
	
	public static void postStatus(String theURLbase, String theJob, String theStatus) throws IOException, Exception
	{
		System.out.println("post job " + theJob + " for status " + theStatus);
		//String myUrl = "http://127.0.0.1:8080/BEI/BEI/JOBupdate?jobId=" + theJob + "&status=" + theStatus;
		String myUrl = theURLbase + "/BEI/BEI/JOBupdate?jobId=" + theJob + "&status=" + theStatus;
		String result = DownloadJsonToString.downloadTextToString(myUrl);
		if (!theJob.equalsIgnoreCase(result))
		{
			throw new Exception("Error updating status");
		}
	}
}
