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

package edu.mda.bcb.gdc.api.endpoints;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.mda.bcb.gdc.api.GDCAPI;

/**
 *
 * @author Tod-Casasent
 */
public class DataTypes extends Endpoint_Mixin
{
	public String mProgram = null;
	public String mProject = null;
	public String mDataType = null;
	
	public DataTypes(String theProgram, String theProject, String theDataType)
	{
		super(false);
		mProgram = theProgram;
		mProject = theProject;
		mDataType = theDataType;
	}

	@Override
	protected void processJson(String theJSON)
	{
		//GDCAPI.printLn("DataTypes::processJson - start");
		GDCAPI.printLn("DataTypes::processJson - for: " + mProgram + ", " + mProject + ", " + mDataType);
		//GDCAPI.printLn(theJSON);
		JsonObject jsonObj = new Gson().fromJson(theJSON, JsonObject.class);
		JsonObject dataObj = new Gson().fromJson(jsonObj.get("data").toString(), JsonObject.class);
		JsonArray hitsArray = dataObj.get("hits").getAsJsonArray();
		for (JsonElement ele : hitsArray)
		{
			//GDCAPI.printLn("DataTypes::processJson - process element");
			JsonObject obj = ele.getAsJsonObject();
			if (null!=obj.get("analysis"))
			{
				String data_type = obj.get("").getAsString();
			}
		}
		//GDCAPI.printLn("DataTypes::processJson - finish");
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
									"\"field\":\"cases.project.program.name\"," +
									"\"value\":\"" + mProgram + "\"" +
								"}" +
							"}," +
							"{" +
								"\"op\":\"=\"," +
								"\"content\":" +
								"{" +
									"\"field\":\"cases.project.project_id\"," +
									"\"value\":\"" + mProject + "\"" +
								"}" +
							"}," +
							"{" +
								"\"op\":\"=\"," +
								"\"content\":" +
								"{" +
									"\"field\":\"data_type\"," +
									"\"value\":\"" + mDataType + "\"" +
								"}" +
							"}," +
							"{" +
								"\"op\":\"in\"," +
								"\"content\":" +
								"{" +
									"\"field\":\"data_format\"," +
									"\"value\":[\"TSV\", \"BCR XML\", \"XLSX\"]" +
								"}" +
							"}" +
						"]" +
					"}," +
					"\"fields\":\"file_name,file_id,md5sum,data_format\"" +
				"}";
	}
	//cases.project.program.program_id
}
