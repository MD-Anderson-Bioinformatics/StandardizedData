/*
 *  Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
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
package edu.mda.bcb.stdmwutils.validate;

import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.mwdata.Metabolite;
import edu.mda.bcb.stdmwutils.mwdata.RefMet;
import edu.mda.bcb.stdmwutils.utils.MetaboliteMapUtil;
import edu.mda.bcb.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bcb.stdmwutils.utils.OtherIdsUtil;
import edu.mda.bcb.stdmwutils.utils.RefMetUtil;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 *
 * @author TDCasasent
 * 
 * 
Metabolites.tsv columns study_id, analysis_id, metabolite_name, refmet_name, and other_id make a key group, dependent on capitalization.

study_id              analysis_id          metabolite_name           refmet_name    pubchem_id
ST000286             AN000454            CREATININE       Creatinine           588
ST000286             AN000454            Creatinine           Creatinine           588
Pubchem_id name is often blank here, even through the refmet name matches.

Metabolites.tsv refmet_name is a foreign key to RefMet.tsv refmet_name.

RefMet.tsv column refmet_name is a key based on capitalization.
refmet_name    pubchem_cid    inchi_key             exactmass           formula super_class        main_class          sub_class
3-methyl-2-oxovaleric acid           47           JVQYSWDUAOAHFM-UHFFFAOYSA-N     130.062995          C6H10O3                Organic acids      Keto acids            Short-chain keto acids
3-Methyl-2-oxovaleric acid           440877  JVQYSWDUAOAHFM-SCSAIBSYSA-N        130.063                 C6H10O3                Organic acids      Keto acids            Short-chain keto acids

Another RefMet excerpt to use with OtherId.
refmet_name    pubchem_cid
Methanol            887
Myo-inositol hexakisphosphate 890
Myo-inositol      892

RefMet.tsv refmet_name and pubchem_cid are a foreign key group to OtherId.tsv name and pubchem_cid.
Metabolites.tsv refmet_name and pubchem_id are also a foreign key group to OtherId.tsv name and pubchem_cid.

OtherId.tsv columns pubchem_cid and name make a key group.
pubchem_cid    name    regno
890         Phytate 126090
890         Myo-inositol hexakisphosphate 38348

It looks like OtherId.tsv is not complete, since this RefMet entry is missing from it. It might be a cut and paste bug or something.
https://www.metabolomicsworkbench.org/rest/compound/pubchem_cid/442460/all/txt
refmet_name    pubchem_cid
(1S,2R,4S)-Bornyl acetate             442460
 */
public class ValidateUtil
{
	MetaboliteUtil mMU = null;
	RefMetUtil mRU = null;
	OtherIdsUtil mOU = null;
	
	public ValidateUtil(MetaboliteUtil theMU, RefMetUtil theRU, OtherIdsUtil theOU)
	{
		mMU = theMU;
		mRU = theRU;
		mOU = theOU;
	}
	
	// keys are checked when downloading and reading
	public boolean validate(String theTimestamp, ArrayList<String> theAnalysisIds) throws IOException
	{
		boolean valid = true;
		if (false==validateMetabolitesToPubChem())
		{
			valid = false;
		}
		if (false==validateMetabolitesToRefMet())
		{
			valid = false;
		}
		if (false==validateMetaboliteMapUtil(theTimestamp, theAnalysisIds))
		{
			valid = false;
		}
		return valid;
	}
	
	private boolean validateMetabolitesToRefMet()
	{
		boolean valid = true;
		// Metabolites.tsv refmet_name is a foreign key to RefMet.tsv refmet_name.
		// RefMet list is not complete, so this check is not valid
		long found = 0;
		long notfd = 0;
		for (Metabolite mtblt : mMU.getAll())
		{
			if (!"".equals(mtblt.refmet_name))
			{
				if (null==mRU.getRefMet(mtblt.refmet_name))
				{
					notfd += 1;
				}
				else
				{
					found += 1;
				}
			}
		}
		StdMwDownload.printLn("Metabolites to RefMet Found=" + found + " and Not Found=" + notfd);
		return valid;
	}
	
	private boolean validateMetabolitesToPubChem()
	{
		boolean valid = true;
		// Metabolites.tsv refmet_name is a foreign key to RefMet.tsv refmet_name.
		// RefMet list is not complete, so this check is not valid
		long found = 0;
		long notfd = 0;
		for (Metabolite mtblt : mMU.getAll())
		{
			if (!"".equals(mtblt.pubchem_id))
			{
				found += 1;
			}
			else
			{
				RefMet rm = mRU.getRefMet(mtblt.refmet_name);
				if (null==rm)
				{
					notfd += 1;
				}
				else if (!"".equals(rm.pubchem_cid))
				{
					found += 1;
				}
				else
				{
					notfd += 1;
				}
			}
		}
		StdMwDownload.printLn("Metabolites to PubChem Found=" + found + " and Not Found=" + notfd);
		return valid;
	}
	
	private boolean validateMetaboliteMapUtil(String theTimestamp, ArrayList<String> theAnalysisIds) throws IOException
	{
		boolean valid = true;
		// RefMet.tsv refmet_name and pubchem_cid are a foreign key group to OtherId.tsv name and pubchem_cid.
		// Metabolites.tsv refmet_name and pubchem_id are also a foreign key group to OtherId.tsv name and pubchem_cid.
		// but not all refmet_names are in the refmet file
		MetaboliteMapUtil mmu = new MetaboliteMapUtil(mMU, mRU, mOU);
		File outDir = new File(MWUrls.M_MW_CACHE, theTimestamp);
		File output = new File(outDir, "validate_map.tsv");
		OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
		try(OutputStream bw = java.nio.file.Files.newOutputStream(output.toPath(), options))
		{
			for (String id : theAnalysisIds)
			{
				StdMwDownload.printLn("MetaboliteMapUtil analysis id '" + id + "'");
				mmu.streamTsv(bw, id);
			}
		}
		output.delete();
		return valid;
	}
}
