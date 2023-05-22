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

package edu.mda.bioinfo.genemap;

import edu.mda.bcb.prm.PlatformReleaseMaps;
import edu.mda.bcb.prm.genemap.EnsemblGeneMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeSet;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author Tod-Casasent
 */
public class EnsemblGeneMapTest
{
	
	public EnsemblGeneMapTest()
	{
	}

	/**
	 * Test of main method, of class EnsemblGeneMap.
	 */
	@Test
	public void testMain()
	{
		try
		{
			// Ensemble Gene Map
			String compareZip = "../../data/testing_static/PlatformReleaseMaps/EnsemblGeneMap/data_compare.zip";
			String dataInDir = "../../data/testing_static/PlatformReleaseMaps/EnsemblGeneMap/data_in";
			String dataOutDir = "../../data/testing_dynamic/PlatformReleaseMaps/EnsemblGeneMap/data_out";
			File outFile = new File(dataOutDir);
			outFile.delete();
			outFile.mkdirs();
			EnsemblGeneMap egm = new EnsemblGeneMap();
			TreeSet<String> outputFiles = egm.run(dataInDir, dataOutDir);
			for (String newFile : outputFiles)
			{
				compareFiles(newFile, compareZip);
			}
		}
		catch(Exception exp)
		{
			exp.printStackTrace(System.err);
			System.err.flush();
			fail(exp.getMessage());
		}
	}

	public void compareFiles(String theNewFile, String theCompareZip) throws IOException, Exception
	{
		File newFile = new File(theNewFile);
		String compareFile = newFile.getName();
		System.out.println("Checking file: " + theNewFile);
		System.out.println("against file: " + compareFile);
		System.out.println("in zip: " + theCompareZip);
		try(BufferedReader br1 = Files.newBufferedReader(Paths.get(newFile.getAbsolutePath()), StandardCharsets.UTF_8))
		{
			try(BufferedReader br2 = PlatformReleaseMaps.getBufferedReaderForZipByFile(theCompareZip, compareFile))
			{
				String line1 = br1.readLine();
				String line2 = br2.readLine();
				while(null!=line2)
				{
					if (!line1.equals(line2))
					{
						throw new Exception("Files do not match: " + theNewFile);
					}
					line1 = br1.readLine();
					line2 = br2.readLine();
				}
			}
		}
	}
	
}
