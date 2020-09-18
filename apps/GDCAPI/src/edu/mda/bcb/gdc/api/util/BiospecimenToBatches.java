/*
 *  Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 University of Texas MD Anderson Cancer Center
 *  
 *  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
 *  MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

 */
package edu.mda.bcb.gdc.api.util;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.dataframes.Dataframe;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author TDCasasent
 */
public class BiospecimenToBatches
{
	File mDataFile = null;
	TreeSet<Path> mBiospecimenFiles = null;
	File mBatchFile = null;
	
	public BiospecimenToBatches(File theDataFile, TreeSet<Path> theBiospecimenFiles, File theBatchFile)
	{
		mDataFile = theDataFile;
		mBiospecimenFiles = theBiospecimenFiles;
		mBatchFile = theBatchFile;
	};
	
	public void generateBatchesFile() throws IOException
	{
		// get sample list from data file
		TreeSet<String> samples = getSamples(mDataFile.toPath());
		// for each sample, check biospecimen files for newest one with data
		GDCAPI.printLn("Starting batch file " + mBatchFile.getAbsolutePath());
		try (BufferedWriter runWriter = java.nio.file.Files.newBufferedWriter(mBatchFile.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			// header
			ArrayList<String> headers = getOutputHeaders();
			String headerString = GDCAPI.arrayToString(headers, "\t");
			runWriter.write(headerString);
			runWriter.newLine();
			// data
			for (String sample : samples)
			{
				String line = getBatchLine(sample);
				if (null!=line)
				{
					runWriter.write(line);
					runWriter.newLine();
				}
				else
				{
					runWriter.write(getBatchLineNA(sample));
					runWriter.newLine();
					GDCAPI.printWarn("Sample not found, using NAs for " + sample);
				}
			}
		}
		GDCAPI.printLn("Wrote batch file " + mBatchFile.getAbsolutePath());
	};
	
	protected String getBatchLine(String theSample) throws IOException
	{
		String line = null;
		for (Path file : mBiospecimenFiles)
		{
			if (null==line)
			{
				Dataframe df = new Dataframe();
				df.readDataFrame(file.toFile());
				ArrayList<String> row = df.getRowFromColumnValueNoWarn("Barcode", theSample);
				if (null!=row)
				{
					if (row.size()>0)
					{
						// Biospecimen file		--			batches.tsv
						// Barcode							Sample
						// PatientBarcode					Patient
						// SampleTypeId + SampleTypeName	Type
						// Batch							BatchId
						// Bcr								BCR
						// Tss								TSS
						// PlateId							PlateId
						// AliquotCenter					AliquotCenter
						// ShipDate							ShipDate
						// SourceCenter						SourceCenter
						line = row.get(df.mHeaders.indexOf("Barcode")) + 
								"\t" + row.get(df.mHeaders.indexOf("PatientBarcode")) + 
								"\t" + row.get(df.mHeaders.indexOf("SampleTypeId")) + " " + row.get(df.mHeaders.indexOf("SampleTypeName")) + 
								"\t" + row.get(df.mHeaders.indexOf("Batch")) + 
								"\t" + row.get(df.mHeaders.indexOf("Bcr")) + 
								"\t" + row.get(df.mHeaders.indexOf("Tss")) + 
								"\t" + row.get(df.mHeaders.indexOf("PlateId")) + 
								"\t" + row.get(df.mHeaders.indexOf("AliquotCenter")) + 
								"\t" + row.get(df.mHeaders.indexOf("ShipDate")) + 
								"\t" + row.get(df.mHeaders.indexOf("SourceCenter"));
					}
				}
			}
		}
		return line;
	};
	
	
	protected String getBatchLineNA(String theSample) throws IOException
	{
		String line = null;
		// Biospecimen file		--			batches.tsv
		// Barcode							Sample
		// PatientBarcode					Patient
		// SampleTypeId + SampleTypeName	Type
		// Batch							BatchId
		// Bcr								BCR
		// Tss								TSS
		// PlateId							PlateId
		// AliquotCenter					AliquotCenter
		// ShipDate							ShipDate
		// SourceCenter						SourceCenter
		String [] splitted = theSample.split("-", -1);
		line = theSample + 
				"\t" + splitted[0] + "-" + splitted[1] + "-" + splitted[2] + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA";
		return line;
	};
	
	// PatientUUID	PatientBarcode	Uuid	
	// Barcode	Project	Disease	Batch	Bcr	Tss	PlateId	AliquotCenter	ShipDate	SourceCenter
	// Sex	SampleTypeId	SampleTypeName	IsFfpe	
	// AliquotConcentration	AliquotQuantity	AliquotVolume	PlateRow	PlateColumn
	
	// TODO: duplicated from Batches.java, de-duplicate in future
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
	
	static public TreeSet<String> getSamples(Path theDataFile) throws IOException
	{
		TreeSet<String> samples = new TreeSet<>();
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theDataFile, Charset.availableCharsets().get("UTF-8")))
		{
			String line = br.readLine();
			String [] tokens = line.split("\t", -1);
			for (String val : tokens)
			{
				if (!"".equals(val))
				{
					samples.add(val);
				}
			}
		}
		return samples;
	}
}
