/*
 *  Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
 *  
 *  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
 *  MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>
 */
package edu.mda.bcb.stdmwutils.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.mda.bcb.stdmwutils.DataToSet;
import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.StdMwException;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.mwdata.OtherId;
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
 * @author TDCasasent
 */
public class OtherIdsUtil
{

	static public OtherIdsUtil updateOtherIdsUtil(String theTimeStamp, RefMetUtil theRMU, MetaboliteUtil theMU) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		// TODO: check for newest file, and download new and compare to old
		File outDir = new File(MWUrls.M_MWB_CACHE, theTimeStamp);
		TreeSet<String> list = new TreeSet<>();
		list.addAll(theRMU.getPubChemId());
		list.addAll(theMU.getPubChemId());
		//
		OtherIdsUtil ou = new OtherIdsUtil();
		File existing = new File(outDir, MWUrls.M_OTHERIDS);
		if (existing.exists())
		{
			ou.readOtherIds(outDir);
		}
		else
		{
			for (String pcid : list)
			{
				ou.fetchOtherIDs(pcid);
			}
		}
		ou.writeMetabolites(outDir);
		return ou;
	}

	static public OtherIdsUtil readNewestOtherIdsFile() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		File timestampDir = MWUrls.findNewestDir(new File(MWUrls.M_MWB_CACHE));
		OtherIdsUtil ou = new OtherIdsUtil();
		ou.readOtherIds(timestampDir);
		return ou;
	}

	private DataToSet<OtherId> mDataToSet = null;

	private OtherIdsUtil()
	{
		mDataToSet = new DataToSet<>();
	}

	private void fetchOtherIDs(String thePubChemId) throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		// already filtered if (!"NA".equals(theRM.pubchem_cid))
		String url = MWUrls.getOtherIDs(thePubChemId);
		StdMwDownload.printLn("fetchOtherIDs - connecting to " + url);
		try (InputStream is = new URL(url).openStream(); Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8))
		{
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			//gson.DisplayRun dr = gson.fromJson(jr, DisplayRun.class);
			StdMwDownload.printLn("fetchOtherIds - call parse reader");
			JsonElement jEle = JsonParser.parseReader(reader);
			if (jEle.isJsonObject())
			{
				JsonObject jObj = jEle.getAsJsonObject();
				if (true == jObj.has("Row1"))
				{
					StdMwDownload.printLn("fetchOtherIds - iterate OtherIds");
					for (Map.Entry<String, JsonElement> entry : jObj.entrySet())
					{
						JsonObject sumObj = entry.getValue().getAsJsonObject();
						OtherId otherid = gson.fromJson(sumObj, OtherId.class);
						// primary pubchem_cid, secondary name
						// ignore 156700 regno until MW can fix it, as it seems to be wrong
						// https://www.metabolomicsworkbench.org/rest/compound/pubchem_cid/119046/all
						if (("156700".equals(otherid.regno))&&("119046".equals(otherid.pubchem_cid)))
						{
							StdMwDownload.printLn("fetchOtherIds - skipping 156700 119046");
						}
						else
						{
							mDataToSet.put(otherid.pubchem_cid, otherid);
						}
					}
					StdMwDownload.printLn("fetchOtherIds - finished iterating");
				}
				else
				{
					StdMwDownload.printLn("fetchOtherIds - single OtherId");
					JsonObject sumObj = jEle.getAsJsonObject();
					OtherId otherid = gson.fromJson(sumObj, OtherId.class);
					// primary pubchem_cid, secondary name
					mDataToSet.put(otherid.pubchem_cid, otherid);
				}
			}
			else
			{
				StdMwDownload.printLn("fetchOtherIds - no OtherIds");
			}
		}
	}

	public void writeMetabolites(File theDir) throws IOException
	{
		if (mDataToSet.size() > 0)
		{
			theDir.mkdir();
			File output = new File(theDir, MWUrls.M_OTHERIDS);
			StdMwDownload.printLn("writeOtherIds - write to " + output.getAbsolutePath());
			OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
			try (BufferedWriter bw = java.nio.file.Files.newBufferedWriter(output.toPath(), Charset.availableCharsets().get("UTF-8"), options))
			{
				bw.write(OtherId.getHeaderString());
				bw.newLine();
				StdMwDownload.printLn("writeOtherIds - iterate OtherIds");
				TreeSet<OtherId> fullSet = mDataToSet.getAll();
				int cnt = 0;
				for (OtherId otherId : fullSet)
				{
					if (0 == cnt % 100)
					{
						System.out.print(".");
					}
					bw.write(otherId.getRowString());
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
				StdMwDownload.printLn("writeOtherIds - finished iterating");
			}
		}
	}

	public void readOtherIds(File theDir) throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		mDataToSet = new DataToSet<>();
		File input = new File(theDir, MWUrls.M_OTHERIDS);
		StdMwDownload.printLn("readOtherIds input = " + input.getAbsolutePath());
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(input.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			// headers
			String line = br.readLine();
			ArrayList<String> headers = new ArrayList<>();
			headers.addAll(Arrays.asList(line.split("\t", -1)));
			line = br.readLine();
			while (null != line)
			{
				OtherId otherid = OtherId.getFromRowString(headers, line);
				// primary pubchem_cid, secondary name
				mDataToSet.put(otherid.pubchem_cid, otherid);
				line = br.readLine();
			}
		}
	}

	public OtherId get(String thePubChemId, String theName)
	{
		// primary pubchem_cid, secondary name
		// but keep in mind, sometimes pubchem id will match, but secondary name will not
		TreeSet<OtherId> otherIds = mDataToSet.get(thePubChemId);
		OtherId otherid = null;
		if (null!=otherIds)
		{
			if (otherIds.size()>1)
			{
				for (OtherId oi : otherIds)
				{
					if (theName.equals(oi.name))
					{
						otherid = oi;
					}
				}
			}
			else
			{
				otherid = otherIds.first();
			}
		}
		return otherid;
	}

}
