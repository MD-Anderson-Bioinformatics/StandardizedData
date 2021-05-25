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
package edu.mda.bcb.gdc.api;

import edu.mda.bcb.gdc.api.indexes.DataIndex;
import edu.mda.bcb.gdc.api.indexes.JsonDataset;
import edu.mda.bcb.gdc.api.indexes.ZipData;
import edu.mda.bcb.gdc.api.pancan.PanCanMetaData;
import edu.mda.bcb.gdc.api.util.FileFind;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class PanCanUpdate
{

	public static void main(String[] args)
	{
		String atlasMethylation = "/PanCanAtlas/2017-03-16/Atlas/DNA-methylation-normalized.tsv";
		String stdMethylation = "/PanCanAtlas/2017-03-16/STD/Methylation/matrix_data.tsv";
		String pancanStdDir = "/PanCanAtlas/2017-03-16/STD";
		String indexesDir = "/PanCanAtlas/2017-03-16/INDEXES";
		String biospecimenDir = "/DAPI/DATA/biospecimen/convert";
		String clinicalDir = "/DAPI/DATA/clinical/convert";
		try
		{
			convertAtlasFile(atlasMethylation, stdMethylation);
			// this is for adding batch, clinical, and annotation files to PanCan Standardized Data
			PanCanMetaData.getMetaData(pancanStdDir, biospecimenDir, clinicalDir);
			// make index files and zip
			GDCAPI.M_QUERY_INDEX = new DataIndex(new File(indexesDir, "index.tsv"));
			PanCanUpdate.indexAndZip(pancanStdDir);
		}
		catch (Exception exp)
		{
			GDCAPI.printErr("Error in main", exp);
		}
	}

	public static void convertAtlasFile(String theAtlasFile, String theStdFile) throws IOException
	{
		OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
		GDCAPI.printLn("Reading " + theAtlasFile);
		GDCAPI.printLn("Writing " + theStdFile);
		boolean header = false;
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(new File(theAtlasFile).toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			String line = br.readLine();
			try (BufferedWriter bw = java.nio.file.Files.newBufferedWriter(new File(theStdFile).toPath(), Charset.availableCharsets().get("UTF-8"), options))
			{
				while (null != line)
				{
					if(false == header)
					{
						// process header - drop first token, remove quotes from rest
						String[] token = line.split("\t", -1);
						for (int i = 0; i < token.length; i++)
						{
							if (i > 0)
							{
								bw.write("\t");
								String wt = token[i].trim();
								wt = wt.replaceAll("\"", "");
								bw.write(wt);
							}
						}
						//
						header = true;
					}
					else
					{
						// process row - remove quotes from first token
						String[] token = line.split("\t", -1);
						for (int i = 0; i < token.length; i++)
						{
							if (i > 0)
							{
								bw.write("\t");
								String wt = token[i].trim();
								bw.write(wt);
							}
							else
							{
								String wt = token[i].trim();
								wt = wt.replaceAll("\"", "");
								bw.write(wt);
							}
						}
					}
					bw.newLine();
					line = br.readLine();
				}
			}
		}
	}

	public static void indexAndZip(String thePanCanStdDir) throws IOException, Exception
	{
		// find matrix_data.tsv files
		System.out.println("find matrix_data.tsv files");
		FileFind ff = new FileFind();
		ff.find(new File(thePanCanStdDir).toPath(), "matrix_data.tsv");
		TreeSet<Path> stdMatrixFiles = new TreeSet<>();
		stdMatrixFiles.addAll(ff.mMatches);
		for (Path myPath : stdMatrixFiles)
		{
			System.out.println(myPath);
		}
		System.out.println("Processing directories");
		for (Path matrixFile : stdMatrixFiles)
		{
			System.out.println("matrixFile=" + matrixFile);
			// PanCan data type
			String source = "PanCan Atlas";
			String variant = "PanCan";
			String project = "TCGA";
			String subProject = "PanCan";
			String dataType = matrixFile.toFile().getParentFile().getName();
			String platform = dataType;
			String dataset = "";
			if ("RPPA".equals(dataType))
			{
				dataset = "standardized-continuous";
				dataType = "Protein Expression";
				platform = "RPPA";
			}
			else if ("mRNA".equals(dataType))
			{
				// TODO: confirm this!!
				dataset = "standardized-continuous";
				dataType = "Gene Expression";
				platform = "mRNA";
			}
			else if ("miRNA".equals(dataType))
			{
				dataset = "standardized-continuous";
				dataType = "miRNA";
				platform = "miRNA-Seq Isoform Quantification";
			}
			else if ("Methylation".equals(dataType))
			{
				dataset = "standardized-continuous";
				dataType = "DNA Methylation";
				platform = "Methylation";
			}
			String version = GDCAPI.getTimestamp();
			JsonDataset jd = new JsonDataset(source, variant, project, subProject, dataType, platform, dataset, version);
			String fileID = jd.getID();
			////////////////////////////////////////////////////////////////
			// internal index file
			////////////////////////////////////////////////////////////////
			File indexFile = new File(matrixFile.toFile().getParentFile(), "index.json");
			File zipFile = new File(matrixFile.toFile().getParentFile(), fileID + ".zip");
			GDCAPI.printLn("matrixFile = " + matrixFile);
			GDCAPI.printLn("indexFile = " + indexFile.getAbsolutePath());
			GDCAPI.printLn("zipFile = " + zipFile.getAbsolutePath());
			if (indexFile.exists())
			{
				indexFile.delete();
			}
			if (zipFile.exists())
			{
				zipFile.delete();
			}
			GDCAPI.printLn("GDC_Mixin update local index");
			jd.writeJson(indexFile);
			ZipData.zip(matrixFile.toFile().getParentFile(), zipFile);
			// Add to global index
			GDCAPI.printLn("GDC_Mixin update external index");
			GDCAPI.M_QUERY_INDEX.updateIndex(zipFile, fileID, jd);
		}
	}
}
