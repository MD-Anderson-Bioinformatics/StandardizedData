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
package edu.mda.bioinfo.stdmwutils.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.mda.bioinfo.stdmwutils.DataToSet;
import edu.mda.bioinfo.stdmwutils.StdMwDownload;
import edu.mda.bioinfo.stdmwutils.StdMwException;
import edu.mda.bioinfo.stdmwutils.mwdata.Analysis;
import edu.mda.bioinfo.stdmwutils.mwdata.MWUrls;
import edu.mda.bioinfo.stdmwutils.mwdata.Metabolite;
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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class MetaboliteUtil
{

	static public MetaboliteUtil updateMetaboliteUtil(String theTimeStamp, AnalysisUtil theAU) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		// TODO: check for newest file, and download new and compare to old
		File outDir = new File(MWUrls.M_MW_CACHE, theTimeStamp);
		MetaboliteUtil au = new MetaboliteUtil();
		for (Analysis ana : theAU.getAnalysesAll())
		{
			au.fetchMetabolites(ana.analysis_id);
		}
		au.writeMetabolites(outDir);
		return au;
	}

	static public MetaboliteUtil readNewestMetaboliteFile() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		File timestampDir = MWUrls.findNewestDir(new File(MWUrls.M_MW_CACHE));
		MetaboliteUtil su = new MetaboliteUtil();
		su.readMetabolites(timestampDir);
		return su;
	}

	private DataToSet<Metabolite> mDataToSet = null;

	private MetaboliteUtil()
	{
		mDataToSet = new DataToSet<>();
	}
	
	public TreeSet<Metabolite> getAll()
	{
		return mDataToSet.getAll();
	}

	public TreeSet<Metabolite> getMetabolitesForAnalysis(String theAnalysisId)
	{
		return mDataToSet.get(theAnalysisId);
	}

	public String getMetabolitesForAnalysisAsJson(String theAnalysisId)
	{
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson.toJson(mDataToSet.get(theAnalysisId));
	}

	private void fetchMetabolites(String theAnalysisId) throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		String url = MWUrls.getMetabolites(theAnalysisId);
		StdMwDownload.printLn("fetchMetabolites - connecting to " + url);
		try (InputStream is = new URL(url).openStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
		{
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			//gson.DisplayRun dr = gson.fromJson(jr, DisplayRun.class);
			StdMwDownload.printLn("fetchMetabolites - call parse reader");
			JsonElement jEle = JsonParser.parseReader(reader);
			if (jEle.isJsonObject())
			{
				JsonObject jObj = jEle.getAsJsonObject();
				// TODO: jObj.has("1") is a clunky way to detect if we got a single object or an array, but I haven't found an alternative
				if (true == jObj.has("1"))
				{
					StdMwDownload.printLn("fetchMetabolites - iterate Metabolite");
					for (Map.Entry<String, JsonElement> entry : jObj.entrySet())
					{
						JsonObject sumObj = entry.getValue().getAsJsonObject();
						Metabolite metabolite = gson.fromJson(sumObj, Metabolite.class);
						metabolite.init();
						mDataToSet.put(metabolite.analysis_id, metabolite);
					}
				}
				else
				{
					StdMwDownload.printLn("fetchMetabolites - single Metabolite");
					JsonObject sumObj = jEle.getAsJsonObject();
					Metabolite metabolite = gson.fromJson(sumObj, Metabolite.class);
					metabolite.init();
					mDataToSet.put(metabolite.analysis_id, metabolite);
				}
				StdMwDownload.printLn("fetchMetabolites - finished iterating");
			}
			else
			{
				StdMwDownload.printLn("fetchMetabolites - no metabolites");
			}
		}
	}

	public void writeMetabolites(File theDir) throws IOException
	{
		if (mDataToSet.size() > 0)
		{
			theDir.mkdir();
			File output = new File(theDir, MWUrls.M_METABOLITES);
			StdMwDownload.printLn("writeMetabolites - write to " + output.getAbsolutePath());
			try (BufferedWriter bw = java.nio.file.Files.newBufferedWriter(output.toPath(), Charset.availableCharsets().get("UTF-8")))
			{
				bw.write(Metabolite.getHeaderString());
				bw.newLine();
				StdMwDownload.printLn("writeMetabolites - iterate Metabolites");
				TreeSet<Metabolite> fullSet = mDataToSet.getAll();
				for (Metabolite metabolite : fullSet)
				{
					System.out.print(".");
					bw.write(metabolite.getRowString());
					bw.newLine();
					bw.flush();
				}
				StdMwDownload.printLn("writeMetabolites - finished iterating");
			}
		}
	}

	public void readMetabolites(File theDir) throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		mDataToSet = new DataToSet<>();
		File input = new File(theDir, MWUrls.M_METABOLITES);
		StdMwDownload.printLn("readMetabolites input = " + input.getAbsolutePath());
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
				Metabolite metabolite = Metabolite.getFromRowString(headers, line);
				mDataToSet.put(metabolite.analysis_id, metabolite);
				line = br.readLine();
			}
		}
	}
	
	public TreeSet<String> getPubChemId()
	{
		TreeSet<String> names = new TreeSet<>();
		TreeSet<Metabolite> fullSet = mDataToSet.getAll();
		for (Metabolite metabolite : fullSet)
		{
			if ((!"".equals(metabolite.pubchem_id))&&(!"NA".equals(metabolite.pubchem_id))&&(null!=metabolite.pubchem_id))
			{
				names.add(metabolite.pubchem_id);
			}
		}
		return names;
	}
}
