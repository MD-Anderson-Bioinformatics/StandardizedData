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
import edu.mda.bcb.gdc.api.portal.GDC_Mixin;

/**
 *
 * @author Tod-Casasent
 */
public class ListProjects extends Endpoint_Mixin
{
	protected GDC_Mixin mGDC = null;
	protected String mState = null;
	protected String mLegacyTCGAonly = "";
	
	public ListProjects(GDC_Mixin theGDC)
	{
		super(theGDC.mIsLegacyFlag);
		mGDC = theGDC;
		mState = "open";
		mLegacyTCGAonly = "";
		if (mIsLegacy)
		{
			mState = "legacy";
			mLegacyTCGAonly = "{\"op\":\"=\",\"content\":{\"field\":\"program.name\",\"value\":\"TCGA\"}},";
		}
	}
	
	@Override
	protected void processJson(String theJSON)
	{
		if (null!=theJSON)
		{
			//GDCAPI.printLn(theJSON);
			GDCAPI.printLn("ListProjects::processJson - start");
			JsonObject jsonObj = new Gson().fromJson(theJSON, JsonObject.class);
			JsonObject dataObj = new Gson().fromJson(jsonObj.get("data").toString(), JsonObject.class);
			JsonArray hitsArray = dataObj.get("hits").getAsJsonArray();
			for (JsonElement ele : hitsArray)
			{
				//GDCAPI.printLn("ListProjects::processJson - process element");
				JsonObject obj = ele.getAsJsonObject();
				// first token in project id, like TCGA
				String program = obj.get("program").getAsJsonObject().get("name").getAsString();
				// program and disease code, like TCGA-BRCA
				String project = obj.get("project_id").getAsString();
				//
				mGDC.addEntry(program, project);
			}
		}
		GDCAPI.printLn("ListProjects::processJson - finish");
	}

	@Override
	protected String getURLendpoint()
	{
		return "/projects";
	}

	@Override
	protected String getParameters()
	{
		return 
				"{" +
					"\"pretty\":\"true\"," +
					"\"size\":\"9999\"," +
					"\"filters\":" +
					"{" + 
						"\"op\":\"and\"," +
						"\"content\":" +
						"[" + mLegacyTCGAonly +
							"{" +
								"\"op\":\"=\"," +
								"\"content\":" +
								"{" +
									"\"field\":\"released\"," +
									"\"value\":\"true\"" +
								"}" +
							"}," +
							"{" +
								"\"op\":\"=\"," +
								"\"content\":" +
								"{" +
									"\"field\":\"state\"," +
									"\"value\":\"" + mState + "\"" +
								"}" +
							"}" +
						"]" +
					"}," +
					"\"fields\":\"program.name,project_id,disease_type\"" +
				"}";
	}
}
