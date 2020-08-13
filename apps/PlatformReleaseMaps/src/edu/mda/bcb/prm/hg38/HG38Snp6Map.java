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

package edu.mda.bcb.prm.hg38;

import edu.mda.bcb.prm.IterateFile_Mixin;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class HG38Snp6Map extends IterateFile_Mixin
{
	public TreeSet<String> mValidChromosomes = null;
	
	public HG38Snp6Map(TreeSet<String> theValidChromosomes, String theOutputFile)
	{
		super(theOutputFile, "probe-id\tchromosome\tlocation\tstrand", null);
		mValidChromosomes = theValidChromosomes;
		System.out.println("HG38Snp6Map");
	}
	
	@Override
	public void processLine(String [] theSplittedLine) throws Exception
	{
		String probeid = getColumnValue("probeid", theSplittedLine);
		String chr = getColumnValue("chr", theSplittedLine);
		String pos = getColumnValue("pos", theSplittedLine);
		String strand = getColumnValue("strand", theSplittedLine);
		//"probe-id\tchromosome\tlocation\tstrand"
		if (mValidChromosomes.contains(chr))
		{
			mOutputLines.add(probeid + "\t" + chr + "\t" + pos + "\t" + strand);
		}
	}
}
