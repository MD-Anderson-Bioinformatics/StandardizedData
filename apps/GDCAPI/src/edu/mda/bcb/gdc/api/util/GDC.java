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

package edu.mda.bcb.gdc.api.util;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.portal.GDCbiospecimen;
import edu.mda.bcb.gdc.api.portal.GDCclinical;
import edu.mda.bcb.gdc.api.portal.GDCcurrent;
import edu.mda.bcb.gdc.api.portal.GDClegacy;
import java.io.File;

/**
 *
 * @author Tod-Casasent
 */
public class GDC
{
	public GDClegacy mLegacy = null;
	public GDCcurrent mCurrent = null;
	public GDCbiospecimen mBiospecimen = null;
	public GDCclinical mClinical = null;
	
	public GDC(File theLegacyDir, File theCurrentDir, File theBiospecimenDir, File theClinicalDir, File theIndexDir)
	{
		mLegacy = new GDClegacy(theLegacyDir);
		mCurrent = new GDCcurrent(theCurrentDir);
		mBiospecimen = new GDCbiospecimen(theBiospecimenDir);
		mClinical = new GDCclinical(theClinicalDir);
	}
	
	////////////////////////////////////////////////////////////////////////////
	//// legacy data processing
	////////////////////////////////////////////////////////////////////////////
	
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
	
	////////////////////////////////////////////////////////////////////////////
	//// current data processing
	////////////////////////////////////////////////////////////////////////////
	
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
	
	////////////////////////////////////////////////////////////////////////////
	//// biospecimen processing
	////////////////////////////////////////////////////////////////////////////
	
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
	
	////////////////////////////////////////////////////////////////////////////
	//// biospecimen processing
	////////////////////////////////////////////////////////////////////////////
	
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
	
	////////////////////////////////////////////////////////////////////////////
	////
	////////////////////////////////////////////////////////////////////////////
	
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
	
	////////////////////////////////////////////////////////////////////////////
	////
	////////////////////////////////////////////////////////////////////////////
	
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
	
	////////////////////////////////////////////////////////////////////////////
	////
	////////////////////////////////////////////////////////////////////////////
	
	public void updateIndexes() throws Exception
	{
		GDCAPI.printLn("GDC::externalIndexes - update external index for current data files if needed");
		mCurrent.updateIndices("standardized");
		GDCAPI.printLn("GDC::externalIndexes - update external index for legacy data files if needed");
		mLegacy.updateIndices("standardized");
	}
	
}
