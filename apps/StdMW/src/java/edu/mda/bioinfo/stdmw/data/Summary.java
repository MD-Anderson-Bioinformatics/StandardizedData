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

package edu.mda.bioinfo.stdmw.data;

import edu.mda.bioinfo.stdmw.utils.FactorUtil;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TreeSet;
import javax.servlet.ServletContext;
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

	public Summary()
	{
		//
	}
	
	public void init(ServletContext theSC) throws NoSuchAlgorithmException, IOException
	{
		theSC.log("Summary::init study_id=" + study_id);
		MessageDigest shaDigest = MessageDigest.getInstance("SHA-1");
		shaDigest.update(study_id.getBytes());
		hash = Hex.encodeHexString(shaDigest.digest());
		//
		theSC.log("Summary::init get factors");
		factors = new TreeSet<>();
		TreeSet<String> factorsList = FactorUtil.getFactorNameListForStudy(theSC, study_id);
		factors.addAll(factorsList);
	}
	
	@Override
	public int compareTo(Summary t)
	{
		return this.study_id.compareTo(t.study_id);
	}
}
