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
import edu.mda.bcb.gdc.api.data.GDCFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tod-Casasent
 */
public class MutationsFile
{

	private MutationsFile()
	{
	}

	final static public String[] M_USED_COLUMNS =
	{
		"Tumor_Sample_Barcode",
		"Gene",
		"EntrezId",
		"Variant_Classification",
		"Position_Chromosome",
		"Position_Start",
		"Position_End",
		"Position_Strand",
		"TranscriptId",
		"Tumor_Depth",
		"Tumor_Reference_Count",
		"Tumor_Variant_Count",
		"Normal_Depth",
		"Normal_Reference_Count",
		"Normal_Variant_Count",
		"HGVSp_Short",
		"amino_acid_position",
		"amino_acid_normal",
		"amino_acid_tumor",
		"NCBI_Build"
	};

	static public ArrayList<String> M_COL_LIST = null;

	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	static public void processDirectory(File theDownloadDir, GDCFile theGDCfile, 
			String[] theRequiredBuilds, File theConvertDir) throws Exception
	{
		if (null == M_COL_LIST)
		{
			M_COL_LIST = new ArrayList<>();
			M_COL_LIST.addAll(Arrays.asList(M_USED_COLUMNS));
		}
		File outFile = new File(theConvertDir, "mutations.tsv");
		MutationsFile.run(theGDCfile.getFileObj(theDownloadDir, true), outFile, theRequiredBuilds);
	}

	static protected String getValue(String[] theSplitted, int theIndex)
	{
		String value = "";
		if (theIndex < theSplitted.length)
		{
			value = theSplitted[theIndex];
		}
		return value;
	}

	static public void run(File theInFile, File theOutFile, String[] theRequiredBuilds) throws Exception, Exception
	{
		ArrayList<String> headers = null;
		TreeSet<String> outputLines = new TreeSet<>();
		// have to use windows-1252 charset due to weird non-UTF-8 characters in file
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theInFile.toPath(), Charset.availableCharsets().get("windows-1252")))
		{
			String line = br.readLine();
			while (null != line)
			{
				// skip extra legacy header line
				// TODO: add sanity check between file contents UUID and file name UUID
				if (!line.startsWith("#"))
				{
					String[] splitted = line.split("\t", -1);
					if (null == headers)
					{
						headers = new ArrayList<>();
						headers.addAll(Arrays.asList(splitted));
						headers = MutationMAF.renameHeadersAsNeeded(headers);
						if (!headers.containsAll(M_COL_LIST))
						{
							ArrayList<String> temp = new ArrayList<>(M_COL_LIST);
							temp.removeAll(headers);
							String missing = "";
							for (String ms : temp)
							{
								missing = " " + ms;
							}
							throw new Exception("Not all header entries found, missing: " + missing);
						}
					}
					else
					{
						String build = GDCAPI.getColumn("NCBI_Build", splitted, headers);
						if (MutationMAF.checkBuilds(build, theRequiredBuilds))
						{
							String newLine = null;
							for (String myHeader : M_COL_LIST)
							{
								String copyValue = "NA";
								if ("amino_acid_position".equals(myHeader))
								{
									copyValue = getValue(splitted, headers.indexOf("HGVSp_Short"));
									if (("NULL".equals(copyValue)) || ("".equals(copyValue)))
									{
										copyValue = "";
									}
									else
									{
										// amino acid location
										String aal = copyValue;
										aal = aal.replace("p.", "");	// trim p. if there
										Pattern p = Pattern.compile("(\\d+)");
										Matcher m = p.matcher(aal);
										if (m.find())
										{
											copyValue = m.group(1);
										}
										else
										{
											copyValue = "";
										}
									}
								}
								else if ("amino_acid_normal".equals(myHeader))
								{
									copyValue = getValue(splitted, headers.indexOf("HGVSp_Short"));
									if (("NULL".equals(copyValue)) || ("".equals(copyValue)))
									{
										copyValue = "";
									}
									else
									{
										// original amino acid
										String oaa = copyValue;
										oaa = oaa.replace("p.", "");	// trim p. if there
										if (oaa.contains(">"))
										{
											Pattern p = Pattern.compile("(\\D+)>");
											Matcher m = p.matcher(oaa);
											if (m.find())
											{
												copyValue = m.group(1);
											}
											else
											{
												copyValue = "";
											}
										}
										else
										{
											Pattern p = Pattern.compile("(\\D+)(\\d+)");
											Matcher m = p.matcher(oaa);
											if (m.find())
											{
												copyValue = m.group(1);
											}
											else
											{
												copyValue = "";
											}
										}
									}
								}
								else if ("amino_acid_tumor".equals(myHeader))
								{
									copyValue = getValue(splitted, headers.indexOf("HGVSp_Short"));
									if (("NULL".equals(copyValue)) || ("".equals(copyValue)))
									{
										copyValue = "";
									}
									else
									{
										// final amino acid
										String faa = copyValue;
										faa = faa.replace("p.", "");	// trim p. if there
										if (faa.contains(">"))
										{
											Pattern p = Pattern.compile(">(\\D+)$");
											Matcher m = p.matcher(faa);
											if (m.find())
											{
												copyValue = m.group(1);
											}
											else
											{
												copyValue = "";
											}
										}
										else
										{
											Pattern p = Pattern.compile("(?:[A-Za-z]+)(?:\\d+)(?:_?[\\d]+)?(.+)?$");
											Matcher m = p.matcher(faa);
											if (m.find())
											{
												String res = m.group(1);
												if (null == res)
												{
													copyValue = "NA";
												}
												else
												{
													copyValue = res;
												}
											}
											else
											{
												copyValue = "";
											}
										}
									}
								}
								else
								{
									copyValue = getValue(splitted, headers.indexOf(myHeader));
									if (("NULL".equals(copyValue)) || ("".equals(copyValue)))
									{
										copyValue = "";
									}
								}
								if (null == newLine)
								{
									newLine = copyValue;
								}
								else
								{
									newLine = newLine + "\t" + copyValue;
								}
							}
							outputLines.add(newLine);
						}
					}
				}
				line = br.readLine();
			}
		}
		catch (IOException exp)
		{
			throw new Exception("For file: " + theInFile, exp);
		}
		catch (Exception exp)
		{
			throw new Exception("For file: " + theInFile, exp);
		}
		// TODO: any MutationFile sanity checks to add?
		if (theOutFile.exists())
		{
			throw new Exception("File should not exist:" + theOutFile);
		}
		String headerString = GDCAPI.arrayToString(M_COL_LIST, "\t");
		GDCAPI.writeDataframe(theOutFile, headerString, outputLines);
	}
}
