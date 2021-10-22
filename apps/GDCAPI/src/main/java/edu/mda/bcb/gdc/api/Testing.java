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
package edu.mda.bcb.gdc.api;

import edu.mda.bcb.gdc.api.data.WorkflowData;
import edu.mda.bcb.gdc.api.endpoints.Manifests;
import edu.mda.bcb.gdc.api.endpoints.Workflows;
import edu.mda.bcb.gdc.api.portal.GDCcurrent;
import java.io.File;

/**
 *
 * @author TDCasasent
 */
public class Testing
{

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		try
		{
			File theBaseDir = new File("/code/development/redacted");
			String theProgram = "TCGA";
			String theProject = "TCGA-GBM";
			String theJobId = "314";
			GDCcurrent gc = new GDCcurrent(theBaseDir);
			Workflows wf = new Workflows(gc);
			wf.testOutput(theProgram, theProject);
		}
		catch(Exception exp)
		{
			exp.printStackTrace(System.err);
		}
	}
	
}
