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

package edu.mda.bcb.gdc.api.convert;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.dataframes.Batches;
import edu.mda.bcb.gdc.api.convert.utils.Matrix;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.data.Sample;
import edu.mda.bcb.gdc.api.data.WorkflowData;
import edu.mda.bcb.gdc.api.dataframes.ClinicalDF;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Tod-Casasent
 */
public class RnaseqTXT
{
	static public void processDirectory(WorkflowData theManifest,
			final File theDownloadDir, File theConvertDir, File theBiospecimenDir, File theClinicalDir, boolean theLegacyFlag) throws IOException, Exception
	{
		GDCFile [] gdcArray = theManifest.getGDCFiles(true);
		if (gdcArray.length>0)
		{
			if (!theConvertDir.exists())
			{
				theConvertDir.mkdirs();
			}
		}
		else
		{
			GDCAPI.printWarn("RnaseqTXT No files to process");
			return;
		}
		GDCAPI.printLn("RnaseqTXT::processDirectory getBarcodes");
		TreeSet<String> barcodes = theManifest.getBarcodes(gdcArray);
		////////////////////////////////////////////////////////////////////////
		//// collect features
		////////////////////////////////////////////////////////////////////////
		GDCAPI.printLn("RnaseqTXT::processDirectory collect features");
		AtomicLong counter = new AtomicLong(0);
		List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
		SortedSet<String> features = Collections.synchronizedSortedSet(new TreeSet<>());
		// note this solution changes the parallelism settings for the entire system
		// so if you have multiple parallel operations going on this could do weird things
		GDCAPI.printLn("PARALLEL-THREADS = 10");
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "10");
		Arrays.asList(gdcArray)
				.parallelStream()
				.forEach((GDCFile loopFile) ->
				{
					long count = counter.incrementAndGet();
					if (0 == count % 100)
					{
						GDCAPI.printLn("RnaseqTXT::processDirectory counter = " + count + " of " + gdcArray.length);
					}
					try
					{
						RnaseqTXT.run(loopFile, features, theDownloadDir);
					}
					catch (Exception exp)
					{
						errors.add(exp);
					}
				});
		////////////////////////////////////////////////////////////////////////
		//// process files
		////////////////////////////////////////////////////////////////////////
		GDCAPI.printLn("RnaseqTXT::processDirectory process files");
		// note this solution changes the parallelism settings for the entire system
		// so if you have multiple parallel operations going on this could do weird things
		GDCAPI.printLn("PARALLEL-THREADS = 5");
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "5");
		Matrix newMatrix = new Matrix(barcodes, new TreeSet<String>(features), false);
		counter.set(0);
		Arrays.asList(gdcArray)
				.parallelStream()
				.forEach((GDCFile loopFile) ->
				{
					long count = counter.incrementAndGet();
					if (0 == count % 100)
					{
						GDCAPI.printLn("RnaseqTXT::processDirectory counter = " + count + " of " + gdcArray.length);
					}
					try
					{
						RnaseqTXT.run(loopFile, newMatrix, theDownloadDir);
					}
					catch (Exception exp)
					{
						GDCAPI.printErr("RnaseqTXT::processDirectory error in process", exp);
						errors.add(exp);
					}
				});
		for (Exception err : errors)
		{
			GDCAPI.printErr("RnaseqTXT::processDirectory errors " + err.getMessage(), err);
		}
		try
		{
			GDCAPI.writeMatrixFileStr(new File(theConvertDir, "matrix_data.tsv"), newMatrix);
			// Batches.tsv file
			Batches batches = new Batches(gdcArray, theConvertDir, theManifest.mProgram, theManifest.mProject);
			batches.writeBatchFile(theBiospecimenDir);
			ClinicalDF clinicalDF = new ClinicalDF(gdcArray, theConvertDir, theManifest.mProgram, theManifest.mProject);
			clinicalDF.writeClinicalFile(theClinicalDir);
		}
		catch (Exception exp)
		{
			GDCAPI.printErr("RnaseqTXT::processDirectory exception caught" + exp.getMessage(), exp);
		}
	}

	private RnaseqTXT()
	{
	}
	
	static public String cleanFeature(String theFeature)
	{
		if (theFeature.endsWith("_calculated"))
		{
			theFeature = theFeature.replaceAll("_calculated", "");
		}
		return theFeature;
	}
	
	static public void run(GDCFile theFile, SortedSet<String> theFeatures, File theDownloadDir) throws IOException, Exception
	{
		ArrayList<String> headers = null;
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theFile.getFileObj(theDownloadDir, true).toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			String line = br.readLine();
			while(null!=line)
			{
				// TODO: add sanity check between file contents UUID and file name UUID
				// no known comments. but htseq.counts files have no headers
				if (!line.startsWith("#"))
				{
					// htseq.counts files have no headers
					if ((null==headers)&&(!line.startsWith("ENSG")))
					{
						headers = new ArrayList<>();
						headers.addAll(Arrays.asList(line.split("\t", -1)));
					}
					else 
					{
						if (null==headers)
						{
							headers = new ArrayList<>();
							headers.add("gene_id");
							headers.add("raw_count");
						}
						String [] splitted = line.split("\t", -1);
						String feature = GDCAPI.getColumnWithAlt("gene_id", "gene", "isoform_id", splitted, headers);
						feature = cleanFeature(feature);
						// leave out features that start with __ as not useful
						if (!feature.startsWith("__"))
						{
							theFeatures.add(feature);
						}
						else
						{
							//GDCAPI.printLn("RnaseqTXT::run skip feature=" + feature);
						}
					}
				}
				line = br.readLine();
			}
		}
		catch(IOException exp)
		{
			throw new Exception("For file: " + theFile, exp);
		}
		catch(Exception exp)
		{
			throw new Exception("For file: " + theFile, exp);
		}
	}
	
	static public void run(GDCFile theFile, Matrix theMatrix, File theDownloadDir) throws IOException, Exception
	{
		String barcode = ((Sample)(theFile.mSamples.firstEntry().getValue())).mBarcode;
		ArrayList<String> headers = null;
		//GDCAPI.printLn("RnaseqTXT::run for matrix theMatrix=" + theMatrix);
		//GDCAPI.printLn("RnaseqTXT::run for matrix theFile=" + theFile.mName);
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theFile.getFileObj(theDownloadDir, true).toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			String line = br.readLine();
			while(null!=line)
			{
				// TODO: add sanity check between file contents UUID and file name UUID
				// no known comments. but htseq.counts files have no headers
				if (!line.startsWith("#"))
				{
					// htseq.counts files have no headers
					if ((null==headers)&&(!line.startsWith("ENSG")))
					{
						headers = new ArrayList<>();
						headers.addAll(Arrays.asList(line.split("\t", -1)));
					}
					else 
					{
						if (null==headers)
						{
							headers = new ArrayList<>();
							headers.add("gene_id");
							headers.add("raw_count");
						}
						String [] splitted = line.split("\t", -1);
						String feature = GDCAPI.getColumnWithAlt("gene_id", "gene", "isoform_id", splitted, headers);
						double value = Double.parseDouble(GDCAPI.getColumnWithAlt("raw_count", "raw_counts", null, splitted, headers));
						feature = cleanFeature(feature);
						// leave out features that start with __ as not useful
						if (!feature.startsWith("__"))
						{
							theMatrix.setValue(barcode, feature, value);
						}
						else
						{
							GDCAPI.printLn("RnaseqTXT::run skip set feature=" + feature);
						}
					}
				}
				line = br.readLine();
			}
		}
		catch(IOException exp)
		{
			GDCAPI.printErr("For file: " + theFile, exp);
			throw new Exception("For file: " + theFile, exp);
		}
		catch(Exception exp)
		{
			GDCAPI.printErr("For file: " + theFile, exp);
			throw new Exception("For file: " + theFile, exp);
		}
	}
}
