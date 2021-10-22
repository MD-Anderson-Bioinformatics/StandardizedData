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
import edu.mda.bcb.gdc.api.convert.GisticTXT;
import edu.mda.bcb.gdc.api.convert.MethylationTXT;
import edu.mda.bcb.gdc.api.convert.MirnaTXT;
import edu.mda.bcb.gdc.api.convert.MutationMAF;
import edu.mda.bcb.gdc.api.convert.RnaseqTXT;
import edu.mda.bcb.gdc.api.convert.RppaTSV;
import edu.mda.bcb.gdc.api.convert.Snp6TXT;
import edu.mda.bcb.gdc.api.data.DataType;
import edu.mda.bcb.gdc.api.data.Program;
import edu.mda.bcb.gdc.api.data.Project;
import edu.mda.bcb.gdc.api.data.Workflow;
import java.io.File;

/**
 * Specialized class for downloading GDC "current" data
 * 
 * @author Tod-Casasent
 */
public class GDCcurrent extends GDC_Mixin
{
	/**
	 * Constructor -- with flags to GDC_Mixin saying to use the datatype and 
	 * workflow options, and that this is not legacy data.
	 * 
	 * @param theDataDir Base data dir (there are primary and secondary data dirs)
	 */
	public GDCcurrent(File theDataDir)
	{
		super(true, true, theDataDir, false);
	}
	
	////////////////////////////////////////////////////////////////////////////
	////
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Download the manifests (lists of files for a dataset) from the GDC.
	 * 
	 * @param theProgram Object storing metadata mapping relationships and data
	 * @param theProject Object storing metadata mapping relationships and data
	 * @param theDataType Object storing metadata mapping relationships and data
	 * @param theWorkflow Object storing metadata mapping relationships and data
	 * @throws Exception 
	 */
	@Override
	public void processManifests(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		theWorkflow.manifestLoadFile(manifestDir, theProgram.mName, theProject.mName, theDataType.mName);
		theWorkflow.manifestGetOrUpdateFromGDC_Current(theProgram.mName, theProject.mName, theDataType.mName);
		theWorkflow.mManifest.writeManifest(manifestDir);
		// clear to free memory
		theWorkflow.mManifest = null;
		System.gc();
	}
	
	/**
	 * Download datafiles from the manifests (lists of files for a dataset) from the GDC.
	 * 
	 * @param theProgram Object storing metadata mapping relationships and data
	 * @param theProject Object storing metadata mapping relationships and data
	 * @param theDataType Object storing metadata mapping relationships and data
	 * @param theWorkflow Object storing metadata mapping relationships and data
	 * @throws Exception 
	 */
	@Override
	public void processCurrentDownloads(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		File downloadDir = getSpecificDownloadDir(theProgram, theProject, theDataType, theWorkflow);
		theWorkflow.manifestLoadFile(manifestDir, theProgram.mName, theProject.mName, theDataType.mName);
		theWorkflow.mManifest.download(mIsLegacyFlag,downloadDir);
		// clear to free memory
		theWorkflow.mManifest = null;
		System.gc();
	}
	
	/**
	 * Convert data in files for biospecimen (metadata and batch information),
	 * public clinical, and numeric data.
	 * 
	 * @param theProgram Object storing metadata mapping relationships and data
	 * @param theProject Object storing metadata mapping relationships and data
	 * @param theDataType Object storing metadata mapping relationships and data
	 * @param theWorkflow Object storing metadata mapping relationships and data
	 * @throws Exception 
	 */
	@Override
	public void processCurrentConverts(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		// load file before getSpecificConvertDir, or that call failes
		theWorkflow.manifestLoadFile(manifestDir, theProgram.mName, theProject.mName, theDataType.mName);
		File downloadDir = getSpecificDownloadDir(theProgram, theProject, theDataType, theWorkflow);
		File convertDir = getSpecificConvertDir(theProgram, theProject, theDataType, theWorkflow);
		File biospecimenDir = GDCAPI.M_GDC.mBiospecimen.findNewestConvertDir(theProgram.mName, theProject.mName);
		File clinicalDir = GDCAPI.M_GDC.mClinical.findNewestConvertDir(theProgram.mName, theProject.mName);
		doCurrentConverts(downloadDir, convertDir, GDCAPI.M_UTIL_DIR, biospecimenDir, clinicalDir, theProgram, theProject, theDataType, theWorkflow);
		theProject.mBiospecimen = null;
		System.gc();
	}
	
