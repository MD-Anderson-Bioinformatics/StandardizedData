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

package edu.mda.bcb.gdc.api.util;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.portal.GDCbiospecimen;
import edu.mda.bcb.gdc.api.portal.GDCclinical;
import edu.mda.bcb.gdc.api.portal.GDCcurrent;
import edu.mda.bcb.gdc.api.portal.GDClegacy;
import java.io.File;

/**
 * Class to encapsulate processing for Biospecimen, Clinical, and Legacy and Current Data.
 * 
 * @author Tod-Casasent
 */
public class GDC
{
	/**
	 * Object for processing legacy data
	 */
	public GDClegacy mLegacy = null;
	
	/**
	 * Object for processing current data
	 */
	public GDCcurrent mCurrent = null;
	
	/**
	 * Object for processing biospecimen data
	 */
	public GDCbiospecimen mBiospecimen = null;
	
	/**
	 * Object for processing clinical data
	 */
	public GDCclinical mClinical = null;
	
	/**
	 * Constructor that takes directories for each type of data.
	 * 
	 * @param theLegacyDir Legacy data directory
	 * @param theCurrentDir Current data directory
	 * @param theBiospecimenDir Biospecimen data directory
	 * @param theClinicalDir Clinical data directory
	 * @param theIndexDir Index file directory
	 */
	public GDC(File theLegacyDir, File theCurrentDir, File theBiospecimenDir, File theClinicalDir, File theIndexDir)
	{
		mLegacy = new GDClegacy(theLegacyDir);
		mCurrent = new GDCcurrent(theCurrentDir);
		mBiospecimen = new GDCbiospecimen(theBiospecimenDir);
		mClinical = new GDCclinical(theClinicalDir);
	}
	
	/**
	 * Load legacy manifests from file system and optionally from GDC. 
	 * Load Program, Project, DataType, and Workflows 
	 * from the file system. If not flagged otherwise, check for new manifests
	 * from the GDC and get them.
	 * 
	 * @throws Exception 
	 */
	public void legacyData() throws Exception
	{
		GDCAPI.printLn("GDC::legacyData - load through workflows from file system");
		mLegacy.loadPPDW();
		if (false==GDCAPI.M_DISABLE_INTERNET_MANIFEST)
		{
			GDCAPI.printLn("GDC::legacyData - load new manifests from GDC");
			mLegacy.newManifests();
			GDCAPI.printLn("GDC::legacyData - update biospecimen manifest files on file system");
			mLegacy.updateManifests();
		}
	}
	
	/**
	 * Load current manifests from file system and optionally from GDC. 
	 * Load Program, Project, DataType, and Workflows 
	 * from the file system. If not flagged otherwise, check for new manifests
	 * from the GDC and get them.
	 * 
	 * @throws Exception 
	 */
	public void currentData() throws Exception
	{
		GDCAPI.printLn("GDC::currentData - load through workflows from file system");
		mCurrent.loadPPDW();
		if (false==GDCAPI.M_DISABLE_INTERNET_MANIFEST)
		{
			GDCAPI.printLn("GDC::currentData - load new manifests from GDC");
			mCurrent.newManifests();
			GDCAPI.printLn("GDC::currentData - update biospecimen manifest files on file system");
			mCurrent.updateManifests();
		}
	}
	
	/**
	 * Load biospecimen manifests from file system and optionally from GDC. 
	 * Load Program, Project, DataType, and Workflows 
	 * from the file system. If not flagged otherwise, check for new manifests
	 * from the GDC and get them.
	 * 
	 * @throws Exception 
	 */
	public void biospecimen() throws Exception
	{
		GDCAPI.printLn("GDC::biospecimen - load through workflows from file system");
		mBiospecimen.loadPPDW();
		if (false==GDCAPI.M_DISABLE_INTERNET_MANIFEST)
		{
			GDCAPI.printLn("GDC::biospecimen - load new manifests from GDC");
			mBiospecimen.newManifests();
			GDCAPI.printLn("GDC::biospecimen - update biospecimen manifest files on file system");
			mBiospecimen.updateManifests();
		}
	}
	
	/**
	 * Load public clinical manifests from file system and optionally from GDC. 
	 * Load Program, Project, DataType, and Workflows 
	 * from the file system. If not flagged otherwise, check for new manifests
	 * from the GDC and get them.
	 * 
	 * @throws Exception 
	 */
	public void clinical() throws Exception
	{
		GDCAPI.printLn("GDC::currentData - load through workflows from file system");
		mClinical.loadPPDW();
		if (false==GDCAPI.M_DISABLE_INTERNET_MANIFEST)
		{
			GDCAPI.printLn("GDC::clinical - load new manifests from GDC");
			mClinical.newManifests();
			GDCAPI.printLn("GDC::clinical - update clinical manifest files on file system");
			mClinical.updateManifests();
		}
	}
	
	/**
	 * Download files from GDC from manifest files for all four manifest options,
	 * unless flagged otherwise.
	 * 
	 * @throws Exception 
	 */
	public void dataDownload() throws Exception
	{
		if (false==GDCAPI.M_DISABLE_INTERNET_DOWNLOAD)
		{
			GDCAPI.printLn("GDC::dataDownload - Biospecimen data files to file system if needed");
			mBiospecimen.downloadCurrentManifests();
			GDCAPI.printLn("GDC::dataDownload - Clinical data files to file system if needed");
			mClinical.downloadCurrentManifests();
			GDCAPI.printLn("GDC::dataDownload - current data files to file system if needed");
			mCurrent.downloadCurrentManifests();
			GDCAPI.printLn("GDC::dataDownload - legacy data files to file system if needed");
			mLegacy.downloadCurrentManifests();
		}
	}
	
	/**
	 * Convert files from GDC for all four manifest options,
	 * unless flagged otherwise.
	 * 
	 * @throws Exception 
	 */
	public void convertFiles() throws Exception
	{
		if (false==GDCAPI.M_DISABLE_CONVERT)
		{
			GDCAPI.printLn("GDC::convertFiles - convert Biospecimen data files if needed");
			mBiospecimen.convertCurrentManifests();
			GDCAPI.printLn("GDC::convertFiles - convert Clinical data files if needed");
			mClinical.convertCurrentManifests();
			GDCAPI.printLn("GDC::convertFiles - convert current data files if needed");
			mCurrent.convertCurrentManifests();
			GDCAPI.printLn("GDC::convertFiles - convert legacy data files if needed");
			mLegacy.convertCurrentManifests();
		}
	}
	
	/**
	 * No longer used code which updates external indexes for each of the datasets
	 * downloaded and converted from GDC. The new setup generates data and results
	 * ZIP file in the MBatchPipeline from the overall index for GDCAPI.
	 * 
	 * @throws Exception 
	 */
	public void updateIndexes() throws Exception
	{
		// TODO: remove after current round of development and testing
		GDCAPI.printLn("GDC::externalIndexes - no longer done under new setup for data and results ZIPs, generated and indexed in MBatchPipeline");
		//GDCAPI.printLn("GDC::externalIndexes - update external index for current data files if needed");
		//mCurrent.updateIndices("standardized");
		//GDCAPI.printLn("GDC::externalIndexes - update external index for legacy data files if needed");
		//mLegacy.updateIndices("standardized");
	}
	
}
