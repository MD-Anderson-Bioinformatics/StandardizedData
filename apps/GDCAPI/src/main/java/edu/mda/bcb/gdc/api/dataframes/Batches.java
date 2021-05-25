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

package edu.mda.bcb.gdc.api.dataframes;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.data.Sample;
import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * For GDC TCGA projects, generate batch files from biospecimen data.
 * 
 * @author Tod-Casasent
 */
public class Batches
{
	/**
	 * Array of GDCFile objects representing entries (samples) in a GDC manifest.
	 */
	public GDCFile [] mFiles = null;
	
	/**
	 * Directory to write the converted file.
	 */
	public File mConvertDir = null;
	
	/**
	 * GDC Program string -- not currently used, but provided for when this is expanded to non-TCGA projects.
	 */
	public String mProgram = null;
	
	/**
	 * GDC Project string -- not currently used, but provided for when this is expanded to non-TCGA projects.
	 */
	public String mProject = null;
	
	/**
	 * Constructor.
	 * 
	 * @param theFiles Array of GDCFile objects representing entries (samples) in a GDC manifest.
	 * @param theConvertDir Directory to write the converted file.
	 * @param theProgram GDC Program string -- not currently used, but provided for when this is expanded to non-TCGA projects.
	 * @param theProject GDC Project string -- not currently used, but provided for when this is expanded to non-TCGA projects.
	 */
	public Batches(GDCFile [] theFiles, File theConvertDir,
			String theProgram, String theProject)
	{
		mFiles = theFiles;
		mConvertDir = theConvertDir;
		mProgram = theProgram;
		mProject = theProject;
	}
	
	/**
	 * If biospecimen data is available in theBiospecimenDir, use that data
	 * to create a batches.tsv file.
	 * 
	 * @param theBiospecimenDir Directory in which to look for a biospecimen.tsv file.
	 * @throws IOException 
	 */
	public void writeBatchFile(File theBiospecimenDir) throws IOException
	{
		GDCAPI.printLn("Batches::writeBatchFile called");
		if (null!=theBiospecimenDir)
		{
			GDCAPI.printLn("Batches::writeBatchFile - getSamples");
			TreeSet<Sample> samples = getSamples();
			GDCAPI.printLn("Batches::writeBatchFile - getDataframe");
			Dataframe df = getDataframe(theBiospecimenDir);
			File outFile = new File(mConvertDir, "batches.tsv");
			GDCAPI.printLn("Batches::writeBatchFile - collects header info");
			Integer [] headerIndexes = getUsableHeaderIndexes(df.mHeaders);
			ArrayList<String> sourceHeaders = getSourceHeaders();
			ArrayList<String> outputHeaders = getOutputHeaders();
			GDCAPI.printLn("Batches::writeBatchFile - write " + outFile.getAbsolutePath());
			try (PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)), true))
			{
				boolean first = true;
				////////////////////////////////////////////
				// header
				for (Integer index : headerIndexes)
				{
					if (false==first)
					{
						ps.print("\t");
					}
					else
					{
						first = false;
					}
					ps.print(outputHeaders.get(index));
				}
				ps.println();
				////////////////////////////////////////////
				// samples
				for (Sample mySample : samples)
				{
					first = true;
					for (Integer index : headerIndexes)
					{
						if (false==first)
						{
							ps.print("\t");
						}
						else
						{
							first = false;
						}
						ps.print(df.getColumnFromColumnValue(sourceHeaders.get(index), getUUIDHeader(), mySample.mUUID));
					}
					ps.println();
				}
			}
		}
	}
	
	/**
	 * If theBiospecimenDir exists, read biospecimen.tsv and return the dataframe.
	 * 
	 * @param theBiospecimenDir Directory in which to look for a biospecimen.tsv file.
	 * @return Dataframe containing biospecimen data used for creating TCGA batches.tsv files.
	 * @throws IOException 
	 */
	private Dataframe getDataframe(File theBiospecimenDir) throws IOException
	{
		Dataframe df = new Dataframe();
		// get path to biospecimen converted file
		if (null!=theBiospecimenDir)
		{
			File myFile = new File(theBiospecimenDir, "biospecimen.tsv");
			df.readDataFrame(myFile);
		}
		return df;
	}
	
	/**
	 * Get Sample objects from GDCFile list.
	 * 
	 * @return Sorted unique list of Sample objects.
	 */
	private TreeSet<Sample> getSamples()
	{
		// collect set of samples
		TreeSet<Sample> samples = new TreeSet<>();
		for (GDCFile gdcFile : mFiles)
		{
			for (Updateable_Mixin<Sample> mySample : gdcFile.mSamples.values())
			{
				Sample objSample = ((Sample)mySample);
				samples.add(objSample);
			}
		}
		return samples;
	}
	
	/**
	 * Array of header column labels from biospecimen files for TCGA batches.tsv file.
	 * Do not put spaces in header labels -- should correspond to R dataframe rules.
	 * 
	 * @return List of strings.
	 */
	private ArrayList<String> getSourceHeaders()
	{
		ArrayList<String> headers = new ArrayList<>();
		headers.add("Barcode");
		headers.add("PatientBarcode");
		headers.add("SampleTypeName");
		headers.add("Batch");
		headers.add("Bcr");
		headers.add("Tss");
		headers.add("PlateId");
		headers.add("AliquotCenter");
		headers.add("ShipDate");
		headers.add("SourceCenter");
		return headers;
	}

	/**
	 * Array of header column labels for TCGA batches.tsv file.
	 * Do not put spaces in header labels -- should correspond to R dataframe rules.
	 * 
	 * @return List of strings.
	 */
	private ArrayList<String> getOutputHeaders()
	{
		ArrayList<String> headers = new ArrayList<>();
		headers.add("Sample");
		headers.add("Patient");
		headers.add("Type");
		headers.add("BatchId");
		headers.add("BCR");
		headers.add("TSS");
		headers.add("PlateId");
		headers.add("AliquotCenter");
		headers.add("ShipDate");
		headers.add("SourceCenter");
		return headers;
	}
	
	/**
	 * UUID column label for biospecimen file.
	 * 
	 * @return Currently, the string "Uuid".
	 */
	private String getUUIDHeader()
	{
		return "Uuid";
	}
	
	/**
	 * Get index numbers of headers available in both the source and the list
	 * of desired source headers. (Not all datasets consistently have all
	 * possible headers.)
	 * 
	 * @param theAvailableHeaders Integer indexes of usable headers.
	 * @return 
	 */
	private Integer [] getUsableHeaderIndexes(ArrayList<String> theAvailableHeaders)
	{
		ArrayList<String> sourceHeaders = getSourceHeaders();
		ArrayList<Integer> usable = new ArrayList<>();
		usable.add(0);
		usable.add(1);
		for (int test=2 ; test<sourceHeaders.size(); test++)
		{
			if (theAvailableHeaders.contains(sourceHeaders.get(test)))
			{
				usable.add(test);
			}
		}
		return usable.toArray(new Integer[0]);
	}
}
