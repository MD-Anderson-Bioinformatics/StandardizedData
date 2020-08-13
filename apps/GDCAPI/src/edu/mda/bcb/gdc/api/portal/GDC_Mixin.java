// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.gdc.api.portal;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.data.DataType;
import edu.mda.bcb.gdc.api.data.Program;
import edu.mda.bcb.gdc.api.data.Project;
import edu.mda.bcb.gdc.api.data.Workflow;
import edu.mda.bcb.gdc.api.data.WorkflowData;
import edu.mda.bcb.gdc.api.indexes.JsonDataset;
import edu.mda.bcb.gdc.api.endpoints.ListProjects;
import edu.mda.bcb.gdc.api.endpoints.Workflows;
import edu.mda.bcb.gdc.api.endpoints.legacy.WorkflowsLegacy;
import edu.mda.bcb.gdc.api.indexes.ZipData;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author Tod-Casasent
 */
abstract public class GDC_Mixin
{

	public TreeMap<String, Program> mFileMap = null;
	public File mManifestDir = null;
	public File mDownloadDir = null;
	public File mConvertDir = null;
	public boolean mDepthDataType = false;
	public boolean mDepthWorkflows = false;
	public boolean mIsLegacyFlag = false;

	public GDC_Mixin(boolean theDepthDataType, boolean theDepthWorkflows, File theDataDir, boolean theIsLegacyFlag)
	{
		mDepthDataType = theDepthDataType;
		mDepthWorkflows = theDepthWorkflows;
		mFileMap = new TreeMap<>();
		mIsLegacyFlag = theIsLegacyFlag;
		// ---------------------------------------------------
		mManifestDir = new File(theDataDir, "manifest");
		if (!mManifestDir.exists())
		{
			mManifestDir.mkdirs();
		}
		// ---------------------------------------------------
		mDownloadDir = new File(theDataDir, "download");
		if (!mDownloadDir.exists())
		{
			mDownloadDir.mkdirs();
		}
		// ---------------------------------------------------
		mConvertDir = new File(theDataDir, "convert");
		if (!mConvertDir.exists())
		{
			mConvertDir.mkdirs();
		}
		// ---------------------------------------------------
	}

	protected void emptyTempDir()
	{
		for (File file : GDCAPI.M_TEMP_DIR.listFiles())
		{
			if (file.isFile())
			{
				file.delete();
			}
		}
	}

	static public boolean checkDirs(File theDownloadDir, File theConvertDir)
	{
		boolean proceedP = true;
		if (!theDownloadDir.exists())
		{
			proceedP = false;
			GDCAPI.printLn("Skipping conversion. No download dir. " + theDownloadDir.getAbsolutePath());
		}
		else if (theConvertDir.exists())
		{
			proceedP = false;
			GDCAPI.printLn("Skipping conversion. Convert dir already exists. " + theConvertDir.getAbsolutePath());
		}
		else
		{
			GDCAPI.printLn("Perform conversion. " + theConvertDir.getAbsolutePath());
		}
		return proceedP;
	}

