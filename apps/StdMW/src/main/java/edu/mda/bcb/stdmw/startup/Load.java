// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>
package edu.mda.bcb.stdmw.startup;

import edu.mda.bcb.stdmw.utils.FIFOQueue;
import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import java.io.File;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Tod-Casasent
 */
@WebListener
public class Load implements ServletContextListener
{
	static public FIFOQueue mQueue = null;

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		StdMwDownload.setLogContext(sce.getServletContext());
		if (null==Load.mQueue)
		{
			Load.mQueue = new FIFOQueue();
		}
		//ServletContextListener.super.contextInitialized(sce);
		try
		{
			// remove ZIPs if present
			File dataDir = new File(MWUrls.M_MWB_TEMP);
			if (dataDir.exists())
			{
				File [] del = dataDir.listFiles();
				for (File rm : del)
				{
					FileUtils.deleteQuietly(rm);
				}
			}
//			// remove live dir if present
//			File liveDir = new File(MWUrls.M_MW_CACHE, "live");
//			if (liveDir.exists())
//			{
//				FileUtils.deleteQuietly(liveDir);
//			}
//			while(liveDir.exists())
//			{
//				// hack to give time for delete to occur
//				new File(MWUrls.M_MW_CACHE).list();
//			}
			// updated used Util objects
			Scheduled.updateUtilObjects(sce.getServletContext());
		}
		catch (Exception exp)
		{
			sce.getServletContext().log("Error during Load startup", exp);
		}

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		// nothing to do
	}
}
