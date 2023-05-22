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

package edu.mda.bcb.prm.hg19;

/**
 *
 * @author Tod-Casasent
 */
public class ComparableExonHG19 implements Comparable<ComparableExonHG19>
{
	public String mGeneSymbol = null;
	public String mUcscId = null;
	public String mEntrezId = null;
	public String mEnsemblId = null;
	public String mChromosome = null;
	public String mStartLoc = null;
	public String mEndLoc = null;
	public String mStrand = null;
	public long mStartCompositeCoord = -1;
	public int mExonNumber = -1;

	public ComparableExonHG19(String theGeneSymbol, String theUcscId, String theEntrezId, String theEnsemblId,
			String theChromosome, String theStartLoc, String theEndLoc, String theStrand, long theStartCompositeCoord, int theExonNumber)
	{
		mGeneSymbol = theGeneSymbol;
		if ("".equals(mGeneSymbol))
		{
			mGeneSymbol = "?";
		}
		mUcscId = theUcscId;
		mEntrezId = theEntrezId;
		mEnsemblId = theEnsemblId;
		//
		mChromosome = theChromosome;
		mStartLoc = theStartLoc;
		mEndLoc = theEndLoc;
		mStrand = theStrand;
		mStartCompositeCoord = theStartCompositeCoord;
		// assigned later
		mExonNumber = theExonNumber;
	}
	
	public String getGeneAppended()
	{
		return mGeneSymbol + "\t" + mUcscId + "\t" + mEntrezId + "\t" + mEnsemblId;
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
	public int compareTo(ComparableExonHG19 o)
	{
		int comp = compareNonNull(this.mGeneSymbol, o.mGeneSymbol);
		if (0==comp)
		{
			comp = compareNonNull(this.mUcscId, o.mUcscId);
			if (0==comp)
			{
				comp = compareNonNull(this.mEntrezId, o.mEntrezId);
				if (0==comp)
				{
					comp = compareNonNull(this.mEnsemblId, o.mEnsemblId);
					if (0==comp)
					{
						Long thisCoord = this.mStartCompositeCoord;
						Long oCoord = o.mStartCompositeCoord;
						comp = thisCoord.compareTo(oCoord);
					}
				}
			}
		}
		return comp;
	}
	
}
