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

/**
 *
 * @author Tod-Casasent
 */
public class GDCJob
{
	public static void main(String[] args)
	{
		System.out.println(GDCAPI.getVersion());
		String outputDir = "/BEI/OUTPUT";
		String urlBase = "http://beiService:8080";
		//String outputDir = "/BEI/OUTPUT/";
		//String urlBase = "http://localhost:8084/";
		try
		{
			System.out.println("check for jobs");
			String jobId = args[0];
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
				String theFailureStatus = "NEWJOB_START";
				File finalMatrix1 = new File(basedir, "matrix_data.tsv");
				File finalBatches1 = new File(basedir, "batches.tsv");
				File finalMutations1 = new File(basedir, "mutations.tsv");
				// process directory
				GDCStack.processDir(urlBase, jobId, primaryCurrentDir, theDatasetConfig, theGDCSuccess, theGDCFailure, theInProcessStatus,
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
				String theFailureStatus = "NEWJOB_DONE";
				File finalMatrix2 = new File(basedir, "matrix_data2.tsv");
				File finalBatches2 = new File(basedir, "batches2.tsv");
				File finalMutations2 = new File(basedir, "mutations2.tsv");
				// process directory
				GDCStack.processDir(urlBase, jobId, secondaryCurrentDir, theDatasetConfig, theGDCSuccess, theGDCFailure, theInProcessStatus,
						theSuccessStatus, theFailureStatus, finalMatrix2, finalBatches2, finalMutations2);
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
	}
}
