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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class HG19MirMap extends IterateFile_Mixin
{
	public TreeSet<String> mValidChromosomes = null;
	public TreeSet<ComparablePreMirHG19> mPreMirs = null;
	public TreeMap<String, ComparableMatureMirHG19> mMatureMirMap = null;
	public ComparablePreMirHG19 mPreviousPreMir = null;

	
	public HG19MirMap(TreeSet<String> theValidChromosomes, String theOutputFile)
	{
		super(theOutputFile, "unique\tmir-name\tmirbase-id\tmir-type\tgene-symbols\tentrez-ids\tpre-mir-names\tpre-mirbase-ids", null);
		mValidChromosomes = theValidChromosomes;
		mPreMirs = new TreeSet<>();
		mMatureMirMap = new TreeMap<>();
		mPreviousPreMir = null;
		System.out.println("HG19MirMap");
	}
	
	public void handlePreMiRNA(String [] theSplittedLine) throws Exception
	{
		String [] splitted = null;
		String featureId = getColumnValue("FeatureID", theSplittedLine);
		splitted = featureId.split("\\|", -1);
		String preMirId = splitted[0];
		String preMiId = splitted[1];
		String gene = getColumnValue("Gene", theSplittedLine);
		String geneSymbol = "";
		String entrezId = "";
		if (!"".equals(gene))
		{
			splitted = gene.split("\\|", -1);
			geneSymbol = splitted[0];
			entrezId = splitted[1];
		}
		ComparablePreMirHG19 mir = new ComparablePreMirHG19(preMirId, preMiId, geneSymbol, entrezId);
		if (false==mPreMirs.add(mir))
		{
			throw new Exception("Duplicate pre-mir " + preMirId);
		}
	}
	
	public void handleMatureRNA(String [] theSplittedLine) throws Exception
	{
		String [] splitted = null;
		String featureId = getColumnValue("FeatureID", theSplittedLine);
		splitted = featureId.split("\\|", -1);
		String matureMirId = splitted[0];
		String matureMimatId = splitted[1];
		// gene symbols and entrez ids
		String gene = getColumnValue("Gene", theSplittedLine);
		ArrayList<String> geneSymbols = new ArrayList<>();
		ArrayList<String> entrezIds = new ArrayList<>();
		if (!"".equals(gene))
		{
			splitted = gene.split(";", -1);
			for (String token : splitted)
			{
				// some tokens are "?" and need to be skipped
				if (token.contains("|"))
				{
					String [] subSplit = token.split("\\|", -1);
					geneSymbols.add(subSplit[0]);
					entrezIds.add(subSplit[1]);
				}
			}
		}
		// pre-mir ids and pre-mi ids
		// Composite hsa-mir-2110|MI0010629
		// or
		// FeatureInfo pre-miRNA=hsa-mir-432|MI0003133 or pre-miRNA=hsa-mir-548i-3|MI0006423,hsa-mir-548i-4|MI0006424,hsa-mir-548i-1|MI0006421,hsa-mir-548i-2|MI0006422
		ArrayList<String> preMirIds = new ArrayList<>();
		ArrayList<String> preMiIds = new ArrayList<>();
		String composite = getColumnValue("Composite", theSplittedLine);
		if (composite.contains("|"))
		{
			// one value
			String [] subSplit = composite.split("\\|", -1);
			preMirIds.add(subSplit[0]);
			preMiIds.add(subSplit[1]);
		}
		else
		{
			// multi values
			String featureInfo = getColumnValue("FeatureInfo", theSplittedLine);
			featureInfo = featureInfo.replaceFirst("pre-miRNA=", "");
			for(String pair : featureInfo.split(","))
			{
				String [] subSplit = pair.split("\\|", -1);
				preMirIds.add(subSplit[0]);
				preMiIds.add(subSplit[1]);
			}
		}
		ComparableMatureMirHG19 mir = mMatureMirMap.get(matureMirId);
		if (null==mir)
		{
			mir = new ComparableMatureMirHG19(matureMirId, matureMimatId, geneSymbols, entrezIds, preMirIds, preMiIds);
		}
		else
		{
			mir.mEntrezIds.addAll(entrezIds);
			mir.mGeneSymbols.addAll(geneSymbols);
			mir.mPreMirIds.addAll(preMirIds);
			mir.mPreMiIds.addAll(preMiIds);
		}
		mMatureMirMap.put(matureMirId, mir);
	}
	
	@Override
	public void processLine(String [] theSplittedLine) throws Exception
	{
		String featureType = getColumnValue("FeatureType", theSplittedLine);
		if ("pre-miRNA".equals(featureType))
		{
			handlePreMiRNA(theSplittedLine);
		}
		else if ("miRNA".equals(featureType))
		{
			handleMatureRNA(theSplittedLine);
		}
	}
	
	@Override
	public void postRun() throws Exception
	{
		// write file
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(mOutputFile)))
		{
			if (null!=mOutputHeaders)
			{
				bw.write(mOutputHeaders);
				bw.newLine();
			}
			for(ComparablePreMirHG19 mir : mPreMirs)
			{
				//"unique\tmir-name\tmirbase-id\tmir-type\tgene-symbols\tentrez-ids\tpre-mir-names\tpre-mirbase-ids"
				bw.write(mir.mPreMirId + "|" + mir.mPreMiId + "\t" + mir.mPreMirId + "\t" + mir.mPreMiId + "\t" + "pre-mir" + "\t" + mir.mGeneSymbol + "\t" + mir.mEntrezId + "\t" + "" + "\t" + "");
				bw.newLine();
			}
			for(ComparableMatureMirHG19 mir : mMatureMirMap.values())
			{
				//"unique\tmir-name\tmirbase-id\tmir-type\tgene-symbols\tentrez-ids\tpre-mir-names\tpre-mirbase-ids"
				bw.write(mir.mMatureMirId + "|" + mir.mMatureMimatId + "\t" + mir.mMatureMirId + "\t" + mir.mMatureMimatId + "\t" + "mature-mir" + "\t" + 
						mir.getCommaList(mir.mGeneSymbols) + "\t" + mir.getCommaList(mir.mEntrezIds) + "\t" + 
						mir.getCommaList(mir.mPreMirIds)  + "\t" + mir.getCommaList(mir.mPreMiIds) );
				bw.newLine();
			}
		}
	}

}
