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

import edu.mda.bcb.prm.IterateFile_Mixin;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class HG19TranscriptMap extends IterateFile_Mixin
{
	public TreeSet<String> mValidChromosomes = null;
	
	public HG19TranscriptMap(TreeSet<String> theValidChromosomes, String theOutputFile)
	{
		super(theOutputFile, "unique\tucsc-transcript-id\tgene-symbol\tentrez-id\tchromosome\tstart-loc\tend-loc\tstrand", null);
		mValidChromosomes = theValidChromosomes;
		System.out.println("HG19TranscriptMap");
	}
	
	@Override
	public void processLine(String [] theSplittedLine) throws Exception
	{
		String featureType = getColumnValue("FeatureType", theSplittedLine);
		String composite = getColumnValue("Composite", theSplittedLine);
		if (("transcript".equals(featureType))&&("GRCh37".equals(composite)))
		{
			String [] splitted = null;
			String featureId = getColumnValue("FeatureID", theSplittedLine);
			String location = getColumnValue("CompositeCoordinates", theSplittedLine);
			//
			String gene = getColumnValue("Gene", theSplittedLine);
			String geneSymbol = "";
			String entrezId = "";
			if (!"".equals(gene))
			{
				splitted = gene.split("\\|", -1);
				geneSymbol = splitted[0];
				entrezId = splitted[1];
			}
			if ("".equals(geneSymbol))
			{
				geneSymbol = "?";
			}
			splitted = location.split(":", -1);
			String chromosome = splitted[0].replace("chr", "");
			String start = getLocation(splitted[1], true);
			String stop = getLocation(splitted[1], false);
			String strand = splitted[2];
			if (mValidChromosomes.contains(chromosome))
			{
				//"unique\tucsc-transcript-id\tgene-id\tgene-symbol\tentrez-id\tchromosome\tstart-loc\tend-loc\tstrand";
				mOutputLines.add(geneSymbol + "|" + featureId + "\t" + featureId + "\t" + geneSymbol + "\t" + entrezId + "\t" + chromosome + "\t" + start + "\t" + stop + "\t" + strand);
			}
		}
	}
}
