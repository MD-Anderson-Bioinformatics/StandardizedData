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

/**
 *
 * @author Tod-Casasent
 */
public class ComparablePreMirHG19 implements Comparable<ComparablePreMirHG19>
{
	public String mPreMirId = null;
	public String mPreMiId = null;
	public String mGeneSymbol = null;
	public String mEntrezId = null;

	public ComparablePreMirHG19(String thePreMirId, String thePreMiId, String theGeneSymbol, String theEntrezId)
	{
		mPreMirId = thePreMirId;
		mPreMiId = thePreMiId;
		mGeneSymbol = theGeneSymbol;
		mEntrezId = theEntrezId;
	}

	public int compareNonNull(String theA, String theB)
	{
		int comp = 0;
		if ((null!=theA)&&(null!=theB))
		{
			comp = theA.compareTo(theB);
		}
		return comp;
	}

	@Override
	public int compareTo(ComparablePreMirHG19 o)
	{
		int comp = compareNonNull(this.mPreMiId, o.mPreMiId);
		if (0==comp)
		{
			comp = compareNonNull(this.mPreMirId, o.mPreMirId);
			if (0==comp)
			{
				comp = compareNonNull(this.mGeneSymbol, o.mGeneSymbol);
				if (0==comp)
				{
					comp = compareNonNull(this.mEntrezId, o.mEntrezId);
				}
			}
		}
		return comp;
	}
	
}
