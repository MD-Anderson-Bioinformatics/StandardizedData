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

package edu.mda.bcb.gdc.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;

/**
 *
 * @author tdcasasent
 */
abstract public class Uncompress
{
	static public String uncompressToFile(File theFile, File theDestFile) throws IOException, Exception
	{
		//GDCAPI.printLn("Uncompress::uncompress start " + theFile.getPath());
		String outputFile = unGzFile(theFile, theDestFile);
		//GDCAPI.printLn("Uncompress::uncompress completed " + theFile.getPath());
		return (outputFile);
	}

	static private String unGzFile(File theFile, File theDestFile) throws IOException
	{
		String uncompressed = null;
		// uncompress
		FileInputStream fin = null;
		BufferedInputStream in = null;
		FileOutputStream out = null;
		GzipCompressorInputStream gzIn = null;
		try
		{
			//GDCAPI.printLn("Uncompress::unGz starting for the file " + theFile.getName());
			fin = new FileInputStream(theFile);
			in = new BufferedInputStream(fin);
			//FileUtils.forceMkdir(theDestDir);
			gzIn = new GzipCompressorInputStream(in);
			GzipParameters gp = gzIn.getMetaData();
			//
			out = new FileOutputStream(theDestFile);
			//
			final byte[] buffer = new byte[1024];
			int n = 0;
			while (-1 != (n = gzIn.read(buffer)))
			{
				out.write(buffer, 0, n);
			}
			uncompressed = theDestFile.getName();
		}
		catch (java.io.FileNotFoundException exp)
		{
			GDCAPI.printErr("Uncompress::uncompress File not found decompressing " + theFile.getPath());
			throw exp;
		}
		catch (java.io.IOException exp)
		{
			GDCAPI.printErr("Uncompress::uncompress IOException decompressing " + theFile.getPath());
			throw exp;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (Exception ignore)
			{
				//ignore
			}
			try
			{
				gzIn.close();
			}
			catch (Exception ignore)
			{
				//ignore
			}
		}
		return (uncompressed);
	}
	/*
	static private void untar(String theTarFileName, String theUntarDir) throws java.io.FileNotFoundException, java.io.IOException, Exception
	{
		int bufLen = 1024 * 10;
		byte[] buf = new byte[bufLen];
		GDCAPI.printLn("Uncompress::untar Creating an InputStream for the file " + theTarFileName);
		FileInputStream in = null;
		TarArchiveInputStream tin = null;
		try
		{
			in = new FileInputStream(new File(theTarFileName));

			tin = new TarArchiveInputStream(in);
			TarArchiveEntry tarEntry = tin.getNextTarEntry();
			if (new File(theUntarDir).exists())
			{
				while (tarEntry != null)
				{
					File destPath = new File(theUntarDir + File.separatorChar + tarEntry.getName());
					//GDCAPI.printLn("Uncompress::untar Processing " + destPath.getAbsoluteFile());
					if (!tarEntry.isDirectory())
					{
						String tempFullPath = destPath.getPath();
						String tempFileName = destPath.getName();
						String tempPathOnly = tempFullPath.substring(0, tempFullPath.length() - tempFileName.length());
						File fileDirPath = new File(tempPathOnly);
						if (!fileDirPath.exists())
						{
							fileDirPath.mkdirs();
						}

						FileOutputStream fout = new FileOutputStream(destPath);
						int bytesRead = tin.read(buf);
						while (bytesRead != -1)
						{
							fout.write(buf, 0, bytesRead);
							bytesRead = tin.read(buf);
						}
						fout.close();
					}
					else
					{
						destPath.mkdirs();
					}
					tarEntry = tin.getNextTarEntry();
				}
				tin.close();
			}
			else
			{
				GDCAPI.printErr("Uncompress::untar Destination directory doesn't exist! " + theUntarDir);
				throw new Exception("Destination directory doesn't exist! " + theUntarDir);
			}
		}
		catch (java.io.FileNotFoundException exp)
		{
			GDCAPI.printErr("Uncompress::untar got FileNotFoundException for file " + theTarFileName + " and dir " + theUntarDir);
			throw exp;
		}
		catch (java.io.IOException exp)
		{
			GDCAPI.printErr("Uncompress::untar Untar got IOException for file " + theTarFileName + " and dir " + theUntarDir);
			throw exp;
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (Exception ignore)
			{
			}
			try
			{
				tin.close();
			}
			catch (Exception ignore)
			{
			}
		}
	}
	 */
}
