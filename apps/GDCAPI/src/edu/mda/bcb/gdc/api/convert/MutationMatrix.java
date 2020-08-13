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
import edu.mda.bcb.gdc.api.data.GDCFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class MutationMatrix
{

	private MutationMatrix()
	{
	}

	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	static public void collectFeatures(File theDownloadDir, GDCFile theGDCfile, SortedSet<String> theFeatures) throws Exception
	{
		GDCAPI.printLn("MutationMatrix::collectFeatures iterate data files");
		collectFeaturesFromFile(theGDCfile.getFileObj(theDownloadDir, true).toPath(), theFeatures);
	}

	static public void collectFeaturesFromFile(Path theInputFile, SortedSet<String> theFeatures) throws Exception
	{
		ArrayList<String> headers = null;
		long lineNo = 0;
		// have to use windows-1252 charset due to weird non-UTF-8 characters in file
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theInputFile, Charset.availableCharsets().get("windows-1252")))
		{
			String line = br.readLine();
			lineNo = 1;
			while (null != line)
			{
				// skip extra legacy header line
				// TODO: add sanity check between file contents UUID and file name UUID
				if (!line.startsWith("#"))
				{
					String[] splitted = line.split("\t", -1);
					if (null == headers)
					{
						headers = new ArrayList<>();
						headers.addAll(Arrays.asList(splitted));
						headers = MutationMAF.renameHeadersAsNeeded(headers);
					}
					else
					{
						String gene = GDCAPI.getColumn("Gene", splitted, headers);
						theFeatures.add(gene);
					}
				}
				lineNo += 1;
				line = br.readLine();
			}
		}
		catch (IOException exp)
		{
			throw new Exception("In file: " + theInputFile + " at " + lineNo, exp);
		}
		catch (Exception exp)
		{
			throw new Exception("In file: " + theInputFile + " at " + lineNo, exp);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	static public void processDirectory(File theDownloadDir, GDCFile theGDCfile, 
			String[] theRequiredBuilds, File theConvertDir) throws Exception
	{
		File outFile = new File(theConvertDir, "matrix_data.tsv");
		MutationMatrix.run(theGDCfile.getFileObj(theDownloadDir, true), outFile, theRequiredBuilds);
	}

	static public void addCount(String theBarcode, String theFeature, Map<String, Map<String, Integer>> theBarcodeToFeatureToCount) throws Exception
	{
		Map<String, Integer> featureToCount = theBarcodeToFeatureToCount.get(theBarcode);
		if (null == featureToCount)
		{
			featureToCount = new TreeMap<>();
		}
		Integer count = featureToCount.get(theFeature);
		if (null == count)
		{
			count = 0;
		}
		count += 1;
		featureToCount.put(theFeature, count);
		theBarcodeToFeatureToCount.put(theBarcode, featureToCount);
	}

	static public void run(File theInFile, File theOutFile, String[] theRequiredBuilds) throws IOException, Exception
	{
		ArrayList<String> headers = null;
		TreeSet<String> features = new TreeSet<>();
		Map<String, Map<String, Integer>> barcodeToFeatureToCount = new TreeMap<>();
		// have to use windows-1252 charset due to weird non-UTF-8 characters in file
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theInFile.toPath(), Charset.availableCharsets().get("windows-1252")))
		{
			String line = br.readLine();
			while (null != line)
			{
				// skip extra legacy header line
				// TODO: add sanity check between file contents UUID and file name UUID
				if (!line.startsWith("#"))
				{
					String[] splitted = line.split("\t", -1);
					if (null == headers)
					{
						headers = new ArrayList<>();
						headers.addAll(Arrays.asList(splitted));
						headers = MutationMAF.renameHeadersAsNeeded(headers);
					}
					else
					{
						String build = GDCAPI.getColumn("NCBI_Build", splitted, headers);
						if (MutationMAF.checkBuilds(build, theRequiredBuilds))
						{
							String gene = GDCAPI.getColumn("Gene", splitted, headers);
							String barcode = GDCAPI.getColumn("Tumor_Sample_Barcode", splitted, headers);
							String variant = GDCAPI.getColumn("Variant_Classification", splitted, headers);
							if (!variant.toLowerCase().equals("silent"))
							{
								features.add(gene);
								addCount(barcode, gene, barcodeToFeatureToCount);
							}
						}
					}
				}
				line = br.readLine();
			}
		}
		catch (IOException exp)
		{
			throw new Exception("For file: " + theInFile, exp);
		}
		catch (Exception exp)
		{
			throw new Exception("For file: " + theInFile, exp);
		}
		// TODO: any MutationMatrix sanity checks to add?
		GDCAPI.writeMatrixFileInt(theOutFile, features, barcodeToFeatureToCount);
	}
}
