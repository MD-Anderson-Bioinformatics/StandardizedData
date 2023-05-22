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

package edu.mda.bcb.stdmwutils.mwdata;

import edu.mda.bcb.stdmwutils.StdMwDownload;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Tod-Casasent
 * 
 {
"Row1":
{
	"study_id":"ST001142",
	"local_sample_id":"MUTZ3_HAEMATOPOIETIC_AND_LYMPHOID_TISSUE",
	"subject_type":"Cultured cells",
	"factors":"
		Classifications:AML | 
		Cell Culture Media:AMEM + 20% FBS | 
		LINE:Batch8 | 
		Gender:male | 
		Doubling time (hr):NA | 
		C4-POS RAW FILE:0015_CCLE_Batch8_LIP-MUTZ3.wiff | 
		HILIC-POS RAW FILE:0015_CCLE_Batch_8_HIL_MUTZ3.wiff | 
		HILIC-NEG RAW FILE:0015_CCLE_Batch_8_CMH_MUTZ3.wiff"
},
 */
public class Factor implements Comparable<Factor>
{
	public String hash = null; // does not come from metabolomics workbench
	public String study_id = null;
	public String local_sample_id = null;
	public String subject_type = null;
	public String factors = null;
	public HashMap<String, String> factorMap = null;

	public Factor()
	{
		//
	}
	
	public void init() throws NoSuchAlgorithmException
	{
		MessageDigest shaDigest = MessageDigest.getInstance("SHA-1");
		shaDigest.update(local_sample_id.getBytes());
		hash = Hex.encodeHexString(shaDigest.digest());
		//
		factorMap = new HashMap<>();
		//StdMwDownload.printLn("Factor init - factors=" + factors);
		String [] splitted = factors.split(" \\| ", -1);
		for (String pair : splitted)
		{
			pair = pair.trim();
			//StdMwDownload.printLn("Factor init - pair=" + pair);
			String [] nv = pair.split("\\:", -1);
			if (2==nv.length)
			{
				String name = nv[0].trim();
				String value = nv[1].trim();
				factorMap.put(name, value);
			}
		}
	}
	
	@Override
	public int compareTo(Factor t)
	{
		int cmp = this.study_id.compareTo(t.study_id);
		if (0==cmp)
		{
			cmp = this.local_sample_id.compareTo(t.local_sample_id);
		}
		return cmp;
	}
}
