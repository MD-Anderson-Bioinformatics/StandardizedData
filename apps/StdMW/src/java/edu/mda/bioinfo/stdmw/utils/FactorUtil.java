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

package edu.mda.bioinfo.stdmw.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.mda.bioinfo.stdmw.data.Factor;
import edu.mda.bioinfo.stdmw.data.MWUrls;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Tod-Casasent
 */
public class FactorUtil
{
	private TreeMap<String, Factor> mFactorMap = null;
	private String mStudyId = null;
	
	private FactorUtil(String theStudyId)
	{
		mStudyId = theStudyId;
	}
	
	private Factor getFactor(String theHash)
	{
		return mFactorMap.get(theHash);
	}

	private void loadFactors(ServletContext theSC) throws MalformedURLException, IOException, NoSuchAlgorithmException
	{
		mFactorMap = new TreeMap<>();
		String url = MWUrls.getFactors(mStudyId);
		theSC.log("FactorUtil - connecting to " + url);
		try (InputStream is = new URL(url).openStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
		{
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			//gson.DisplayRun dr = gson.fromJson(jr, DisplayRun.class);
			theSC.log("FactorUtil - call parse reader");
			JsonElement jEle = JsonParser.parseReader(reader);
			JsonObject jObj = jEle.getAsJsonObject();
			// TODO: jObj.has("1") is a clunky way to detect if we got a single object or an array, but I haven't found an alternative
			if (true==jObj.has("1"))
			{
				theSC.log("FactorUtil - iterate factor");
				for (Map.Entry<String, JsonElement> entry : jObj.entrySet())
				{
					JsonObject sumObj = entry.getValue().getAsJsonObject();
					Factor factor = gson.fromJson(sumObj, Factor.class);
					factor.init(theSC);
					mFactorMap.put(factor.hash, factor);
				}
			}
			else
			{
				theSC.log("FactorUtil - single factor");
				JsonObject sumObj = jEle.getAsJsonObject();
				Factor factor = gson.fromJson(sumObj, Factor.class);
				factor.init(theSC);
				mFactorMap.put(factor.hash, factor);
			}
			theSC.log("FactorUtil - finished iterating");
		}
	}
	
	static private TreeSet<Factor> getFactorsForStudy(ServletContext theSC, String theStudyId) throws IOException, MalformedURLException, NoSuchAlgorithmException
	{
		theSC.log("FactorUtil getFactorsForStudy");
		FactorUtil au = new FactorUtil(theStudyId);
		au.loadFactors(theSC);
		return new TreeSet<Factor>(au.mFactorMap.values());
	}
	
	static public TreeSet<String> getFactorNameListForStudy(ServletContext theSC, String theStudyId) throws IOException, MalformedURLException, NoSuchAlgorithmException
	{
		theSC.log("FactorUtil getFactorNameListForStudy");
		TreeSet<String> names = new TreeSet<>();
		TreeSet<Factor> factors = getFactorsForStudy(theSC, theStudyId);
		for (Factor fac : factors)
		{
			for (String name : fac.factorMap.keySet())
			{
				names.add(name);
			}
		}
		return names;
	}
	
	
	static public void getBatchesTSV(OutputStream theOut, ServletContext theSC, HttpSession theSession, String theStudyId) throws MalformedURLException, IOException, NoSuchAlgorithmException
	{
		TreeSet<String> names = getFactorNameListForStudy(theSC, theStudyId);
		// headers
		theOut.write("Samples".getBytes());
		for(String name : names)
		{
			theOut.write(("\t" + name).getBytes());
		}
		theOut.write("\n".getBytes());
		// write rows
		TreeSet<Factor> factors = getFactorsForStudy(theSC, theStudyId);
		for (Factor fac : factors)
		{
			theOut.write(fac.local_sample_id.getBytes());
			for(String name : names)
			{
				theOut.write(("\t" + fac.factorMap.get(name)).getBytes());
			}
			theOut.write("\n".getBytes());
		}
	}
}
