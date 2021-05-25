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

import edu.mda.bcb.gdc.api.convert.MutationMAF;
import edu.mda.bcb.gdc.api.data.Workflow;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class DevData
{
	static public String M_BASE_DIR = "set path";
	
	////////////////////////////////////////////////////////////////////////////
	//// What mutation manifests have more than one file?
	////////////////////////////////////////////////////////////////////////////
	public static void main1(String[] args)
	{
		String baseDir = M_BASE_DIR;
		try
		{
			Files.walk(Paths.get(baseDir))
					.filter(Files::isRegularFile)
					.forEach((Path thePath) ->
					{
						String filename = thePath.toFile().getName();
						if (filename.equals("2020_01_29_1500.tsv"))
						{
							String filepath = thePath.toFile().getAbsolutePath();
							if (	(filepath.contains("Simple somatic mutation")) ||
									(filepath.contains("Masked Somatic Mutation")) )
							{
								try
								{
									TreeSet<String> files = new TreeSet<>();
									try (BufferedReader br = java.nio.file.Files.newBufferedReader(thePath, Charset.availableCharsets().get("UTF-8")))
									{
										String line = br.readLine();
										while (null!=line)
										{
											if (line.startsWith("FILE"))
											{
												files.add(line);
											}
											line = br.readLine();
										}
									}
									if (files.size()>1)
									{
										System.out.println(filepath);
										for (String file : files)
										{
											System.out.println("\t" + MutationMAF.filenameToExtensions(file) + "\t" + file);
										}
									}
								}
								catch (Exception exp)
								{
									GDCAPI.printErr("Error in read", exp);
								}
							}
						}
					});
		}
		catch (Exception exp)
		{
			GDCAPI.printErr("Error in main", exp);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//// What manifests and files have more than one barcode per file
	////////////////////////////////////////////////////////////////////////////
	public static void main2(String[] args)
	{
		String baseDir = M_BASE_DIR;
		try
		{
			Files.walk(Paths.get(baseDir))
					.filter(Files::isRegularFile)
					.forEach((Path thePath) ->
					{
						String filename = thePath.toFile().getName();
						if (filename.equals("2020_01_29_1500.tsv"))
						{
							boolean filePrinted = false;
							try
							{
								int sampleCount = -1;
								String currentFile = "";
								try (BufferedReader br = java.nio.file.Files.newBufferedReader(thePath, Charset.availableCharsets().get("UTF-8")))
								{
									String line = br.readLine();
									while (null!=line)
									{
										if (line.startsWith("FILE"))
										{
											// print samples for previous file
											if (sampleCount>1)
											{
												if (false==filePrinted)
												{
													System.out.println(thePath.toFile().getAbsolutePath());
													filePrinted = true;
												}
												System.out.println(sampleCount + " \t " + currentFile);
											}
											currentFile = line;
											sampleCount = 0;
										}
										else if (line.startsWith("Sample"))
										{
											sampleCount = sampleCount + 1;
										}
										line = br.readLine();
									}
									if (sampleCount>1)
									{
										if (false==filePrinted)
										{
											System.out.println(thePath.toFile().getAbsolutePath());
											filePrinted = true;
										}
										System.out.println(sampleCount + " \t " + currentFile);
									}
								}
							}
							catch (Exception exp)
							{
								GDCAPI.printErr("Error in read", exp);
							}
						}
					});
		}
		catch (Exception exp)
		{
			GDCAPI.printErr("Error in main", exp);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//// loading an existing legacy manifest
	////////////////////////////////////////////////////////////////////////////
	public static void main3(String[] args)
	{
		try
		{
			File manifestDir = new File(M_BASE_DIR);
			
			Workflow theWorkflow = new Workflow("Illumina Human Methylation 450", null);
			theWorkflow.manifestLoadFile(manifestDir, "TCGA", "TCGA-ACC", "DNA methylation");
			System.out.println(theWorkflow.mLegacyName);
		}
		catch (Exception exp)
		{
			GDCAPI.printErr("Error in main", exp);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	//// checking for newest manifest
	////////////////////////////////////////////////////////////////////////////
	public static void main4(String[] args)
	{
		try
		{
			File manifestDir = new File(M_BASE_DIR);
			System.out.println(GDCAPI.findNewestTSV(manifestDir));
		}
		catch (Exception exp)
		{
			GDCAPI.printErr("Error in main", exp);
		}
	}
}
