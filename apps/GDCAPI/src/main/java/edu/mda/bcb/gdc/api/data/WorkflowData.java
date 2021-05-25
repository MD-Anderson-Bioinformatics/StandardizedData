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
import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 *
 * @author Tod-Casasent
 */
public class WorkflowData extends Fileable implements Comparable<WorkflowData>
{
	public String mProgram = null;
	public String mProject = null;
	public String mDataType = null;
	public String mWorkflow = null;
	public String mLegacyName = null;
	
	public WorkflowData(String theProgram, String theProject, String theDataType, String theWorkflow, String theLegacyName)
	{
		mProgram = theProgram;
		mProject = theProject;
		mDataType = theDataType;
		mWorkflow = theWorkflow;
		mLegacyName = theLegacyName;
	}
	
	public void addFile(GDCFile theFile)
	{
		// TODO: this may need code to detect if file is changed between runs
		mFiles.put(theFile.mUUID, theFile);
	}

	@Override
	public int compareTo(WorkflowData o)
	{
		int compare = this.mProgram.compareTo(o.mProgram);
		if (0==compare)
		{
			compare = this.mProject.compareTo(o.mProject);
			if (0==compare)
			{
				compare = this.mDataType.compareTo(o.mDataType);
				if (0==compare)
				{
					compare = this.mWorkflow.compareTo(o.mWorkflow);
					if (0==compare)
					{
						if (null!=this.mLegacyName)
						{
							compare = this.mLegacyName.compareTo(o.mLegacyName);
						}
					}
				}
			}
		}
		return compare;
	}

	@Override
	public String toString()
	{
		return "workflowdata-manifest".toUpperCase() + "{" + "mProgram=" + mProgram + ", mProject=" + mProject + ", mDataType=" + mDataType + ", mWorkflow=" + mWorkflow + '}';
	}

