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

package edu.mda.bcb.gdc.api.indexes;

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
	/**
	 * The "source" value for the data, generally GDC (or PanCan).
	 */
	public String source = null;
	
	/**
	 * The "variant" value, such as "current" or "legacy" for the GDC.
	 */
	public String variant = null;
	
	/**
	 * The "project" value such as PanCan, TCGA, or TARGET.
	 */
	public String project = null;
	
	/**
	 * The "sub-project" value which may be a disease type (TCGA-BRCA) or something more project specific.
	 */
	public String subProject = null;
	
	/**
	 * The "category" value, usually related to the type of data, such as 
	 * Gene Expression Quantification, Isoform Expression Quantification, 
	 * or miRNA Expression Quantification. May not match GUI values.
	 */
	public String category = null;
	
	/**
	 * The "platform" value which for the GDC is the workflow, such as
	 * Liftover, BCGSC miRNA Profiling, DNAcopy, or HTSeq - Counts. 
	 * May not match GUI values.
	 */
	public String platform = null;
	
	/**
	 * The "data" value is the first half of theDataset string: Standardized, Analyzed, AutoCorrected, or Corrected.
	 */
	public String data = null; // dataset first half: Standardized, Analyzed, AutoCorrected, or Corrected
	
	/**
	 * The "algorithm" value is the second half of theDataset string: Discrete, Continuous, or the correction algorithm.
	 */
	public String algorithm = null; // dataset second half: Discrete, Continuous, or the correction algorithm
	
	/**
	 * The "details" value is the after the underscore of theDataset string: values such as wXY or noXY for dataset variations.
	 */
	public String details = null; // underscore specialization
	
	/**
	 * The "version" value is a standardized timestamp yyyy_MM_DD_hhMM like 2020_12_22_1000.
	 */
	public String version = null;
	
	/**
	 * Constructor for the data with a theDataset string parsable for details, algorithm, and data, as found in pipeline.
	 * 
	 * @param theSource The "source" value for the data, generally GDC (or PanCan)
	 * @param theVariant The "variant" value, such as "current" or "legacy" for the GDC.
	 * @param theProject The "project" value such as PanCan, TCGA, or TARGET.
	 * @param theSubProject The "sub-project" value which may be a disease type (TCGA-BRCA) or something more project specific.
	 * @param theDataType The "category" value, usually related to the type of data, such as Gene Expression Quantification, Isoform Expression Quantification, or miRNA Expression Quantification.
	 * @param thePlatform The "platform" value which for the GDC is the workflow, such as Liftover, BCGSC miRNA Profiling, DNAcopy, or HTSeq - Counts.
	 * @param theDataset Used to generate details, algorithm, and data entries
	 * @param theVersion The "version" value is a standardized timestamp yyyy_MM_DD_hhMM like 2020_12_22_1000.
	 */
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

	/**
	 * Constructor with all entries pre-parsed, as found in file.
	 * 
	 * @param theSource The "source" value for the data, generally GDC (or PanCan)
	 * @param theVariant The "variant" value, such as "current" or "legacy" for the GDC.
	 * @param theProject The "project" value such as PanCan, TCGA, or TARGET.
	 * @param theSubProject The "sub-project" value which may be a disease type (TCGA-BRCA) or something more project specific.
	 * @param theDataType The "category" value, usually related to the type of data, such as Gene Expression Quantification, Isoform Expression Quantification, or miRNA Expression Quantification.
	 * @param thePlatform The "platform" value which for the GDC is the workflow, such as Liftover, BCGSC miRNA Profiling, DNAcopy, or HTSeq - Counts.
	 * @param theData The "data" value is the first half of theDataset string: Standardized, Analyzed, AutoCorrected, or Corrected.
	 * @param theAlgorithm The "algorithm" value is the second half of theDataset string: Discrete, Continuous, or the correction algorithm.
	 * @param theDetails The "details" value is the after the underscore of theDataset string: values such as wXY or noXY for dataset variations.
	 * @param theVersion The "version" value is a standardized timestamp yyyy_MM_DD_hhMM like 2020_12_22_1000.
	 */
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
		return source + "\t" + variant + "\t" + project + "\t" + subProject + "\t" + category + "\t" + 
				platform + "\t" + data + "\t" + algorithm + "\t" + details + "\t" + version;
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
