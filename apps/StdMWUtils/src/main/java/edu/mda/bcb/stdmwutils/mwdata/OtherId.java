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
package edu.mda.bcb.stdmwutils.mwdata;

import java.util.ArrayList;

/**
 *
 * @author TDCasasent
 */
public class OtherId implements Comparable<OtherId>
{
	public String pubchem_cid = null;
	public String regno = null;
	public String sys_name = null;
	public String lm_id = null;
	public String hmdb_id = null;
	public String kegg_id = "";
	public String chebi_id = "";
	public String metacyc_id = "";
	public String smiles = "";
	public String name = "";

	public OtherId()
	{
		//
	}

	static public String getHeaderString()
	{
		return "pubchem_cid\tregno\tsys_name\tlm_id\thmdb_id\tkegg_id\tchebi_id\tmetacyc_id\tsmiles\tname";
	}

	static public OtherId getFromRowString(ArrayList<String> theHeaders, String theRowString)
	{
		OtherId otherid = new OtherId();
		String[] splitted = theRowString.split("\t", -1);
		otherid.pubchem_cid = splitted[theHeaders.indexOf("pubchem_cid")];
		otherid.regno = splitted[theHeaders.indexOf("regno")];
		otherid.sys_name = splitted[theHeaders.indexOf("sys_name")];
		otherid.lm_id = splitted[theHeaders.indexOf("lm_id")];
		otherid.hmdb_id = splitted[theHeaders.indexOf("hmdb_id")];
		otherid.kegg_id = splitted[theHeaders.indexOf("kegg_id")];
		otherid.chebi_id = splitted[theHeaders.indexOf("chebi_id")];
		otherid.metacyc_id = splitted[theHeaders.indexOf("metacyc_id")];
		otherid.smiles = splitted[theHeaders.indexOf("smiles")];
		otherid.name = splitted[theHeaders.indexOf("name")];
		return otherid;
	}

	public String getRowString()
	{
		return pubchem_cid + "\t"
				+ MWUrls.cleanNull(regno) + "\t"
				+ MWUrls.cleanNull(sys_name) + "\t"
				+ MWUrls.cleanNull(lm_id) + "\t"
				+ MWUrls.cleanNull(hmdb_id) + "\t"
				+ MWUrls.cleanNull(kegg_id) + "\t"
				+ MWUrls.cleanNull(chebi_id) + "\t"
				+ MWUrls.cleanNull(metacyc_id) + "\t"
				+ MWUrls.cleanNull(smiles) + "\t"
				+ name;
	}

	;
	
	@Override
	public int compareTo(OtherId t)
	{
		int cmp = this.pubchem_cid.compareTo(t.pubchem_cid);
		if (0 == cmp)
		{
			cmp = this.name.compareTo(t.name);
		}
		return cmp;
	}

}
