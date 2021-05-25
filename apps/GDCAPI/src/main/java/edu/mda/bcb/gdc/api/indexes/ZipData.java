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

package edu.mda.bcb.gdc.api.indexes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Tod-Casasent
 */
public class ZipData
{

	static public File[] zip(File theDir, File theZipFile) throws FileNotFoundException, IOException
	{
		// first collect contents to add (to prevent ZIP being self-referential
		File[] dirList = theDir.listFiles();
		// then create ZIP
		FileOutputStream fos = new FileOutputStream(theZipFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ZipOutputStream zos = new ZipOutputStream(bos);
		try
		{
			byte[] buffer = new byte[1024];
			for (File myFile : dirList)
			{
				// not available on BufferedOutputStream
				zos.putNextEntry(new ZipEntry(myFile.getName()));
				try (FileInputStream fis = new FileInputStream(myFile))
				{
					for (int bytesRead; (bytesRead = fis.read(buffer)) >= 0;)
					{
						zos.write(buffer, 0, bytesRead);
					}
				}
				zos.closeEntry();
			}
		}
		finally
		{
			zos.close();
		}
		return dirList;
	}

	static public TreeSet<String> getListOfFiles(File theZipFile) throws FileNotFoundException, IOException
	{
		TreeSet<String> myFiles = new TreeSet<>();
		ZipInputStream zis = new ZipInputStream(new FileInputStream(theZipFile));
		try
		{
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null)
			{
				myFiles.add(zipEntry.getName());
				zipEntry = zis.getNextEntry();
			}
		}
		finally
		{
			zis.closeEntry();
			zis.close();
		}
		return myFiles;
	}
}
