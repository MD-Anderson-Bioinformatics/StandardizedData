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
public class MethylationTXT
{	
//	static public String getDiseaseFromFilename(String theFilename)
//	{
//		String[] splitted = theFilename.split("_", -1);
//		splitted = splitted[1].split("\\.", -1);
//		return splitted[0];
//	}

//	static public String getBarcodeFromFilename(String theFilename)
//	{
//		// trim any numbers off end of file, in case there was a duplicate filename
//		theFilename = theFilename.replaceAll("\\d+$", "");
//		// current ends with .barcode.gdc_hg38.txt and legacy ends with .barcode.txt 
//		// both start with <processer>_<DISEASE>.
//		theFilename = theFilename.replace(".txt", "");
//		theFilename = theFilename.replace(".gdc_hg38", "");
//		String[] splitted = theFilename.split("\\.", -1);
//		return splitted[splitted.length - 1];
//	}
	
//	static public String getProjectFromBarcode(String theBarcode)
//	{
//		String[] splitted = theBarcode.split("-", -1);
//		return splitted[0];
//	}

	static public void processDirectory(WorkflowData theManifest, final File theDownloadDir, 
			File theConvertDir, File theBiospecimenDir, File theClinicalDir, boolean theNoXYFlag) throws IOException, Exception
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
			GDCAPI.printWarn("MethylationTXT No files to process");
			return;
		}
		TreeSet<String> barcodes = theManifest.getBarcodes(gdcArray);
		////////////////////////////////////////////////////////////////////////
		GDCAPI.printLn("MethylationTXT evaluating");
		List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
		////////////////////////////////////////////////////////////////////////
		//// collect probe names
		////////////////////////////////////////////////////////////////////////
		// note this solution changes the parallelism settings for the entire system
		// so if you have multiple parallel operations going on this could do weird things
		GDCAPI.printLn("PARALLEL-THREADS = 5");
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "5");
		SortedSet<MethylationProbe> probeList = Collections.synchronizedSortedSet(new TreeSet<>());
		SortedSet<String> probeNameSet = Collections.synchronizedSortedSet(new TreeSet<>());
		AtomicLong counter = new AtomicLong(0);
		{
			GDCAPI.printLn("MethylationTXT::processDirectory collect probes");
			Arrays.asList(gdcArray)
					.parallelStream()
					.forEach((GDCFile loopFile) ->
					{
						long count = counter.incrementAndGet();
						if (0 == count % 100)
						{
							GDCAPI.printLn("MethylationTXT probes counter = " + count + " of " + gdcArray.length);
						}
						try
						{
							MethylationTXT.getProbes(loopFile, probeList, probeNameSet, theDownloadDir, theNoXYFlag);
						}
						catch (Exception exp)
						{
							errors.add(exp);
						}
					});
		}
		TreeSet<String> probeNames = new TreeSet<>();
		for (MethylationProbe bs : probeList)
		{
			probeNames.add(bs.mProbe);
		}
		////////////////////////////////////////////////////////////////////////
		//// process 
		////////////////////////////////////////////////////////////////////////
		// note this solution changes the parallelism settings for the entire system
		// so if you have multiple parallel operations going on this could do weird things
		GDCAPI.printLn("PARALLEL-THREADS = 5");
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "5");
		Matrix newMatrix = new Matrix(barcodes, probeNames, false);
		counter.set(0);
		Arrays.asList(gdcArray)
					.parallelStream()
					.forEach((GDCFile loopFile) ->
				{
					long count = counter.incrementAndGet();
					if (0 == count % 100)
					{
						GDCAPI.printLn("MethylationTXT diseases counter = " + count + " of " + gdcArray.length);
					}
					try
					{
						MethylationTXT.run(loopFile, probeNames, newMatrix, theDownloadDir);
					}
					catch (Exception exp)
					{
						errors.add(exp);
					}
				});
		for (Exception err : errors)
		{
			GDCAPI.printErr("MethylationTXT::processDirectory errors " + err.getMessage(), err);
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
			GDCAPI.printErr("MethylationTXT::processDirectory exception caught" + exp.getMessage(), exp);
		}
	}

	private MethylationTXT()
	{
	}

	static public void getProbes(GDCFile theFile, SortedSet<MethylationProbe> theProbes, 
		SortedSet<String> theProbeNames, File theDownloadDir, boolean theNoXYFlag) throws IOException, Exception
	{
		ArrayList<String> headers = null;
		long count = 0;
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theFile.getFileObj(theDownloadDir, true).toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			String line = br.readLine();
			while (null != line)
			{
				count += 1;
				// skip extra legacy header line
				if (!line.startsWith("Hybridization REF"))
				{
					if (null == headers)
					{
						headers = new ArrayList<>();
						headers.addAll(Arrays.asList(line.split("\t", -1)));
					}
					else
					{
						String[] splitted = line.split("\t", -1);
						String probe = GDCAPI.getColumn("Composite Element REF", splitted, headers);
						if (!theProbeNames.contains(probe))
						{
							theProbeNames.add(probe);
							String chromosome = GDCAPI.getColumn("Chromosome", splitted, headers).replace("chr", "");
							if ("NaN".equals(chromosome))
							{
								chromosome = "NA";
							}
							String[] geneSymbols = GDCAPI.getColumn("Gene_Symbol", splitted, headers).split(";", -1);
							if ("NaN".equals(geneSymbols[0]))
							{
								geneSymbols[0] = "NA";
							}
							String start = GDCAPI.getColumn("Start", splitted, headers);
							String end = GDCAPI.getColumn("End", splitted, headers);
							if ("".equals(start))
							{
								start = GDCAPI.getColumn("Genomic_Coordinate", splitted, headers);
							}
							if ("".equals(end))
							{
								end = "NA";
							}
							if ((false==theNoXYFlag) || 
								((true==theNoXYFlag)&&
									(!"X".equalsIgnoreCase(chromosome))&&
									(!"Y".equalsIgnoreCase(chromosome))&&
									(!"NA".equalsIgnoreCase(chromosome))))
							{
								theProbes.add(new MethylationProbe(probe, chromosome, start, end, geneSymbols));
							}
						}
					}
				}
				line = br.readLine();
			}
		}
		catch (IOException exp)
		{
			throw new Exception("For line=" + count + " and file: " + theFile.mName, exp);
		}
		catch (Exception exp)
		{
			throw new Exception("For line=" + count + " and file: " + theFile.mName, exp);
		}
	}

	static public void run(GDCFile theFile, TreeSet<String> theProbeNames, Matrix theMatrix, File theDownloadDir) throws IOException, Exception
	{
		String barcode = ((Sample)(theFile.mSamples.firstEntry().getValue())).mBarcode;
		ArrayList<String> headers = null;
		long count = 0;
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theFile.getFileObj(theDownloadDir, true).toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			String line = br.readLine();
			while (null != line)
			{
				count += 1;
				// skip extra legacy header line
				if (!line.startsWith("Hybridization REF"))
				{
					if (null == headers)
					{
						headers = new ArrayList<>();
						headers.addAll(Arrays.asList(line.split("\t", -1)));
					}
					else
					{
						String[] splitted = line.split("\t", -1);
						String probe = GDCAPI.getColumn("Composite Element REF", splitted, headers);
						if (theProbeNames.contains(probe))
						{
							double betaValue = Double.parseDouble(GDCAPI.getColumn("Beta_value", splitted, headers));
							theMatrix.addValue(barcode, probe, betaValue);
						}
					}
				}
				line = br.readLine();
			}
		}
		catch (IOException exp)
		{
			throw new Exception("For line=" + count + " theBarcode=" + barcode + " and file: " + theFile.mName, exp);
		}
		catch (Exception exp)
		{
			throw new Exception("For line=" + count + " theBarcode=" + barcode + " and file: " + theFile.mName, exp);
		}
	}
}
