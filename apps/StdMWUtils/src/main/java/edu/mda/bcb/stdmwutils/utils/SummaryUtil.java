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
package edu.mda.bcb.stdmwutils.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.mda.bcb.stdmwutils.DataMap;
import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.StdMwException;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.mwdata.Summary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class SummaryUtil
{

	static public SummaryUtil updateSummaryUtil(String theTimeStamp) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		// TODO: check for newest summary file, and download new and compare to old
		File outDir = new File(MWUrls.M_MW_CACHE, theTimeStamp);
		SummaryUtil su = new SummaryUtil();
		su.fetchSummaries();
		su.writeSummaries(outDir);
		return su;
	}

	static public SummaryUtil readNewestSummaryFile() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		File timestampDir = MWUrls.findNewestDir(new File(MWUrls.M_MW_CACHE));
		SummaryUtil su = new SummaryUtil();
		su.readSummaries(timestampDir);
		return su;
	}

	private DataMap<Summary> mDataMap = null;
	
	public TreeSet<Summary> getAll()
	{
		return mDataMap.getAll();
	}
	
	public Summary get(String theHash)
	{
		return mDataMap.get(theHash);
	}

	public SummaryUtil()
	{
	}

	public void fetchSummaries() throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		mDataMap = new DataMap<>();
		StdMwDownload.printLn("fetchSummaries - connecting to " + MWUrls.M_SUMMARY_LIST);
		try (InputStream is = new URL(MWUrls.M_SUMMARY_LIST).openStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
		{
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			StdMwDownload.printLn("fetchSummaries - call parse reader");
			JsonElement jEle = JsonParser.parseReader(reader);
			JsonObject jObj = jEle.getAsJsonObject();
			StdMwDownload.printLn("fetchSummaries - iterate summaries");
			for (Map.Entry<String, JsonElement> entry : jObj.entrySet())
			{
				JsonObject sumObj = entry.getValue().getAsJsonObject();
				Summary summary = gson.fromJson(sumObj, Summary.class);
				summary.init();
				mDataMap.put(summary.hash, summary);
			}
			StdMwDownload.printLn("fetchSummaries - finished iterating");
		}
	}

	public void writeSummaries(File theDir) throws IOException
	{
		if (mDataMap.size() > 0)
		{
			theDir.mkdir();
			File output = new File(theDir, MWUrls.M_STUDIES);
			StdMwDownload.printLn("writeSummaries - write to " + output.getAbsolutePath());
			OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
			try (BufferedWriter bw = java.nio.file.Files.newBufferedWriter(output.toPath(), Charset.availableCharsets().get("UTF-8"), options))
			{
				bw.write(Summary.getHeaderString());
				bw.newLine();
				StdMwDownload.printLn("writeSummaries - iterate summaries");
				TreeSet<Summary> sortedSummaries = mDataMap.getAll();
				for (Summary summary : sortedSummaries)
				{
					System.out.print(".");
					bw.write(summary.getRowString());
					bw.newLine();
					bw.flush();
				}
				System.out.println(".");
				StdMwDownload.printLn("writeSummaries - finished iterating");
			}
		}
	}

	public void readSummaries(File theDir) throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		mDataMap = new DataMap<>();
		File input = new File(theDir, MWUrls.M_STUDIES);
		StdMwDownload.printLn("readSummaries input = " + input.getAbsolutePath());
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(input.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			// headers
			String line = br.readLine();
			ArrayList<String> headers = new ArrayList<>();
			headers.addAll(Arrays.asList(line.split("\t", -1)));
			// first summary line
			line = br.readLine();
			while (null != line)
			{
				// empty string for no prefix on headers
				Summary sum = Summary.getFromRowString(headers, line, "");
				mDataMap.put(sum.hash, sum);
				line = br.readLine();
			}
		}
	}

	public String getAsJson()
	{
		TreeSet<Summary> summarySet = mDataMap.getAll();
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.toJson(summarySet);
	}
}
