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

package edu.mda.bcb.gdc.api;

/**
 *
 * @author Tod-Casasent
 */
abstract public class DownloadJsonToString
{

	public static String downloadJsonToString(String theURL, String theParameters) throws Exception
	{
		//String theUrl, File theDestFile, String theMD5,
		//int theRetries, int theBaseTimeout, String theRequestMethod, 
		//boolean theFollowRedirectsFlag, String theContentType, 
		//String theParameters, Map<String, String> theRequestProperties
		//GDCManifestDownload.printLn(theURL);
		DownloadUrl du = new DownloadUrl(theURL, null, null,
				3, 60000, "POST", 
				false, "application/json", 
				theParameters, null);
		String result = du.download();
		return result;
	}
	
	public synchronized static String downloadTextToStringWithSyncWait(String theURL) throws Exception
	{
		try
		{
			Thread.sleep(3000);
		}
		catch(Exception ignore)
		{
			// ignore - sleep to prevent rapid hits
		}
		return downloadTextToString(theURL);
	}
	
	public static String downloadTextToString(String theURL) throws Exception
	{
		//String theUrl, File theDestFile, String theMD5,
		//int theRetries, int theBaseTimeout, String theRequestMethod, 
		//boolean theFollowRedirectsFlag, String theContentType, 
		//String theParameters, Map<String, String> theRequestProperties
		//GDCManifestDownload.printLn(theURL);
		DownloadUrl du = new DownloadUrl(theURL, null, null,
				3, 60000, "POST", 
				false, "application/json", 
				null, null);
		String result = du.download();
		return result;
	}
}