	@Override
	public String loadManifestInternal(File theDir) throws Exception
	{
		String loaded = null;
		// find newest tsv file
		File newestTsv = GDCAPI.findNewestTSV(theDir);
		if (null!=newestTsv)
		{
			// loadfile
			try(BufferedReader br = new BufferedReader(new FileReader(newestTsv)))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					// line should start with "manifest" or be "-FileStart-"
					if (line.startsWith("manifest"))
					{
						// read manifest data
						String [] splitted = line.split("\t", -1);
						if (!mProgram.equals(splitted[1]))
						{
							throw new Exception("For " + newestTsv + ": manifest program does not match directory path");
						}
						if (!mProject.equals(splitted[2]))
						{
							throw new Exception("For " + newestTsv + ": manifest project does not match directory path");
						}
						if (!mDataType.equals(splitted[3]))
						{
							throw new Exception("For " + newestTsv + ": manifest dataType does not match directory path");
						}
						if (!mWorkflow.equals(splitted[4]))
						{
							throw new Exception("For " + newestTsv + ": manifest workflow does not match directory path");
						}
						if ("TRUE".equals(splitted[5]))
						{
							mNotInGDC = Boolean.TRUE;
						}
						else
						{
							mNotInGDC = Boolean.FALSE;
						}
						if (splitted.length>6)
						{
							mLegacyName = splitted[6];
						}
					}
					else if (line.equals("-FileStart-"))
					{
						// load file info
						GDCFile gdcFile = GDCFile.loadFile(br, newestTsv);
						this.addFile(gdcFile);
					}
					else
					{
						throw new Exception("For " + newestTsv + ": unknown line command '" + line + "'");
					}
				}
			}
			mReadFromFile = Boolean.TRUE;
			mNewFromGDC = Boolean.FALSE;
			loaded = newestTsv.getName().replace(".tsv", "");
		}
		return loaded;
	}

	@Override
	public void writeManifest(File theDir) throws Exception
	{
		if(Objects.equals(Boolean.TRUE, mNewFromGDC) || Objects.equals(Boolean.TRUE, mUpdatedByGDC))
		{
			if (!theDir.exists())
			{
				theDir.mkdirs();
			}
			File outfile = new File(theDir, GDCAPI.M_TIMESTAMP + ".tsv");
			GDCAPI.printLn("WorkflowData::writeManifest - outfile = " + outfile.getAbsolutePath());
			OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
			try (BufferedWriter runWriter = java.nio.file.Files.newBufferedWriter(outfile.toPath(), Charset.availableCharsets().get("UTF-8"), options))
			{
				runWriter.write("manifest");
				runWriter.write("\t");
				runWriter.write(mProgram);
				runWriter.write("\t");
				runWriter.write(mProject);
				runWriter.write("\t");
				runWriter.write(mDataType);
				runWriter.write("\t");
				runWriter.write(mWorkflow);
				runWriter.write("\t");
				runWriter.write((Boolean.TRUE==mNotInGDC)?"TRUE":"FALSE");
				if (null!=mLegacyName)
				{
					runWriter.write("\t");
					runWriter.write(mLegacyName);
				}
				runWriter.newLine();
				Collection<Updateable_Mixin<GDCFile>> fs = mFiles.values();
				for (Updateable_Mixin<GDCFile> myfile : fs)
				{
					((GDCFile)myfile).writeFile(runWriter);
				}
			}
		}
	}
	
	public String firstLegacyName()
	{
		int index = mWorkflow.lastIndexOf("-");
		return mWorkflow.substring(0, index);
	}
	
	public String secondLegacyName()
	{
		int index = mWorkflow.lastIndexOf("-");
		return mWorkflow.substring(index+1);
	}
	
	public ArrayList<String> getDatasetName(String theFirstString)
	{
		ArrayList<String> dsNames = new ArrayList<>();
		if (("DNAcopy".equals(mWorkflow))&&("Copy Number Segment".equals(mDataType)))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if (("DNAcopy".equals(mWorkflow))&&("Masked Copy Number Segment".equals(mDataType)))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("HTSeq - Counts".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous");
		}
		else if ("HTSeq - FPKM".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("HTSeq - FPKM-UQ".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("GISTIC - Copy Number Score".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if (("BCGSC miRNA Profiling".equals(mWorkflow))&&("Isoform Expression Quantification".equals(mDataType)))
		{
			dsNames.add(theFirstString + "-continuous");
		}
		else if (("BCGSC miRNA Profiling".equals(mWorkflow))&&("miRNA Expression Quantification".equals(mDataType)))
		{
			dsNames.add(theFirstString + "-continuous");
		}
		else if ("MuSE Variant Aggregation and Masking".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("MuTect2 Variant Aggregation and Masking".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("SomaticSniper Variant Aggregation and Masking".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("VarScan2 Variant Aggregation and Masking".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("Liftover".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous_wXY");
			dsNames.add(theFirstString + "-continuous_noXY");
		}
		else if ("STAR - Counts".equals(mWorkflow))
		{
			// TODO: update STAR conversion
			dsNames.add(theFirstString + "-discrete");
		}
		
		else if ("Affymetrix SNP Array 6.0-hg19-nocnv".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("DNA-Seq-ABI SOLiD".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("DNA-Seq-Illumina GA".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("DNA-Seq-Illumina HiSeq".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("DNA-Seq-Illumina MiSeq".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("DNA-Seq-Mixed platforms".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-discrete");
		}
		else if ("Illumina Human Methylation 27".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous_wXY");
			dsNames.add(theFirstString + "-continuous_noXY");
		}
		else if ("Illumina Human Methylation 450".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous_wXY");
			dsNames.add(theFirstString + "-continuous_noXY");
		}
		else if ("MDA_RPPA_Core".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous");
		}
		else if ("miRNA gene quantification-gene-hg19-miRNA".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous");
		}
		else if ("miRNA isoform quantification-hg19-isoform-miRNA".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous");
		}
		else if ("RNA-Seq-gene-unnormalized-v2".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous");
		}
		else if ("RNA-Seq-isoform-unnormalized-v2".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous");
		}
		else if ("RNA-Seq-v1".equals(mWorkflow))
		{
			dsNames.add(theFirstString + "-continuous");
		}
		else
		{
			GDCAPI.printWarn("Unrecognized data " + toString());
		}
		return dsNames;
	}
}
