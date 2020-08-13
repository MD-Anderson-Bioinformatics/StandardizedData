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
import edu.mda.bioinfo.stdmw.data.Analysis;
import edu.mda.bioinfo.stdmw.data.MWUrls;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
public class AnalysisUtil
{
	private TreeMap<String, Analysis> mAnalysisMap = null;
	private String mStudyId = null;
	
	private AnalysisUtil(String theStudyId)
	{
		mStudyId = theStudyId;
	}
	
	private Analysis getAnalysis(String theHash)
	{
		return mAnalysisMap.get(theHash);
	}

	private void loadAnalyses(ServletContext theSC) throws MalformedURLException, IOException, NoSuchAlgorithmException
	{
		mAnalysisMap = new TreeMap<>();
		String url = MWUrls.getAnalyses(mStudyId);
		theSC.log("AnalysisUtil - connecting to " + url);
		try (InputStream is = new URL(url).openStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
		{
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			//gson.DisplayRun dr = gson.fromJson(jr, DisplayRun.class);
			theSC.log("AnalysisUtil - call parse reader");
			JsonElement jEle = JsonParser.parseReader(reader);
			JsonObject jObj = jEle.getAsJsonObject();
			// TODO: jObj.has("1") is a clunky way to detect if we got a single object or an array, but I haven't found an alternative
			if (true==jObj.has("1"))
			{
				theSC.log("AnalysisUtil - iterate analysis");
				for (Map.Entry<String, JsonElement> entry : jObj.entrySet())
				{
					JsonObject sumObj = entry.getValue().getAsJsonObject();
					Analysis analysis = gson.fromJson(sumObj, Analysis.class);
					analysis.init();
					mAnalysisMap.put(analysis.hash, analysis);
				}
			}
			else
			{
				theSC.log("AnalysisUtil - single analysis");
				JsonObject sumObj = jEle.getAsJsonObject();
				Analysis analysis = gson.fromJson(sumObj, Analysis.class);
				analysis.init();
				mAnalysisMap.put(analysis.hash, analysis);
			}
			theSC.log("AnalysisUtil - finished iterating");
		}
	}
	
	static public TreeSet<Analysis> getAnalysesForStudy(ServletContext theSC, HttpSession theSession, String theStudyId) throws IOException, MalformedURLException, NoSuchAlgorithmException
	{
		AnalysisUtil au = (AnalysisUtil)(theSession.getAttribute("ANALYSIS"));
		if ((null==au)||(!au.mStudyId.equals(theStudyId)))
		{
			theSC.log("AnalysisUtil load new analyses list");
			au = new AnalysisUtil(theStudyId);
			au.loadAnalyses(theSC);
			theSession.setAttribute("ANALYSIS", au);
		}
		return new TreeSet<Analysis>(au.mAnalysisMap.values());
	}
	
	static public Analysis getAnalysis(ServletContext theSC, HttpSession theSession, String theHash)
	{
		AnalysisUtil au = (AnalysisUtil)(theSession.getAttribute("ANALYSIS"));
		return au.getAnalysis(theHash);
	}
}
