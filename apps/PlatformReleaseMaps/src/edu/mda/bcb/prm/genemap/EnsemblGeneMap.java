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

package edu.mda.bcb.prm.genemap;

import edu.mda.bcb.prm.PlatformReleaseMaps;
import static edu.mda.bcb.prm.PlatformReleaseMaps.printVersion;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class EnsemblGeneMap
{
	static public TreeSet<String> M_VALID_CHROMOSOMES = new TreeSet<>();

	public TreeSet<String> run(String theDataInDir, String theDataOutDir) throws IOException, Exception
	{
		PlatformReleaseMaps.printVersion();
		TreeSet<String> outputFiles = new TreeSet<>();
		M_VALID_CHROMOSOMES.add("X");
		M_VALID_CHROMOSOMES.add("Y");
		M_VALID_CHROMOSOMES.add("1");
		M_VALID_CHROMOSOMES.add("2");
		M_VALID_CHROMOSOMES.add("3");
		M_VALID_CHROMOSOMES.add("4");
		M_VALID_CHROMOSOMES.add("5");
		M_VALID_CHROMOSOMES.add("6");
		M_VALID_CHROMOSOMES.add("7");
		M_VALID_CHROMOSOMES.add("8");
		M_VALID_CHROMOSOMES.add("9");
		M_VALID_CHROMOSOMES.add("10");
		M_VALID_CHROMOSOMES.add("11");
		M_VALID_CHROMOSOMES.add("12");
		M_VALID_CHROMOSOMES.add("13");
		M_VALID_CHROMOSOMES.add("14");
		M_VALID_CHROMOSOMES.add("15");
		M_VALID_CHROMOSOMES.add("16");
		M_VALID_CHROMOSOMES.add("17");
		M_VALID_CHROMOSOMES.add("18");
		M_VALID_CHROMOSOMES.add("19");
		M_VALID_CHROMOSOMES.add("20");
		M_VALID_CHROMOSOMES.add("21");
		M_VALID_CHROMOSOMES.add("22");
		System.out.println("Process NCBI36");
		String inputGtf = new File(theDataInDir, "Homo_sapiens.NCBI36.54.gtf.zip").getAbsolutePath();
		String outputScore = new File(theDataOutDir, "GeneInfo-hg18.txt").getAbsolutePath();
		String outputMap = new File(theDataOutDir, "HG18map.tsv").getAbsolutePath();
		String outputExons = new File(theDataOutDir, "HG18exons.tsv").getAbsolutePath();
		String outputTranscripts = new File(theDataOutDir, "HG18transcripts.tsv").getAbsolutePath();
		outputFiles.add(outputScore);
		outputFiles.add(outputMap);
		outputFiles.add(outputExons);
		outputFiles.add(outputTranscripts);
		convertGtfFiles(outputScore, outputMap, outputExons, outputTranscripts, 
				Gene.processEnsembleFileList36(inputGtf), Exon.processEnsembleFileList36(inputGtf), Transcript.processEnsembleFileList36(inputGtf));
		//
		System.out.println("Process GRCh37");
		inputGtf = new File(theDataInDir, "Homo_sapiens.GRCh37.75.gtf.zip").getAbsolutePath();
		outputScore = new File(theDataOutDir, "GeneInfo-hg19.txt").getAbsolutePath();
		outputMap = new File(theDataOutDir, "HG19map.tsv").getAbsolutePath();
		outputExons = new File(theDataOutDir, "HG19exons.tsv").getAbsolutePath();
		outputTranscripts = new File(theDataOutDir, "HG19transcripts.tsv").getAbsolutePath();
		outputFiles.add(outputScore);
		outputFiles.add(outputMap);
		outputFiles.add(outputExons);
		outputFiles.add(outputTranscripts);
		convertGtfFiles(outputScore, outputMap, outputExons, outputTranscripts, 
				Gene.processEnsembleFileList37(inputGtf), Exon.processEnsembleFileList37(inputGtf), Transcript.processEnsembleFileList37(inputGtf));
		//
		System.out.println("Process GRCh38");
		inputGtf = new File(theDataInDir, "Homo_sapiens.GRCh38.85.gtf.zip").getAbsolutePath();
		outputScore = new File(theDataOutDir, "GeneInfo-GRCh38.txt").getAbsolutePath();
		outputMap = new File(theDataOutDir, "GRCh38map.tsv").getAbsolutePath();
		outputExons = new File(theDataOutDir, "GRCh38exons.tsv").getAbsolutePath();
		outputTranscripts = new File(theDataOutDir, "GRCh38transcripts.tsv").getAbsolutePath();
		outputFiles.add(outputScore);
		outputFiles.add(outputMap);
		outputFiles.add(outputExons);
		outputFiles.add(outputTranscripts);
		convertGtfFiles(outputScore, outputMap, outputExons, outputTranscripts, 
				Gene.processEnsembleFileList38(inputGtf), Exon.processEnsembleFileList38(inputGtf), Transcript.processEnsembleFileList38(inputGtf));
		return outputFiles;
	}
	
	static public void convertGtfFiles(String theOutputScore, String theOutputMap, String theExonFile, String theTranscriptFile,
			TreeMap<String, Gene> theGenes, TreeMap<String, Exon> theExons, TreeMap<String, Transcript> theTranscripts) throws IOException
	{
		printVersion();
		// write theGenes files
		System.out.println("Write theGenes files");
		try(BufferedWriter bwScore = Files.newBufferedWriter(Paths.get(theOutputScore), StandardCharsets.UTF_8))
		{
			try(BufferedWriter bwMap = Files.newBufferedWriter(Paths.get(theOutputMap), StandardCharsets.UTF_8))
			{
				bwScore.write("HGNC symbol\tChromosome\tGene Start (bp)\tGene End (bp)");
				bwScore.newLine();
				bwMap.write("gene_symbol\tchromosome\tstart_loc\tstop_loc\tstrand");
				bwMap.newLine();
				for(Map.Entry<String, Gene> entry : theGenes.entrySet())
				{
					Gene loc = entry.getValue();
					String geneName = entry.getKey();
					bwScore.write(geneName + "\t" + loc.mChromosome + "\t" + loc.mStart + "\t" + loc.mEnd);
					bwScore.newLine();
					bwMap.write(geneName + "\t" + loc.mChromosome + "\t" + loc.mStart + "\t" + loc.mEnd + "\t" + loc.mStrand);
					bwMap.newLine();
				}
			}
		}
		// write theExons files
		System.out.println("Write theExons files");
		try(BufferedWriter bwExon = Files.newBufferedWriter(Paths.get(theExonFile), StandardCharsets.UTF_8))
		{
			bwExon.write("gene\tchromosome\tstart\tend\tstrand\texon_type\texon_id\texon_number\ttranscript_id\ttranscript_symbol");
			bwExon.newLine();
			for(Map.Entry<String, Exon> entry : theExons.entrySet())
			{
				Exon loc = entry.getValue();
				bwExon.write(loc.mGeneSymbol + "|" + loc.mEnsemblId + "\t" + loc.mChromosome + "\t" + loc.mStart + "\t" + loc.mEnd + "\t" + loc.mStrand + "\t" +
						loc.mExonType + "\t" + loc.mExonId + "\t" + loc.mExonNumber + "\t" + loc.mTranscriptId + "\t" + loc.mTranscriptSymbol);
				bwExon.newLine();
			}
		}
		// write theTranscripts files
		System.out.println("Write theTranscripts files");
		try(BufferedWriter bwTranscript = Files.newBufferedWriter(Paths.get(theTranscriptFile), StandardCharsets.UTF_8))
		{
			bwTranscript.write("gene\tchromosome\tstart\tend\tstrand\ttranscript_type\ttranscript_id\ttranscript_symbol");
			bwTranscript.newLine();
			for(Map.Entry<String, Transcript> entry : theTranscripts.entrySet())
			{
				Transcript loc = entry.getValue();
				bwTranscript.write(loc.mGeneSymbol + "|" + loc.mEnsemblId + "\t" + loc.mChromosome + "\t" + loc.mStart + "\t" + loc.mEnd + "\t" + loc.mStrand + "\t" +
						loc.mTranscriptType + "\t" + loc.mTranscriptId + "\t" + loc.mTranscriptSymbol);
				bwTranscript.newLine();
			}
		}
	}
	
	static public String padToString(int theInt)
	{
		return String.format("%05d", theInt);
	}
		
	static public String extractFromLastColumn(String theKey, String theColumnEntry)
	{
		// something like:
		// gene_id "ENSG00000265030"; transcript_id "ENST00000580568"; exon_number "1"; gene_name "RP11-763B22.6"; 
		String [] splitted = theColumnEntry.split(";", -1);
		String value = "not-found";
		for(String pair : splitted)
		{
			pair = pair.trim();
			String [] vals = pair.split(" ", -1);
			if (theKey.equals(vals[0]))
			{
				value = vals[1];
				value = value.replaceAll("\"", "");
			}
		}
		return value;
	}
	
	static public String extractGeneName(String theColumnEntry)
	{
		// something like:
		// gene_id "ENSG00000265030"; transcript_id "ENST00000580568"; exon_number "1"; gene_name "RP11-763B22.6"; 
		String geneName = extractFromLastColumn("gene_name", theColumnEntry);
		String geneId = extractFromLastColumn("gene_id", theColumnEntry);
		geneName = geneName + "|" + geneId;
		return geneName;
	}
}
