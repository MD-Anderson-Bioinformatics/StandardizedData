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

import edu.mda.bcb.prm.PlatformReleaseMaps;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class HG19Maps
{
	public TreeSet<String> mValidChromosomes = null;
			
	public HG19Maps()
	{
		mValidChromosomes = new TreeSet<>();
		// note: this skips mitochondrial and other chromosomes
		mValidChromosomes.add("X");
		mValidChromosomes.add("Y");
		mValidChromosomes.add("1");
		mValidChromosomes.add("2");
		mValidChromosomes.add("3");
		mValidChromosomes.add("4");
		mValidChromosomes.add("5");
		mValidChromosomes.add("6");
		mValidChromosomes.add("7");
		mValidChromosomes.add("8");
		mValidChromosomes.add("9");
		mValidChromosomes.add("10");
		mValidChromosomes.add("11");
		mValidChromosomes.add("12");
		mValidChromosomes.add("13");
		mValidChromosomes.add("14");
		mValidChromosomes.add("15");
		mValidChromosomes.add("16");
		mValidChromosomes.add("17");
		mValidChromosomes.add("18");
		mValidChromosomes.add("19");
		mValidChromosomes.add("20");
		mValidChromosomes.add("21");
		mValidChromosomes.add("22");
	}
	

	public TreeSet<String> run(String theDataInDir, String theDataOutDir) throws IOException, Exception
	{
		PlatformReleaseMaps.printVersion();
		TreeSet<String> outputFiles = new TreeSet<>();
		// Genes - map
		HG19GeneMap hg19geneMap = new HG19GeneMap(mValidChromosomes, new File(theDataOutDir, "HG19_Genes.tsv").getAbsolutePath());
		outputFiles.add(hg19geneMap.run(new File(theDataInDir, "TCGA.hg19.June2011.gaf.zip").getAbsolutePath()));
		// Transcripts - map
		HG19TranscriptMap hg19transcriptMap = new HG19TranscriptMap(mValidChromosomes, new File(theDataOutDir, "HG19_Transcripts.tsv").getAbsolutePath());
		outputFiles.add(hg19transcriptMap.run(new File(theDataInDir, "TCGA.hg19.June2011.gaf.zip").getAbsolutePath()));
		// Exons - map
		HG19ExonMap hg19ExonMap = new HG19ExonMap(mValidChromosomes, new File(theDataOutDir, "HG19_Exons.tsv").getAbsolutePath());
		outputFiles.add(hg19ExonMap.run(new File(theDataInDir, "TCGA.hg19.June2011.gaf.zip").getAbsolutePath()));
		// miRNA - map
		HG19MirMap hg19MirMap = new HG19MirMap(mValidChromosomes, new File(theDataOutDir, "HG19_Mirs.tsv").getAbsolutePath());
		outputFiles.add(hg19MirMap.run(new File(theDataInDir, "TCGA.hg19.June2011.gaf.zip").getAbsolutePath()));
		// SNP6 - map		
		HG19Snp6Map hg19snp6map = new HG19Snp6Map(mValidChromosomes, new File(theDataOutDir, "HG19_SNP6.tsv").getAbsolutePath());
		outputFiles.add(hg19snp6map.run(new File(theDataInDir, "TCGA.hg19.June2011.gaf.zip").getAbsolutePath()));
		return outputFiles;
	}

}
