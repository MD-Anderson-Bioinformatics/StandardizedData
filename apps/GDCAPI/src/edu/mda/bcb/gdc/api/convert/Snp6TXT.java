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
import edu.mda.bcb.gdc.api.convert.utils.GeneMap;
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
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Tod-Casasent
 */
public class Snp6TXT
{
	static public void processDirectory(WorkflowData theManifest,
			final File theDownloadDir, File theConvertDir, File theBiospecimenDir, File theClinicalDir, File theGeneFile,
			boolean theLegacyFlag, boolean theNoXYFlag) throws IOException, Exception
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
			GDCAPI.printWarn("Snp6TXT No files to process");
			return;
		}
		////////////////////////////////////////////////////////////////////////
		//// getBarcodes
		////////////////////////////////////////////////////////////////////////
		GDCAPI.printLn("Snp6TXT getBarcodes");
		TreeSet<String> barcodes = theManifest.getBarcodes(gdcArray);
		////////////////////////////////////////////////////////////////////////
		//// collect features
		////////////////////////////////////////////////////////////////////////
		GDCAPI.printLn("Snp6TXT collect features");
		GeneMap geneMap = new GeneMap();
		geneMap.load(theGeneFile);
		////////////////////////////////////////////////////////////////////////
		//// build matrix
		////////////////////////////////////////////////////////////////////////
		GDCAPI.printLn("Snp6TXT build matrix");
		List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
		AtomicLong counter = new AtomicLong(0);
		Matrix newMatrix = new Matrix(barcodes, new TreeSet<String>(Arrays.asList(geneMap.getGeneList())), false);
		// note this solution changes the parallelism settings for the entire system
		// so if you have multiple parallel operations going on this could do weird things
		GDCAPI.printLn("PARALLEL-THREADS = 10");
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "10");
		counter.set(0);
		Arrays.asList(gdcArray)
				.parallelStream()
				.forEach((GDCFile loopFile) ->
				{
					long count = counter.incrementAndGet();
					if (0 == count % 100)
					{
						GDCAPI.printLn("Snp6TXT::processDirectory counter = " + count + " of " + gdcArray.length);
					}
					try
					{
						Snp6TXT.run(loopFile, newMatrix, geneMap, theNoXYFlag, theDownloadDir);
					}
					catch (Exception exp)
					{
						errors.add(exp);
					}
				});
		for (Exception err : errors)
		{
			GDCAPI.printErr("Snp6TXT::processDirectory errors " + err.getMessage(), err);
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
			GDCAPI.printErr("Snp6TXT::processDirectory exception caught" + exp.getMessage(), exp);
		}
	}

	private Snp6TXT()
	{
	}
	
	static public long parseSnp6Number(String theString)
	{
		// necessary, since sometimes the SNP6 data is a long, and sometimes a scientific number, technically a double
		long result = Long.MIN_VALUE;
		try
		{
			result = Long.parseLong(theString);
		}
		catch(Exception exp)
		{
			result = Double.valueOf(theString).longValue();
		}
		return result;
	}

	static public void run(GDCFile theFile, Matrix theMatrix, GeneMap theGeneMap, boolean theNoXYFlag, File theDownloadDir) throws IOException, Exception
	{
		String barcode = ((Sample)(theFile.mSamples.firstEntry().getValue())).mBarcode;
		ArrayList<String> headers = null;
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theFile.getFileObj(theDownloadDir, true).toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			String line = br.readLine();
			while (null != line)
			{
				// TODO: add sanity check between file contents UUID and file name UUID
				// no known comments. but htseq.counts files have no headers
				if (!line.startsWith("#"))
				{
					// htseq.counts files have no headers
					if (null == headers)
					{
						headers = new ArrayList<>();
						headers.addAll(Arrays.asList(line.split("\t", -1)));
					}
					else
					{
						String[] splitted = line.split("\t", -1);
						String chromosome = GDCAPI.getColumn("Chromosome", splitted, headers);
						if ((false==theNoXYFlag)||((!"y".equalsIgnoreCase(chromosome))&&(!"x".equalsIgnoreCase(chromosome))))
						{
							long start = Math.min(parseSnp6Number(GDCAPI.getColumn("Start", splitted, headers)), parseSnp6Number(GDCAPI.getColumn("End", splitted, headers)));
							long end = Math.max(parseSnp6Number(GDCAPI.getColumn("Start", splitted, headers)), parseSnp6Number(GDCAPI.getColumn("End", splitted, headers)));
							//long numProbes = Long.parseLong(MethylationTXT.getColumn("Num_Probes", splitted, headers));
							double segmentMean = Double.parseDouble(GDCAPI.getColumn("Segment_Mean", splitted, headers));
							//
							for (String gene : theGeneMap.getGeneList())
							{
								theMatrix.addValue(barcode, gene, 0.0);
								String geneChromosome = theGeneMap.getGeneChromosome(gene);
								long geneStart = Math.min(theGeneMap.getGeneStart(gene), theGeneMap.getGeneEnd(gene));
								long geneEnd = Math.max(theGeneMap.getGeneStart(gene), theGeneMap.getGeneEnd(gene));
								if (chromosome.equals(geneChromosome))
								{
									// if there is any overlap
									long overlap = Math.max(0, Math.min(end, geneEnd) - Math.max(start, geneStart));
									if (overlap>0)
									{
										overlap += 1;
										long partsize = Math.abs(start - end) + 1;
										long geneSize = Math.abs(geneEnd - geneStart) + 1;
										if (overlap > geneSize)
										{
											overlap = geneSize;
										}
										if (overlap > partsize)
										{
											overlap = partsize;
										}
										double ov = overlap;
										double gs = geneSize;
										double newScore = (ov / gs) * segmentMean;
										//System.out.println(newScore + " == sample " + start + " : " + end + " : " + segmentMean + " == gene " + gene + " : " + geneStart + " : " + geneEnd);
										theMatrix.addValue(barcode, gene, newScore);
									}
								}
							}
						}
					}
				}
				line = br.readLine();
			}
		}
		catch (IOException exp)
		{
			throw new Exception("For file: " + theFile, exp);
		}
		catch (Exception exp)
		{
			throw new Exception("For file: " + theFile, exp);
		}
	}

}
