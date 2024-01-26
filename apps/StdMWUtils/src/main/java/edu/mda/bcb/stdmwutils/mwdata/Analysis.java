// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Tod-Casasent
 */
public class Analysis implements Comparable<Analysis>
{
	public String hash = null; // does not come from metabolomics workbench
	public String study_hash = null; // does not come from metabolomics workbench
	public String study_id = null;
	public String analysis_id = null;
	public String analysis_summary = null;
	public String analysis_type = null;
	public String instrument_name = null;
	public String instrument_type = null;
	public String ms_type = null;
	public String ion_mode = null;

	public Analysis()
	{
		// object is built from JSON
		// use init() below instead of constructor
	}
	
	static public String getHeaderString()
	{
		return "hash\tstudy_hash\tstudy_id\tanalysis_id\tanalysis_summary\tanalysis_type\tinstrument_name\tinstrument_type\tms_type\tion_mode";
	}
	
	static public Analysis getFromRowString(ArrayList<String> theHeaders, String theRowString, String thePrefix)
	{
		Analysis analysis = new Analysis();
		String [] splitted = theRowString.split("\t", -1);
		analysis.hash = splitted[theHeaders.indexOf(thePrefix + "hash")];
		analysis.study_hash = splitted[theHeaders.indexOf(thePrefix + "study_hash")];
		analysis.study_id = splitted[theHeaders.indexOf(thePrefix + "study_id")];
		analysis.analysis_id = splitted[theHeaders.indexOf(thePrefix + "analysis_id")];
		analysis.analysis_summary = splitted[theHeaders.indexOf(thePrefix + "analysis_summary")];
		analysis.analysis_type = splitted[theHeaders.indexOf(thePrefix + "analysis_type")];
		analysis.instrument_name = splitted[theHeaders.indexOf(thePrefix + "instrument_name")];
		analysis.instrument_type = splitted[theHeaders.indexOf(thePrefix + "instrument_type")];
		analysis.ms_type = splitted[theHeaders.indexOf(thePrefix + "ms_type")];
		analysis.ion_mode = splitted[theHeaders.indexOf(thePrefix + "ion_mode")];
		return analysis;
	}
	
	public String getRowString()
	{
		return MWUrls.cleanString(hash, false) + "\t" + 
				MWUrls.cleanString(study_hash, false) + "\t" + 
				study_id + "\t" + 
				analysis_id + "\t" + 
				MWUrls.cleanString(analysis_summary, false) + "\t" + 
				MWUrls.cleanString(analysis_type, false) + "\t" + 
				MWUrls.cleanString(instrument_name, false) + "\t" + 
				MWUrls.cleanString(instrument_type, false) + "\t" + 
				MWUrls.cleanString(ms_type, false) + "\t" + 
				MWUrls.cleanString(ion_mode, false);
	};
	
	public void init(String theStudyHash) throws NoSuchAlgorithmException, IOException
	{
		MessageDigest shaDigest = MessageDigest.getInstance("SHA-1");
		shaDigest.update(analysis_id.getBytes());
		hash = Hex.encodeHexString(shaDigest.digest());
		study_hash = theStudyHash;
	}
	
	@Override
	public int compareTo(Analysis t)
	{
		int cmp = this.study_id.compareTo(t.study_id);
		if (0==cmp)
		{
			cmp = this.analysis_id.compareTo(t.analysis_id);
		}
		return cmp;
	}
}
