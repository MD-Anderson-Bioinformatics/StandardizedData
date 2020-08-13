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
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author Tod-Casasent
 */
public class Sample implements Comparable<Sample>, Updateable_Mixin<Sample>
{
	public String mUUID = null;
	public String mBarcode = null;
	public String mCaseUUID = null;
	public String mSampleType = null;
	
	public Sample(String theUUID, String theBarcode, String theCaseUUID, String theSampleType)
	{
		mUUID = theUUID;
		mBarcode = theBarcode;
		mCaseUUID = theCaseUUID;
		mSampleType = theSampleType;
	}

	@Override
	public int compareTo(Sample o)
	{
		int compare = this.mUUID.compareTo(o.mUUID);
		if (0==compare)
		{
			compare = this.mBarcode.compareTo(o.mBarcode);
		}
		return compare;
	}
	
	static public Sample loadFile(String [] theSplitted)
	{
		return new Sample(theSplitted[1], theSplitted[2], theSplitted[3], theSplitted[4]);
	}
	
	public void writeFile(BufferedWriter theWriter) throws IOException
	{
		theWriter.write("Sample");
		theWriter.write("\t");
		theWriter.write(mUUID);
		theWriter.write("\t");
		theWriter.write(mBarcode);
		theWriter.write("\t");
		theWriter.write(mCaseUUID);
		theWriter.write("\t");
		theWriter.write(mSampleType);
		theWriter.newLine();
	}

	@Override
	public boolean updateIfNeeded(Updateable_Mixin<Sample> theNew)
	{
		Sample local = (Sample)theNew;
		boolean updated = false;
		if (!this.mBarcode.equals(local.mBarcode))
		{
			this.mBarcode = local.mBarcode;
			updated = true;
		}
		if (!this.mCaseUUID.equals(local.mCaseUUID))
		{
			this.mCaseUUID = local.mCaseUUID;
			updated = true;
		}
		if (!this.mSampleType.equals(local.mSampleType))
		{
			this.mSampleType = local.mSampleType;
			updated = true;
		}
		return updated;
	}

	@Override
	public String getIdentifier()
	{
		return mUUID;
	}
}
