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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class SingleDataset
{
	public static void main_test(String[] args)
	{
		try
		{
			SingleDataset sd = new SingleDataset();
			TreeSet<String> datasets = sd.getDatasets();
			for(String dataset : datasets)
			{
				System.out.println(dataset);
			}
		}
		catch(Exception theExp)
		{
			GDCAPI.printErr("Error in main", theExp);
		}
	}
	
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

	public TreeSet<String> getDatasets() throws Exception
	{
		// Datasets are combinations of program, project, datatype, and workflow
		DownloadUrl du = new DownloadUrl("https://api.gdc.cancer.gov/files",
				null,
				null,
				5,
				60000,
				"POST",
				false,
				"application/json",
				getParameters(),
				null);
		return processJson(du.download());
	}
	
	protected TreeSet<String> processJson(String theJSON)
	{
		TreeSet<String> datasets = new TreeSet<>();
		JsonObject jsonObj = new Gson().fromJson(theJSON, JsonObject.class);
		JsonObject dataObj = new Gson().fromJson(jsonObj.get("data").toString(), JsonObject.class);
		JsonArray hitsArray = dataObj.get("hits").getAsJsonArray();
		for (JsonElement ele : hitsArray)
		{
			//GDCAPI.printLn("Workflows::processJson - process element");
			JsonObject obj = ele.getAsJsonObject();
			if (null!=obj.get("analysis"))
			{
				// workflow -- multiple values, with repeats, keep unique occurances
				String workflow_type = obj.get("analysis").getAsJsonObject().get("workflow_type").getAsString();
				// parent value to workflow type
				String data_type = obj.get("data_type").getAsString();
				if (null!=obj.get("cases"))
				{
					JsonArray casesArray = obj.get("cases").getAsJsonArray();
					for (JsonElement eleC : casesArray)
					{
						JsonObject objC = eleC.getAsJsonObject();
						if (null!=objC.get("project"))
						{
							JsonObject project = objC.get("project").getAsJsonObject();
							String project_id = project.get("project_id").getAsString();
							if (null!=project.get("program"))
							{
								String program = project.get("program").getAsJsonObject().get("name").getAsString();
								datasets.add(program + " <> " + project_id + " <> " + data_type + " <> " + workflow_type);
							}
						}
					}
				}
			}
		}
		return datasets;
	}
	
	protected String getParameters()
	{
		return	"{\n" +
				"	\"pretty\":\"true\",\n" +
				"	\"size\":\"999999\",\n" +
				"	\"filters\":\n" +
				"	{\n" +
				"		\"op\":\"and\",\n" +
				"		\"content\":\n" +
				"		[\n" +
				"			{\n" +
				"				\"op\":\"=\",\n" +
				"				\"content\":\n" +
				"				{\n" +
				"					\"field\":\"state\",\n" +
				"					\"value\":\"released\"\n" +
				"				}\n" +
				"			},\n" +
				"			{\n" +
				"				\"op\":\"=\",\n" +
				"				\"content\":\n" +
				"				{\n" +
				"					\"field\":\"access\",\n" +
				"					\"value\":\"open\"\n" +
				"				}\n" +
				"			},\n" +
				"			{\n" +
				"				\"op\":\"NOT\",\n" +
				"				\"content\":\n" +
				"				{\n" +
				"					\"field\":\"analysis.workflow_type\",\n" +
				"					\"value\":\"MISSING\"\n" +
				"				}\n" +
				"			}\n" +
				"		]\n" +
				"	\n" +
				"	},\n" +
				"	\"fields\":\"analysis.workflow_type,data_type,cases.project.project_id,cases.project.program.name\"\n" +
				"}";
	}
}
