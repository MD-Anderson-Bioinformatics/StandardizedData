// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.gdc.api.endpoints.legacy;

import edu.mda.bcb.gdc.api.endpoints.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.data.WorkflowData;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.data.Patient;
import edu.mda.bcb.gdc.api.data.Sample;
import edu.mda.bcb.gdc.api.util.UpdateableMap;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Tod-Casasent
 */
public class ManifestsLegacy extends Endpoint_Mixin
{
	protected WorkflowData mManifest = null;
	
	public ManifestsLegacy(WorkflowData theManifest, boolean theIsLegacyFlag)
	{
		super(theIsLegacyFlag);
		mManifest = theManifest;
	}
	
	@Override
	protected void processJson(String theJSON)
	{
		if (null!=theJSON)
		{
			//GDCAPI.printLn("ManifestsLegacy::processJson - start");
			GDCAPI.printLn("ManifestsLegacy::processJson - " + mManifest.toString());
			//GDCAPI.printLn(theJSON);
			JsonObject jsonObj = new Gson().fromJson(theJSON, JsonObject.class);
			JsonObject dataObj = new Gson().fromJson(jsonObj.get("data").toString(), JsonObject.class);
			JsonArray hitsArray = dataObj.get("hits").getAsJsonArray();
			if(0==hitsArray.size())
			{
				mManifest.mNotInGDC = Boolean.TRUE;
			}
			else
			{
				mManifest.mNotInGDC = Boolean.FALSE;
				for (JsonElement ele : hitsArray)
				{
					//GDCAPI.printLn("ManifestsLegacy::processJson - process element");
					JsonObject obj = ele.getAsJsonObject();
					String filename = obj.get("file_name").getAsString();
					String md5sum = obj.get("md5sum").getAsString();
					String fileUUID = obj.get("file_id").getAsString();
					// treeset used in GDCFile object -- not not delete or clear
					UpdateableMap<Sample> sampleList = new UpdateableMap<>();
					if (null!=obj.get("associated_entities"))
					{
						JsonArray entitiesArray = obj.get("associated_entities").getAsJsonArray();
						for (JsonElement entity : entitiesArray)
						{
							String sampleUUID = entity.getAsJsonObject().get("entity_id").getAsString();
							String sampleType = "-";
							if (null!=entity.getAsJsonObject().get("entity_type"))
							{
								sampleType = entity.getAsJsonObject().get("entity_type").getAsString();
							}
							String sampleBarcode = entity.getAsJsonObject().get("entity_submitter_id").getAsString();
							String caseUUID = entity.getAsJsonObject().get("case_id").getAsString();
							sampleList.put(sampleUUID, new Sample(sampleUUID, sampleBarcode, caseUUID, sampleType));
						}
					}
					// treeset used in GDCFile object -- not not delete or clear
					UpdateableMap<Patient> patientMap = new UpdateableMap<>();
					if (null!=obj.get("cases"))
					{
						JsonArray casesArray = obj.get("cases").getAsJsonArray();
						for (JsonElement cases : casesArray)
						{
							String patientUUID = cases.getAsJsonObject().get("case_id").getAsString();
							String patientBarcode = cases.getAsJsonObject().get("submitter_id").getAsString();
							patientMap.put(patientUUID, new Patient(patientUUID, patientBarcode));
						}
					}
					mManifest.addFile(new GDCFile(fileUUID, filename, md5sum, sampleList, patientMap));
				}
			}
		}
		//GDCAPI.printLn("ManifestsLegacy::processJson - finish");
	}

	@Override
	protected String getURLendpoint()
	{
		return "/files";
	}
	
	public String getWorkflowJson() throws Exception
	{
		String result = "";
		if (null!=mManifest.mLegacyName)
		{
			ArrayList<String> legacyNames = new ArrayList<>();
			legacyNames.addAll(Arrays.asList(LegacyWorkflowStrings.M_WORKFLOW_NAMES));
			int index = legacyNames.indexOf(mManifest.mLegacyName);
			if (-1!=index)
			{
				result = LegacyWorkflowStrings.M_WORKFLOW_JSON[index];
			}
			else
			{
				throw new Exception("Legacy Name '" + mManifest.mLegacyName + "' not recognized");
			}
		}
		return result;
	}

	@Override
	protected String getParameters()throws Exception
	{
		String param = "{" +
					"\"pretty\":\"true\"," +
					"\"size\":\"99999\"," +
					"\"filters\":" +
					"{" +
						"\"op\":\"and\"," +
						"\"content\":" +
						"[" +
							"{" +
								"\"op\":\"=\"," +
								"\"content\":" +
								"{" +
									"\"field\":\"state\"," +
									"\"value\":\"live\"" +
								"}" +
							"}," +
							"{" +
								"\"op\":\"=\"," +
								"\"content\":" +
								"{" +
									"\"field\":\"access\"," +
									"\"value\":\"open\"" +
								"}" +
							"}," +
							"{" +
								"\"op\":\"=\"," +
								"\"content\":" +
								"{" +
									"\"field\":\"cases.project.project_id\"," +
									"\"value\":\"" + mManifest.mProject + "\"" +
								"}" +
							"}"+ getMutationJson() + getWorkflowJson() +
						"]" +
					"}," +
					"\"fields\":\"archive.submitter_id,file_name,file_id,md5sum,cases.case_id,cases.submitter_id,cases.aliquot_ids,cases.submitter_aliquot_ids,cases.submitter_analyte_ids,associated_entities.entity_id,associated_entities.entity_submitter_id,associated_entities.case_id\"" +
				"}";
		//GDCAPI.printLn("ManifestsLegacy param=" + param);
		return param;
	}
	
	public String getMutationJson()
	{
		String json = "";
		if (mManifest.mDataType.equals("Simple somatic mutation"))
		{
			json = "," +
					"{" +
						"\"op\":\"=\"," +
						"\"content\":" +
						"{" +
							"\"field\":\"experimental_strategy\"," +
							"\"value\":\"" + mManifest.firstLegacyName() + "\"" +
						"}" +
					"}," +
					"{" +
						"\"op\":\"=\"," +
						"\"content\":" +
						"{" +
							"\"field\":\"platform\"," +
							"\"value\":\"" + mManifest.secondLegacyName() + "\"" +
						"}" +
					"}";
		}
		return json;
	}
}
