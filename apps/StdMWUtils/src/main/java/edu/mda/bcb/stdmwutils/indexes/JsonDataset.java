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

package edu.mda.bcb.stdmwutils.indexes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

/**
 * Class that contains information about a dataset and configured for use with GSON to generation JSON.
 * 
 * @author Tod-Casasent
 */
public class JsonDataset
{
	public String program = "";
	public String project = "";
	public String category = "";
	public String platform = "";
	public String data = "";
	public String details = "";
	
	/**
	 * Constructor for the data with a theDataset string parsable for details, algorithm, and data, as found in pipeline.
	 */
	public JsonDataset(String theProgram, String theProject, 
			String theCategory, String thePlatform, 
			String theData, String theDetails)
	{
		program = theProgram;
		project = theProject;
		category = theCategory;
		platform = thePlatform;
		data = theData;
		details = theDetails;
	}

	/**
	 * Write JSON version to file
	 * 
	 * @param theFile File to which to write the JSON
	 * @throws IOException 
	 */
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
	
	/**
	 * Conversion this into a line fragment for the index file
	 * 
	 * @return Tab delimited line.
	 */
	public String getLine()
	{
		return program + "\t" + project + "\t" + category + "\t" + 
				platform + "\t" + data + "\t" + details;
	}
	
	/**
	 * Build the unique id for the dataset based on the line.
	 * 
	 * @return MD5 sum for line.
	 */
	public String getID()
	{
		return new DigestUtils(MessageDigestAlgorithms.MD5).digestAsHex(getLine());
	}
	
	/**
	 * Build a JsonDataset object from a line of the index file
	 * 
	 * @param theLine The line to parse for data.
	 * @return  JsonDataset object created from the line.
	 */
	static public JsonDataset setLine(String theLine)
	{
		String [] splitted = theLine.split("\t", -1);
		String program = splitted[0];
		String project = splitted[1];
		String category = splitted[2];
		String platform = splitted[3];
		String data = splitted[4];
		String details = splitted[5];
		return new JsonDataset(program, project, category, platform, data, details);
	}
}
