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

package edu.mda.bioinfo.stdmwutils.mwdata;

import edu.mda.bioinfo.stdmwutils.StdMwDownload;
import edu.mda.bioinfo.stdmwutils.StdMwException;
import edu.mda.bioinfo.stdmwutils.utils.FactorUtil;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.TreeSet;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Tod-Casasent
 */
public class Summary implements Comparable<Summary>
{
	public String hash = null; // does not come from metabolomics workbench
	public String study_id = null;
	public String study_title = null;
	public String study_type = null;
	public String institute = null;
	public String department = null;
	public String last_name = null;
	public String first_name = null;
	public String email = null;
	public String submit_date = null;
	public String study_summary = null;
	public String subject_species = null;
	public String phone = null;
	public TreeSet<String> factors = null;
	public int sample_count = -1;

	public Summary()
	{
		//
	}
	
	static public String getHeaderString()
	{
		return "hash\tstudy_id\tstudy_title\tstudy_type\tinstitute\tdepartment\tlast_name\tfirst_name\temail\tsubmit_date\tstudy_summary\tsubject_species\tphone\tfactors\tsample_count";
	};
	
	static public Summary getFromRowString(ArrayList<String> theHeaders, String theRowString)
	{
		Summary summary = new Summary();
		String [] splitted = theRowString.split("\t", -1);
		summary.hash = splitted[theHeaders.indexOf("hash")];
		summary.study_id = splitted[theHeaders.indexOf("study_id")];
		summary.study_title = splitted[theHeaders.indexOf("study_title")];
		summary.study_type = splitted[theHeaders.indexOf("study_type")];
		summary.institute = splitted[theHeaders.indexOf("institute")];
		summary.department = splitted[theHeaders.indexOf("department")];
		summary.last_name = splitted[theHeaders.indexOf("last_name")];
		summary.first_name = splitted[theHeaders.indexOf("first_name")];
		summary.email = splitted[theHeaders.indexOf("email")];
		summary.submit_date = splitted[theHeaders.indexOf("submit_date")];
		summary.study_summary = splitted[theHeaders.indexOf("study_summary")];
		summary.subject_species = splitted[theHeaders.indexOf("subject_species")];
		summary.phone = splitted[theHeaders.indexOf("phone")];
		summary.factors = MWUrls.stringToSet(splitted[theHeaders.indexOf("factors")], "\\|");
		summary.sample_count = Integer.parseInt(splitted[theHeaders.indexOf("sample_count")]);
		return summary;
	}
	
	public String getRowString()
	{
		return MWUrls.cleanString(hash, false) + "\t" + 
				study_id + "\t" + 
				MWUrls.cleanString(study_title, false) + "\t" + 
				MWUrls.cleanString(study_type, false) + "\t" + 
				MWUrls.cleanString(institute, false) + "\t" + 
				MWUrls.cleanString(department, false) + "\t" + 
				MWUrls.cleanString(last_name, false) + "\t" + 
				MWUrls.cleanString(first_name, false) + "\t" + 
				MWUrls.cleanString(email, false) + "\t" + 
				MWUrls.cleanString(submit_date, false) + "\t" + 
				MWUrls.cleanString(study_summary, true) + "\t" + 
				MWUrls.cleanString(subject_species, false) + "\t" + 
				MWUrls.cleanString(phone, false) + "\t" + 
				MWUrls.setToString(factors, "|") + "\t" + 
				sample_count;
	};
	
	public void init() throws NoSuchAlgorithmException, IOException, StdMwException
	{
		StdMwDownload.printLn("Summary::init study_id=" + study_id);
		MessageDigest shaDigest = MessageDigest.getInstance("SHA-1");
		shaDigest.update(study_id.getBytes());
		hash = Hex.encodeHexString(shaDigest.digest());
		//
		StdMwDownload.printLn("Analysis::init get factors");
		factors = new TreeSet<>();
		TreeSet<String> factorsList = new TreeSet<>();
		int sampleCount = FactorUtil.getFactorNameListForStudy(study_id, factorsList);
		sample_count = sampleCount;
		factors.addAll(factorsList);
	}
	
	@Override
	public int compareTo(Summary t)
	{
		return this.study_id.compareTo(t.study_id);
	}
}
