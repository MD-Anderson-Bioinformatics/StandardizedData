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

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.convert.GisticTXT;
import edu.mda.bcb.gdc.api.convert.MethylationTXT;
import edu.mda.bcb.gdc.api.convert.MirnaTXT;
import edu.mda.bcb.gdc.api.convert.MutationMAF;
import edu.mda.bcb.gdc.api.convert.RnaseqTXT;
import edu.mda.bcb.gdc.api.convert.Snp6TXT;
import edu.mda.bcb.gdc.api.endpoints.DataDownload;
import edu.mda.bcb.gdc.api.util.UpdateableMap;
import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import java.io.File;
import java.util.Objects;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
abstract public class Fileable
{
	public Boolean mNotInGDC = null;
	public Boolean mNewFromGDC = null;
	public Boolean mUpdatedByGDC = null;
	public Boolean mReadFromFile = null;
	public UpdateableMap<GDCFile> mFiles = null;
	public String mTimestamp = null;
	
	public Fileable()
	{
		mFiles = new UpdateableMap<>();
		mNotInGDC = Boolean.FALSE;
		mNewFromGDC = null;
		mUpdatedByGDC = null;
		mReadFromFile = null;
		mTimestamp = null;
	}
	
	public boolean loadManifest(File theDir) throws Exception
	{
		boolean loaded = false;
		String timestamp = loadManifestInternal(theDir);
		if (null==timestamp)
		{
			loaded = false;
		}
		else
		{
			loaded = true;
			mTimestamp = timestamp;
		}
		return loaded;
	}
	
	abstract public String loadManifestInternal(File theDir) throws Exception;
	abstract public void writeManifest(File theDir) throws Exception;
	
	public void update(Fileable theNew) throws Exception
	{
		if (Objects.equals(Boolean.TRUE, theNew.mNotInGDC))
		{
			this.mNotInGDC = Boolean.TRUE;
		}
		else if (true==this.mFiles.updateFromTreeSets(theNew.mFiles))
		{
			this.mUpdatedByGDC = Boolean.TRUE;
		}
	}
	
	public void download(boolean theIsLegacyFlag, File theDownloadDir) throws Exception
	{
		if (!theDownloadDir.exists())
		{
			theDownloadDir.mkdirs();
		}
		DataDownload dd = new DataDownload(theIsLegacyFlag, this, theDownloadDir);
		dd.processEndpoint();
	}
	
	public GDCFile [] getGDCFiles(boolean theCheckSamplesP)
	{
		////////////////////////////////////////////////////////////////////////
		//// file array
		////////////////////////////////////////////////////////////////////////
		TreeSet<GDCFile> gdcFiles = new TreeSet<>();
		for (Updateable_Mixin<GDCFile> data : this.mFiles.values())
		{
			// skip files with more than one sample -- is an error
			GDCFile gdcFile = (GDCFile)data;
			if ((false==theCheckSamplesP) || (1==gdcFile.mSamples.size()))
			{
				gdcFiles.add((GDCFile)data);
			}
			else
			{
				GDCAPI.printWarn("Wrong number of samples '" + gdcFile.mSamples.size() + "' for " + gdcFile.toString() );
			}
		}
		final GDCFile [] gdcArray = gdcFiles.toArray(new GDCFile[0]);
		return gdcArray;
	}

	////////////////////////////////////////////////////////////////////////
	//// collect samples
	////////////////////////////////////////////////////////////////////////
	public TreeSet<String> getBarcodes(GDCFile [] theGdcFiles)
	{
		TreeSet<String> barcodes = new TreeSet<>();
		for (GDCFile gdcFile : theGdcFiles)
		{
			for (Updateable_Mixin<Sample> mySample : gdcFile.mSamples.values())
			{
				Sample objSample = ((Sample)mySample);
				barcodes.add(objSample.mBarcode);
			}
		}
		return barcodes;
	}
}
