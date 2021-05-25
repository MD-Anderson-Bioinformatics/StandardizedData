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

import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author Tod-Casasent
 */
public class Patient implements Comparable<Patient>, Updateable_Mixin<Patient>
{
	public String mUUID = null;
	public String mBarcode = null;
	
	public Patient(String theUUID, String theBarcode)
	{
		mUUID = theUUID;
		mBarcode = theBarcode;
	}

	@Override
	public int compareTo(Patient o)
	{
		int compare = this.mUUID.compareTo(o.mUUID);
		if (0==compare)
		{
			compare = this.mBarcode.compareTo(o.mBarcode);
		}
		return compare;
	}
	
	static public Patient loadFile(String [] theSplitted)
	{
		return new Patient(theSplitted[1], theSplitted[2]);
	}
	
	public void writeFile(BufferedWriter theWriter) throws IOException
	{
		theWriter.write("Patient");
		theWriter.write("\t");
		theWriter.write(mUUID);
		theWriter.write("\t");
		theWriter.write(mBarcode);
		theWriter.newLine();
	}

	@Override
	public boolean updateIfNeeded(Updateable_Mixin<Patient> theNew)
	{
		Patient local = (Patient)theNew;
		boolean updated = false;
		if (!this.mBarcode.equals(local.mBarcode))
		{
			this.mBarcode = local.mBarcode;
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
