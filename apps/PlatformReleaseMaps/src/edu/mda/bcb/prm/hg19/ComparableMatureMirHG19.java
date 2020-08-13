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

package edu.mda.bcb.prm.hg19;

import java.util.ArrayList;

/**
 *
 * @author Tod-Casasent
 */
public class ComparableMatureMirHG19 implements Comparable<ComparableMatureMirHG19>
{
	public String mMatureMirId = null;
	public String mMatureMimatId = null;
	public ArrayList<String> mGeneSymbols = null;
	public ArrayList<String> mEntrezIds = null;
	public ArrayList<String> mPreMirIds = null;
	public ArrayList<String> mPreMiIds = null;

	public ComparableMatureMirHG19(String theMatureMirId, String theMatureMimatId, ArrayList<String> theGeneSymbols, ArrayList<String> theEntrezIds,
			ArrayList<String> thePreMirIds, ArrayList<String> thePreMiIds)
	{
		mMatureMirId = theMatureMirId;
		mMatureMimatId = theMatureMimatId;
		mGeneSymbols = new ArrayList<>();
		mGeneSymbols.addAll(theGeneSymbols);
		mEntrezIds = new ArrayList<>();
		mEntrezIds.addAll(theEntrezIds);
		mPreMirIds = new ArrayList<>();
		mPreMirIds.addAll(thePreMirIds);
		mPreMiIds = new ArrayList<>();
		mPreMiIds.addAll(thePreMiIds);
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
	public int compareTo(ComparableMatureMirHG19 o)
	{
		int comp = this.mMatureMirId.compareTo(o.mMatureMirId);
		if (0==comp)
		{
			comp = this.mMatureMimatId.compareTo(o.mMatureMimatId);
		}
		return comp;
	}
	
}
