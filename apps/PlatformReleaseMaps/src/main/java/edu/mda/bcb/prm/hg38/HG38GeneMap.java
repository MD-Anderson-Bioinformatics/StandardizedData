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

package edu.mda.bcb.prm.hg38;

import edu.mda.bcb.prm.IterateFile_Mixin;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class HG38GeneMap extends IterateFile_Mixin
{
	public TreeSet<String> mValidChromosomes = null;
	
	public HG38GeneMap(TreeSet<String> theValidChromosomes, String theOutputFile)
	{
		super(theOutputFile, "unique\tgene-symbol\tensembl-id\tchromosome\tstart-loc\tend-loc\tstrand\tgene-type\tcalling-center", "#");
		mValidChromosomes = theValidChromosomes;
		System.out.println("HG38GeneMap");
	}
	
	@Override
	public void processLine(String [] theSplittedLine) throws Exception
	{
		String featureType = theSplittedLine[2];
		if ("gene".equals(featureType))
		{
			String chromosome = theSplittedLine[0].replace("chr", "");
			if (mValidChromosomes.contains(chromosome))
			{
				String start = theSplittedLine[3];
				String stop = theSplittedLine[4];
				String strand = theSplittedLine[6];
				String center = theSplittedLine[1];
				String ensemblId = extractValueGTF("gene_id", theSplittedLine[8]);
				String geneSymbol = extractValueGTF("gene_name", theSplittedLine[8]);
				String geneType = extractValueGTF("gene_type", theSplittedLine[8]);
				//"unique\tgene-symbol\tensembl-id\tchromosome\tstart-loc\tend-loc\tstrand\tgene-type\tcalling-center";
				mOutputLines.add(geneSymbol + "|" + ensemblId + "\t" + geneSymbol + "\t" + ensemblId + "\t" + chromosome + "\t" + start + "\t" + stop + "\t" + strand + "\t" + geneType + "\t" + center);
			}
		}
	}
}
