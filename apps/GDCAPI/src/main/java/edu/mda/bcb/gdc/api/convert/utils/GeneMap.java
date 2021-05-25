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

package edu.mda.bcb.gdc.api.convert.utils;

import edu.mda.bcb.gdc.api.GDCAPI;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class GeneMap
{

	private TreeSet<String> mGenes = null;
	private TreeMap<String, String> mChromosome = null;
	private TreeMap<String, Long> mStart = null;
	private TreeMap<String, Long> mEnd = null;

	public GeneMap()
	{
		mGenes = new TreeSet<>();
		mChromosome = new TreeMap<>();
		mStart = new TreeMap<>();
		mEnd = new TreeMap<>();
	}

	public void load(File theGeneMapFile) throws IOException
	{
		mGenes.clear();
		mChromosome.clear();
		mStart.clear();
		mEnd.clear();
		//
		ArrayList<String> headers = null;
		String inLine = null;
		long fileCounter = 0;
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theGeneMapFile.toPath(), Charset.availableCharsets().get("UTF-8")))
		{
			inLine = br.readLine();
			while(null!=inLine)
			{
				String [] splitted = inLine.split("\t", -1);
				if (null==headers)
				{
					headers = new ArrayList<>();
					headers.addAll(Arrays.asList(splitted));
				}
				else
				{
					fileCounter = fileCounter + 1;
					String gene = GDCAPI.getColumn("unique", splitted, headers);
					String chromosome = GDCAPI.getColumn("chromosome", splitted, headers);
					String start = GDCAPI.getColumn("start-loc", splitted, headers);
					String end = GDCAPI.getColumn("end-loc", splitted, headers);
					mGenes.add(gene);
					mChromosome.put(gene, chromosome);
					mStart.put(gene, Long.parseLong(start));
					mEnd.put(gene, Long.parseLong(end));
				}
				inLine = br.readLine();
			}
		}
	}

	public String[] getGeneList()
	{
		return mGenes.toArray(new String[0]);
	}

	public String getGeneChromosome(String theGene)
	{
		return mChromosome.get(theGene);
	}

	public long getGeneStart(String theGene)
	{
		return mStart.get(theGene);
	}

	public long getGeneEnd(String theGene)
	{
		return mEnd.get(theGene);
	}
}
