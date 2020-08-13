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

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Tod-Casasent
 */
public class MethylationProbe implements Comparable<MethylationProbe>
{
	public String mProbe = null;
	public String mChromosome = null;
	public String mStart = null;
	public String mEnd = null;
	public ArrayList<String> mGenes = null;

	public MethylationProbe(String theProbe, String theChromosome, 
			String theStart, String theEnd, 
			String [] theGenes)
	{
		mProbe = theProbe;
		mChromosome = theChromosome;
		mStart = theStart;
		mEnd = theEnd;
		if ((null!=theGenes)&&(theGenes.length>0))
		{
			mGenes = new ArrayList<>();
			mGenes.addAll(Arrays.asList(theGenes));
		}
	}

	@Override
	public int compareTo(MethylationProbe o)
	{
		return this.toString().compareTo(o.toString());
	}
	
	static public String arrayToString(ArrayList<String> theArray, String theDelimiter)
	{
		String result = null;
		if (null!=theArray)
		{
			for (String element : theArray)
			{
				if (null==result)
				{
					result = element;
				}
				else
				{
					result = result + theDelimiter + element;
				}
			}
		}
		else
		{
			result = "";
		}
		return result;
	}

	@Override
	public String toString()
	{
		return mProbe + "\t"
				+ mChromosome + "\t"
				+ mStart + "\t"
				+ mEnd + "\t"
				+ arrayToString(mGenes, ";");
	}

	static public String getHeaders()
	{
		return "Probe" + "\t"
				+ "Chromosome" + "\t"
				+ "Start" + "\t"
				+ "End" + "\t"
				+ "Genes";
	}
}
