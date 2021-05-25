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
package edu.mda.bcb.gdc.api;

import static edu.mda.bcb.gdc.api.GDCAPI.getVersion;
import edu.mda.bcb.gdc.api.convert.BiospecimenXML;
import edu.mda.bcb.gdc.api.convert.ClinicalXML;
import edu.mda.bcb.gdc.api.data.DataType;
import edu.mda.bcb.gdc.api.data.Program;
import edu.mda.bcb.gdc.api.data.Project;
import edu.mda.bcb.gdc.api.data.Workflow;
import edu.mda.bcb.gdc.api.portal.GDCcurrent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;

/**
 * Used in Batch Effects Interface to download a particular dataset from the GDC
 * within a BEI docker container. Checks with BEI webapp for pending requests.
 * Call in a loop from a Bash script.
 * 
 * @author Tod-Casasent
 */
public class GDCStack
{
	// This class is used to check for GDC download requests part of BEI
	public static void main(String[] args)
	{
		System.out.println(GDCAPI.getVersion());
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
							GDCAPI.setLogDir(primaryCurrentDir);
							// if it exists, process the directory
							// this will remove the PROCESS.TXT
							//String theDatasetConfig = "DatasetConfig_PRI-current.log";
							String theGDCSuccess = "GDC_SUCCESS.txt";
							String theGDCFailure = "GDC_FAILURE.txt";
							String theInProcessStatus = "NEWJOB_PRIMARY_GDCRUN_WAIT";
							String theSuccessStatus = "NEWJOB_DONE";
							if (new File(secondaryCurrentDir, "PROCESS.TXT").exists())
							{
								theSuccessStatus = "NEWJOB_PRIMARY_DONE";
							}
							String theFailureStatus = "NEWJOB_FAILURE";
							File finalDataDir = new File(new File(basedir, "ZIP-DATA"), "original");
							finalDataDir.mkdirs();
							File finalMatrix1 = new File(finalDataDir, "matrix_data.tsv");
							File finalBatches1 = new File(finalDataDir, "batches.tsv");
							File finalMutations1 = new File(finalDataDir, "mutations.tsv");
							// process directory
							processDir(urlBase, jobId, primaryCurrentDir, theGDCSuccess, theGDCFailure, theInProcessStatus,
									theSuccessStatus, theFailureStatus, finalMatrix1, finalBatches1, finalMutations1);
						}
						if (new File(secondaryCurrentDir, "PROCESS.TXT").exists())
						{
							GDCAPI.setLogDir(secondaryCurrentDir);
							// if it exists, process the directory
							// this will remove the PROCESS.TXT
							//String theDatasetConfig = "DatasetConfig_SEC-current.log";
							String theGDCSuccess = "GDC_SUCCESS2.txt";
							String theGDCFailure = "GDC_FAILURE2.txt";
							String theInProcessStatus = "NEWJOB_SECONDARY_GDCRUN_WAIT";
							String theSuccessStatus = "NEWJOB_SECONDARY_DONE";
							String theFailureStatus = "NEWJOB_FAILURE";
							File finalDataDir = new File(new File(basedir, "ZIP-DATA"), "original");
							finalDataDir.mkdirs();
							File finalMatrix2 = new File(finalDataDir, "matrix_data2.tsv");
							File finalBatches2 = new File(finalDataDir, "batches2.tsv");
							File finalMutations2 = new File(finalDataDir, "mutations2.tsv");
							// process directory
							processDir(urlBase, jobId, secondaryCurrentDir, theGDCSuccess, theGDCFailure, theInProcessStatus,
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
	}
	
	/**
	 * For a given directory (there are primary and secondary data dirs),
	 * download the data, convert it, and copy to needed locations.
	 * 
	 * @param theUrlBase URL base, such as http://127.0.0.1:8080 or http://bei
	 * @param theJobId Job Id for data being processed
	 * @param theBaseDir Base directory for data
	 * @param theGDCSuccess File to write to indicate success
	 * @param theGDCFailure File to write to indicate failure
	 * @param theInProcessStatus Status to set job in web-app to indicate in-process
	 * @param theSuccessStatus Status to set job in web-app to indicate success
	 * @param theFailureStatus Status to set job in web-app to indicate failure
	 * @param theFinalMatrix Place to write final Matrix file
	 * @param theFinalBatches Place to write final batch file
	 * @param theFinalMutations  Place to write final mutations file
	 */
	public static void processDir(String theUrlBase, String theJobId,
			File theBaseDir, String theGDCSuccess, String theGDCFailure, 
			String theInProcessStatus, String theSuccessStatus, String theFailureStatus,
			File theFinalMatrix, File theFinalBatches, File theFinalMutations)
	{
		try
		{
			postStatus(theUrlBase, theJobId, theInProcessStatus);
			GDCAPI.printLn("main start");
			// process download and convert
			File convertDir = processDirectory(theBaseDir, theJobId);
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
		//String myUrl = "http://127.0.0.1:8080/BEI/BEI/JOBnext?jobType=GDCDLD";
		String myUrl = theURLbase + "/BEI/BEI/JOBnext?jobType=GDCDLD";
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
		String myUrl = theURLbase + "/BEI/BEI/JOBupdate?jobId=" + theJob + "&status=" + theStatus;
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
		GDCAPI.printLn("baseDir ='" + theBaseDir + "'");
		GDCAPI.printLn("jobid   ='" + theJobId + "'");
		String program = null;
		String project = null;
		String dataType = null;
		String workflow = null;
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(new File(theBaseDir, "dataset.txt").toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			program = br.readLine();
			project = br.readLine();
			dataType = br.readLine();
			workflow = br.readLine();
		}
		GDCAPI.printLn("program ='" + program + "'");
		GDCAPI.printLn("project ='" + project + "'");
		GDCAPI.printLn("dataType='" + dataType + "'");
		GDCAPI.printLn("workflow='" + workflow + "'");
		return downloadDataset(theBaseDir, program, project, dataType, workflow, theJobId);
	}

	/**
	 * Perform download and convert using the given data from the 
	 * dataset.txt file from processDirectory.
	 * 
	 * @param theBaseDir Base directory (there are primary and secondary data dirs)
	 * @param theProgram String giving GDC program name (may not match common usage or BEV GUI)
	 * @param theProject String giving GDC project name (may not match common usage or BEV GUI)
	 * @param theDataType String giving GDC datatype name (may not match common usage or BEV GUI)
	 * @param theWorkflow String giving GDC workflow name (may not match common usage or BEV GUI)
	 * @param theJobId job id string from BEI webapp
	 * @return File pointing to data download/convert location -- null indicates nothing downloaded.
	 * @throws Exception 
	 */
	public static File downloadDataset(File theBaseDir, String theProgram,
			String theProject, String theDataType, String theWorkflow, String theJobId) throws Exception
	{
		File utilDir = new File(theBaseDir, "util");
		GDCAPI.printLn(getVersion());
		GDCAPI.printLn("baseDir ='" + theBaseDir + "'");
		GDCAPI.printLn("program ='" + theProgram + "'");
		GDCAPI.printLn("project ='" + theProject + "'");
		GDCAPI.printLn("dataType='" + theDataType + "'");
		GDCAPI.printLn("workflow='" + theWorkflow + "'");
		GDCAPI.printLn("jobid   ='" + theJobId + "'");
		// temp dir
		GDCAPI.M_TEMP_DIR = new File(theBaseDir, "tmp");
		GDCAPI.M_TEMP_DIR.mkdirs();
		// this is timestamp, but is used to name manifest files
		GDCAPI.M_TIMESTAMP = theJobId;
		Program programInst = new Program(theProgram);
		programInst.addEntry(theProject);
		Project projectInst = programInst.mProjects.get(theProject);
		projectInst.addEntry(theDataType);
		DataType dataTypeInst = projectInst.mDatatypes.get(theDataType);
		dataTypeInst.addEntry(theWorkflow, null);
		Workflow workflowInst = dataTypeInst.mWorkflows.get(theWorkflow);
		// load data from GDC
		projectInst.biospecimenGetOrUpdateFromGDC(theProgram);
		projectInst.clinicalGetOrUpdateFromGDC(theProgram);
		workflowInst.manifestGetOrUpdateFromGDC_Current(theProgram, theProject, theDataType);
		// make local dirs
		File currentDir = new File(theBaseDir, "current");
		currentDir.mkdirs();
		File clinicalDir = new File(theBaseDir, "clinical");
		clinicalDir.mkdirs();
		File biospecimenDir = new File(theBaseDir, "biospecimen");
		biospecimenDir.mkdirs();
		// manifest sub-dirs
		File currentManifestDir = new File(currentDir, "manifest");
		currentManifestDir.mkdirs();
		File clinicalManifestDir = new File(clinicalDir, "manifest");
		clinicalManifestDir.mkdirs();
		File biospecimenManifestDir = new File(biospecimenDir, "manifest");
		biospecimenManifestDir.mkdirs();
		// write manifest files
		workflowInst.mManifest.writeManifest(currentManifestDir);
		projectInst.mClinical.writeManifest(clinicalManifestDir);
		projectInst.mBiospecimen.writeManifest(biospecimenManifestDir);
		// download sub-dirs
		File currentDownloadDir = new File(currentDir, "download");
		currentDownloadDir.mkdirs();
		File clinicalDownloadDir = new File(clinicalDir, "download");
		clinicalDownloadDir.mkdirs();
		File biospecimenDownloadDir = new File(biospecimenDir, "download");
		biospecimenDownloadDir.mkdirs();
		// download files
		workflowInst.mManifest.download(false, currentDownloadDir);
		projectInst.mClinical.download(false, clinicalDownloadDir);
		projectInst.mBiospecimen.download(false, biospecimenDownloadDir);
		// convert sub-dirs
		File currentConvertDir = new File(currentDir, "convert");
		currentConvertDir.mkdirs();
		File clinicalConvertDir = new File(clinicalDir, "convert");
		clinicalConvertDir.mkdirs();
		File biospecimenConvertDir = new File(biospecimenDir, "convert");
		biospecimenConvertDir.mkdirs();
		// convert files
		BiospecimenXML.processDirectory(projectInst.mBiospecimen.mFiles, biospecimenDownloadDir,
				new File(biospecimenConvertDir, "biospecimen.tsv"),
				programInst, projectInst, dataTypeInst, workflowInst);
		ClinicalXML.processDirectory(projectInst.mClinical.mFiles, clinicalDownloadDir, new File(clinicalConvertDir, "clinical.tsv"));
		return GDCcurrent.doCurrentConverts(currentDownloadDir, currentConvertDir, utilDir, biospecimenConvertDir, clinicalConvertDir,
				programInst, projectInst, dataTypeInst, workflowInst);
	}
}
