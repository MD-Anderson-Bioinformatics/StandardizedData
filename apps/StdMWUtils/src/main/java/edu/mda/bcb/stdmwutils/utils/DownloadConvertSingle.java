/*
 *  Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
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
package edu.mda.bcb.stdmwutils.utils;

import edu.mda.bcb.samval.SamplesValidationUtil;
import edu.mda.bcb.samval.matrix.Builder;
import edu.mda.bcb.samval.matrix.Matrix;
import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.StdMwException;
import edu.mda.bcb.stdmwutils.mwdata.Analysis;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.std.ZipData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Tod-Casasent
 */
public class DownloadConvertSingle
{
	public Analysis mAnalysis = null;
	public AnalysisUtil mAnalysisUtil = null;
	public RefMetUtil mRefMetUtil = null;
	public OtherIdsUtil mOtherIdsUtil = null;
	public MetaboliteUtil mMetaboliteUtil = null;
	
	public DownloadConvertSingle(Analysis theAnalysis, AnalysisUtil theAnalysisUtil, RefMetUtil theRefMetUtil, OtherIdsUtil theOtherIdsUtil, MetaboliteUtil theMetaboliteUtil)
	{
		mAnalysis = theAnalysis;
		mAnalysisUtil = theAnalysisUtil;
		mRefMetUtil = theRefMetUtil;
		mOtherIdsUtil = theOtherIdsUtil;
		mMetaboliteUtil = theMetaboliteUtil;
	}
	
	public String downloadLocation()
	{
		// Do not use path, since it is exposed in the web app
		return mAnalysis.hash;
	}
	
	public String dAndC() throws IOException, NoSuchAlgorithmException, MalformedURLException, StdMwException, Exception
	{
		File zip = new File(getZipDir(true), "MWB_" + mAnalysis.study_id + "_" + mAnalysis.analysis_id + ".zip");
		StdMwDownload.printLn("dAndC zip=" + zip);
		if (!zip.exists())
		{
			StdMwDownload.printLn("dAndC download it " + zip);
			downloadDataOptions();
			StdMwDownload.printLn("dAndC convert it " + zip);
			convertDataOptions(); 
			StdMwDownload.printLn("dAndC cleanup " + zip);
			cleanupDataOptions();
		}
		StdMwDownload.printLn("dAndC zip exists -- get download location");
		return downloadLocation();
	}
	
	protected File getZipDir(boolean theMkFlag)
	{
		File dataDir = new File(MWUrls.M_MWB_TEMP);
		if (theMkFlag)
		{
			dataDir.mkdir();
		}
		File stDir = new File(dataDir, mAnalysis.study_hash);
		if (theMkFlag)
		{
			stDir.mkdir();
		}
		File dldDir = new File(stDir, mAnalysis.hash);
		if (theMkFlag)
		{
			dldDir.mkdir();
		}
		return dldDir;
	}
	
	protected void postDownload(String theGood, File theFile, File theUrl, String theTimestamp, OpenOption[] theOptions) throws IOException
	{
		if (null==theGood)
		{
			if (theFile.exists())
			{
				theFile.delete();
			}
			if (theUrl.exists())
			{
				theUrl.delete();
			}
		}
		else
		{
			java.nio.file.Files.write(theUrl.toPath(), 
					(StdMwDownload.getVersion() + "\t" + theTimestamp + "\t" + StdMwDownload.getTimestamp() + "\t" + theGood).getBytes(), 
					theOptions);
		}
	}
	
	protected void convertBatchOptions(File theOldBatch, File theNewBatch, File theMatrixData,
			String theOriginalColumn, String theNewColumn) throws Exception
	{
		boolean noRectangleFlag = false;
		Matrix batches = new Builder(theOldBatch.getAbsolutePath())
				.allowNonRectangle(noRectangleFlag)
				.build();
		Matrix matrix = new Builder(theMatrixData.getAbsolutePath()).build();
		Matrix.filterMatrix(matrix, batches.getRowSet(), 1);
		SamplesValidationUtil.createMissingBatchEntries(batches, matrix.getColumns());
		batches.sortRows();
		batches.sortColumns();
		batches.write(theNewBatch.getAbsolutePath(), true, theOriginalColumn, theNewColumn);
	}

