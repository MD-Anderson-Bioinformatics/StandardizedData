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
package edu.mda.bcb.stdmwutils.mwdata;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class MWUrls
{

	static public String M_VERSION = "StdMW BEA_VERSION_TIMESTAMP";
	static public String M_MW_CACHE = "/SMW/MW_CACHE/";
	static public String M_MW_PIPELINE = "/SMW/MW_API/";
	static public String M_MW_ZIPTMP = "/SMW/MW_ZIP/";
	static public String M_STUDIES = "studies.tsv";
	static public String M_ANALYSIS = "analysis.tsv";
	static public String M_METABOLITES = "metabolites.tsv";
	static public String M_REFMET = "refmet.tsv";
	static public String M_OTHERIDS = "otherids.tsv";

	static public String M_SUMMARY_LIST = "https://www.metabolomicsworkbench.org/rest/study/study_id/ST/summary";
	//This no longer returns JSON
	//static public String M_REFMET_LIST = "https://www.metabolomicsworkbench.org/rest/refmet/all/json";

	// get inchi key from cid https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/cid/9547410/property/InChIKey
	// get cid and inchi key from name https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/9R,10S-EpOME/property/InChIKey
	
	static public String getRefMet(String theRefMetName)
	{
		return "https://www.metabolomicsworkbench.org/rest/refmet/name/" + theRefMetName + "/all";
	}
	
	static public String getAnalyses(String theStudyId)
	{
		return "https://www.metabolomicsworkbench.org/rest/study/study_id/" + theStudyId + "/analysis";
	}

	static public String getDatatable(String theAnalysisId)
	{
		return "https://www.metabolomicsworkbench.org/rest/study/analysis_id/" + theAnalysisId + "/datatable/txt";
	}

	static public String getFactors(String theStudyId)
	{
		return "https://www.metabolomicsworkbench.org/rest/study/study_id/" + theStudyId + "/factors";
	}

	static public String getMetabolites(String theAnalysisId)
	{
		return "https://www.metabolomicsworkbench.org/rest/study/analysis_id/" + theAnalysisId + "/metabolites";
	}

	static public String getOtherIDs(String pubchemId)
	{
		return "https://www.metabolomicsworkbench.org/rest/compound/pubchem_cid/" + pubchemId + "/all";
	}

	static public File findNewestDir(File theDir)
	{
		File newest = null;
		TreeSet<File> fileList = new TreeSet<>();
		File[] tmp = theDir.listFiles();
		if (null != tmp)
		{
			for (File nf : tmp)
			{
				if (nf.isDirectory())
				{
					fileList.add(nf);
				}
			}
			newest = fileList.descendingSet().first();
		}
		return newest;
	}

	static public String cleanNull(String theString)
	{
		if (null == theString)
		{
			theString = "";
		}
		return theString;
	}

	static public String restoreNull(String theString)
	{
		if ("".equals(theString))
		{
			theString = null;
		}
		return theString;
	}

	static public String cleanString(String theString, boolean theCompleteFlag)
	{
		String result = "NA";
		if (null != theString)
		{
			if (true == theCompleteFlag)
			{
				result = theString.replaceAll("[^a-zA-Z0-9]", " ");
			}
			else
			{
				result = theString.replace("\r", " ").replace("\n", " ");
			}
			while (result.contains("  "))
			{
				result = result.replace("  ", " ");
			}
		}
		return result;
	}

	static public TreeSet<String> stringToSet(String theString, String theDelimiter)
	{
		String[] splitted = theString.split(theDelimiter, -1);
		return new TreeSet<String>(Arrays.asList(splitted));
	}

	static public String setToString(Set<String> theArray, String theDelimiter)
	{
		String result = null;
		if (null != theArray)
		{
			for (String element : theArray)
			{
				if (null == result)
				{
					result = element;
				}
				else
				{
					result = result + theDelimiter + element;
				}
			}
		}
		else
		{
			result = "";
		}
		return result;
	}
}
