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

package edu.mda.bcb.gdc.api.data;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.endpoints.Biospecimens;
import edu.mda.bcb.gdc.api.endpoints.Clinicals;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

/**
 *
 * @author Tod-Casasent
 */
public class Project implements Comparable<Project>
{
	public String mName = null;
	public TreeMap<String, DataType> mDatatypes = null;
	public Biospecimen mBiospecimen = null;
	public Clinical mClinical = null;
	
	public Project(String theName)
	{
		mName = theName;
		mDatatypes = new TreeMap<>();
	}
			
	@Override
	public int compareTo(Project o)
	{
		return mName.compareTo(o.mName);
	}
	
	public DataType addEntry(String theDataType)
	{
		DataType dataType = mDatatypes.get(theDataType);
		if (null==dataType)
		{
			GDCAPI.printLn("Project::addEntry - adding new DataType '" + theDataType + "'");
			dataType = new DataType(theDataType);
			mDatatypes.put(theDataType, dataType);
		}
		return dataType;
	}
	
	public boolean biospecimenLoadFile(File theManifestDir, String theProgram) throws NoSuchAlgorithmException, Exception
	{
		Biospecimen man = new Biospecimen(theProgram, mName);
		boolean loaded = man.loadManifest(theManifestDir);
		if (loaded)
		{
			mBiospecimen = man;
		}
		return loaded;
	}
	
	public void biospecimenGetOrUpdateFromGDC(String theProgram) throws Exception
	{
		Biospecimen man = new Biospecimen(theProgram, mName);
		Biospecimens mans = new Biospecimens(man);
		mans.processEndpoint();
		if (null==mBiospecimen)
		{
			mBiospecimen = man;
			// mark as new from GDC
			mBiospecimen.mNewFromGDC = Boolean.TRUE;
		}
		else
		{
			mBiospecimen.update(man);
		}
	}
	
	public boolean clinicalLoadFile(File theManifestDir, String theProgram) throws NoSuchAlgorithmException, Exception
	{
		Clinical man = new Clinical(theProgram, mName);
		boolean loaded = man.loadManifest(theManifestDir);
		if (loaded)
		{
			mClinical = man;
		}
		return loaded;
	}
	
	public void clinicalGetOrUpdateFromGDC(String theProgram) throws Exception
	{
		Clinical man = new Clinical(theProgram, mName);
		Clinicals mans = new Clinicals(man);
		mans.processEndpoint();
		if (null==mClinical)
		{
			mClinical = man;
			// mark as new from GDC
			mClinical.mNewFromGDC = Boolean.TRUE;
		}
		else
		{
			mClinical.update(man);
		}
	}
}
