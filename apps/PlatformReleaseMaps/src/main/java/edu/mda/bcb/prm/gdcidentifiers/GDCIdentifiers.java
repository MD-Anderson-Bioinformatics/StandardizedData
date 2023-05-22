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

package edu.mda.bcb.prm.gdcidentifiers;

import edu.mda.bcb.prm.PlatformReleaseMaps;
import edu.mda.bcb.prm.genemap.EnsemblGeneMap;
import edu.mda.bcb.prm.genemap.Exon;
import edu.mda.bcb.prm.genemap.Gene;
import edu.mda.bcb.prm.genemap.Transcript;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class GDCIdentifiers
{

	public TreeSet<String> run(String theDataInDir, String theDataOutDir) throws IOException, Exception
	{
		PlatformReleaseMaps.printVersion();
		TreeSet<String> outputFiles = new TreeSet<>();
		//String baseDir = args[0];
		PrepFiles pf = new PrepFiles();
		pf.run(theDataInDir, theDataOutDir);
		//
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("X");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("Y");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("1");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("2");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("3");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("4");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("5");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("6");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("7");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("8");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("9");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("10");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("11");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("12");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("13");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("14");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("15");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("16");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("17");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("18");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("19");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("20");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("21");
		EnsemblGeneMap.M_VALID_CHROMOSOMES.add("22");
		String inputGtf = new File(theDataInDir, "gencode.v36.annotation.gtf.zip").getAbsolutePath();
		String outputScore = new File(theDataOutDir, "GeneInfo-GRCh38.txt").getAbsolutePath();
		String outputMap = new File(theDataOutDir, "GRCh38map.tsv").getAbsolutePath();
		String outputExons = new File(theDataOutDir, "GRCh38exons.tsv").getAbsolutePath();
		String outputTranscripts = new File(theDataOutDir, "GRCh38transcripts.tsv").getAbsolutePath();
		EnsemblGeneMap.convertGtfFiles(outputScore, outputMap, outputExons, outputTranscripts, 
				Gene.processEnsembleFileListGDC38(inputGtf), Exon.processEnsembleFileListGDC38(inputGtf), Transcript.processEnsembleFileListGDC38(inputGtf));
		outputFiles.add(outputScore);
		outputFiles.add(outputMap);
		outputFiles.add(outputExons);
		outputFiles.add(outputTranscripts);
		// build rnaseqMap.tsv
		String rnaseqInTranscripts = new File(theDataOutDir, "GRCh38transcripts.tsv").getAbsolutePath();
		String rnaseqInGeneIds = new File(theDataOutDir, "entreznumTOgenesymbol.tsv").getAbsolutePath();
		String rnaseqMapOut = new File(theDataOutDir, "rnaseqMap.tsv").getAbsolutePath();
		buildRNASeqMap(rnaseqInTranscripts, rnaseqInGeneIds, rnaseqMapOut);
		outputFiles.add(rnaseqInGeneIds);
		outputFiles.add(rnaseqMapOut);
		// build mirHG38map.tsv
		// hsa_mb21_hg38.gff3 comes from Ensembl but is the same release (mb21) as the GDC is using
		String miRNAhg38input = new File(theDataInDir, "hsa_mb21_hg38.gff3.zip").getAbsolutePath();
		String miRNAhg38output = new File(theDataOutDir, "mirHG38map.tsv").getAbsolutePath();
		miRnaCreate mirna = new miRnaCreate();
		mirna.fileConvert(miRNAhg38input, miRNAhg38output);
		outputFiles.add(miRNAhg38output);
		return outputFiles;
	}
	
	static public void buildRNASeqMap(String theTranscripts, String theGeneIds, String theMapOut) throws FileNotFoundException, IOException
	{
		// read in gene ids, first column to second column
		TreeMap<String, String> symbolToId = new TreeMap<>();
		try(BufferedReader br = new BufferedReader(new FileReader(theGeneIds)))
		{
			String inLine = br.readLine();
			while(null!=inLine)
			{
				String [] splitted = inLine.split("\t", -1);
				if ((!"".equals(splitted[0])) && (!"".equals(splitted[1])))
				{
					symbolToId.put(splitted[0], splitted[1]);
				}
				inLine = br.readLine();
			}
		}
		// read from theTranscripts
		try(BufferedReader br = new BufferedReader(new FileReader(theTranscripts));
				BufferedWriter bw = new BufferedWriter(new FileWriter(theMapOut)))
		{
			bw.write("gene_symbol\tgene_id\tversion_index\tchromosome\tlocation_start\tlocation_end\tstrand");
			bw.newLine();
			String inLine = br.readLine();
			// skip header line
			inLine = br.readLine();
			while(null!=inLine)
			{
				// for each line of transcripts, write to map out
				String [] splitted = inLine.split("\t", -1);
				String gene_symbol = splitted[0].split("\\|")[0];
				String gene_id = symbolToId.get(gene_symbol);
				if (null==gene_id)
				{
					gene_id = "unknown";
				}
				String version_index = splitted[6];
				String chromosome = splitted[1];
				String location_start = splitted[2];
				String location_end = splitted[3];
				String strand = splitted[4];
				bw.write(gene_symbol + "\t" + gene_id + "\t" + version_index + "\t" + chromosome + "\t" + location_start + "\t" + location_end + "\t" + strand);
				bw.newLine();
				// next input line
				inLine = br.readLine();
			}
		}
	}
}
