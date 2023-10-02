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
package edu.mda.bcb.stdmw.startup;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 *
 * @author tdcasasent
 */
@WebListener
public class Scheduler implements ServletContextListener
{
	private ScheduledExecutorService mScheduler = null;

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		sce.getServletContext().log("Starting Scheduler");
		mScheduler = Executors.newSingleThreadScheduledExecutor();
		sce.getServletContext().log("Setting up scheduleAtFixedRate");
		mScheduler.scheduleAtFixedRate(new Scheduled(sce.getServletContext()), 0, 1, TimeUnit.DAYS);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		sce.getServletContext().log("Shutdown Scheduler");
		List<Runnable> running = mScheduler.shutdownNow();
		if (running.size()>0)
		{
			try
			{
				sce.getServletContext().log("awaiting termination of running threads");
				mScheduler.awaitTermination(2, TimeUnit.MINUTES);
			}
			catch(Exception exp)
			{
				sce.getServletContext().log("termination");
			}
		}
	}
}
