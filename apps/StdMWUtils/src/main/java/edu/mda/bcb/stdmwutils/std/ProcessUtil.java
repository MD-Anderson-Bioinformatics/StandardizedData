/*
 *  Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
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
package edu.mda.bcb.stdmwutils.std;

import edu.mda.bcb.gdc.api.indexes.JsonDataset;
import edu.mda.bcb.samval.SamplesValidationUtil;
import edu.mda.bcb.samval.matrix.Builder;
import edu.mda.bcb.samval.matrix.Matrix;
import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.StdMwException;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import edu.mda.bcb.stdmwutils.utils.DatatableUtil;
import edu.mda.bcb.stdmwutils.utils.FactorUtil;
import edu.mda.bcb.stdmwutils.utils.MetaboliteMapUtil;
import edu.mda.bcb.stdmwutils.utils.MetaboliteUtil;
import edu.mda.bcb.stdmwutils.utils.OtherIdsUtil;
import edu.mda.bcb.stdmwutils.utils.RefMetUtil;
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
public class ProcessUtil
{
	static final public String M_STATUS_NEW = "new";
	static final public String M_STATUS_DOWNLOADED = "downloaded";
	static final public String M_STATUS_CONVERTED = "converted";
	static final public String M_STATUS_FAILED = "failed";
	static final public String M_STATUS_SUCCESS = "success";
	
	static public ProcessUtil readNewestProcessFile(MetaboliteUtil theMu, RefMetUtil theRu, OtherIdsUtil theOu) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		File processIndex = new File(new File(MWUrls.M_MW_PIPELINE, "INDEX"), "mw_process.tsv");
		ProcessUtil pu = new ProcessUtil(theMu, theRu, theOu);
		pu.readProcesses(processIndex);
		return pu;
	}
	
	public TreeMap<String, ProcessEntry> mHashToProcessEntries = null;
	public MetaboliteUtil mMu = null;
	public RefMetUtil mRu = null;
	public OtherIdsUtil mOu = null;

	private ProcessUtil(MetaboliteUtil theMu, RefMetUtil theRu, OtherIdsUtil theOu)
	{
		mMu = theMu;
		mRu = theRu;
		mOu = theOu;
		mHashToProcessEntries = new TreeMap<>();
	}
	
	private void readProcesses(File theProcessIndex) throws MalformedURLException, IOException, NoSuchAlgorithmException, StdMwException
	{
		StdMwDownload.printLn("readProcesses input = " + theProcessIndex.getAbsolutePath());
		if (theProcessIndex.exists())
		{
			try(BufferedReader br = java.nio.file.Files.newBufferedReader(theProcessIndex.toPath(), Charset.availableCharsets().get("UTF-8")))
			{
				// headers
				String line = br.readLine();
				ArrayList<String> headers = new ArrayList<>();
				headers.addAll(Arrays.asList(line.split("\t", -1)));
				// first summary line
				line = br.readLine();
				while(null!=line)
				{
					ProcessEntry processEntry = ProcessEntry.getFromRowString(headers, line);
					mHashToProcessEntries.put(processEntry.mHash, processEntry);
					line = br.readLine();
				}
			}
		}
	}
	
	public void writeProcesses() throws IOException
	{
		OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
		File processIndex = new File(new File(MWUrls.M_MW_PIPELINE, "INDEX"), "mw_process.tsv");
		if (mHashToProcessEntries.size()>0)
		{
			StdMwDownload.printLn("writeProcesses - write to " + processIndex.getAbsolutePath());
			try(BufferedWriter bw = java.nio.file.Files.newBufferedWriter(processIndex.toPath(), Charset.availableCharsets().get("UTF-8"), options))
			{
				bw.write(ProcessEntry.getHeaderString());
				bw.newLine();
				StdMwDownload.printLn("writeAnalyses - iterate summaries");
				TreeSet<ProcessEntry> fullSet = new TreeSet<>(mHashToProcessEntries.values());
				for (ProcessEntry pe : fullSet)
				{
					System.out.print(".");
					bw.write(pe.getRowString());
					bw.newLine();
					bw.flush();
				}
				System.out.println(".");
				StdMwDownload.printLn("writeAnalyses - finished iterating");
			}
		}
	}
	
	public void addNewEntries(ArrayList<ProcessEntry> puList, int theSize)
	{
		int count = 0;
		for (ProcessEntry pe : puList)
		{
			if (count<theSize)
			{
				if (!mHashToProcessEntries.containsKey(pe.mHash))
				{
					mHashToProcessEntries.put(pe.mHash, pe);
					count += 1;
				}
			}
		}
	}
	
	public void processPending(String theTimestamp) throws IOException, NoSuchAlgorithmException, MalformedURLException, StdMwException, Exception
	{
		for (ProcessEntry pe : mHashToProcessEntries.values())
		{
			if (!pe.mStatus.equals(ProcessUtil.M_STATUS_FAILED))
			{
				if (!pe.mStatus.equals(ProcessUtil.M_STATUS_SUCCESS))
				{
					if (pe.mStatus.equals(ProcessUtil.M_STATUS_NEW))
					{
						// do download
						downloadDataOptions(pe, theTimestamp);
						this.writeProcesses();
					}
					if (pe.mStatus.equals(ProcessUtil.M_STATUS_DOWNLOADED))
					{
						// do convert
						convertDataOptions(pe);
						this.writeProcesses();
					}
					if (pe.mStatus.equals(ProcessUtil.M_STATUS_CONVERTED))
					{
						// set to completed
						cleanupDataOptions(pe);
						pe.mStatus = ProcessUtil.M_STATUS_SUCCESS;
						this.writeProcesses();
					}
				}
			}
		}
	}
	
	protected int convertBatchOptions(File theOldBatch, File theNewBatch, File theMatrixData,
			String theOriginalColumn, String theNewColumn) throws Exception
	{
		boolean noRectangleFlag = false;
		Matrix batches = new Builder(theOldBatch.getAbsolutePath())
				.allowNonRectangle(noRectangleFlag)
				.build();
		batches.removeNonBatches(theOriginalColumn);
		Matrix matrix = new Builder(theMatrixData.getAbsolutePath()).build();
		Matrix.filterMatrix(matrix, batches.getRowSet(), 1);
		SamplesValidationUtil.createMissingBatchEntries(batches, matrix.getColumns());
		batches.sortRows();
		batches.sortColumns();
		int col = batches.write(theNewBatch.getAbsolutePath(), true, theOriginalColumn, theNewColumn);
		return col;
	}
	
	public File getZipDir(ProcessEntry thePe)
	{
		File dataDir = new File(MWUrls.M_MW_PIPELINE, "DATA");
		// directory for this data -- multiple levels, to avoid all at top
		File anDir = new File(dataDir, thePe.mAn.hash.substring(0,2));
		File suDir = new File(anDir, thePe.mSu.hash.substring(0,2));
		File dldDir = new File(suDir, thePe.mHash);
		return dldDir;
	}
	
	public void convertDataOptions(ProcessEntry thePe) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException, Exception
	{
		File dldDir = getZipDir(thePe);
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
			StdMwDownload.printLn("Use drop class for " + thePe.mAn.analysis_id + " -- " + thePe.mSu.study_id);
			useMe = dropFile;
			copyMe = dropUrl;
		}
		else
		{
			if (mergeFile.exists())
			{
				StdMwDownload.printLn("Use merge class for " + thePe.mAn.analysis_id + " -- " + thePe.mSu.study_id);
				useMe = mergeFile;
				copyMe = mergeUrl;
				merge = true;
			}
			else
			{
				StdMwDownload.printWarn("No data file for " + thePe.mAn.analysis_id + " -- " + thePe.mSu.study_id);
			}
		}
		if (null==useMe)
		{
			StdMwDownload.printWarn("No usable data file for " + thePe.mAn.analysis_id + " -- " + thePe.mSu.study_id);
			// set convert failed
			thePe.mStatus = M_STATUS_FAILED;
		}
		else
		{
			// copy to matrix data and download time
			FileUtils.copyFile(useMe, new File(dldDir, "matrix_data.tsv"));
			FileUtils.copyFile(copyMe, new File(dldDir, "download.tsv"));
			// create batches.tsv
			// TODO: true==merge means sample and class were merged
			if (true==merge)
			{
				throw new StdMwException("Merge handle is true for " + thePe.mAn.analysis_id + " -- " + thePe.mSu.study_id + " this is not yet implemented");
			}
			File batchFile = new File(dldDir, "batch_factors.tsv");
			if (batchFile.exists())
			{
				File batchesFile = new File(dldDir, "batches.tsv");
				boolean wrote = false;
				try
				{
					int cols = convertBatchOptions(batchFile, batchesFile, new File(dldDir, "matrix_data.tsv"), "Samples", "Sample");
					if (cols>0)
					{
						wrote = true;
					}
				}
				catch(Exception exp)
				{
					wrote = false;
					StdMwDownload.printErr("Error converting batch data file for " + thePe.mAn.analysis_id + " -- " + thePe.mSu.study_id, exp);
				}
				if (true==wrote)
				{
					// set convert done
					thePe.mStatus = M_STATUS_CONVERTED;
					boolean linkouts = false;
					try
					{
						File rowColTypeFile = new File(dldDir, "row_col_types.tsv");
						File ngchmLinkMapFile = new File(dldDir, "ngchm_link_map.tsv");
						File metabolitesFile = new File(dldDir, "metabolites.tsv");
						File metaboliteMapFile = new File(MWUrls.M_MW_CACHE, "metabolite_map.tsv");
						linkouts = convertLinkOuts(rowColTypeFile, ngchmLinkMapFile, new File(dldDir, "matrix_data.tsv"), metabolitesFile, metaboliteMapFile);
					}
					catch(Exception exp)
					{
						StdMwDownload.printErr("Error converting link outs for " + thePe.mAn.analysis_id + " -- " + thePe.mSu.study_id, exp);
					}
					if (true==linkouts)
					{
						// set convert done
						thePe.mStatus = M_STATUS_CONVERTED;				
					}
					else
					{
						// set convert failed
						thePe.mStatus = M_STATUS_FAILED;
					}
				}
				else
				{
					StdMwDownload.printLn("No usable batch types found");
					// set convert failed
					thePe.mStatus = M_STATUS_FAILED;
				}
			}
			else
			{
				StdMwDownload.printWarn("No batch data file for " + thePe.mAn.analysis_id + " -- " + thePe.mSu.study_id);
				// set convert failed
				thePe.mStatus = M_STATUS_FAILED;
			}
		}
	}
	
	public void postDownload(String theGood, File theFile, File theUrl, String theTimestamp, OpenOption[] theOptions) throws IOException
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
	
	public void downloadDataOptions(ProcessEntry thePe, String theTimestamp) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		File dataDir = new File(MWUrls.M_MW_PIPELINE, "DATA");
		// directory for this data -- multiple levels, to avoid all at top
		File anDir = new File(dataDir, thePe.mAn.hash.substring(0,2));
		anDir.mkdir();
		File suDir = new File(anDir, thePe.mSu.hash.substring(0,2));
		suDir.mkdir();
		File dldDir = new File(suDir, thePe.mHash);
		dldDir.mkdir();
		try
		{
			// download all versions of data (Raw, Drop Class, Merge Sample-Class)
			OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
			File rawFile = new File(dldDir, "raw_data.tsv");
			File rawUrl = new File(dldDir, "raw_url.tsv");
			String rawGood = null;
			try(OutputStream out = java.nio.file.Files.newOutputStream(rawFile.toPath(), options))
			{
				rawGood = DatatableUtil.getDatatableRaw(out, thePe.mAn.analysis_id);
			}
			finally
			{
				postDownload(rawGood, rawFile, rawUrl, theTimestamp, options);
			}
			File mergeFile = new File(dldDir, "merge_data.tsv");
			File mergeUrl = new File(dldDir, "merge_url.tsv");
			String mergeGood = null;
			try(OutputStream out = java.nio.file.Files.newOutputStream(mergeFile.toPath(), options))
			{
				mergeGood = DatatableUtil.getDatatableMSC(out, thePe.mAn.analysis_id);
			}
			finally
			{
				postDownload(mergeGood, mergeFile, mergeUrl, theTimestamp, options);
			}
			File dropFile = new File(dldDir, "drop_data.tsv");
			File dropUrl = new File(dldDir, "drop_url.tsv");
			String dropGood = null;
			try(OutputStream out = java.nio.file.Files.newOutputStream(dropFile.toPath(), options))
			{
				dropGood = DatatableUtil.getDatatableDC(out, thePe.mAn.analysis_id);
			}
			finally
			{
				postDownload(dropGood, dropFile, dropUrl, theTimestamp, options);
			}
			// get Batch/Factors and Metabolites
			File batchFile = new File(dldDir, "batch_factors.tsv");
			try(OutputStream out = java.nio.file.Files.newOutputStream(batchFile.toPath(), options))
			{
				FactorUtil.getBatchesTSV(out, thePe.mSu.study_id);
			}
			File metaFile = new File(dldDir, "metabolites.tsv");
			try(OutputStream out = java.nio.file.Files.newOutputStream(metaFile.toPath(), options))
			{
				MetaboliteMapUtil mmu = new MetaboliteMapUtil(mMu, mRu, mOu);
				mmu.streamTsv(out, thePe.mAn.analysis_id);
			}
			thePe.mStatus = M_STATUS_DOWNLOADED;
		}
		catch(Exception exp)
		{
			StdMwDownload.printErr("Error processing dataset", exp);
			thePe.mStatus = M_STATUS_FAILED;
			// clean up files
			if (dldDir.exists())
			{
				dldDir.delete();
			}
		}
	}
	
	public void cleanupDataOptions(ProcessEntry thePe) throws IOException, MalformedURLException, NoSuchAlgorithmException, StdMwException
	{
		File dataDir = new File(MWUrls.M_MW_PIPELINE, "DATA");
		File anDir = new File(dataDir, thePe.mAn.hash.substring(0,2));
		File suDir = new File(anDir, thePe.mSu.hash.substring(0,2));
		File dldDir = new File(suDir, thePe.mHash);
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
		// json index "index.json"
		File jsonindexFile = new File(dldDir, "index.json");
		JsonDataset jd = thePe.getJsonDataset(true);
		jd.writeJson(jsonindexFile);
		// zip directory
		File [] files = ZipData.zip(dldDir, new File(dldDir, (thePe.mHash + ".zip")));
		for (File myF : files)
		{
			if (myF.exists())
			{
				myF.delete();
			}
		}
		thePe.mStatus = M_STATUS_DOWNLOADED;
	}
	
	public boolean convertLinkOuts(File theRowColTypeFileOut, File theNgchmLinkMapFileOut, 
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
				// first summary line
				line = br.readLine();
				while(null!=line)
				{
					String [] splitted = line.split("\t", -1);
					// feature
					bw.write(splitted[headerToIndex.get(metaboliteIdCol)]);
					bw.write("\t");
					bw.write(splitted[headerToIndex.get(metaboliteIdCol)]);
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

	public void updateStandardizedIndex() throws Exception
	{
		for (ProcessEntry pe : mHashToProcessEntries.values())
		{
			if (pe.mStatus.equals(ProcessUtil.M_STATUS_SUCCESS))
			{
				// M_QUERY_INDEX
				File dldDir = getZipDir(pe);
				File zipFile = new File(dldDir, (pe.mHash + ".zip"));
				JsonDataset jd = pe.getJsonDataset(true);
				StdMwDownload.M_QUERY_INDEX.updateIndex(zipFile, pe.mHash, jd);
			}
		}
	}
}
