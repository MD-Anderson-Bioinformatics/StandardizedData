// Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
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
import edu.mda.bcb.stdmwutils.mwdata.Analysis;
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
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class AnalysisUtil
{
	static public AnalysisUtil updateAnalysisUtil(String theTimeStamp, SummaryUtil theSU) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		// TODO: check for newest summary file, and download new and compare to old
		File outDir = new File(MWUrls.M_MWB_CACHE, theTimeStamp);
		AnalysisUtil au = new AnalysisUtil();
		File existing = new File(outDir, MWUrls.M_ANALYSIS);
		if (existing.exists())
		{
			au.readAnalyses(outDir);
		}
		else
		{
			for (Summary sum : theSU.getAll())
			{
				au.fetchAnalyses(sum.hash, sum.study_id);
			}
		}
		au.writeAnalyses(outDir);
		return au;
	}
	
	static public AnalysisUtil readNewestAnalysisFile() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		File timestampDir = MWUrls.findNewestDir(new File(MWUrls.M_MWB_CACHE));
		AnalysisUtil su = new AnalysisUtil();
		su.readAnalyses(timestampDir);
		return su;
	}
	
	private DataMap<Analysis> mDataMap = null;
	private TreeMap<String, TreeSet<Analysis>> mStudyHashToMap = null;
	
	public AnalysisUtil()
	{
		mDataMap = new DataMap<>();
		mStudyHashToMap = new TreeMap<>();
	}
	
	public TreeSet<Analysis> getAnalysisFromStudyHash(String theStudyHash)
	{
		return mStudyHashToMap.get(theStudyHash);
	}
	
	public Analysis getAnalysisFromId(String theAnalysisId)
	{
		Analysis an = null;
		for (Analysis check : mDataMap.getAll())
		{
			if (theAnalysisId.equals(check.analysis_id))
			{
				an = check;
			}
		}
		return an;
	}
	
	public Analysis getAnalysis(String theHash)
	{
		return mDataMap.get(theHash);
	}
	
	public TreeSet<Analysis> getAnalysesAll()
	{
		return mDataMap.getAll();
	}
	
	public void fetchAnalyses(String theStudyHash, String theStudyId) throws MalformedURLException, IOException, NoSuchAlgorithmException
	{
		String url = MWUrls.getAnalyses(theStudyId);
		StdMwDownload.printLn("fetchAnalyses - connecting to " + url);
		try (InputStream is = new URL(url).openStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
		{
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			//gson.DisplayRun dr = gson.fromJson(jr, DisplayRun.class);
			StdMwDownload.printLn("fetchAnalyses - call parse reader");
			JsonElement jEle = JsonParser.parseReader(reader);
			if (jEle.isJsonObject())
			{
				JsonObject jObj = jEle.getAsJsonObject();
				// TODO: jObj.has("1") is a clunky way to detect if we got a single object or an array, but I haven't found an alternative
				if (true==jObj.has("1"))
				{
					StdMwDownload.printLn("fetchAnalyses - iterate analysis");
					for (Map.Entry<String, JsonElement> entry : jObj.entrySet())
					{
						JsonObject sumObj = entry.getValue().getAsJsonObject();
						Analysis analysis = gson.fromJson(sumObj, Analysis.class);
						analysis.init(theStudyHash);
						try
						{
							mDataMap.put(analysis.hash, analysis);
							TreeSet<Analysis> ts = mStudyHashToMap.get(analysis.study_hash);
							if (null==ts)
							{
								ts = new TreeSet<>();
							}
							ts.add(analysis);
							mStudyHashToMap.put(analysis.study_hash, ts);
						}
						catch(Exception exp)
						{
							// duplicate analysis is weird, but acceptible
							StdMwDownload.printWarn("Duplicate analysis " + analysis.analysis_id + " for " + analysis.study_id);
						}
					}
				}
				else
				{
					StdMwDownload.printLn("fetchAnalyses - single analysis");
					JsonObject sumObj = jEle.getAsJsonObject();
					Analysis analysis = gson.fromJson(sumObj, Analysis.class);
					analysis.init(theStudyHash);
					try
					{
						mDataMap.put(analysis.hash, analysis);
						TreeSet<Analysis> ts = mStudyHashToMap.get(analysis.study_hash);
						if (null==ts)
						{
							ts = new TreeSet<>();
						}
						ts.add(analysis);
						mStudyHashToMap.put(analysis.study_hash, ts);
					}
					catch(StdMwException exp)
					{
						// duplicate analysis is weird, but acceptible
						StdMwDownload.printWarn("Duplicate analysis " + analysis.analysis_id + " for " + analysis.study_id);
					}
				}
				StdMwDownload.printLn("fetchAnalyses - finished iterating");
			}
			else
			{
				StdMwDownload.printLn("fetchAnalyses - no analysis");
			}
		}
	}
	
	public void writeAnalyses(File theDir) throws IOException
	{
		if (mDataMap.size()>0)
		{
			theDir.mkdir();
			File output = new File(theDir, MWUrls.M_ANALYSIS);
			StdMwDownload.printLn("writeAnalyses - write to " + output.getAbsolutePath());
			OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
			try(BufferedWriter bw = java.nio.file.Files.newBufferedWriter(output.toPath(), Charset.availableCharsets().get("UTF-8"), options))
			{
				bw.write(Analysis.getHeaderString());
				bw.newLine();
				StdMwDownload.printLn("writeAnalyses - iterate summaries");
				TreeSet<Analysis> fullSet = mDataMap.getAll();
				int cnt = 0;
				for (Analysis analysis : fullSet)
				{
					if (0 == cnt % 100)
					{
						System.out.print(".");
					}
					bw.write(analysis.getRowString());
					bw.newLine();
					bw.flush();
					cnt += 1;
					if (cnt > 10000)
					{
						System.out.println(".");
						cnt = 0;
					}
				}
				System.out.println(".");
				StdMwDownload.printLn("writeAnalyses - finished iterating");
			}
		}
	}

	public void readAnalyses(File theDir) throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		mDataMap = new DataMap<>();
		File input = new File(theDir, MWUrls.M_ANALYSIS);
		StdMwDownload.printLn("readAnalyses input = " + input.getAbsolutePath());
		try(BufferedReader br = java.nio.file.Files.newBufferedReader(input.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			// headers
			String line = br.readLine();
			ArrayList<String> headers = new ArrayList<>();
			headers.addAll(Arrays.asList(line.split("\t", -1)));
			// first summary line
			line = br.readLine();
			while(null!=line)
			{
				// empty string for no prefix on headers
				Analysis analysis = Analysis.getFromRowString(headers, line, "");
				mDataMap.put(analysis.hash, analysis);
				TreeSet<Analysis> ts = mStudyHashToMap.get(analysis.study_hash);
				if (null==ts)
				{
					ts = new TreeSet<>();
				}
				ts.add(analysis);
				mStudyHashToMap.put(analysis.study_hash, ts);
				line = br.readLine();
			}
		}
	}

	public String getAsJson()
	{
		TreeSet<Analysis> fullSet = mDataMap.getAll();
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.toJson(fullSet);
	}
	
	public ArrayList<String> getRandomIds()
	{
		ArrayList<Analysis> set = new ArrayList<>(mDataMap.getAll());
		ArrayList<String> ids = new ArrayList<>();
		//
		Random rand = new Random(314);
		ids.add(set.get(0).analysis_id);
		ids.add(set.get(rand.nextInt(set.size())).analysis_id);
		ids.add(set.get(rand.nextInt(set.size())).analysis_id);
		ids.add(set.get(rand.nextInt(set.size())).analysis_id);
		ids.add(set.get(rand.nextInt(set.size())).analysis_id);
		ids.add(set.get(rand.nextInt(set.size())).analysis_id);
		ids.add(set.get(set.size()-1).analysis_id);
		return ids;
	}
	
	public ArrayList<String> getAllIds()
	{
		ArrayList<Analysis> set = new ArrayList<>(mDataMap.getAll());
		ArrayList<String> ids = new ArrayList<>();
		for (Analysis an : set)
		{
			ids.add(an.analysis_id);
		}
		return ids;
	}
}
