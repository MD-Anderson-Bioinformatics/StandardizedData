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

import edu.mda.bcb.prm.IterateFile_Mixin;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class HG19GeneMap extends IterateFile_Mixin
{
	public TreeSet<String> mValidChromosomes = null;
	
	public HG19GeneMap(TreeSet<String> theValidChromosomes, String theOutputFile)
	{
		super(theOutputFile, "unique\tgene-symbol\tentrez-id\ttranscript\tchromosome\tstart-loc\tend-loc\tstrand", null);
		mValidChromosomes = theValidChromosomes;
		System.out.println("HG19GeneMap");
	}
	
	@Override
	public void processLine(String [] theSplittedLine) throws Exception
	{
		String featureType = getColumnValue("FeatureType", theSplittedLine);
		if ("gene".equals(featureType))
		{
			String [] splitted = null;
			String featureId = getColumnValue("FeatureID", theSplittedLine);
			String location = getColumnValue("CompositeCoordinates", theSplittedLine);
			String gene = getColumnValue("Gene", theSplittedLine);
			splitted = featureId.split("\\|", -1);
			String geneSymbol1 = splitted[0];
			String entrezId1 = splitted[1];
			String transcript = "";
			if (splitted.length>2)
			{
				transcript = splitted[2];
			}
			splitted = gene.split("\\|", -1);
			String geneSymbol2 = splitted[0];
			String entrezId2 = splitted[1];
			splitted = location.split(":", -1);
			String chromosome = splitted[0].replace("chr", "");
			String start = getLocation(splitted[1], true);
			String stop = getLocation(splitted[1], false);
			String strand = splitted[2];
			if (mValidChromosomes.contains(chromosome))
			{
				if (!geneSymbol1.equals(geneSymbol2))
				{
					throw new Exception("Gene symbol '" + geneSymbol1 + "' does not match '" + geneSymbol2 + "'");
				}
				if (!entrezId1.equals(entrezId2))
				{
					throw new Exception("Entrez id '" + entrezId1 + "' does not match '" + entrezId2 + "'");
				}
				//"unique\tgene-symbol\tentrez-id\ttranscript\tchromosome\tstart-loc\tend-loc\tstrand";
				mOutputLines.add(featureId + "\t" + geneSymbol1 + "\t" + entrezId1 + "\t" + transcript + "\t" + chromosome + "\t" + start + "\t" + stop + "\t" + strand);
			}
		}
	}
}
