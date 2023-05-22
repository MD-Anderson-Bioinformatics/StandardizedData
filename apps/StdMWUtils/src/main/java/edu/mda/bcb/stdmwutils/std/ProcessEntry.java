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
package edu.mda.bcb.stdmwutils.std;

import edu.mda.bcb.stdmwutils.indexes.JsonDataset;
import edu.mda.bcb.stdmwutils.mwdata.Analysis;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.mwdata.Summary;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Tod-Casasent
 */
public class ProcessEntry implements Comparable<ProcessEntry>
{
	public String mHash = null;
	public Analysis mAn = null;
	public Summary mSu = null;
	public String mTimestamp = null;
	public String mStatus = null;
	
	public String getZipName()
	{
		return "MWB_" + mAn.study_id + "_" + mAn.analysis_id + ".zip";
	}
	
	public ProcessEntry(Analysis theAn, Summary theSu, String theTimestamp, String theStatus, String theHash) throws NoSuchAlgorithmException
	{
		mAn = theAn;
		mSu = theSu;
		mTimestamp = theTimestamp;
		mStatus = theStatus;
		if (null==theHash)
		{
			MessageDigest shaDigest = MessageDigest.getInstance("SHA-1");
			shaDigest.update((theAn.analysis_id + "--" + theTimestamp).getBytes());
			mHash = Hex.encodeHexString(shaDigest.digest());
		}
		else
		{
			mHash = theHash;
		}
	}
	
	static public String addPrefixToHeaderString(String thePrefix, String theHeaders)
	{
		String [] split = theHeaders.split("\t", -1);
		String preString = "";
		for (String token : split)
		{
			preString = preString + "\t" + thePrefix + token;
		}
		return preString;
	}
	
	static public String getHeaderString()
	{
		return "hash\ttimestamp\tstatus" + 
				addPrefixToHeaderString("Analysis.", Analysis.getHeaderString()) +
				addPrefixToHeaderString("Summary.", Summary.getHeaderString());
	}
	
	static public ProcessEntry getFromRowString(ArrayList<String> theHeaders, String theRowString) throws NoSuchAlgorithmException
	{
		String [] splitted = theRowString.split("\t", -1);
		String hash = splitted[theHeaders.indexOf("hash")];
		String timestamp = splitted[theHeaders.indexOf("timestamp")];
		String status = splitted[theHeaders.indexOf("status")];
		Analysis an = Analysis.getFromRowString(theHeaders, theRowString, "Analysis.");
		Summary su = Summary.getFromRowString(theHeaders, theRowString, "Summary.");
		ProcessEntry processEntry = new ProcessEntry(an, su, timestamp, status, hash);
		return processEntry;
	}

	public String getRowString()
	{
		return MWUrls.cleanString(mHash, false) + "\t" + 
				MWUrls.cleanString(mTimestamp, false) + "\t" + 
				MWUrls.cleanString(mStatus, false) + "\t" + 
				mAn.getRowString() + "\t" +
				mSu.getRowString();
	};
	
	@Override
	public int compareTo(ProcessEntry t)
	{
		int comp = this.mAn.compareTo(t.mAn);
		if (0==comp)
		{
			comp = this.mSu.compareTo(t.mSu);
			if (0==comp)
			{
				comp = this.mTimestamp.compareTo(t.mTimestamp);
			}
		}
		return comp;
	}

	public JsonDataset getJsonDataset(boolean theContinuousFlag)
	{
		// String theSource, String theVariant, 
		// String theProject, String theSubProject, String theDataType, 
		// String thePlatform, String theData, String theAlgorithm, String theDetails, String theVersion
		JsonDataset jd = new JsonDataset(
				this.mAn.study_id,			// program
				this.mAn.analysis_id,		// project
				this.mAn.instrument_type,	// category
				this.mAn.ion_mode,			// platform
				this.mSu.study_title,		// data
				"Metabolomics-Workbench"    // details
			);
		return jd;
	}
}
