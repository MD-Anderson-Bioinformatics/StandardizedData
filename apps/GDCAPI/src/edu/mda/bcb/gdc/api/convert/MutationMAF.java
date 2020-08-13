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
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.data.WorkflowData;
import edu.mda.bcb.gdc.api.dataframes.ClinicalDF;
import edu.mda.bcb.gdc.api.portal.GDC_Mixin;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Tod-Casasent
 */
public class MutationMAF
{
	public MutationMAF()
	{
	}
	
	public static boolean checkForDirStartsWith(File theBaseDir, String theStartsWith)
	{
		boolean found = false;
		File [] subDir = theBaseDir.listFiles();
		for (File dir : subDir)
		{
			if (dir.isDirectory())
			{
				if (dir.getName().startsWith(theStartsWith))
				{
					found = true;
				}
			}
		}
		return found;
	}
	
	public static String [] M_EXTENSIONS =
	{
		"hgsc",
		"wustl",
		"ucsc",
		"bcgsc",
		"mdanderson",
		"sanger",
		"curated",
		"automated",
		"aggregated",
		"Level_2",
	};
	
	public static String filenameToExtensions(String theFilename)
	{
		String extensions = "";
		for (String ext : M_EXTENSIONS)
		{
			if (theFilename.contains(ext))
			{
				extensions = extensions + "-" + ext;
			}
		}
		return extensions;
	}
	
	public static void processDirectory_legacy(GDC_Mixin theGDC, WorkflowData theManifest,
			final File theDownloadDir, File theConvertDir, File theBiospecimenDir, File theClinicalDir, String [] theRequiredBuilds) throws Exception
	{
		GDCAPI.printLn("MutationMAF::processDirectory_legacy");
		GDCFile [] gdcFiles = theManifest.getGDCFiles(false);
		for (GDCFile gdcFile : gdcFiles)
		{
			File myDir = new File(theConvertDir.getParent(), theConvertDir.getName() + filenameToExtensions(gdcFile.mName));
			if (theGDC.checkDirs(theDownloadDir, myDir))
			{
				MutationMatrix.processDirectory(theDownloadDir, gdcFile, theRequiredBuilds, myDir);
				MutationsFile.processDirectory(theDownloadDir, gdcFile, theRequiredBuilds, myDir);
				// Batches.tsv file
				GDCFile [] gdcArray = { gdcFile };
				Batches batches = new Batches(gdcArray, myDir, theManifest.mProgram, theManifest.mProject);
				batches.writeBatchFile(theBiospecimenDir);
				ClinicalDF clinicalDF = new ClinicalDF(gdcArray, myDir, theManifest.mProgram, theManifest.mProject);
				clinicalDF.writeClinicalFile(theClinicalDir);
			}
		}
	}
	
	public static void processDirectory_current(WorkflowData theManifest,
			final File theDownloadDir, File theConvertDir, File theBiospecimenDir, File theClinicalDir, String [] theRequiredBuilds) throws Exception
	{
		GDCAPI.printLn("MutationMAF::processDirectory_current");
		if (theManifest.mFiles.size()>1)
		{
			throw new Exception("MutationMAF " + theManifest.toString() + " should only have one file, not " + theManifest.mFiles.size());
		}
		GDCFile gdcFile = (GDCFile)(theManifest.mFiles.firstEntry().getValue());
		MutationMatrix.processDirectory(theDownloadDir, gdcFile, theRequiredBuilds, theConvertDir);
		MutationsFile.processDirectory(theDownloadDir, gdcFile, theRequiredBuilds, theConvertDir);
		// Batches.tsv file
		GDCFile [] gdcArray = { gdcFile };
		Batches batches = new Batches(gdcArray, theConvertDir, theManifest.mProgram, theManifest.mProject);
		batches.writeBatchFile(theBiospecimenDir);
		ClinicalDF clinicalDF = new ClinicalDF(gdcArray, theConvertDir, theManifest.mProgram, theManifest.mProject);
		clinicalDF.writeClinicalFile(theClinicalDir);

	}

	static public void doRenameOrAdd(String theOld, String theNew, ArrayList<String> theHeaders)
	{
		int index = theHeaders.indexOf(theOld);
		if (-1!=index)
		{
			theHeaders.set(index, theNew);
		}
		else
		{
			GDCAPI.printWarn("Adding header column '" + theNew + "'");
			theHeaders.add(theNew);
		}
	}

	static public void doRenameOrAdd(String theOldA, String theOldB, String theNew, ArrayList<String> theHeaders)
	{
		int indexA = theHeaders.indexOf(theOldA);
		int indexB = theHeaders.indexOf(theOldB);
		if (-1!=indexA)
		{
			theHeaders.set(indexA, theNew);
		}
		else if (-1!=indexB)
		{
			theHeaders.set(indexB, theNew);
		}
		else 
		{
			GDCAPI.printWarn("Adding header column '" + theNew + "'");
			theHeaders.add(theNew);
		}
	}
	
	static public ArrayList<String> renameHeadersAsNeeded(ArrayList<String> theHeaders)
	{
		doRenameOrAdd("Hugo_Symbol", "Gene", theHeaders);
		doRenameOrAdd("Entrez_Gene_Id", "EntrezId", theHeaders);
		doRenameOrAdd("Chromosome", "Position_Chromosome", theHeaders);
		doRenameOrAdd("Start_Position", "Start_position", "Position_Start", theHeaders);
		doRenameOrAdd("End_Position", "End_position", "Position_End", theHeaders);
		doRenameOrAdd("Strand", "Position_Strand", theHeaders);
		doRenameOrAdd("Transcript_ID", "Annotation_Transcript", "TranscriptId", theHeaders);
		doRenameOrAdd("t_depth", "Tumor_Depth", theHeaders);
		doRenameOrAdd("t_ref_count", "Tumor_Reference_Count", theHeaders);
		doRenameOrAdd("t_alt_count", "Tumor_Variant_Count", theHeaders);
		doRenameOrAdd("n_depth", "Normal_Depth", theHeaders);
		doRenameOrAdd("n_ref_count", "Normal_Reference_Count", theHeaders);
		doRenameOrAdd("n_alt_count", "Normal_Variant_Count", theHeaders);
		doRenameOrAdd("Protein_Change", "HGVSp_Short", "HGVSp_Short", theHeaders);
		//original: HGVSp_Short
		//converted into: amino_acid_position amino_acid_normal amino_acid_tumor
		// add three new headers
		theHeaders.add("amino_acid_position");
		theHeaders.add("amino_acid_normal");
		theHeaders.add("amino_acid_tumor");
		/////////////
		return theHeaders;
	}
	
	static public boolean checkBuilds(String theBuildFound, String [] theBuildsWanted)
	{
		boolean found = false;
		if (null==theBuildsWanted)
		{
			found = true;
		}
		else
		{
			for (String wanted : theBuildsWanted)
			{
				if (theBuildFound.contains(wanted))
				{
					found = true;
				}
			}
		}
		return found;
	}
}
