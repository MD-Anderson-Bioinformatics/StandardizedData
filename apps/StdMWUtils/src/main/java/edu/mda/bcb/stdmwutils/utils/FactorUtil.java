// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.stdmwutils.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.mda.bcb.stdmwutils.DataMap;
import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.StdMwException;
import edu.mda.bcb.stdmwutils.mwdata.Factor;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
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
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class FactorUtil
{
	private DataMap<Factor> mDataMap = null;
	private String mStudyId = null;
	
	private FactorUtil(String theStudyId)
	{
		mStudyId = theStudyId;
	}
	
	private Factor getFactor(String theHash)
	{
		return mDataMap.get(theHash);
	}

	private int loadFactors() throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		mDataMap = new DataMap<>();
		String url = MWUrls.getAllFactors(mStudyId);
		StdMwDownload.printLn("FactorUtil - connecting to " + url);
		int countSamples = 0;
		try (InputStream is = new URL(url).openStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
		{
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			//gson.DisplayRun dr = gson.fromJson(jr, DisplayRun.class);
			StdMwDownload.printLn("FactorUtil - call parse reader");
			JsonElement jEle = JsonParser.parseReader(reader);
			JsonObject jObj = jEle.getAsJsonObject();
			// TODO: jObj.has("Row1") is a clunky way to detect if we got a single object or an array, but I haven't found an alternative
			if (true==jObj.has("Row1"))
			{
				StdMwDownload.printLn("FactorUtil - iterate factor");
				for (Map.Entry<String, JsonElement> entry : jObj.entrySet())
				{
					JsonObject sumObj = entry.getValue().getAsJsonObject();
					Factor factor = gson.fromJson(sumObj, Factor.class);
					factor.init();
					mDataMap.put(factor.hash, factor);
					countSamples += 1;
				}
			}
			else
			{
				StdMwDownload.printLn("FactorUtil - single factor");
				JsonObject sumObj = jEle.getAsJsonObject();
				Factor factor = gson.fromJson(sumObj, Factor.class);
				factor.init();
				mDataMap.put(factor.hash, factor);
				countSamples += 1;
			}
			StdMwDownload.printLn("FactorUtil - finished iterating");
		}
		return countSamples;
	}
	
	static private int getFactorsForStudy(String theStudyId, TreeSet<Factor> theFactors) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		StdMwDownload.printLn("FactorUtil getFactorsForStudy");
		FactorUtil au = new FactorUtil(theStudyId);
		int countSamples = au.loadFactors();
		theFactors.addAll(au.mDataMap.getAll());
		return countSamples;
	}
	
	static public int getFactorNameListForStudy(String theStudyId, TreeSet<String> theNames) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		StdMwDownload.printLn("FactorUtil getFactorNameListForStudy");
		TreeSet<Factor> factors = new TreeSet<>();
		int sampleCount = getFactorsForStudy(theStudyId, factors);
		for (Factor fac : factors)
		{
			for (String name : fac.factorMap.keySet())
			{
				theNames.add(name);
			}
		}
		return sampleCount;
	}
	
	
	static public void getBatchesTSV(OutputStream theOut, String theStudyId) throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		TreeSet<String> names = new TreeSet<>();
		getFactorNameListForStudy(theStudyId, names);
		// headers
		theOut.write("Samples".getBytes());
		for(String name : names)
		{
			theOut.write(("\t" + name).getBytes());
		}
		theOut.write("\n".getBytes());
		// write rows
		TreeSet<Factor> factors = new TreeSet<>();
		getFactorsForStudy(theStudyId, factors);
		for (Factor fac : factors)
		{
			theOut.write(fac.local_sample_id.getBytes());
			for(String name : names)
			{
				String val = fac.factorMap.get(name);
				if (null==val)
				{
					val = "Unknown";
				}
				theOut.write(("\t" + val).getBytes());
			}
			theOut.write("\n".getBytes());
		}
	}
}
