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

package edu.mda.bcb.gdc.api.portal;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.convert.MethylationTXT;
import edu.mda.bcb.gdc.api.convert.MirnaTXT;
import edu.mda.bcb.gdc.api.convert.MutationMAF;
import edu.mda.bcb.gdc.api.convert.RnaseqTXT;
import edu.mda.bcb.gdc.api.convert.RppaTXT;
import edu.mda.bcb.gdc.api.convert.Snp6TXT;
import edu.mda.bcb.gdc.api.data.DataType;
import edu.mda.bcb.gdc.api.data.Program;
import edu.mda.bcb.gdc.api.data.Project;
import edu.mda.bcb.gdc.api.data.Workflow;
import java.io.File;

/**
 *
 * @author Tod-Casasent
 */
public class GDClegacy extends GDC_Mixin
{
	
	public GDClegacy(File theDataDir)
	{
		super(true, true, theDataDir, true);
	}
	
	////////////////////////////////////////////////////////////////////////////
	////
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void processManifests(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		theWorkflow.manifestLoadFile(manifestDir, theProgram.mName, theProject.mName, theDataType.mName);
		theWorkflow.manifestGetOrUpdateFromGDC_Legacy(theProgram.mName, theProject.mName, theDataType.mName, theWorkflow.mLegacyName);
		theWorkflow.mManifest.writeManifest(manifestDir);
		// clear to free memory
		theWorkflow.mManifest = null;
		System.gc();
	}
	
