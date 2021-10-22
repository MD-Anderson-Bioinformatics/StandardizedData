// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
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

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.data.Fileable;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import java.io.File;

/**
 *
 * @author Tod-Casasent
 */
public class DataDownload extends Endpoint_Mixin
{
	protected Fileable mFileable = null;
	protected GDCFile mGDCFile = null;
	protected File mDownloadDir = null;
	
	public DataDownload(boolean theIsLegacyFlag, Fileable theFileable, File theDownloadDir)
	{
		super(theIsLegacyFlag);
		mFileable = theFileable;
		mDownloadDir = theDownloadDir;
		// filled in by other process before called
		mGDCFile = null;
	}
	
	@Override
	public void processEndpoint() throws Exception
	{
		for (Updateable_Mixin<GDCFile> myFile : mFileable.mFiles.values())
		{
			if (((GDCFile)myFile).needsDownloadP(mDownloadDir))
			{
				// this used in call to getParameters
				mGDCFile = ((GDCFile)myFile);
				super.processEndpoint(); 
			}
		}
	}
	
	@Override
	protected void processJson(String theJSON) throws Exception
	{
		// check results, not JSON
		if (null==theJSON)
		{
			GDCAPI.printWarn("Unable to download " + mGDCFile + " to " + mDownloadDir + ". May be redacted.");
		}
	}

	@Override
	protected String getURLendpoint()
	{
		return "/data/" + mGDCFile.mUUID;
	}

	@Override
	protected String getParameters()
	{
		return null;
	}

	@Override
	protected String getMD5()
	{
		return mGDCFile.mMD5Sum;
	}

	@Override
	protected File getDestFile() throws Exception
	{
		return mGDCFile.getFileObj(mDownloadDir, false);
	}
	
	@Override
	protected String getRequestMethod()
	{
		return "GET";
	}

}