	/**
	 * Convert data files based on workflow and datatypes.
	 * 
	 * @param theDownloadDir Directory containing downloaded files
	 * @param theConvertDir Directory to place converted data
	 * @param theUtilDir util directory with HG19 and HG38 mapping files
	 * @param theBiospecimenDir Directory with biospecimen (metadata and batch) data
	 * @param theClinicalDir Directory with public clinical data
	 * @param theProgram Object storing metadata mapping relationships and data
	 * @param theProject Object storing metadata mapping relationships and data
	 * @param theDataType Object storing metadata mapping relationships and data
	 * @param theWorkflow Object storing metadata mapping relationships and data
	 * @return
	 * @throws Exception 
	 */
	static public File doCurrentConverts(File theDownloadDir, File theConvertDir, File theUtilDir, File theBiospecimenDir, File theClinicalDir, 
			Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File outdir = null;
		if (theWorkflow.mManifest.mFiles.size()>0)
		{
			if (("DNAcopy".equals(theWorkflow.mName))&&("Copy Number Segment".equals(theDataType.mName)))
			{
				// Snp6TXT	Discrete
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					Snp6TXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir,
							new File(theUtilDir, "HG38_Genes.tsv"), false, true);
				}
			}
			else if (("DNAcopy".equals(theWorkflow.mName))&&("Masked Copy Number Segment".equals(theDataType.mName)))
			{
				// Snp6TXT	Discrete (nocnv)
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					Snp6TXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir,
						new File(theUtilDir, "HG38_Genes.tsv"), false, true);
				}
			}
			else if ("HTSeq - Counts".equals(theWorkflow.mName))
			{
				//RnaseqTXT	Continuous
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					RnaseqTXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, false);
				}
			}
			else if ("HTSeq - FPKM".equals(theWorkflow.mName))
			{
				//RnaseqTXT	Discrete
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					RnaseqTXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, false);
				}
			}
			else if ("HTSeq - FPKM-UQ".equals(theWorkflow.mName))
			{
				//RnaseqTXT	Discrete
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					RnaseqTXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, false);
				}
			}
			else if ("GISTIC - Copy Number Score".equals(theWorkflow.mName))
			{
				//GisticTXT	Discrete
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					GisticTXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, false);
				}
			}
			else if (("BCGSC miRNA Profiling".equals(theWorkflow.mName))&&("Isoform Expression Quantification".equals(theDataType.mName)))
			{
				//MirnaTXT	Continuous
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					MirnaTXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, false);
				}
			}
			else if (("BCGSC miRNA Profiling".equals(theWorkflow.mName))&&("miRNA Expression Quantification".equals(theDataType.mName)))
			{
				//MirnaTXT	Continuous
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					MirnaTXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, false);
				}
			}
			else if ("MuSE Variant Aggregation and Masking".equals(theWorkflow.mName))
			{
				//MutationMAF	Discrete
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					MutationMAF.processDirectory_current(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, new String[]{"38"});
				}
			}
			else if ("MuTect2 Variant Aggregation and Masking".equals(theWorkflow.mName))
			{
				//MutationMAF	Discrete
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					MutationMAF.processDirectory_current(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, new String[]{"38"});
				}
			}
			else if ("SomaticSniper Variant Aggregation and Masking".equals(theWorkflow.mName))
			{
				//MutationMAF	Discrete
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					MutationMAF.processDirectory_current(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, new String[]{"38"});
				}
			}
			else if ("VarScan2 Variant Aggregation and Masking".equals(theWorkflow.mName))
			{
				//MutationMAF	Discrete
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					MutationMAF.processDirectory_current(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, new String[]{"38"});
				}
			}
			else if ("Liftover".equals(theWorkflow.mName))
			{
				//MethylationTXT	Continuous
				// with XY
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				if (checkDirs(theDownloadDir, myDir))
				{
					MethylationTXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, false);
				}
				// no XY
				myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(1));
				outdir = myDir;
				if (checkDirs(theDownloadDir, myDir))
				{
					MethylationTXT.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, true);
				}
			}
			else if ("RPPA".equals(theWorkflow.mName))
			{
				// RPPA value was made up in Workflows.java, as GDC has metadata bug for this type
				File myDir = new File(theConvertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				if (checkDirs(theDownloadDir, myDir))
				{
					RppaTSV.processDirectory(theWorkflow.mManifest, theDownloadDir, myDir, theBiospecimenDir, theClinicalDir, false);
				}
			}
			else if ("STAR - Counts".equals(theWorkflow.mName))
			{
				GDCAPI.printLn("Ignore STAR - Counts data for now");
			}
			else
			{
				GDCAPI.printWarn("Unrecognized data '" + theProgram.mName + "' '" + theProject.mName + "' '" + theDataType.mName + "' '" + theWorkflow.mName);
			}
		}
		return outdir;
	}

	/**
	 * Generate "standard" manifest dir, based on GDC naming conventions.
	 * 
	 * @param theProgram Object storing metadata mapping relationships and data
	 * @param theProject Object storing metadata mapping relationships and data
	 * @param theDataType Object storing metadata mapping relationships and data
	 * @param theWorkflow Object storing metadata mapping relationships and data
	 * @return 
	 */
	@Override
	public File getSpecificManifestDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(new File(new File(mManifestDir, theProgram.mName), theProject.mName), theDataType.mName), theWorkflow.mName);
	}

	/**
	 * Generate "standard" download dir, based on GDC naming conventions.
	 * 
	 * @param theProgram Object storing metadata mapping relationships and data
	 * @param theProject Object storing metadata mapping relationships and data
	 * @param theDataType Object storing metadata mapping relationships and data
	 * @param theWorkflow Object storing metadata mapping relationships and data
	 * @return 
	 */
	@Override
	public File getSpecificDownloadDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(new File(new File(mDownloadDir, theProgram.mName), theProject.mName), theDataType.mName), theWorkflow.mName);
	}

	/**
	 * Generate "standard" convert dir, based on GDC naming conventions.
	 * 
	 * @param theProgram Object storing metadata mapping relationships and data
	 * @param theProject Object storing metadata mapping relationships and data
	 * @param theDataType Object storing metadata mapping relationships and data
	 * @param theWorkflow Object storing metadata mapping relationships and data
	 * @return 
	 */
@Override
	public File getSpecificConvertDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(new File(new File(new File(mConvertDir, theProgram.mName), theProject.mName), theDataType.mName), theWorkflow.mName), theWorkflow.mManifest.mTimestamp);
	}

	/**
	 * String for "current" GDC data.
	 * 
	 * @return String "current"
	 */
	@Override
	public String getIndexVariant()
	{
		return "current";
	}
}
