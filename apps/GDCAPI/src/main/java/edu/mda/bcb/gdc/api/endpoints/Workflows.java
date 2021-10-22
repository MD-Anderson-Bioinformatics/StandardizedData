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

package edu.mda.bcb.gdc.api.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.data.DataType;
import edu.mda.bcb.gdc.api.data.Program;
import edu.mda.bcb.gdc.api.data.Project;
import edu.mda.bcb.gdc.api.portal.GDC_Mixin;

/**
 *
 * @author Tod-Casasent
 */
public class Workflows extends Endpoint_Mixin
{
	protected GDC_Mixin mGDC = null;
	protected Project mProject = null;
	
	public Workflows(GDC_Mixin theGDC)
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
				super.processEndpoint(); 
			}
		}
	}
	
	public void testOutput(String theProgram, String theProject) throws Exception
	{
		Project project = new Project(theProject);
		mProject = project;
		super.processEndpoint(); 
	}
	
	@Override
	protected void processJson(String theJSON)
	{
		if (null!=theJSON)
		{
			GDCAPI.printLn("Workflows::processJson - start");
			GDCAPI.printLn(theJSON);
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
					DataType dt = mProject.addEntry(data_type);
					dt.addEntry(workflow_type, null);
				}
				else
				{
					// handle current RPPA with no analysis and workflow type
					String data_type = obj.get("data_type").getAsString();
					if ("Protein Expression Quantification".equals(data_type))
					{
						String workflow_type = "RPPA";
					}
				}
			}
		}
		//GDCAPI.printLn("Workflows::processJson - finish");
	}

	@Override
	protected String getURLendpoint()
	{
		return "/files";
	}

	@Override
	protected String getParameters()
	{
		return "{" +
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
									"\"value\":\"released\"" +
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
							"}" +
						"]" +
					"}," +
					"\"fields\":\"analysis.workflow_type,data_type\"" +
				"}";
	}
}
