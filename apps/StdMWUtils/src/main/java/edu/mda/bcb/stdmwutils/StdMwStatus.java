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

import edu.mda.bcb.stdmwutils.indexes.DataIndex;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.std.MWAPI;
import edu.mda.bcb.stdmwutils.utils.AnalysisUtil;
import edu.mda.bcb.stdmwutils.utils.SummaryUtil;
import edu.mda.bcb.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bcb.stdmwutils.utils.RefMetUtil;
import edu.mda.bcb.stdmwutils.utils.OtherIdsUtil;
import edu.mda.bcb.stdmwutils.validate.ValidateUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import jakarta.servlet.ServletContext;

/**
 *
 * @author TDCasasent
 */
public class StdMwStatus
{	
	public static void main(String[] args)
	{
		// uses directories specified in MWUrls
		String myDataVersion = "2023-06-07";
		try
		{
			RefMetUtil.updateRefMetUtil(myDataVersion, false);
			StdMwDownload.printLn("StdMwStatus Starting");
			StdMwDownload.printLn(StdMwDownload.getVersion());
			StdMwDownload.printLn("download current datasets");
			SummaryUtil su = SummaryUtil.updateSummaryUtil(myDataVersion, false);
			AnalysisUtil au = AnalysisUtil.updateAnalysisUtil(myDataVersion, su, false);
			MetaboliteUtil mu = MetaboliteUtil.updateMetaboliteUtil(myDataVersion, au, false);
			RefMetUtil ru = RefMetUtil.updateRefMetUtil(myDataVersion, false);
			OtherIdsUtil ou = OtherIdsUtil.updateOtherIdsUtil(myDataVersion, ru, mu, false);
			MWAPI sp = new MWAPI(su, au, mu, ru, ou, new File(""));
			sp.listProcessable(myDataVersion);
			StdMwDownload.printLn("StdMwStatus Complete");
		}
		catch (Exception exp)
		{
			StdMwDownload.printErr("Error in main", exp);
		}
	}

}
