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

package edu.mda.bcb.gdc.api.data;

import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.util.UpdateableMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Objects;

/**
 *
 * @author Tod-Casasent
 */
public class Clinical extends Fileable implements Comparable<Clinical>
{
	public String mProgram = null;
	public String mProject = null;
	
	public Clinical(String theProgram, String theProject)
	{
		mProgram = theProgram;
		mProject = theProject;
		mFiles = new UpdateableMap<>();
		mNotInGDC = Boolean.FALSE;
		mNewFromGDC = null;
		mUpdatedByGDC = null;
		mReadFromFile = null;
	}
	
	public void addFile(GDCFile theFile)
	{
		// TODO: this may need code to detect if file is changed between runs
		mFiles.put(theFile.mUUID, theFile);
	}

	@Override
	public int compareTo(Clinical o)
	{
		int compare = this.mProgram.compareTo(o.mProgram);
		if (0==compare)
		{
			compare = this.mProject.compareTo(o.mProject);
		}
		return compare;
	}

	@Override
	public String toString()
	{
		return "clinical-manifest".toUpperCase() + "{" + "mProgram=" + mProgram + ", mProject=" + mProject + '}';
	}

	@Override
	public String loadManifestInternal(File theDir) throws Exception
	{
		String loaded = null;
		// find newest tsv file
		File newestTsv = GDCAPI.findNewestTSV(theDir);
		if (null!=newestTsv)
		{
			// loadfile
			try(BufferedReader br = new BufferedReader(new FileReader(newestTsv)))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					// line should start with "clinical" or be "-FileStart-"
					if (line.startsWith("manifest"))
					{
						// read manifest data
						String [] splitted = line.split("\t", -1);
						if (!mProgram.equals(splitted[1]))
						{
							throw new Exception("For " + newestTsv + ": manifest program does not match directory path");
						}
						if (!mProject.equals(splitted[2]))
						{
							throw new Exception("For " + newestTsv + ": manifest project does not match directory path");
						}
						if ("TRUE".equals(splitted[3]))
						{
							mNotInGDC = Boolean.TRUE;
						}
						else
						{
							mNotInGDC = Boolean.FALSE;
						}
					}
					else if (line.equals("-FileStart-"))
					{
						// load file info
						GDCFile gdcFile = GDCFile.loadFile(br, newestTsv);
						this.addFile(gdcFile);
					}
					else
					{
						throw new Exception("For " + newestTsv + ": unknown line command '" + line + "'");
					}
				}
			}
			mReadFromFile = Boolean.TRUE;
			mNewFromGDC = Boolean.FALSE;
			loaded = newestTsv.getName().replace(".tsv", "");
		}
		return loaded;
	}

	@Override
	public void writeManifest(File theDir) throws Exception
	{
		if ((Objects.equals(Boolean.TRUE, mNewFromGDC)) || (Objects.equals(Boolean.TRUE, mUpdatedByGDC)))
		{
			if (!theDir.exists())
			{
				theDir.mkdirs();
			}
			File outfile = new File(theDir, GDCAPI.M_TIMESTAMP + ".tsv");
			GDCAPI.printLn("Clinical::writeManifest - outfile = " + outfile.getAbsolutePath());
			try (BufferedWriter runWriter = java.nio.file.Files.newBufferedWriter(outfile.toPath(), Charset.availableCharsets().get("UTF-8")))
			{
				runWriter.write("manifest");
				runWriter.write("\t");
				runWriter.write(mProgram);
				runWriter.write("\t");
				runWriter.write(mProject);
				runWriter.write("\t");
				runWriter.write((Boolean.TRUE==mNotInGDC)?"TRUE":"FALSE");
				runWriter.newLine();
				Collection<Updateable_Mixin<GDCFile>> fs = mFiles.values();
				for (Updateable_Mixin<GDCFile> myfile : fs)
				{
					((GDCFile)myfile).writeFile(runWriter);
				}
			}
		}
	}
}
