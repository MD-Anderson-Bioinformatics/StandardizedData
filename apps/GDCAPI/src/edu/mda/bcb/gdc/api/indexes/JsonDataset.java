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

package edu.mda.bcb.gdc.api.indexes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

/**
 *
 * @author Tod-Casasent
 */
public class JsonDataset
{
	public String source = null;
	public String variant = null;
	public String project = null;
	public String subProject = null;
	public String category = null;
	public String platform = null;
	public String data = null; // dataset first half: Standardized, Analyzed, AutoCorrected, or Corrected
	public String algorithm = null; // dataset second half: Discrete, Continuous, or the correction algorithm
	public String details = null; // underscore specialization
	public String version = null;
	
	public JsonDataset(String theSource, String theVariant, 
			String theProject, String theSubProject, String theDataType, 
			String thePlatform, String theDataset, String theVersion)
	{
		source = theSource;
		variant = theVariant;
		project = theProject;
		subProject = theSubProject;
		category = theDataType;
		platform = thePlatform;
		String [] splitted = theDataset.split("-", -1);
		data = splitted[0];
		if (splitted[1].contains("_"))
		{
			String [] subSplit = splitted[1].split("_", -1);
			algorithm = subSplit[0];
			details = subSplit[1];
		}
		else
		{
			algorithm = splitted[1];
			details = "";
		}
		version = theVersion;
	}
	
	public JsonDataset(String theSource, String theVariant, 
			String theProject, String theSubProject, String theDataType, 
			String thePlatform, String theData, String theAlgorithm, String theDetails, String theVersion)
	{
		source = theSource;
		variant = theVariant;
		project = theProject;
		subProject = theSubProject;
		category = theDataType;
		platform = thePlatform;
		data = theData;
		algorithm = theAlgorithm;
		details = theDetails;
		version = theVersion;
	}
	
	public void writeJson(File theFile) throws IOException
	{
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		if (theFile.exists())
		{
			theFile.delete();
		}
		Files.write(theFile.toPath(), gson.toJson(this).getBytes());
	}
	
	public String getLine()
	{
		return source + "\t" + variant + "\t" + project + "\t" + subProject + "\t" + category + "\t" + 
				platform + "\t" + data + "\t" + algorithm + "\t" + details + "\t" + version;
	}
	
	public String getID()
	{
		return new DigestUtils(MessageDigestAlgorithms.MD5).digestAsHex(getLine());
	}
	
	static public JsonDataset setLine(String theLine)
	{
		String [] splitted = theLine.split("\t", -1);
		String source = splitted[0];
		String variant = splitted[1];
		String project = splitted[2];
		String subProject = splitted[3];
		String category = splitted[4];
		String platform = splitted[5];
		String data = splitted[6];
		String algorithm = splitted[7];
		String details = splitted[8];
		String version = splitted[9];
		return new JsonDataset(source, variant, project, subProject, category, platform, data, algorithm, details, version);
	}
}
