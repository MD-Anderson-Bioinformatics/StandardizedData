// Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.prm;

import edu.mda.bcb.prm.genemap.EnsemblGeneMap;
import edu.mda.bcb.prm.hg19.HG19Maps;
import edu.mda.bcb.prm.hg38.HG38Maps;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author Tod-Casasent
 */
public class PlatformReleaseMaps
{

	static public void printVersion()
	{
		System.out.println("PlatformReleaseMaps BEA_VERSION_TIMESTAMP");
	}
	
	static public BufferedReader getBufferedReaderForZip(String theZipFile) throws IOException
	{
		ZipFile zipFile = new ZipFile(theZipFile);
		// should have only one entry
		ZipEntry zipEntry = zipFile.entries().nextElement();
		return new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
	}
	
	static public BufferedReader getBufferedReaderForZipByFile(String theZipFile, String theInternalEntry) throws IOException
	{
		ZipFile zipFile = new ZipFile(theZipFile);
		// should have only one entry
		ZipEntry zipEntry = zipFile.getEntry(theInternalEntry);
		return new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		try
		{
			// Ensembl Gene Map
			System.out.println("=====================================");
			System.out.println("Ensembl conversions");
			String egmDataInDir = "../../data/apps_in/PlatformReleaseMaps/EnsemblGeneMap";
			String egmDataOutDir = "../../data/apps_out/PlatformReleaseMaps/EnsemblGeneMap";
			new File(egmDataOutDir).mkdirs();
			EnsemblGeneMap egm = new EnsemblGeneMap();
			egm.run(egmDataInDir, egmDataOutDir);
			// HG19 maps
			System.out.println("=====================================");
			System.out.println("HG19 conversions");
			String hg19DataInDir = "../../data/apps_in/PlatformReleaseMaps/HG19";
			String hg19DataOutDir = "../../data/apps_out/PlatformReleaseMaps/HG19";
			new File(hg19DataOutDir).mkdirs();
			HG19Maps hg19maps = new HG19Maps();
			hg19maps.run(hg19DataInDir, hg19DataOutDir);
			// HG38 maps
			System.out.println("=====================================");
			System.out.println("HG38 conversions");
			String hg38DataInDir = "../../data/apps_in/PlatformReleaseMaps/HG38";
			String hg38DataOutDir = "../../data/apps_out/PlatformReleaseMaps/HG38";
			new File(hg38DataOutDir).mkdirs();
			HG38Maps hg38maps = new HG38Maps();
			hg38maps.run(hg38DataInDir, hg38DataOutDir);
		}
		catch(Exception exp)
		{
			exp.printStackTrace(System.err);
		}
	}
	
	// NOT USED
	// GDCIdentifiers
	//String giDataInDir = "../../data/apps_in/PlatformReleaseMaps/GDCIdentifiers/data_in";
	//String giDataOutDir = "../../data/apps_out/PlatformReleaseMaps/GDCIdentifiers/data_out";
	//new File(giDataOutDir).mkdirs();
	//GDCIdentifiers gi = new GDCIdentifiers();
	//gi.run(giDataInDir, giDataOutDir);
	
}