	@Override
	public void processCurrentDownloads(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws Exception
	{
		File manifestDir = getSpecificManifestDir(theProgram, theProject, theDataType, theWorkflow);
		File downloadDir = getSpecificDownloadDir(theProgram, theProject, theDataType, theWorkflow);
		theWorkflow.manifestLoadFile(manifestDir, theProgram.mName, theProject.mName, theDataType.mName);
		theWorkflow.mManifest.download(mIsLegacyFlag, downloadDir);
		// clear to free memory
		theWorkflow.mManifest = null;
		System.gc();
	}

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
		if (false==convertDir.exists())
		{
			if (theWorkflow.mManifest.mFiles.size()>0)
			{
				if ("Affymetrix SNP Array 6.0-hg19-nocnv".equals(theWorkflow.mName))
				{
					//Snp6TXT	Discrete
				File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
				if (checkDirs(downloadDir, myDir))
				{
						Snp6TXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir,
								new File(GDCAPI.M_UTIL_DIR, "HG19_Genes.tsv"), true, true);
					}
				}
				else if ("DNA-Seq-ABI SOLiD".equals(theWorkflow.mName))
				{
					//MutationMAF	Discrete
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					// checkDirs inside, since direcotry names change
					MutationMAF.processDirectory_legacy(this, theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, new String[]{"37","19"});
				}
				else if ("DNA-Seq-Illumina GA".equals(theWorkflow.mName))
				{
					//MutationMAF	Discrete
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					// checkDirs inside, since direcotry names change
					MutationMAF.processDirectory_legacy(this, theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, new String[]{"37","19"});
				}
				else if ("DNA-Seq-Illumina HiSeq".equals(theWorkflow.mName))
				{
					//MutationMAF	Discrete
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					// checkDirs inside, since direcotry names change
					MutationMAF.processDirectory_legacy(this, theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, new String[]{"37","19"});
				}
				else if ("DNA-Seq-Illumina MiSeq".equals(theWorkflow.mName))
				{
					//MutationMAF	Discrete
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					// checkDirs inside, since direcotry names change
					MutationMAF.processDirectory_legacy(this, theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, new String[]{"37","19"});
				}
				else if ("DNA-Seq-Mixed platforms".equals(theWorkflow.mName))
				{
					//MutationMAF	Discrete
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					// checkDirs inside, since direcotry names change
					MutationMAF.processDirectory_legacy(this, theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, new String[]{"37","19"});
				}
				else if ("Illumina Human Methylation 27".equals(theWorkflow.mName))
				{
					//MethylationTXT	Continuous
					// with XY
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					if (checkDirs(downloadDir, myDir))
					{
						MethylationTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, false);
					}
					// no XY
					myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(1));
					if (checkDirs(downloadDir, myDir))
					{
						MethylationTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, true);
					}
				}
				else if ("Illumina Human Methylation 450".equals(theWorkflow.mName))
				{
					//MethylationTXT	Continuous
					// with XY
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					if (checkDirs(downloadDir, myDir))
					{
						MethylationTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, false);
					}
					// no XY
					myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(1));
					if (checkDirs(downloadDir, myDir))
					{
						MethylationTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, true);
					}
				}
				else if ("MDA_RPPA_Core".equals(theWorkflow.mName))
				{
					//RppaTXT	Continuous
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					if (checkDirs(downloadDir, myDir))
					{
						RppaTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, true);
					}
				}
				else if ("miRNA gene quantification-gene-hg19-miRNA".equals(theWorkflow.mName))
				{
					//MirnaTXT	Continuous
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					if (checkDirs(downloadDir, myDir))
					{
						MirnaTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, true);
					}
				}
				else if ("miRNA isoform quantification-hg19-isoform-miRNA".equals(theWorkflow.mName))
				{
					//MirnaTXT	Continuous
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					if (checkDirs(downloadDir, myDir))
					{
						MirnaTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, true);
					}
				}
				else if ("RNA-Seq-gene-unnormalized-v2".equals(theWorkflow.mName))
				{
					//RnaseqTXT	Continuous
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					if (checkDirs(downloadDir, myDir))
					{
						RnaseqTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, true);
					}
				}
				else if ("RNA-Seq-isoform-unnormalized-v2".equals(theWorkflow.mName))
				{
					//RnaseqTXT	Continuous
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					if (checkDirs(downloadDir, myDir))
					{
						RnaseqTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, true);
					}
				}
				else if ("RNA-Seq-v1".equals(theWorkflow.mName))
				{
					//RnaseqTXT	Continuous
					File myDir = new File(convertDir, theWorkflow.mManifest.getDatasetName("standardized").get(0));
					if (checkDirs(downloadDir, myDir))
					{
						RnaseqTXT.processDirectory(theWorkflow.mManifest, downloadDir, myDir, biospecimenDir, clinicalDir, true);
					}
				}
				else
				{
					GDCAPI.printWarn("Unrecognized data '" + theProgram.mName + "' '" + theProject.mName + "' '" + theDataType.mName + "' '" + theWorkflow.mName);
				}
				//ClinicalXML.processDirectory(theProject.mBiospecimen.mFiles, downloadDir, new File(convertDir, "matrix_data.tsv"));
			}
			else
			{
				GDCAPI.printLn("GDClegacy::processCurrentConverts no files to convert -- do not convert");
			}
		}
		else
		{
			GDCAPI.printLn("GDClegacy::processCurrentConverts convert dir exists -- do not convert " + convertDir.getAbsolutePath());
		}
		theProject.mBiospecimen = null;
		System.gc();
	}

	@Override
	public File getSpecificManifestDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(new File(new File(mManifestDir, theProgram.mName), theProject.mName), theDataType.mName), theWorkflow.mName);
	}

	@Override
	public File getSpecificDownloadDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(new File(new File(mDownloadDir, theProgram.mName), theProject.mName), theDataType.mName), theWorkflow.mName);
	}

	@Override
	public File getSpecificConvertDir(Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow)
	{
		return new File(new File(new File(new File(new File(mConvertDir, theProgram.mName), theProject.mName), theDataType.mName), theWorkflow.mName), theWorkflow.mManifest.mTimestamp);
	}

	@Override
	public String getIndexVariant()
	{
		return "legacy";
	}
}
