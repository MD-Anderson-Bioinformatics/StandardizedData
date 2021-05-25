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

package edu.mda.bcb.gdc.api.data;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.Uncompress;
import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import edu.mda.bcb.gdc.api.util.UpdateableMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Used by all types of information from the GDC (Biospecimen, Public-Clinical, 
 * Matrix Data, and Mutation Data) to represent GDCFile mapped to patients
 * and samples.
 * 
 * @author Tod-Casasent
 */
public class GDCFile implements Comparable<GDCFile>, Updateable_Mixin<GDCFile>
{
	public String mUUID = null;
	public String mName = null;
	public String mMD5Sum = null;
	public UpdateableMap<Sample> mSamples = null;
	public UpdateableMap<Patient> mPatients = null;
	
	/**
	 * Constructor 
	 * 
	 * @param theUUID GDC UUID for file
	 * @param theName GDC Name of the file
	 * @param theMD5Sum MD5 sum for file in download form
	 * @param theSamples Sample objects mapped to this file
	 * @param thePatients Patient objects mapped to this file 
	 */
	public GDCFile(String theUUID, String theName, String theMD5Sum, UpdateableMap<Sample> theSamples, UpdateableMap<Patient> thePatients)
	{
		mUUID = theUUID;
		mName = theName;
		mMD5Sum = theMD5Sum;
		mSamples = theSamples;
		mPatients = thePatients;
	}

	/**
	 * See if two GDCFile objects reference the same file comparing, in order,
	 * the UUID, name, MD5 sum, sample count, and patient count.
	 * 
	 * @param o GDCFile to compare
	 * @return the value 0 if comparison is equal; a value less than 0 if "this" object's comparison value is lexicographically less; and a value greater than 0 if "this" object's comparison value is lexicographically greater
	 */
	@Override
	public int compareTo(GDCFile o)
	{
		int compare = this.mUUID.compareTo(o.mUUID);
		if (0==compare)
		{
			compare = this.mName.compareTo(o.mName);
			if (0==compare)
			{
				compare = this.mMD5Sum.compareTo(o.mMD5Sum);
				if (0==compare)
				{
					Integer thisSize = this.mSamples.size();
					Integer oSize = o.mSamples.size();
					compare = thisSize.compareTo(oSize);
					if (0==compare)
					{
						thisSize = this.mPatients.size();
						oSize = o.mPatients.size();
						compare = thisSize.compareTo(oSize);
					}
				}
			}
		}
		return compare;
	}
	
	/**
	 * Static file to process theReader and generate the corresponding GDCFile
	 * 
	 * @param theReader A BufferedReader already opened to the file
	 * @param theManifest File object being read -- used for reporting exceptions only
	 * @return GDCFile build from the open file
	 * @throws IOException usually file not found or network error
	 * @throws Exception theManifest is used to name the file the threw the exception
	 */
	static public GDCFile loadFile(BufferedReader theReader, File theManifest) throws IOException, Exception
	{
		String uuid = null;
		String file = null;
		String md5 = null;
		UpdateableMap<Sample> samples = new UpdateableMap<>();
		UpdateableMap<Patient> patients = new UpdateableMap<>();
		// opening -FileStart- already read
		String line = theReader.readLine();
		// set line to null to end loop
		while (null!=line)
		{
			String [] splitted = line.split("\t", -1);
			if ("UUID".equals(splitted[0]))
			{
				uuid = splitted[1];
				line = theReader.readLine();
			}
			else if ("FILE".equals(splitted[0]))
			{
				file = splitted[1];
				line = theReader.readLine();
			}
			else if ("MD5".equals(splitted[0]))
			{
				md5 = splitted[1];
				line = theReader.readLine();
			}
			else if ("Sample".equals(splitted[0]))
			{
				Sample sam = Sample.loadFile(splitted);
				samples.put(sam.mUUID, sam);
				line = theReader.readLine();
			}
			else if ("Patient".equals(splitted[0]))
			{
				Patient pat = Patient.loadFile(splitted);
				patients.put(pat.mUUID, pat);
				line = theReader.readLine();
			}
			else if ("-FileDone-".equals(splitted[0]))
			{
				line = null;
			}
			else
			{
				throw new Exception("For " + theManifest + ": manifest line in file not understood '" + line + "'");
			}
		}
		return new GDCFile(uuid, file, md5, samples, patients);
	}
	
