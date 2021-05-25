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
package edu.mda.bcb.gdc.api.pancan;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.mda.bcb.gdc.api.DownloadUrl;
import edu.mda.bcb.gdc.api.util.BiospecimenToBatches;
import edu.mda.bcb.gdc.api.util.ClinicalToClinical;
import edu.mda.bcb.gdc.api.util.FileFind;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.TreeSet;

/**
 *
 * @author TDCasasent
 */
public class PanCanMetaData
{

	public static void getMetaData(String thePanCanStdDir, String theBiospecDir, String theClinDir) throws IOException
	{
		// find matrix_data.tsv files
		System.out.println("find matrix_data.tsv files");
		FileFind ff = new FileFind();
		ff.find(new File(thePanCanStdDir).toPath(), "matrix_data.tsv");
		TreeSet<Path> stdMatrixFiles = new TreeSet<>();
		stdMatrixFiles.addAll(ff.mMatches);
		for (Path myPath : stdMatrixFiles)
		{
			System.out.println(myPath);
		}
		// find list of newest biospecimen.tsv files
		System.out.println("find list of newest biospecimen.tsv files");
		ff = new FileFind();
		ff.find(new File(theBiospecDir).toPath(), "biospecimen.tsv");
		TreeSet<Path> biospecimenFiles = new TreeSet<>();
		biospecimenFiles.addAll(ff.mMatches);
		for (Path myPath : biospecimenFiles)
		{
			System.out.println(myPath);
		}
		for (Path myDatafile : stdMatrixFiles)
		{
			File myBatchFile = new File(myDatafile.toFile().getParentFile(), "batches.tsv");
			BiospecimenToBatches btb = new BiospecimenToBatches(myDatafile.toFile(), biospecimenFiles, myBatchFile);
			btb.generateBatchesFile();
		}
		// find list of newest clinical.tsv file
		System.out.println("find list of newest clinical.tsv file");
		ff = new FileFind();
		ff.find(new File(theClinDir).toPath(), "clinical.tsv");
		TreeSet<Path> clinicalFiles = new TreeSet<>();
		clinicalFiles.addAll(ff.mMatches);
		for (Path myPath : clinicalFiles)
		{
			System.out.println(myPath);
		}
		for (Path myDatafile : stdMatrixFiles)
		{
			File myClinicalFile = new File(myDatafile.toFile().getParentFile(), "clinical.tsv");
			ClinicalToClinical ctc = new ClinicalToClinical(myDatafile.toFile(), clinicalFiles, myClinicalFile);
			ctc.generateClinicalFile();
		}
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
			if (null != obj.get("analysis"))
			{
				// workflow -- multiple values, with repeats, keep unique occurances
				String workflow_type = obj.get("analysis").getAsJsonObject().get("workflow_type").getAsString();
				// parent value to workflow type
				String data_type = obj.get("data_type").getAsString();
				if (null != obj.get("cases"))
				{
					JsonArray casesArray = obj.get("cases").getAsJsonArray();
					for (JsonElement eleC : casesArray)
					{
						JsonObject objC = eleC.getAsJsonObject();
						if (null != objC.get("project"))
						{
							JsonObject project = objC.get("project").getAsJsonObject();
							String project_id = project.get("project_id").getAsString();
							if (null != project.get("program"))
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
		return "{\n"
				+ "	\"pretty\":\"true\",\n"
				+ "	\"size\":\"999999\",\n"
				+ "	\"filters\":\n"
				+ "	{\n"
				+ "		\"op\":\"and\",\n"
				+ "		\"content\":\n"
				+ "		[\n"
				+ "			{\n"
				+ "				\"op\":\"=\",\n"
				+ "				\"content\":\n"
				+ "				{\n"
				+ "					\"field\":\"state\",\n"
				+ "					\"value\":\"released\"\n"
				+ "				}\n"
				+ "			},\n"
				+ "			{\n"
				+ "				\"op\":\"=\",\n"
				+ "				\"content\":\n"
				+ "				{\n"
				+ "					\"field\":\"access\",\n"
				+ "					\"value\":\"open\"\n"
				+ "				}\n"
				+ "			},\n"
				+ "			{\n"
				+ "				\"op\":\"NOT\",\n"
				+ "				\"content\":\n"
				+ "				{\n"
				+ "					\"field\":\"analysis.workflow_type\",\n"
				+ "					\"value\":\"MISSING\"\n"
				+ "				}\n"
				+ "			}\n"
				+ "		]\n"
				+ "	\n"
				+ "	},\n"
				+ "	\"fields\":\"analysis.workflow_type,data_type,cases.project.project_id,cases.project.program.name\"\n"
				+ "}";
	}
}
