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

package edu.mda.bcb.gdc.api.convert;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.dataframes.Batches;
import edu.mda.bcb.gdc.api.convert.utils.Matrix;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.data.Sample;
import edu.mda.bcb.gdc.api.data.WorkflowData;
import edu.mda.bcb.gdc.api.dataframes.ClinicalDF;
import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Tod-Casasent
 */
public class GisticTXT
{
	static public void processDirectory(WorkflowData theManifest,
			final File theDownloadDir, File theConvertDir, 
			File theBiospecimenDir, File theClinicalDir, boolean theLegacyFlag) throws IOException, Exception
	{
		////////////////////////////////////////////////////////////////////////
		//// file array
		////////////////////////////////////////////////////////////////////////
		ArrayList<GDCFile> gdcFiles = new ArrayList<>();
		for (Updateable_Mixin<GDCFile> data : theManifest.mFiles.values())
		{
			gdcFiles.add((GDCFile)data);
		}
		if (gdcFiles.size()>0)
		{
			if (!theConvertDir.exists())
			{
				theConvertDir.mkdirs();
			}
		}
		else
		{
			GDCAPI.printWarn("GisticTXT No files to process");
			return;
		}
		final GDCFile [] gdcArray = gdcFiles.toArray(new GDCFile[0]);
		gdcFiles = null;
		////////////////////////////////////////////////////////////////////////
		//// collect features
		////////////////////////////////////////////////////////////////////////
		List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
		// note this solution changes the parallelism settings for the entire system
		// so if you have multiple parallel operations going on this could do weird things
		GDCAPI.printLn("PARALLEL-THREADS = 5");
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "5");
		AtomicLong counter = new AtomicLong(0);
		GDCAPI.printLn("GisticTXT::processDirectory collect features");
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
							GDCAPI.printLn("GisticTXT features counter = " + count + " of " + gdcArray.length);
						}
						ArrayList<String> headers = null;
						try (BufferedReader br = java.nio.file.Files.newBufferedReader(loopFile.getFileObj(theDownloadDir, true).toPath(), Charset.availableCharsets().get("UTF-8")))
						{
							String line = br.readLine();
							while (null!=line)
							{
								if (null==headers)
								{
									headers = new ArrayList<>();
									headers.addAll(Arrays.asList(line.split("\t", -1)));
								}
								else
								{
									String [] splitted = line.split("\t", -1);
									String feature = GDCAPI.getTwoColumns("Gene Symbol", "Cytoband", splitted, headers);
									features.add(feature);
								}
								line = br.readLine();
							}
						}
						catch (Exception exp)
						{
							errors.add(exp);
						}
					});
		////////////////////////////////////////////////////////////////////////
		//// collect samples
		////////////////////////////////////////////////////////////////////////
		TreeSet<String> barcodes = new TreeSet<>();
		TreeMap<String, String> uuidToBarcode = new TreeMap<>();
		Collection<Updateable_Mixin<GDCFile>> fs = theManifest.mFiles.values();
		for (Updateable_Mixin<GDCFile> myfile : fs)
		{
			GDCFile gdcFile = ((GDCFile)myfile);
			for (Updateable_Mixin<Sample> mySample : gdcFile.mSamples.values())
			{
				Sample objSample = ((Sample)mySample);
				barcodes.add(objSample.mBarcode);
				GDCAPI.printLn("GisticTXT mUUID = " + objSample.mUUID);
				//GDCAPI.printLn("GisticTXT barcode = " + objSample.mBarcode);
				uuidToBarcode.put(objSample.mUUID, objSample.mBarcode);
			}
		}
		////////////////////////////////////////////////////////////////////////
		//// process  files
		////////////////////////////////////////////////////////////////////////
		// note this solution changes the parallelism settings for the entire system
		// so if you have multiple parallel operations going on this could do weird things
		GDCAPI.printLn("PARALLEL-THREADS = 5");
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "5");
		counter.set(0);
		Matrix newMatrix = new Matrix(barcodes, new TreeSet<String>(features), true);
		Arrays.asList(gdcArray)
				.parallelStream()
				.forEach((GDCFile loopFile) ->
				{
					long count = counter.incrementAndGet();
					if (0 == count % 100)
					{
						GDCAPI.printLn("GisticTXT diseases counter = " + count + " of " + gdcArray.length);
					}
					try
					{
						ArrayList<String> headers = null;
						try (BufferedReader br = java.nio.file.Files.newBufferedReader(loopFile.getFileObj(theDownloadDir, true).toPath(), Charset.availableCharsets().get("UTF-8")))
						{
							String line = br.readLine();
							while (null!=line)
							{
								if (null==headers)
								{
									headers = new ArrayList<>();
									headers.addAll(Arrays.asList(line.split("\t", -1)));
								}
								else
								{
									String [] splitted = line.split("\t", -1);
									String feature = GDCAPI.getTwoColumns("Gene Symbol", "Cytoband", splitted, headers);
									for (int index=3;index<headers.size();index++)
									{
										String uuid = headers.get(index);
										String barcode = uuidToBarcode.get(uuid);
										if (null!=barcode)
										{
											//GDCAPI.printLn("GisticTXT processing uuid '" + uuid + "'");
											int value = Integer.parseInt(splitted[index]);
											newMatrix.setValue(barcode, feature, value);
										}
										else
										{
											GDCAPI.printWarn("GisticTXT processing uuid not found '" + uuid + "'");
										}
									}
								}
								line = br.readLine();
							}
						}
					}
					catch (Exception exp)
					{
						errors.add(exp);
					}
				});
		for (Exception err : errors)
		{
			if (null!=err)
			{
				GDCAPI.printErr("GisticTXT::processDirectory errors " + err.getMessage(), err);
			}
		}
		try
		{
			GDCAPI.writeMatrixFileAsInt(new File(theConvertDir, "matrix_data.tsv"), newMatrix);
			// Batches.tsv file
			Batches batches = new Batches(gdcArray, theConvertDir, theManifest.mProgram, theManifest.mProject);
			batches.writeBatchFile(theBiospecimenDir);
			ClinicalDF clinicalDF = new ClinicalDF(gdcArray, theConvertDir, theManifest.mProgram, theManifest.mProject);
			clinicalDF.writeClinicalFile(theClinicalDir);
		}
		catch (Exception exp)
		{
			GDCAPI.printErr("GisticTXT::processDirectory exception caught" + exp.getMessage(), exp);
		}
	}
}