	/**
	 * Write this GDCFile information to a GDCAPI.*.tsv file for later records.
	 * 
	 * @param theWriter BufferedWriter open to file
	 * @throws IOException 
	 */
	public void writeFile(BufferedWriter theWriter) throws IOException
	{
		theWriter.write("-FileStart-");
		theWriter.newLine();
		theWriter.write("UUID\t");
		theWriter.write(mUUID);
		theWriter.newLine();
		theWriter.write("FILE\t");
		theWriter.write(mName);
		theWriter.newLine();
		theWriter.write("MD5\t");
		theWriter.write(mMD5Sum);
		theWriter.newLine();
		Collection<Updateable_Mixin<Sample>> sl = mSamples.values();
		for (Updateable_Mixin<Sample> mySample : sl)
		{
			((Sample)mySample).writeFile(theWriter);
		}
		Collection<Updateable_Mixin<Patient>> pl = mPatients.values();
		for (Updateable_Mixin<Patient> myPatient : pl)
		{
			((Patient)myPatient).writeFile(theWriter);
		}
		theWriter.write("-FileDone-");
		theWriter.newLine();
	}

	/**
	 * Indicates if the GDCFile passed in is different from "this" GDCFile,
	 * indicating that an existing dataset needs to be updated.
	 * 
	 * @param theNew Potentially new version of this GDCFile
	 * @return true if file needs to be update due to changes to existing dataset, or false
	 */
	@Override
	public boolean updateIfNeeded(Updateable_Mixin<GDCFile> theNew)
	{
		GDCFile local = (GDCFile)theNew;
		boolean updated = false;
		if (!this.mName.equals(local.mName))
		{
			this.mName = local.mName;
			updated = true;
		}
		if (!this.mMD5Sum.equals(local.mMD5Sum))
		{
			this.mMD5Sum = local.mMD5Sum;
			updated = true;
		}
		if (true == this.mSamples.updateFromTreeSets(local.mSamples))
		{
			updated = true;
		}
		if (true == this.mPatients.updateFromTreeSets(local.mPatients))
		{
			updated = true;
		}
		return updated;
	}

	/**
	 * gets UUID
	 * @return unique identifier for this file
	 */
	@Override
	public String getIdentifier()
	{
		return mUUID;
	}
	
	/**
	 * For this GDCFile object, look in the given directory for a file name
	 * matching expected name, uncompress to get readable file, and return the File object.
	 * 
	 * @param theDir look in the given directory for a file name matching expected name
	 * @param theUncompressP true if file on disk needs to be uncompressed
	 * @return File pointing to file to be processed
	 * @throws Exception Usually indicates corrupted gz file.
	 */
	public File getFileObj(File theDir, boolean theUncompressP) throws Exception
	{
		File result = null;
		String filename = this.mMD5Sum + "==" + this.mName;
		File dlFile = new File(theDir, filename);
		// if file ends in .gz, then decompress into tmp directory
		if ((true==theUncompressP)&&(filename.endsWith(".gz")))
		{
			File uncmp = new File(GDCAPI.M_TEMP_DIR, filename.substring(0, filename.length()-3));
			if (!uncmp.exists())
			{
				Uncompress.uncompressToFile(dlFile, uncmp);
			}
			result = uncmp;
		}
		else
		{
			result = dlFile;
		}
		return result;
	}
	
	/**
	 * Checks to see if a directory exists and has the file needed for process 
	 * of this dataset/GDCFile.
	 * 
	 * @param theDir Directory to check
	 * @return true if it exists, false otherwise
	 * @throws Exception 
	 */
	public boolean needsDownloadP(File theDir) throws Exception
	{
		boolean downloaded = false;
		File myDir = getFileObj(theDir, false);
		// TOOD: check MD5 ?
		if (myDir.exists())
		{
			// file exists
			downloaded = false;
		}
		else
		{
			downloaded = true;
		}
		return downloaded;
	}

	/**
	 * Somewhat user friendly string for this file -- mostly for logging and errors
	 * 
	 * @return Somewhat user friendly string for this file
	 */
	@Override
	public String toString()
	{
		return "GDCFile{" + "mUUID=" + mUUID + ", mName=" + mName + ", mMD5Sum=" + mMD5Sum + '}';
	}
}
