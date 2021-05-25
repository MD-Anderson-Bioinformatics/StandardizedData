// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
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
public class HG19Snp6Map extends IterateFile_Mixin
{
	public TreeSet<String> mValidChromosomes = null;
	
	public HG19Snp6Map(TreeSet<String> theValidChromosomes, String theOutputFile)
	{
		super(theOutputFile, "probe-id\tchromosome\tlocation\tstrand\tdbsnp\tgene-symbols\tentrez-ids", null);
		mValidChromosomes = theValidChromosomes;
		System.out.println("HG19Snp6Map");
	}
	
	@Override
	public void processLine(String [] theSplittedLine) throws Exception
	{
		String featureType = getColumnValue("FeatureType", theSplittedLine);
		if ("AffySNP".equals(featureType))
		{
			String [] splitted = null;
			String featureId = getColumnValue("FeatureID", theSplittedLine);
			String location = getColumnValue("CompositeCoordinates", theSplittedLine);
			//
			String gene = getColumnValue("Gene", theSplittedLine);
			String geneSymbol = null;
			String entrezId = null;
			if (!"".equals(gene))
			{
				splitted = gene.split(";", -1);
				for(String token : splitted )
				{
					String [] subSplit = token.split("\\|", -1);
					if (null==geneSymbol)
					{
						geneSymbol = subSplit[0];
						entrezId = subSplit[1];
					}
					else
					{
						geneSymbol = geneSymbol + "," + subSplit[0];
						entrezId = entrezId + "," + subSplit[1];
					}
				}
			}
			if (null==geneSymbol)
			{
				geneSymbol = "";
				entrezId = "";
			}
			String featureInfo = getColumnValue("FeatureInfo", theSplittedLine);
			splitted = featureInfo.split(";", -1);
			featureInfo = splitted[splitted.length-1];
			splitted = featureInfo.split("=", -1);
			featureInfo = splitted[1];
			splitted = location.split(":", -1);
			String chromosome = splitted[0].replace("chr", "");
			String probeLoc = splitted[1];
			String strand = splitted[2];
			if (mValidChromosomes.contains(chromosome))
			{
				//"probe-id\tchromosome\tlocation\tstrand\tdbsnp\tgene-symbols\tentrez-ids"
				mOutputLines.add(featureId + "\t" + chromosome + "\t" + probeLoc + "\t" + strand + "\t" + featureInfo + "\t" + geneSymbol + "\t" + entrezId);
			}
		}
	}
}
