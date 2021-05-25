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
import edu.mda.bcb.gdc.api.data.DataType;
import edu.mda.bcb.gdc.api.data.Program;
import edu.mda.bcb.gdc.api.data.Project;
import edu.mda.bcb.gdc.api.portal.GDC_Mixin;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class WorkflowsLegacy extends Endpoint_Mixin
{
	protected GDC_Mixin mGDC = null;
	protected Project mProject = null;
	protected String mWorkflowName = null;
	protected String mWorkflowJson = null;
	protected String mWorkflowDatatype = null;
	protected String mWorkflowPlatform = null;
	
	public WorkflowsLegacy(GDC_Mixin theGDC)
	{
		super(theGDC.mIsLegacyFlag);
		mGDC = theGDC;
		// processEndpoint override sets this before calling super
		mProject = null;
	}

	@Override
	public void processEndpoint() throws Exception
	{
		for (Program program : mGDC.mFileMap.values())
		{
			for (Project project : program.mProjects.values())
			{
				// this used in call to getParameters
				mProject = project;
				for (int cnt=0;cnt<LegacyWorkflowStrings.M_WORKFLOW_NAMES.length;cnt++)
				{
					// iterate over different desired legacy types
					mWorkflowName = LegacyWorkflowStrings.M_WORKFLOW_NAMES[cnt];
					mWorkflowJson = LegacyWorkflowStrings.M_WORKFLOW_JSON[cnt];
					mWorkflowDatatype = LegacyWorkflowStrings.M_WORKFLOW_DATATYPE[cnt];
					mWorkflowPlatform = LegacyWorkflowStrings.M_WORKFLOW_PLATFORM[cnt];
					//GDCAPI.printLn("WorkflowsLegacy::processEndpoint - process name " + mWorkflowName + " for project " + mProject.mName);
					super.processEndpoint(); 
				}
			}
		}
	}
	
	public String processForJsonString(JsonObject theObj, String theAttributeTokens)
	{
		String [] splitted = theAttributeTokens.split("\\|", -1);
		String result = null;
		for(String attribute : splitted)
		{
			String addMe = null;
			if ("tags".equals(attribute))
			{
				JsonArray tagsArray = theObj.get(attribute).getAsJsonArray();
				TreeSet<String> tagSet = new TreeSet<>();
				for (JsonElement ele : tagsArray)
				{
					tagSet.add(ele.getAsString());
				}
				for(String tag : tagSet)
				{
					if (null==addMe)
					{
						addMe = tag;
					}
					else
					{
						addMe = addMe + "-" + tag;
					}
				}
			}
			else
			{
				addMe = theObj.get(attribute).getAsString();
			}
			if (null==result)
			{
				result = addMe;
			}
			else
			{
				result = result + "-" + addMe;
			}
		}
		return result;
	}
	
	@Override
	protected void processJson(String theJSON)
	{
		//GDCAPI.printLn("WorkflowsLegacy::processJson - start");
		//GDCAPI.printLn(theJSON);
		JsonObject jsonObj = new Gson().fromJson(theJSON, JsonObject.class);
		JsonObject dataObj = new Gson().fromJson(jsonObj.get("data").toString(), JsonObject.class);
		JsonArray hitsArray = dataObj.get("hits").getAsJsonArray();
		for (JsonElement ele : hitsArray)
		{
			//GDCAPI.printLn("WorkflowsLegacy::processJson - process element");
			JsonObject obj = ele.getAsJsonObject();
			String data_type = processForJsonString(obj, mWorkflowDatatype);
			String workflow_type = processForJsonString(obj, mWorkflowPlatform);
			DataType dt = mProject.addEntry(data_type);
			dt.addEntry(workflow_type, mWorkflowName);
		}
		//GDCAPI.printLn("WorkflowsLegacy::processJson - finish");
	}

	@Override
	protected String getURLendpoint()
	{
		return "/files";
	}

	@Override
	protected String getParameters()
	{
		String params = "{" +
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
									"\"value\":\"" + mProject.mName + "\"" +
								"}" +
							"}" + mWorkflowJson +
						"]" +
					"}," +
					"\"fields\":\"file_name,data_type,data_category,experimental_strategy,tags,platform\"" +
				"}";
		//GDCAPI.printLn("WorkflowsLegacy json request = " + params);
		return params;
	}
}
