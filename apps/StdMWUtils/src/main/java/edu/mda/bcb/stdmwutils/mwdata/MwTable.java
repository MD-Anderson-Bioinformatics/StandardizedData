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

import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bcb.stdmwutils.utils.RefMetUtil;
import java.util.TreeSet;

/**
 *
 * @author TDCasasent
 */
public class MwTable
{
	public Analysis analysis = null;
	public Summary study = null;
	public int metabolite_count = -1;
	public int refmet_count = -1;
	
	public MwTable(Analysis theAnalysis, Summary theStudy)
	{
		analysis = theAnalysis;
		study = theStudy;
	}
	
	public void init(MetaboliteUtil theMU, RefMetUtil theRMU)
	{
		TreeSet<Metabolite> mblts = theMU.getMetabolitesForAnalysis(analysis.analysis_id);
		if (null!=mblts)
		{
			metabolite_count = mblts.size();
			refmet_count = 0;
			for (Metabolite mtb : mblts)
			{
				if (!"".equals(mtb.refmet_name))
				{
					if (null!=theRMU.getRefMet(mtb.refmet_name))
					{
						refmet_count += 1;
					}
				}
			}
		}
		else
		{
			metabolite_count = 0;
			refmet_count = 0;
			StdMwDownload.printLn("MwTable::init - no Metabolite list for " + analysis.analysis_id);
		}
	}
}
