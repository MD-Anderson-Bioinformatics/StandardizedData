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

package edu.mda.bcb.gdc.api.portal;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.convert.ClinicalXML;
import edu.mda.bcb.gdc.api.data.DataType;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.data.Program;
import edu.mda.bcb.gdc.api.data.Project;
import edu.mda.bcb.gdc.api.data.Workflow;
import java.io.File;

/**
 *
 * @author Tod-Casasent
 */
public class GDCclinical extends GDC_Mixin
{
	public GDCclinical(File theDataDir)
	{
		super(false, false, theDataDir, false);
	}

	////////////////////////////////////////////////////////////////////////////
	////
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void processManifests(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		theProject.clinicalLoadFile(manifestDir, theProgram.mName);
		theProject.clinicalGetOrUpdateFromGDC(theProgram.mName);
		theProject.mClinical.writeManifest(manifestDir);
		// clear to free memory
		theProject.mClinical = null;
		System.gc();
	}

	@Override
	public void processCurrentDownloads(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		File downloadDir = getSpecificDownloadDir(theProgram, theProject, theDataType, theWorkflow);
		theProject.clinicalLoadFile(manifestDir, theProgram.mName);
		theProject.mClinical.download(mIsLegacyFlag, downloadDir);
		// clear to free memory
		theProject.mClinical = null;
		System.gc();
	}

	@Override
	public void processCurrentConverts(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		// load file before getSpecificConvertDir, or that call failes
		theProject.clinicalLoadFile(manifestDir, theProgram.mName);
		File downloadDir = getSpecificDownloadDir(theProgram, theProject, theDataType, theWorkflow);
		File convertDir = getSpecificConvertDir(theProgram, theProject, theDataType, theWorkflow);
		if (theProject.mClinical.mFiles.size()>0)
		{
			// list files for converting
			GDCFile first = ((GDCFile)theProject.mClinical.mFiles.firstEntry().getValue());
			//for (Updateable_Mixin<GDCFile> data : theProject.mClinical.mFiles.values())
			//{
			//	GDCAPI.printLn("GDCclinical::processCurrentConverts convert file = " + ((GDCFile)data).mName);
			//}
			if (first.mName.endsWith(".xml"))
			{
				if (checkDirs(downloadDir, convertDir))
				{
					convertDir.mkdirs();
					ClinicalXML.processDirectory(theProject.mClinical.mFiles, downloadDir, new File(convertDir, "clinical.tsv"));
				}
			}
			else
			{
				GDCAPI.printWarn("GDCclinical::processCurrentConverts skip processing unrecognized file format " + first.mName);
			}
		}
		else
		{
			GDCAPI.printLn("GDCclinical::processCurrentConverts no files to convert -- do not convert");
		}
		theProject.mClinical = null;
		System.gc();
	}

	@Override
	public File getSpecificManifestDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(mManifestDir, theProgram.mName), theProject.mName);
	}

	@Override
	public File getSpecificDownloadDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(mDownloadDir, theProgram.mName), theProject.mName);
	}

	@Override
	public File getSpecificConvertDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(new File(mConvertDir, theProgram.mName), theProject.mName), theProject.mClinical.mTimestamp);
	}

	@Override
	public String getIndexVariant()
	{
		return null;
	}
}
