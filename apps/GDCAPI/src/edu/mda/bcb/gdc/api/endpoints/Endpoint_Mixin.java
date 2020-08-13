// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.gdc.api.endpoints;

import edu.mda.bcb.gdc.api.DownloadUrl;
import java.io.File;
import java.util.Map;

/**
 *
 * @author Tod-Casasent
 */
abstract public class Endpoint_Mixin
{
	public boolean mIsLegacy = false;

	public Endpoint_Mixin(boolean theIsLegacy)
	{
		mIsLegacy = theIsLegacy;
	}

	public void processEndpoint() throws Exception
	{
		//GDCAPI.printLn("Sleep quarter second before call");
		try
		{
			Thread.sleep(1000 / 4);
		}
		catch (Exception exp)
		{
			// ignore
		}
		DownloadUrl du = new DownloadUrl(getURL(),
				getDestFile(),
				getMD5(),
				getRetries(),
				getBaseTimeout(),
				getRequestMethod(),
				getFollowRedirectsFlag(),
				getContentType(),
				getParameters(),
				getRequestProperties());
		processJson(du.download());
	}
	
	protected String getLegacyURL()
	{
		String url = "/legacy";
		if (false==mIsLegacy)
		{
			url = "";
		}
		return url;
	}

	abstract protected void processJson(String theJSON) throws Exception;
	
	final protected String getURL()
	{
		return getURLbase() + getLegacyURL() + getURLendpoint();
	}
	
	protected String getURLbase()
	{
		return "https://api.gdc.cancer.gov";
	}
	
	abstract protected String getURLendpoint();

	protected File getDestFile() throws Exception
	{
		return null;
	}

	protected String getMD5()
	{
		return null;
	}

	protected int getRetries()
	{
		return 5;
	}

	protected int getBaseTimeout()
	{
		return 60000;
	}

	protected String getRequestMethod()
	{
		return "POST";
	}

	protected boolean getFollowRedirectsFlag()
	{
		return false;
	}

	protected String getContentType()
	{
		return "application/json";
	}

	abstract protected String getParameters()throws Exception;

	protected Map<String, String> getRequestProperties()
	{
		return null;
	}
}
