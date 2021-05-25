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
import edu.mda.bcb.gdc.api.convert.BiospecimenXML;
import edu.mda.bcb.gdc.api.data.DataType;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.data.Program;
import edu.mda.bcb.gdc.api.data.Project;
import edu.mda.bcb.gdc.api.data.Workflow;
import java.io.File;

/**
 * Wraps the pipeline logic for processing biospecimen files from the GDC.
 * Note that the "current" files are used for both current and legacy datasets.
 * 
 * @author Tod-Casasent
 */
public class GDCbiospecimen extends GDC_Mixin
{
	public GDCbiospecimen(File theDataDir)
	{
		super(false, false, theDataDir, false);
	}

	/**
	 * processManifests loads current manifest information, checks for new or
	 * updated information at the GDC, retrieves it, and writes it out.
	 * 
	 * @param theProgram a Program object, representing the GDC program information.
	 * @param theProject a Project object, representing the GDC Project information.
	 * @param theDataType a DataType object, representing the GDC DataType (one level more general from workflow) information.
	 * @param theWorkflow a Workflow object, representing the GDC Workflow (similar to platform) information.
	 * @throws Exception 
	 */
	@Override
	public void processManifests(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		theProject.biospecimenLoadFile(manifestDir, theProgram.mName);
		theProject.biospecimenGetOrUpdateFromGDC(theProgram.mName);
		theProject.mBiospecimen.writeManifest(manifestDir);
		// clear to free memory
		theProject.mBiospecimen = null;
		System.gc();
	}

	/**
	 * processCurrentDownloads downloads current manifests that are new or updated from the GDC.
	 * 
	 * @param theProgram a Program object, representing the GDC program information.
	 * @param theProject a Project object, representing the GDC Project information.
	 * @param theDataType a DataType object, representing the GDC DataType (one level more general from workflow) information.
	 * @param theWorkflow a Workflow object, representing the GDC Workflow (similar to platform) information.
	 * @throws Exception 
	 */
	@Override
	public void processCurrentDownloads(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		File downloadDir = getSpecificDownloadDir(theProgram, theProject, theDataType, theWorkflow);
		theProject.biospecimenLoadFile(manifestDir, theProgram.mName);
		theProject.mBiospecimen.download(mIsLegacyFlag, downloadDir);
		// clear to free memory
		theProject.mBiospecimen = null;
		System.gc();
	}

	/**
	 * processCurrentConverts converts new or updated current biospecimen files
	 * to biospecimen TSV files. Does not handle GDC TSV or XLSX files.
	 * TODO: add support for other biospecimen formats.
	 * 
	 * @param theProgram a Program object, representing the GDC program information.
	 * @param theProject a Project object, representing the GDC Project information.
	 * @param theDataType a DataType object, representing the GDC DataType (one level more general from workflow) information.
	 * @param theWorkflow a Workflow object, representing the GDC Workflow (similar to platform) information.
	 * @throws Exception 
	 */
	@Override
	public void processCurrentConverts(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		// load file before getSpecificConvertDir, or that call failes
		theProject.biospecimenLoadFile(manifestDir, theProgram.mName);
		File downloadDir = getSpecificDownloadDir(theProgram, theProject, theDataType, theWorkflow);
		File convertDir = getSpecificConvertDir(theProgram, theProject, theDataType, theWorkflow);
		if (theProject.mBiospecimen.mFiles.size()>0)
		{
			// list files for converting
			GDCFile first = ((GDCFile)theProject.mBiospecimen.mFiles.firstEntry().getValue());
			//for (Updateable_Mixin<GDCFile> data : theProject.mBiospecimen.mFiles.values())
			//{
			//	GDCAPI.printLn("GDCbiospecimen::processCurrentConverts convert file = " + ((GDCFile)data).mName);
			//}
			if (first.mName.endsWith(".xml"))
			{
				if (checkDirs(downloadDir, convertDir))
				{
					convertDir.mkdirs();
					BiospecimenXML.processDirectory(theProject.mBiospecimen.mFiles, downloadDir, new File(convertDir, "biospecimen.tsv"),
							theProgram, theProject, theDataType, theWorkflow);
				}
			}
			else if (first.mName.endsWith(".tsv"))
			{
				GDCAPI.printLn("GDCbiospecimen::processCurrentConverts skip processing TSV files");
			}
			else if (first.mName.endsWith(".xlsx"))
			{
				GDCAPI.printLn("GDCbiospecimen::processCurrentConverts skip processing XLSX files");
			}
			else
			{
				GDCAPI.printWarn("GDCbiospecimen::processCurrentConverts skip processing unrecognized file format " + first.mName);
			}
		}
		else
		{
			GDCAPI.printLn("GDCbiospecimen::processCurrentConverts no files to convert -- do not convert");
		}
		theProject.mBiospecimen = null;
		System.gc();
	}

	/**
	 * getSpecificManifestDir builds the directory path for the manifest directory using program and project.
	 * 
	 * @param theProgram a Program object, representing the GDC program information.
	 * @param theProject a Project object, representing the GDC Project information.
	 * @param theDataType a DataType object, representing the GDC DataType (one level more general from workflow) information.
	 * @param theWorkflow a Workflow object, representing the GDC Workflow (similar to platform) information.
	 * @return File object pointing to directory
	 */
	@Override
	public File getSpecificManifestDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(mManifestDir, theProgram.mName), theProject.mName);
	}

	/**
	 * getSpecificDownloadDir builds the directory path for the download directory using program and project.
	 * 
	 * @param theProgram a Program object, representing the GDC program information.
	 * @param theProject a Project object, representing the GDC Project information.
	 * @param theDataType a DataType object, representing the GDC DataType (one level more general from workflow) information.
	 * @param theWorkflow a Workflow object, representing the GDC Workflow (similar to platform) information.
	 * @return File object pointing to directory
	 */
	@Override
	public File getSpecificDownloadDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(mDownloadDir, theProgram.mName), theProject.mName);
	}

	/**
	 * getSpecificConvertDir builds the directory path for the convert directory using program and project.
	 * 
	 * @param theProgram a Program object, representing the GDC program information.
	 * @param theProject a Project object, representing the GDC Project information.
	 * @param theDataType a DataType object, representing the GDC DataType (one level more general from workflow) information.
	 * @param theWorkflow a Workflow object, representing the GDC Workflow (similar to platform) information.
	 * @return File object pointing to directory
	 */
	@Override
	public File getSpecificConvertDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(new File(mConvertDir, theProgram.mName), theProject.mName), theProject.mBiospecimen.mTimestamp);
	}

	/**
	 * getIndexVariant is used elsewhere -- null indicates not used here.
	 * 
	 * @return null indicating not used here
	 */
	@Override
	public String getIndexVariant()
	{
		return null;
	}
}
