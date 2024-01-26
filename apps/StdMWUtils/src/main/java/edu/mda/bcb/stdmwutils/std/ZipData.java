// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.stdmwutils.std;

import edu.mda.bcb.stdmwutils.StdMwDownload;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author Tod-Casasent
 */
public class ZipData
{

	static public File[] zip(File theDir, File theZipFile, boolean theRemovePathsFlag) throws FileNotFoundException, IOException
	{
		// first collect contents to add (to prevent ZIP being self-referential
		File parent = theDir.getParentFile().getParentFile();
		Collection<File> cf = FileUtils.listFilesAndDirs(parent, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
		cf.remove(parent);
		File[] dirList = cf.toArray(File[]::new);
		// then create ZIP
		try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(theZipFile))))
		{
			byte[] buffer = new byte[1024];
			for (File myFile : dirList)
			{
				if (myFile.isFile())
				{
					String zipName = myFile.getAbsolutePath();
					if (theRemovePathsFlag)
					{
						zipName = myFile.getName();
					}
					else
					{
						zipName = zipName.replace(parent.getAbsolutePath(), "");
					}
					// remove leading slash
					zipName = zipName.substring(1);
					zos.putNextEntry(new ZipEntry(zipName));
					try (FileInputStream fis = new FileInputStream(myFile))
					{
						for (int bytesRead; (bytesRead = fis.read(buffer)) >= 0;)
						{
							zos.write(buffer, 0, bytesRead);
						}
					}
					finally
					{
						zos.closeEntry();
					}
				}
				else
				{
					String zipName = myFile.getAbsolutePath();
					zipName = zipName.replace(parent.getAbsolutePath(), "");
					// remove leading slash
					zipName = zipName.substring(1);
					if (!zipName.endsWith("/"))
					{
						zipName = zipName + "/";
					}
					zos.putNextEntry(new ZipEntry(zipName));
					zos.closeEntry();
				}
			}
		}
		return dirList;
	}

	static public TreeSet<String> getListOfFiles(File theZipFile) throws FileNotFoundException, IOException
	{
		TreeSet<String> myFiles = new TreeSet<>();
		try(ZipInputStream zis = new ZipInputStream(new FileInputStream(theZipFile)))
		{
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null)
			{
				myFiles.add(zipEntry.getName());
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
		}
		return myFiles;
	}
	
	static public boolean extractFile(File theZipArchive, String theFilename, File theDestFile) throws IOException
	{
		boolean extract = false;
		StdMwDownload.printLn("Copy '" + theFilename + "' from '" + theZipArchive.getAbsolutePath() + "' to '" + theDestFile.getAbsolutePath());
		try(ZipInputStream zis = new ZipInputStream(new FileInputStream(theZipArchive)))
		{
			ZipEntry ze = zis.getNextEntry();
			while ((false==extract)&&(null!=ze))
			{
				if (ze.getName().equals(theFilename))
				{
					FileUtils.copyToFile(zis, theDestFile);
					extract = true;
					ze = null;
				}
				else
				{
					ze = zis.getNextEntry();
				}
			}
		}
		return extract;
	}
}
