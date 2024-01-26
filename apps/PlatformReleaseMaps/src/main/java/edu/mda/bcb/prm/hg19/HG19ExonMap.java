// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
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

import edu.mda.bcb.prm.IterateFile_Mixin;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class HG19ExonMap extends IterateFile_Mixin
{
	public TreeSet<String> mValidChromosomes = null;
	public TreeSet<ComparableExonHG19> mExons = null;
	
	public HG19ExonMap(TreeSet<String> theValidChromosomes, String theOutputFile)
	{
		super(theOutputFile, "unique\tgene-id\tucsc-id\tentrez-id\texon-number\tchromosome\tstart-loc\tend-loc\tstrand", null);
		mValidChromosomes = theValidChromosomes;
		mExons = new TreeSet<>();
		System.out.println("HG19ExonMap");
	}
	
	@Override
	public void processLine(String [] theSplittedLine) throws Exception
	{
		String featureType = getColumnValue("FeatureType", theSplittedLine);
		if ("componentExon".equals(featureType))
		{
			String [] splitted = null;
			String location = getColumnValue("FeatureID", theSplittedLine);
			splitted = location.split(":", -1);
			String chromosome = splitted[0].replace("chr", "");
			String start = getLocation(splitted[1], true);
			String stop = getLocation(splitted[1], false);
			String strand = splitted[2];
			if (mValidChromosomes.contains(chromosome))
			{
				String ucscGeneName = getColumnValue("Composite", theSplittedLine);
				String compositeCoordinates = getColumnValue("CompositeCoordinates", theSplittedLine);
				splitted = compositeCoordinates.split("-", -1);
				//String myLongStr = splitted[0];
				long compStart = Long.parseLong(splitted[0]);
				String unique = getColumnValue("Gene", theSplittedLine);
				String geneSymbol = "";
				String entrezId = "";
				if (!"".equals(unique))
				{
					splitted = unique.split("\\|", -1);
					geneSymbol = splitted[0];
					entrezId = splitted[1];
				}
				ComparableExonHG19 ce = new ComparableExonHG19(geneSymbol, ucscGeneName, entrezId, "", chromosome, start, stop, strand, compStart, -1);
				mExons.add(ce);
			}
		}
	}
	
	@Override
	public void postRun() throws Exception
	{
		// number exons
		String oldGene = "";
		int count = 1;
		for (ComparableExonHG19 ce : mExons)
		{
			String newGene = ce.getGeneAppended();
			if (!oldGene.equals(newGene))
			{
				oldGene = newGene;
				count = 1;
			}
			else
			{
				count = count + 1;
			}
			ce.mExonNumber = count;
		}
		// write file
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(mOutputFile)))
		{
			if (null!=mOutputHeaders)
			{
				bw.write(mOutputHeaders);
				bw.newLine();
			}
			for (ComparableExonHG19 ce : mExons)
			{
				// "unique\tgene-id\tucsc-id\tentrez-id\texon-number\tchromosome\tstart-loc\tend-loc\tstrand"
				bw.write(ce.mGeneSymbol + "|" + ce.mUcscId + "|" + ce.mExonNumber + "\t" + ce.mGeneSymbol + "\t" + ce.mUcscId + "\t" + ce.mEntrezId + "\t" + ce.mExonNumber + "\t" + ce.mChromosome + "\t" + ce.mStartLoc + "\t" + ce.mEndLoc + "\t" + ce.mStrand);
				bw.newLine();
			}
		}
	}

}