	protected void downloadDataOptions() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		String timestamp = StdMwDownload.getTimestamp();
		File dldDir = getZipDir(true);
		// download all versions of data (Raw, Drop Class, Merge Sample-Class)
		OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
		File rawFile = new File(dldDir, "raw_data.tsv");
		File rawUrl = new File(dldDir, "raw_url.tsv");
		String rawGood = null;
		try(OutputStream out = java.nio.file.Files.newOutputStream(rawFile.toPath(), options))
		{
			rawGood = DatatableUtil.getDatatableRaw(out, mAnalysis.analysis_id);
		}
		finally
		{
			postDownload(rawGood, rawFile, rawUrl, timestamp, options);
		}
		File mergeFile = new File(dldDir, "merge_data.tsv");
		File mergeUrl = new File(dldDir, "merge_url.tsv");
		String mergeGood = null;
		try(OutputStream out = java.nio.file.Files.newOutputStream(mergeFile.toPath(), options))
		{
			mergeGood = DatatableUtil.getDatatableMSC(out, mAnalysis.analysis_id);
		}
		finally
		{
			postDownload(mergeGood, mergeFile, mergeUrl, timestamp, options);
		}
		File dropFile = new File(dldDir, "drop_data.tsv");
		File dropUrl = new File(dldDir, "drop_url.tsv");
		String dropGood = null;
		try(OutputStream out = java.nio.file.Files.newOutputStream(dropFile.toPath(), options))
		{
			dropGood = DatatableUtil.getDatatableDC(out, mAnalysis.analysis_id);
		}
		finally
		{
			postDownload(dropGood, dropFile, dropUrl, timestamp, options);
		}
		// get Batch/Factors and Metabolites
		File batchFile = new File(dldDir, "batch_factors.tsv");
		try(OutputStream out = java.nio.file.Files.newOutputStream(batchFile.toPath(), options))
		{
			FactorUtil.getBatchesTSV(out, mAnalysis.study_id);
		}
		File metaFile = new File(dldDir, "metabolites.tsv");
		try(OutputStream out = java.nio.file.Files.newOutputStream(metaFile.toPath(), options))
		{
			MetaboliteMapUtil mmu = new MetaboliteMapUtil(mMetaboliteUtil, mRefMetUtil, mOtherIdsUtil);
			mmu.streamTsv(out, mAnalysis.analysis_id);
		}
	}
	
	protected void convertDataOptions() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException, Exception
	{
		File dldDir = getZipDir(false);
		// files containing data in different format options
		File dropFile = new File(dldDir, "drop_data.tsv");
		File mergeFile = new File(dldDir, "merge_data.tsv");
		File dropUrl = new File(dldDir, "drop_url.tsv");
		File mergeUrl = new File(dldDir, "merge_url.tsv");
		// determine which file works
		File useMe = null;
		File copyMe = null;
		boolean merge = false;
		if (dropFile.exists())
		{
			StdMwDownload.printLn("Use drop class for " + mAnalysis.analysis_id + " -- " + mAnalysis.study_id);
			useMe = dropFile;
			copyMe = dropUrl;
		}
		else
		{
			if (mergeFile.exists())
			{
				StdMwDownload.printLn("Use merge class for " + mAnalysis.analysis_id + " -- " + mAnalysis.study_id);
				useMe = mergeFile;
				copyMe = mergeUrl;
				merge = true;
			}
			else
			{
				StdMwDownload.printWarn("No data file for " + mAnalysis.analysis_id + " -- " + mAnalysis.study_id);
			}
		}
		if (null==useMe)
		{
			StdMwDownload.printWarn("No usable data file for " + mAnalysis.analysis_id + " -- " + mAnalysis.study_id);
			// set convert failed
			throw new StdMwException("Download/convert failed::" + "No usable data file for " + mAnalysis.analysis_id + " -- " + mAnalysis.study_id);
		}
		else
		{
			// copy to matrix data and download time
			FileUtils.copyFile(useMe, new File(dldDir, "matrix.tsv"));
			FileUtils.copyFile(copyMe, new File(dldDir, "download.tsv"));
			// create batches.tsv
			// TODO: true==merge means sample and class were merged
			if (true==merge)
			{
				throw new StdMwException("Merge handle is true for " + mAnalysis.analysis_id + " -- " + mAnalysis.study_id + " this is not yet implemented");
			}
			File batchFile = new File(dldDir, "batch_factors.tsv");
			if (batchFile.exists())
			{
				File batchesFile = new File(dldDir, "batches.tsv");
				try
				{
					convertBatchOptions(batchFile, batchesFile, new File(dldDir, "matrix.tsv"), "Samples", "Sample");
				}
				catch(Exception exp)
				{
					StdMwDownload.printErr("Error converting batch data file for " + mAnalysis.analysis_id + " -- " + mAnalysis.study_id, exp);
					throw exp;
				}
				try
				{
					File rowColTypeFile = new File(dldDir, "row_col_types.tsv");
					File ngchmLinkMapFile = new File(dldDir, "ngchm_link_map.tsv");
					File metabolitesFile = new File(dldDir, "metabolites.tsv");
					File metaboliteMapFile = new File(MWUrls.M_MWB_CACHE, "metabolite_map.tsv");
					convertLinkOuts(rowColTypeFile, ngchmLinkMapFile, new File(dldDir, "matrix.tsv"), metabolitesFile, metaboliteMapFile);
				}
				catch(Exception exp)
				{
					StdMwDownload.printErr("Error converting link outs for " + mAnalysis.analysis_id + " -- " + mAnalysis.study_id, exp);
					throw exp;
				}
			}
			else
			{
				StdMwDownload.printWarn("No batch data file for " + mAnalysis.analysis_id + " -- " + mAnalysis.study_id);
			}
		}
	}
	
	protected boolean convertLinkOuts(File theRowColTypeFileOut, File theNgchmLinkMapFileOut, 
			File theMatrixFileIn, File theMetabolitesFileIn, File theMetaboliteMapFileIn) throws Exception
	{
		OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
		boolean wrote = false;
		String row_type = null; // link outs
		String col_type = "bio.metabolite.MW.name"; // bio.metabolite.MW.name
		String metaboliteIdCol = null;
		ArrayList<String> headerOrder = new ArrayList<>();
		// read theMetaboliteMapFileIn for row_type (link out types), metaboliteIdCol, and headers for new complex feature label
		try(BufferedReader br = java.nio.file.Files.newBufferedReader(theMetaboliteMapFileIn.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			// headers
			String line = br.readLine();
			int linkType = -1;
			int headerName = -1;
			{
				ArrayList<String> headers = new ArrayList<>();
				headers.addAll(Arrays.asList(line.split("\t", -1)));
				linkType = headers.indexOf("link_type");
				headerName = headers.indexOf("header_name");
			}
			// first summary line
			line = br.readLine();
			while(null!=line)
			{
				String [] splitted = line.split("\t", -1);
				String linkTypeVal = splitted[linkType];
				String headerNameVal = splitted[headerName];
				if (null==row_type)
				{
					row_type = linkTypeVal;
					metaboliteIdCol = headerNameVal;
				}
				else
				{
					row_type = row_type + ".|." + linkTypeVal;
					headerOrder.add(headerNameVal);
				}
				line = br.readLine();
			}
		}
		// write theRowColTypeFileOut
		try(BufferedWriter bw = java.nio.file.Files.newBufferedWriter(theRowColTypeFileOut.toPath(), Charset.availableCharsets().get("UTF-8"), options))
		{
			bw.write(row_type);
			bw.newLine();
			bw.write(col_type);
			bw.newLine();
		}
		// read theMatrixFileIn for list of metabolites (features)
		TreeSet<String> features = new TreeSet<>();
		try(BufferedReader br = java.nio.file.Files.newBufferedReader(theMetaboliteMapFileIn.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			// skip headers
			String line = br.readLine();
			line = br.readLine();
			while(null!=line)
			{
				String [] splitted = line.split("\t", -1);
				if (splitted.length>1)
				{
					features.add(splitted[0]);
				}
				line = br.readLine();
			}
		}
		// for each feature, get complex mapping string for linkouts
		try(BufferedWriter bw = java.nio.file.Files.newBufferedWriter(theNgchmLinkMapFileOut.toPath(), Charset.availableCharsets().get("UTF-8"), options))
		{
			bw.write("feature\tlinkout");
			bw.newLine();
			try(BufferedReader br = java.nio.file.Files.newBufferedReader(theMetabolitesFileIn.toPath(), Charset.availableCharsets().get("UTF-8")))
			{
				// headers
				String line = br.readLine();
				HashMap<String, Integer> headerToIndex = new HashMap<>();
				{
					ArrayList<String> headers = new ArrayList<>();
					headers.addAll(Arrays.asList(line.split("\t", -1)));
					headerToIndex.put(metaboliteIdCol, headers.indexOf(metaboliteIdCol));
					for (String headerCol : headerOrder)
					{
						headerToIndex.put(headerCol, headers.indexOf(headerCol));
					}
				}
				// track processed feature names
				TreeMap<String, Integer> featureToCount = new TreeMap<>();
				// first summary line
				line = br.readLine();
				while(null!=line)
				{
					String [] splitted = line.split("\t", -1);
					// feature
					String featureRowLabel = splitted[headerToIndex.get(metaboliteIdCol)];
					Integer count = featureToCount.get(featureRowLabel);
					if (null==count)
					{
						count = 0;
					}
					else
					{
						count = count + 1;
					}
					featureToCount.put(featureRowLabel, count);
					if (count>0)
					{
						featureRowLabel = featureRowLabel + "_" + count;
					}
					bw.write(featureRowLabel);
					bw.write("\t");
					bw.write(featureRowLabel);
					for (String headerCol : headerOrder)
					{
						bw.write("|");
						bw.write(splitted[headerToIndex.get(headerCol)]);
					}
					bw.newLine();
					line = br.readLine();
				}
			}
		}
		wrote = true;
		return wrote;
	}

	
	protected File cleanupDataOptions() throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		File dldDir = getZipDir(false);
		StdMwDownload.printLn("cleanupDataOptions dldDir=" + dldDir);
		// download all versions of data (Raw, Drop Class, Merge Sample-Class)
		File rawFile = new File(dldDir, "raw_data.tsv");
		if (rawFile.exists())
		{
			rawFile.delete();
			File rawUrl = new File(dldDir, "raw_url.tsv");
			if (rawUrl.exists())
			{
				rawUrl.delete();
			}
		}
		File mergeFile = new File(dldDir, "merge_data.tsv");
		if (mergeFile.exists())
		{
			mergeFile.delete();
			File mergeUrl = new File(dldDir, "merge_url.tsv");
			if (mergeUrl.exists())
			{
				mergeUrl.delete();
			}
		}
		File dropFile = new File(dldDir, "drop_data.tsv");
		if (dropFile.exists())
		{
			dropFile.delete();
			File dropUrl = new File(dldDir, "drop_url.tsv");
			if (dropUrl.exists())
			{
				dropUrl.delete();
			}
		}
		// Do not delete -- keep in case we want some of these other factors
		//File batchFile = new File(dldDir, "batch_factors.tsv");
		//if (batchFile.exists())
		//{
		//	batchFile.delete();
		//}
		// zip directory
		File zipFile = new File(dldDir, ("MWB_" + mAnalysis.study_id + "_" + mAnalysis.analysis_id + ".zip"));
		File [] files = ZipData.zip(dldDir, zipFile, true);
		for (File myF : files)
		{
			if (myF.exists())
			{
				myF.delete();
			}
		}
		return zipFile;
	}
	
}
