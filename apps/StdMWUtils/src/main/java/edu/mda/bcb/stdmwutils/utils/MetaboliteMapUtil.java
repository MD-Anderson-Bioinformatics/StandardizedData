/*
 *  Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
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
package edu.mda.bcb.stdmwutils.utils;

import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.mwdata.Metabolite;
import edu.mda.bcb.stdmwutils.mwdata.RefMet;
import edu.mda.bcb.stdmwutils.mwdata.OtherId;
import java.io.IOException;
import java.io.OutputStream;
import java.util.TreeSet;

/**
 *
 * @author TDCasasent
 */
public class MetaboliteMapUtil
{

	public MetaboliteUtil mMetabolite = null;
	public RefMetUtil mRefMet = null;
	public OtherIdsUtil mOtherIds = null;

	public MetaboliteMapUtil(MetaboliteUtil theMetabolite, RefMetUtil theRefMet, OtherIdsUtil theOtherIds)
	{
		mMetabolite = theMetabolite;
		mRefMet = theRefMet;
		mOtherIds = theOtherIds;
	}

	public void streamTsv(OutputStream theOut, String theAnalysisId) throws IOException
	{
		TreeSet<Metabolite> mblts = mMetabolite.getMetabolitesForAnalysis(theAnalysisId);
		// handle no metablites
		if (null!=mblts)
		{
			// header
			theOut.write("mtblt.metabolite_name".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("mtblt.other_id".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("mtblt.other_id_type".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("mtblt.pubchem_id".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("refmet.refmet_name".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("refmet.pubchem_cid".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("refmet.inchi_key".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("refmet.exactmass".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("refmet.formula".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("refmet.super_class".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("refmet.main_class".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("refmet.sub_class".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("oi.regno".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("oi.sys_name".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("oi.lm_id".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("oi.hmdb_id".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("oi.kegg_id".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("oi.chebi_id".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("oi.metacyc_id".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("oi.smiles".getBytes());
			theOut.write("\t".getBytes());
			theOut.write("oi.name".getBytes());
			theOut.write("\n".getBytes());
			//
			for (Metabolite mtb : mblts)
			{
				// write metabolite data
				theOut.write(mtb.metabolite_name.getBytes());
				theOut.write("\t".getBytes());
				theOut.write(mtb.other_id.getBytes());
				theOut.write("\t".getBytes());
				theOut.write(mtb.other_id_type.getBytes());
				theOut.write("\t".getBytes());
				theOut.write(mtb.pubchem_id.getBytes());
				theOut.write("\t".getBytes());
				// mtb.refmet_name may be empty, or RefMet may not exist
				// in either case, rm will be null
				RefMet rm = mRefMet.getRefMet(mtb.refmet_name);
				if (null==rm)
				{
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
					theOut.write("\t".getBytes());
					theOut.write("".getBytes());
				}
				else
				{
					theOut.write(rm.name.getBytes());
					theOut.write("\t".getBytes());
					theOut.write(rm.pubchem_cid.getBytes());
					theOut.write("\t".getBytes());
					theOut.write(rm.inchi_key.getBytes());
					theOut.write("\t".getBytes());
					theOut.write(rm.exactmass.getBytes());
					theOut.write("\t".getBytes());
					theOut.write(rm.formula.getBytes());
					theOut.write("\t".getBytes());
					theOut.write(rm.super_class.getBytes());
					theOut.write("\t".getBytes());
					theOut.write(rm.main_class.getBytes());
					theOut.write("\t".getBytes());
					theOut.write(rm.sub_class.getBytes());
					// rm.pubchem_cid may be "NA"
					// in which case, otherid will be null
					OtherId otherid = mOtherIds.get(rm.pubchem_cid, rm.name);
					if (null==otherid)
					{
						theOut.write("\t".getBytes());
						theOut.write("".getBytes());
						theOut.write("\t".getBytes());
						theOut.write("".getBytes());
						theOut.write("\t".getBytes());
						theOut.write("".getBytes());
						theOut.write("\t".getBytes());
						theOut.write("".getBytes());
						theOut.write("\t".getBytes());
						theOut.write("".getBytes());
						theOut.write("\t".getBytes());
						theOut.write("".getBytes());
						theOut.write("\t".getBytes());
						theOut.write("".getBytes());
						theOut.write("\t".getBytes());
						theOut.write("".getBytes());
						theOut.write("\t".getBytes());
						theOut.write("".getBytes());
					}
					else
					{
						theOut.write("\t".getBytes());
						theOut.write(MWUrls.cleanNull(otherid.regno).getBytes());
						theOut.write("\t".getBytes());
						theOut.write(MWUrls.cleanNull(otherid.sys_name).getBytes());
						theOut.write("\t".getBytes());
						theOut.write(MWUrls.cleanNull(otherid.lm_id).getBytes());
						theOut.write("\t".getBytes());
						theOut.write(MWUrls.cleanNull(otherid.hmdb_id).getBytes());
						theOut.write("\t".getBytes());
						theOut.write(MWUrls.cleanNull(otherid.kegg_id).getBytes());
						theOut.write("\t".getBytes());
						theOut.write(MWUrls.cleanNull(otherid.chebi_id).getBytes());
						theOut.write("\t".getBytes());
						theOut.write(MWUrls.cleanNull(otherid.metacyc_id).getBytes());
						theOut.write("\t".getBytes());
						theOut.write(MWUrls.cleanNull(otherid.smiles).getBytes());
						theOut.write("\t".getBytes());
						theOut.write(MWUrls.cleanNull(otherid.name).getBytes());
					}
					theOut.write("\n".getBytes());
				}
			}
		}
	}
}
