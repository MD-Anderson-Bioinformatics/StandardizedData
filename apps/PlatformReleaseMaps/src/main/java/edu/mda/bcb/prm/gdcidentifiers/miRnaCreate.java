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

package edu.mda.bcb.prm.gdcidentifiers;

import edu.mda.bcb.prm.PlatformReleaseMaps;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class miRnaCreate
{
	protected String mLineHeaders = null;
	protected TreeSet<String> mLineData = null;
	
	public miRnaCreate()
	{
		mLineData = new TreeSet<>();
	}
	
	protected String makeLine(String theMirId, String theMimatId, String theType,
			String theChromosome, String theStartingCoord, String theEndingCoord, String theStrand,
			String theDerivedFrom)
	{
		return theMirId + "\t" + theMimatId + "\t" + theType + "\t" + 
				theChromosome + "\t" + theStartingCoord + "\t" + theEndingCoord + "\t" + theStrand + "\t" + 
				theDerivedFrom;
	}
	
	protected String getToken(String theString, String theTokenId)
	{
		//Example strings
		// GFF3	ID=MI0022705;Alias=MI0022705;Name=hsa-mir-6859-1
		// GFF	ACC="MI0006363"; ID="hsa-mir-1302-2";
		String [] splitted = theString.split(";", -1);
		String result = "none";
		for(String substr : splitted)
		{
			substr = substr.trim();
			if (substr.startsWith(theTokenId))
			{
				substr = substr.substring(theTokenId.length()+1);
				substr = substr.trim();
				substr = substr.replaceAll("\"", "");
				result = substr.trim();
			}
		}
		return result;
	}
	
	protected void parseAndMakeLine(String [] theLine)
	{
		//Example lines
		// GFF3
		// chr1	.	miRNA_primary_transcript	17369	17436	.	-	.	ID=MI0022705;Alias=MI0022705;Name=hsa-mir-6859-1
		// GFF
		// 1	.	miRNA	20229	20366	.	+	.	ACC="MI0006363"; ID="hsa-mir-1302-2";
		String chromosome = theLine[0].replace("chr", "");
		String mir_type = theLine[2];
		String location_start = theLine[3];
		String location_end = theLine[4];
		String strand = theLine[6];
		String parsing = theLine[8];
		String mimat_id = null;
		String mir_id = null;
		String derived_from = null;
		if (parsing.startsWith("ID"))
		{
			// GFF3
			mimat_id = getToken(parsing, "ID");
			mir_id = getToken(parsing, "Name");
			derived_from = getToken(parsing, "Derives_from");
		}
		else
		{
			// GFF
			mimat_id = getToken(parsing, "ACC");
			mir_id = getToken(parsing, "ID");
			derived_from = "NaN";
		}
		if (Integer.parseInt(location_start)>Integer.parseInt(location_end))
		{
			location_start = location_end;
			location_end = location_start;
		}
		mLineData.add(makeLine(mir_id, mimat_id, mir_type, chromosome, location_start, location_end, strand, derived_from));
	}
	
	public void fileConvert(String theSource, String theDest) throws IOException, Exception
	{
		System.out.println("Convert source = " + theSource);
		System.out.println("Convert dest = " + theDest);
		mLineData.clear();
		mLineHeaders = makeLine("mir_id", "mimat_id", "mir_type", "chromosome", "location_start", "location_end", "strand", "derived_from");
		TreeMap<String, Integer> headers = null;
		// open file for reading
		int line = 0;
		try(BufferedReader br = PlatformReleaseMaps.getBufferedReaderForZip(theSource))
		{
			String strLine = br.readLine();
			while(null!=strLine)
			{
				if (strLine.startsWith("#"))
				{
					// skip - line is a comment
				}
				else
				{
					//System.out.println("processing line " + line);
					// process lines
					String [] splitted = strLine.split("\t", -1);
					parseAndMakeLine(splitted);
				}
				strLine = br.readLine();
				line = line + 1;
			}
			System.out.println("processed " + line + " lines");
		}
		// open file for writing
		System.out.println("write " + theDest);
		OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
		Charset cs = StandardCharsets.UTF_8;
		//Charset cs = Charset.availableCharsets().get("ISO-8859-1");
		try(BufferedWriter bw = Files.newBufferedWriter(Paths.get(theDest), cs, options))
		{
			bw.write(mLineHeaders);
			bw.newLine();
			for(String myLine : mLineData)
			{
				bw.write(myLine);
				bw.newLine();
			}
		}
	}
}
