// Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.prm.hg38;

import java.util.ArrayList;

/**
 *
 * @author Tod-Casasent
 */
public class ComparableMatureMirHG38 implements Comparable<ComparableMatureMirHG38>
{
	public String mMatureMirId = null;
	public String mMatureMimatId = null;
	public ArrayList<String> mPreMirIds = null;
	public ArrayList<String> mPreMiIds = null;
	public String mUniqueId = null;

	public ComparableMatureMirHG38(String theUniqueId, String theMatureMirId, String theMatureMimatId, 
			ArrayList<String> thePreMirIds, ArrayList<String> thePreMiIds)
	{
		mUniqueId = theUniqueId;
		mMatureMirId = theMatureMirId;
		mMatureMimatId = theMatureMimatId;
		mPreMirIds = new ArrayList<>();
		if (null!=thePreMirIds)
		{
			mPreMirIds.addAll(thePreMirIds);
		}
		mPreMiIds = new ArrayList<>();
		if (null!=thePreMiIds)
		{
			mPreMiIds.addAll(thePreMiIds);
		}
	}
	
	public String getCommaList(ArrayList<String> theList)
	{
		String result = null;
		for(String token : theList)
		{
			if (null==result)
			{
				result = token;
			}
			else
			{
				result = result + "," + token;
			}
		}
		if (null==result)
		{
			result = "";
		}
		return result;
	}

	@Override
	public int compareTo(ComparableMatureMirHG38 o)
	{
		int comp = this.mMatureMirId.compareTo(o.mMatureMirId);
		if (0==comp)
		{
			comp = this.mMatureMimatId.compareTo(o.mMatureMimatId);
		}
		return comp;
	}
	
}
