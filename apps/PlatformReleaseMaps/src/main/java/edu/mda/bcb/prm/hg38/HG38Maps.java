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

package edu.mda.bcb.prm.hg38;

import edu.mda.bcb.prm.PlatformReleaseMaps;
import java.io.File;
import java.io.IOException;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class HG38Maps
{
	public TreeSet<String> mValidChromosomes = null;
			
	public HG38Maps()
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
		HG38GeneMap hg38geneMap = new HG38GeneMap(mValidChromosomes, new File(theDataOutDir, "HG38_Genes.tsv").getAbsolutePath());
		outputFiles.add(hg38geneMap.run(new File(theDataInDir, "gencode.v36.annotation.gtf.zip").getAbsolutePath()));
		// Transcripts - map
		HG38TranscriptMap hg38transcriptMap = new HG38TranscriptMap(mValidChromosomes, new File(theDataOutDir, "HG38_Transcripts.tsv").getAbsolutePath());
		outputFiles.add(hg38transcriptMap.run(new File(theDataInDir, "gencode.v36.annotation.gtf.zip").getAbsolutePath()));
		// Exons - map
		HG38ExonMap hg38ExonMap = new HG38ExonMap(mValidChromosomes, new File(theDataOutDir, "HG38_Exons.tsv").getAbsolutePath());
		outputFiles.add(hg38ExonMap.run(new File(theDataInDir, "gencode.v36.annotation.gtf.zip").getAbsolutePath()));
		// miRNA - map
		HG38MirMap hg38MirMap = new HG38MirMap(mValidChromosomes, new File(theDataOutDir, "HG38_Mirs.tsv").getAbsolutePath());
		outputFiles.add(hg38MirMap.run(new File(theDataInDir, "mirna.txt.zip").getAbsolutePath()));
		outputFiles.add(hg38MirMap.run(new File(theDataInDir, "mirna_mature.txt.zip").getAbsolutePath()));
		outputFiles.add(hg38MirMap.run(new File(theDataInDir, "mirna_chromosome_build.txt.zip").getAbsolutePath()));
		outputFiles.add(hg38MirMap.run(new File(theDataInDir, "mirna_pre_mature.txt.zip").getAbsolutePath()));
		// SNP6 - map
		HG38Snp6Map hg38snp6map = new HG38Snp6Map(mValidChromosomes, new File(theDataOutDir, "HG38_SNP6.tsv").getAbsolutePath());
		outputFiles.add(hg38snp6map.run(new File(theDataInDir, "snp6.na35.liftoverhg38.txt.zip").getAbsolutePath()));
		// SeSAMe Probes
		HG38SesameMethMap hg38sesameMethMap = new HG38SesameMethMap(mValidChromosomes, new File(theDataOutDir, "HG38_SSM450.tsv").getAbsolutePath());
		outputFiles.add(hg38sesameMethMap.run(new File(theDataInDir, "HM450.hg38.manifest.gencode.v36.tsv.zip").getAbsolutePath()));
		hg38sesameMethMap = new HG38SesameMethMap(mValidChromosomes, new File(theDataOutDir, "HG38_SSM27.tsv").getAbsolutePath());
		outputFiles.add(hg38sesameMethMap.run(new File(theDataInDir, "HM27.hg38.manifest.gencode.v36.tsv.zip").getAbsolutePath()));
		//
		return outputFiles;
	}
	
}
