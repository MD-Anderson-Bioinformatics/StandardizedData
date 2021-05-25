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

import edu.mda.bcb.gdc.api.endpoints.Manifests;
import edu.mda.bcb.gdc.api.endpoints.legacy.ManifestsLegacy;
import java.io.File;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Tod-Casasent
 */
public class Workflow implements Comparable<Workflow>
{
	public String mName = null;
	public String mLegacyName = null;
	public WorkflowData mManifest = null;
	
	/**
	 * 
	 * @param theName String for the workflow name
	 * @param theLegacyName For current data is null, otherwise is the legacy TCGA workflow name where theWorkflow is a more generalized name making legacy follow the same patterns as current data.
	 */
	public Workflow(String theName, String theLegacyName)
	{
		mName = theName;
		mLegacyName = theLegacyName;
		mManifest = null;
	}
			
	@Override
	public int compareTo(Workflow o)
	{
		return mName.compareTo(o.mName);
	}
	
	public boolean manifestLoadFile(File theManifestDir, String theProgram, String theProject, String theDataType) throws NoSuchAlgorithmException, Exception
	{
		// use null for legacy name, will be updated by loadManifest if needed
		WorkflowData man = new WorkflowData(theProgram, theProject, theDataType, mName, null);
		boolean loaded = man.loadManifest(theManifestDir);
		if (loaded)
		{
			this.mLegacyName = man.mLegacyName;
			mManifest = man;
		}
		return loaded;
	}

	public void manifestGetOrUpdateFromGDC_Legacy(String theProgram, String theProject, String theDataType, String theLegacyName) throws Exception
	{
		WorkflowData man = new WorkflowData(theProgram, theProject, theDataType, mName, theLegacyName);
		ManifestsLegacy mans = new ManifestsLegacy(man, true);
		mans.processEndpoint();
		if (null==mManifest)
		{
			mManifest = man;
			// mark as new from GDC
			mManifest.mNewFromGDC = Boolean.TRUE;
		}
		else
		{
			mManifest.update(man);
		}
	}
	
	public void manifestGetOrUpdateFromGDC_Current(String theProgram, String theProject, String theDataType) throws Exception
	{
		// legacy name is null for current
		WorkflowData man = new WorkflowData(theProgram, theProject, theDataType, mName, null);
		Manifests mans = new Manifests(man, false);
		mans.processEndpoint();
		if (null==mManifest)
		{
			mManifest = man;
			// mark as new from GDC
			mManifest.mNewFromGDC = Boolean.TRUE;
		}
		else
		{
			mManifest.update(man);
		}
	}
}
