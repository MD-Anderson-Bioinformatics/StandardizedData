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

import java.util.ArrayList;

/**
 *
 * @author Tod-Casasent
 */
public class Metabolite implements Comparable<Metabolite>
{
	public String study_id = null;
	public String analysis_id = null;
	public String analysis_summary = null;
	public String metabolite_name = null;
	public String refmet_name = "";
	public String pubchem_id = "";
	public String other_id = "";
	public String other_id_type = "";

	public Metabolite()
	{
		//
	}
	
	public void init()
	{
		// needed since at least one refmet name has erroneous space at front
		refmet_name = refmet_name.trim();
	}
	
	static public String getHeaderString()
	{
		return "study_id\tanalysis_id\tanalysis_summary\tmetabolite_name\trefmet_name\tpubchem_id\tother_id\tother_id_type";
	}
	
	static public Metabolite getFromRowString(ArrayList<String> theHeaders, String theRowString)
	{
		Metabolite metabolite = new Metabolite();
		String [] splitted = theRowString.split("\t", -1);
		metabolite.study_id = splitted[theHeaders.indexOf("study_id")];
		metabolite.analysis_id = splitted[theHeaders.indexOf("analysis_id")];
		metabolite.analysis_summary = splitted[theHeaders.indexOf("analysis_summary")];
		metabolite.metabolite_name = splitted[theHeaders.indexOf("metabolite_name")];
		metabolite.refmet_name = splitted[theHeaders.indexOf("refmet_name")];
		metabolite.pubchem_id = splitted[theHeaders.indexOf("pubchem_id")];
		metabolite.other_id = splitted[theHeaders.indexOf("other_id")];
		metabolite.other_id_type = splitted[theHeaders.indexOf("other_id_type")];
		return metabolite;
	}
	
	public String getRowString()
	{
		return study_id + "\t" + 
				analysis_id + "\t" + 
				analysis_summary + "\t" + 
				MWUrls.cleanNull(metabolite_name) + "\t" + 
				MWUrls.cleanNull(refmet_name) + "\t" + 
				MWUrls.cleanNull(pubchem_id) + "\t" + 
				MWUrls.cleanNull(other_id) + "\t" + 
				MWUrls.cleanNull(other_id_type);
	};
	
	@Override
	public int compareTo(Metabolite t)
	{
		int cmp = this.study_id.compareTo(t.study_id);
		if (0==cmp)
		{
			cmp = this.analysis_id.compareTo(t.analysis_id);
			if (0==cmp)
			{
				cmp = this.metabolite_name.compareTo(t.metabolite_name);
				if (0==cmp)
				{
					cmp = this.refmet_name.compareTo(t.refmet_name);
					if (0==cmp)
					{
						cmp = this.other_id.compareTo(t.other_id);
					}
				}
			}
		}
		return cmp;
	}
}
