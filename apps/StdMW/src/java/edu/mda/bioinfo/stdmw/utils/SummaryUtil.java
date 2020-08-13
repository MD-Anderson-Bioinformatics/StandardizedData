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
import edu.mda.bioinfo.stdmw.data.MWUrls;
import edu.mda.bioinfo.stdmw.data.Summary;
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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Tod-Casasent
 */
public class SummaryUtil
{
	private TreeMap<String, Summary> mSummaryMap = null;
	private long mTimestamp = 0;
	
	public SummaryUtil()
	{
		
	}
	
	synchronized public TreeMap<String, Summary> getMap(ServletContext theSC) throws MalformedURLException, IOException, NoSuchAlgorithmException
	{
		long timestamp = System.currentTimeMillis();
		// if over one day
		if ((((timestamp-mTimestamp)/1000)/60/60)>=24)
		{
			theSC.log("over a day: reload summaries");
			loadSummaries(theSC);
		}
		return mSummaryMap;
	}
	
	synchronized public Summary getSummary(String theHash)
	{
		return mSummaryMap.get(theHash);
	}

	public void loadSummaries(ServletContext theSC) throws MalformedURLException, IOException, NoSuchAlgorithmException
	{
		mSummaryMap = new TreeMap<>();
		theSC.log("SummaryUtil - connecting to " + MWUrls.M_SUMMARY_LIST);
		try (InputStream is = new URL(MWUrls.M_SUMMARY_LIST).openStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
		{
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			//gson.DisplayRun dr = gson.fromJson(jr, DisplayRun.class);
			theSC.log("SummaryUtil - call parse reader");
			JsonElement jEle = JsonParser.parseReader(reader);
			JsonObject jObj = jEle.getAsJsonObject();
			theSC.log("SummaryUtil - iterate summaries");
			for (Map.Entry<String, JsonElement> entry : jObj.entrySet())
			{
				JsonObject sumObj = entry.getValue().getAsJsonObject();
				Summary summary = gson.fromJson(sumObj, Summary.class);
				summary.init(theSC);
				mSummaryMap.put(summary.hash, summary);
			}
			theSC.log("SummaryUtil - finished iterating");
			mTimestamp = System.currentTimeMillis();
		}
	}

}