	abstract public File getSpecificManifestDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow);

	abstract public File getSpecificDownloadDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow);

	abstract public File getSpecificConvertDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow);

	public Project addEntry(String theProgram, String theProject)
	{
		Program program = mFileMap.get(theProgram);
		if (null == program)
		{
			GDCAPI.printLn("GDC_Mixin::addEntry - adding new Program '" + theProgram + "'");
			program = new Program(theProgram);
			mFileMap.put(theProgram, program);
		}
		Project project = program.addEntry(theProject);
		return project;
	}

	////////////////////////////////////////////////////////////////////////////
	//// Program, Project, DataType, Workflows
	////////////////////////////////////////////////////////////////////////////
	public void loadPPDW() throws Exception
	{
		// Program, Project, DataType, Workflows
		// get from disk drive
		GDCAPI.printLn("GDCdata::loadPPDW - Load Program, Project, DataType, Workflows from Disk Drive");
		File topDir = mManifestDir;
		for (File programDir : topDir.listFiles())
		{
			if (programDir.isDirectory())
			{
				String program = programDir.getName();
				for (File projectDir : programDir.listFiles())
				{
					if (projectDir.isDirectory())
					{
						String project = projectDir.getName();
						Project projectObj = addEntry(program, project);
						if (true == mDepthDataType)
						{
							for (File dataTypeDir : projectDir.listFiles())
							{
								if (dataTypeDir.isDirectory())
								{
									String dataType = dataTypeDir.getName();
									DataType dataTypeObj = projectObj.addEntry(dataType);
									if (true == mDepthWorkflows)
									{
										for (File workflowDir : dataTypeDir.listFiles())
										{
											if (workflowDir.isDirectory())
											{
												String workflow = workflowDir.getName();
												//Workflow workflowObj = 
												String workflowName = null;
												if (mIsLegacyFlag)
												{
													workflowName = null;
												}
												dataTypeObj.addEntry(workflow, workflowName);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void newManifests() throws Exception
	{
		// get from GDC
		GDCAPI.printLn("GDCdata::gdcPPDW - Call ListProjects from GDC");
		ListProjects lp = new ListProjects(this);
		lp.processEndpoint();
		if (true == mDepthWorkflows)
		{
			if (mIsLegacyFlag)
			{
				GDCAPI.printLn("GDCdata::gdcPPDW - Call Legacy Workflows from GDC");
				WorkflowsLegacy wf = new WorkflowsLegacy(this);
				wf.processEndpoint();
			}
			else
			{
				GDCAPI.printLn("GDCdata::gdcPPDW - Call Current Workflows from GDC");
				Workflows wf = new Workflows(this);
				wf.processEndpoint();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//// Manifest files
	////////////////////////////////////////////////////////////////////////////
	public void updateManifests() throws Exception
	{
		for (Program program : mFileMap.values())
		{
			for (Project project : program.mProjects.values())
			{
				if (true == mDepthDataType)
				{
					for (DataType dataType : project.mDatatypes.values())
					{
						if (true == mDepthWorkflows)
						{
							for (Workflow workflow : dataType.mWorkflows.values())
							{
								processManifests(program, project, dataType, workflow);
							}
						}
						else
						{
							processManifests(program, project, dataType, null);
						}
					}
				}
				else
				{
					processManifests(program, project, null, null);
				}
			}
		}
	}

	abstract public void processManifests(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//// data files files
	////////////////////////////////////////////////////////////////////////////
	public void downloadCurrentManifests() throws Exception
	{
		for (Program program : mFileMap.values())
		{
			for (Project project : program.mProjects.values())
			{
				if (true == mDepthDataType)
				{
					for (DataType dataType : project.mDatatypes.values())
					{
						if (true == mDepthWorkflows)
						{
							for (Workflow workflow : dataType.mWorkflows.values())
							{
								processCurrentDownloads(program, project, dataType, workflow);
							}
						}
						else
						{
							processCurrentDownloads(program, project, dataType, null);
						}
					}
				}
				else
				{
					processCurrentDownloads(program, project, null, null);
				}
			}
		}
	}

	abstract public void processCurrentDownloads(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//// convert files
	////////////////////////////////////////////////////////////////////////////
	public void convertCurrentManifests() throws Exception
	{
		for (Program program : mFileMap.values())
		{
			for (Project project : program.mProjects.values())
			{
				if (true == mDepthDataType)
				{
					for (DataType dataType : project.mDatatypes.values())
					{
						if (true == mDepthWorkflows)
						{
							for (Workflow workflow : dataType.mWorkflows.values())
							{
								processCurrentConverts(program, project, dataType, workflow);
								emptyTempDir();
							}
						}
						else
						{
							processCurrentConverts(program, project, dataType, null);
							emptyTempDir();
						}
					}
				}
				else
				{
					processCurrentConverts(program, project, null, null);
					emptyTempDir();
				}
			}
		}
	}

	abstract public void processCurrentConverts(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//// updateIndices files
	////////////////////////////////////////////////////////////////////////////
	public void updateIndices(String theFirstString) throws Exception
	{
		//IndexInternal ii = new JsonDataset(mIndexFile);
		// only created for Workflows
		for (Program program : mFileMap.values())
		{
			for (Project project : program.mProjects.values())
			{
				for (DataType dataType : project.mDatatypes.values())
				{
					if (true == mDepthWorkflows)
					{
						for (Workflow workflow : dataType.mWorkflows.values())
						{
							File manifestDir = getSpecificManifestDir(program, project, dataType, workflow);
							workflow.manifestLoadFile(manifestDir, program.mName, project.mName, dataType.mName);
							processIndices(workflow.mManifest, theFirstString);
							emptyTempDir();
						}
					}
				}
			}
		}
	}

	abstract public String getIndexVariant();

	public void processIndices(WorkflowData theWorkflowData, String theFirstString) throws Exception
	{
		GDCAPI.printLn("processIndices - processing " + theWorkflowData.toString());
		// future path
		// base "Source" "Variant" "Project" "Sub-Project" "Data Type" "Platform" "Dataset" "Version"
		// dataset should be two strings separated by a "-"
		// first half should be Standardized, Analyzed, AutoCorrected, or Corrected
		// Second half should be Discrete, Continuous, or the correction algorithm
		// if further specialization is neded, use an underscore "_" after the second half string
		ArrayList<String> datasets = theWorkflowData.getDatasetName(theFirstString);
		for (String myDataset : datasets)
		{
			GDCAPI.printLn("processIndices - myDataset = " + myDataset);
			GDCAPI.printLn("processIndices - mConvertDir = " + mConvertDir.getAbsolutePath());
			String source = "GDC";
			String variant = getIndexVariant();
			String project = theWorkflowData.mProgram;
			String subProject = theWorkflowData.mProject;
			String dataType = theWorkflowData.mDataType;
			String platform = theWorkflowData.mWorkflow;
			String dataset = myDataset;
			String version = theWorkflowData.mTimestamp;
			File currentPath = new File(new File(new File(new File(new File(new File(mConvertDir, project), subProject), dataType), platform), version), dataset);
			////////////////////////////////////////////////////////////////
			// internal index file
			////////////////////////////////////////////////////////////////
			File matrixFile = new File(currentPath, "matrix_data.tsv");
			File indexFile = new File(currentPath, "index.json");
			GDCAPI.printLn("matrixFile = " + matrixFile.getAbsolutePath());
			GDCAPI.printLn("indexFile = " + indexFile.getAbsolutePath());
			GDCAPI.printLn("GDC_Mixin update local index");
			JsonDataset jd = new JsonDataset(source, variant, project, subProject, dataType, platform, dataset, version);
			if ((matrixFile.exists())&&(!indexFile.exists()))
			{
				if (false == GDCAPI.M_DISABLE_INDEX_INTERNAL)
				{
					jd.writeJson(indexFile);
				}
			}
			else
			{
				GDCAPI.printLn("GDC_Mixin No matrix file or internal index file already exists. Skipping INDEX_INTERNAL.");
			}
			String fileID = jd.getID();
			File zipFile = new File(currentPath, fileID + ".zip");
			if (indexFile.exists())
			{
				if (!zipFile.exists())
				{
					// ZIP files
					if (false == GDCAPI.M_DISABLE_ZIP_CREATION)
					{
						GDCAPI.printLn("GDC_Mixin zip files ");
						ZipData.zip(currentPath, zipFile);
					}
				}
				else
				{
					GDCAPI.printLn("GDC_Mixin Zip file already exists. Skipping ZIP_CREATION");
				}
			}
			else
			{
				GDCAPI.printLn("GDC_Mixin No internal index. Skipping ZIP_CREATION");
			}
			if (zipFile.exists())
			{
				if (false == GDCAPI.M_DISABLE_INDEX_EXTERNAL)
				{
					// Add to global index
					GDCAPI.printLn("GDC_Mixin update external index");
					GDCAPI.M_QUERY_INDEX.updateIndex(zipFile, fileID, jd);
				}
				// remove old files
				if (false == GDCAPI.M_DISABLE_ZIP_CLEAN)
				{
					GDCAPI.printLn("GDC_Mixin delete zipped files");
					File[] dirList = currentPath.listFiles();
					for (File myFile : dirList)
					{
						if ((myFile.isFile()) && (!myFile.getName().endsWith(".zip")))
						{
							myFile.delete();
						}
					}
				}
			}
			else
			{
				GDCAPI.printLn("GDC_Mixin No Zip. Skipping INDEX_EXTERNAL and ZIP_CLEAN");
			}
		}
	}
	
	public File findNewestConvertDir(String theProgram, String theProject)
	{
		File projectDir = new File(new File(mConvertDir, theProgram), theProject);
		File timestampDir = null;
		for (File fileDir : projectDir.listFiles())
		{
			if (fileDir.isDirectory())
			{
				if ((null==timestampDir)||(fileDir.getName().compareTo(timestampDir.getName())>0))
				{
					timestampDir = fileDir;
				}
			}
		}
		return timestampDir;
	}
}
