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
package edu.mda.bcb.stdmwutils;

import static edu.mda.bcb.stdmwutils.StdMwDownload.getVersion;
import java.io.File;

/**
 *
 * @author TDCasasent
 */
public class TestDownload
{

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		try
		{
			StdMwDownload.printLn("StdMwDownload Starting");
			StdMwDownload.printLn(getVersion());
			File baseDir = new File("");
			//String analysisId = "AN000025";
			String analysisId = "AN000001";
			String jobId = "NANANANANA";
			MWStack.downloadDataset(baseDir, analysisId, jobId);
			StdMwDownload.printLn("StdMwDownload Complete");
		}
		catch (Exception exp)
		{
			StdMwDownload.printErr("Error in main", exp);
		}
	}
	
}
