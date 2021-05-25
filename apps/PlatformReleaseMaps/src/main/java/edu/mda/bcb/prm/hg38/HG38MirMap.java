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

package edu.mda.bcb.prm.hg38;

import edu.mda.bcb.prm.IterateFile_Mixin;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class HG38MirMap extends IterateFile_Mixin
{
	public TreeSet<String> mValidChromosomes = null;
	public TreeMap<String, ComparablePreMirHG38> mPreMirMap = null;
	public TreeMap<String, ComparableMatureMirHG38> mMatureMirMap = null;
	public ComparablePreMirHG38 mPreviousPreMir = null;

	
	public HG38MirMap(TreeSet<String> theValidChromosomes, String theOutputFile)
	{
		super(theOutputFile, "unique\tmir-name\tmirbase-id\tmir-type\tchromosome\tstart-loc\tend-loc\tstrand\tpre-mir-names\tpre-mirbase-ids", null);
		mValidChromosomes = theValidChromosomes;
		mPreMirMap = new TreeMap<>();
		mMatureMirMap = new TreeMap<>();
		mPreviousPreMir = null;
		System.out.println("HG38MirMap");
	}
	
	public void handlePreMiRNA(String [] theSplittedLine) throws Exception
	{
		String [] splitted = null;
		String autoSpecies = getColumnValue("auto_species", theSplittedLine);
		// 22 is hsa homo sapiens
		if ("22".equals(autoSpecies))
		{
			String auto_mirna = getColumnValue("auto_mirna", theSplittedLine);
			String mirna_acc = getColumnValue("mirna_acc", theSplittedLine);
			String mirna_id = getColumnValue("mirna_id", theSplittedLine);
			ComparablePreMirHG38 mir = new ComparablePreMirHG38(auto_mirna, mirna_acc, mirna_id, null, null, null, null);
			if (null!=mPreMirMap.put(auto_mirna, mir))
			{
				throw new Exception("Duplicate pre-mir " + mirna_acc);
			}
		}
	}
	
	public void handleMatureRNA(String [] theSplittedLine) throws Exception
	{
		String [] splitted = null;
		String mature_name = getColumnValue("mature_name", theSplittedLine);
		if (mature_name.startsWith("hsa-"))
		{
			String auto_mature = getColumnValue("auto_mature", theSplittedLine);
			String mature_acc = getColumnValue("mature_acc", theSplittedLine);
			//String mature_name = getColumnValue("mature_name", theSplittedLine);
			
			ComparableMatureMirHG38 mir = mMatureMirMap.get(auto_mature);
			if (null==mir)
			{
				mir = new ComparableMatureMirHG38(auto_mature, mature_acc, mature_name, null, null);
			}
			else
			{
				throw new Exception("Duplicate mature mir auto_mature=" + auto_mature);
			}
			mMatureMirMap.put(auto_mature, mir);
		}
	}
	
	public void handleChromosome(String [] theSplittedLine) throws Exception
	{
		String [] splitted = null;
		String auto_mirna = getColumnValue("auto_mirna", theSplittedLine);
		ComparablePreMirHG38 mir = mPreMirMap.get(auto_mirna);
		if (null!=mir)
		{
			String xsome = getColumnValue("xsome", theSplittedLine);
			xsome = xsome.replace("chr", "");
			if (mValidChromosomes.contains(xsome))
			{
				String contig_start = getColumnValue("contig_start", theSplittedLine);
				String contig_end = getColumnValue("contig_end", theSplittedLine);
				String strand = getColumnValue("strand", theSplittedLine);
				mir.mChromosome = xsome;
				mir.mStartLoc = contig_start;
				mir.mEndLoc = contig_end;
				mir.mStrand = strand;
				mPreMirMap.put(auto_mirna, mir);
			}
		}
	}
	
	public void handleMatureMap(String [] theSplittedLine) throws Exception
	{
		String [] splitted = null;
		String auto_mirna = getColumnValue("auto_mirna", theSplittedLine);
		ComparablePreMirHG38 mir = mPreMirMap.get(auto_mirna);
		if (null!=mir)
		{
			String auto_mature = getColumnValue("auto_mature", theSplittedLine);
			ComparableMatureMirHG38 mature = mMatureMirMap.get(auto_mature);
			if (null!=mature)
			{
				mature.mPreMirIds.add(mir.mPreMirId);
				mature.mPreMiIds.add(mir.mPreMiId);
			}
		}
	}
	
	@Override
	public void processLine(String [] theSplittedLine) throws Exception
	{
		// files should be processed in this order
		if ("mirna.txt.zip".equals(mInputFileName))
		{
			handlePreMiRNA(theSplittedLine);
		}
		else if ("mirna_mature.txt.zip".equals(mInputFileName))
		{
			handleMatureRNA(theSplittedLine);
		}
		else if ("mirna_chromosome_build.txt.zip".equals(mInputFileName))
		{
			handleChromosome(theSplittedLine);
		}
		else if ("mirna_pre_mature.txt.zip".equals(mInputFileName))
		{
			handleMatureMap(theSplittedLine);
		}
	}
	
	protected void removeDuplicateMirs() throws Exception
	{
		// weirdly, the mirna data includes the same hsa and MIMAT id for mature mirs,
		// but with different auto_mature ids. One of the two has no genes associated with it
		// so for duplicates, remove the one without mPreMirIds and mPreMiIds
		//
		// copy id list to avoid concurrent modification issue
		TreeSet<String> auto_mature_ids = new TreeSet<>();
		auto_mature_ids.addAll(mMatureMirMap.keySet());
		for (String auto_mature : auto_mature_ids)
		{
			ComparableMatureMirHG38 myMir = mMatureMirMap.get(auto_mature);
			if (null!=myMir)
			{
				if (0==myMir.mPreMiIds.size())
				{
					for (String auto_mature2 : auto_mature_ids)
					{
						if (!auto_mature.equals(auto_mature2))
						{
							ComparableMatureMirHG38 cmpMir = mMatureMirMap.get(auto_mature2);
							if (null!=cmpMir)
							{
								if (!myMir.mUniqueId.equals(cmpMir.mUniqueId))
								{
									if (myMir.mMatureMimatId.equals(cmpMir.mMatureMimatId))
									{
										if (myMir.mMatureMirId.equals(cmpMir.mMatureMirId))
										{
											mMatureMirMap.remove(auto_mature);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void postRun() throws Exception
	{
		removeDuplicateMirs();
		// write file
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(mOutputFile)))
		{
			if (null!=mOutputHeaders)
			{
				bw.write(mOutputHeaders);
				bw.newLine();
			}
			for(ComparablePreMirHG38 mir : mPreMirMap.values())
			{
				//"unique\tmir-name\tmirbase-id\tmir-type\tchromosome\tstart-loc\tend-loc\tstrand\tpre-mir-names\tpre-mirbase-ids"
				bw.write(mir.mPreMiId + "|" + mir.mPreMirId + "\t" + mir.mPreMiId + "\t" + mir.mPreMirId + "\t" + "pre-mir" + "\t" + mir.mChromosome + "\t" + mir.mStartLoc + "\t" + mir.mEndLoc + "\t" + mir.mStrand + "\t" + "" + "\t" + "");
				bw.newLine();
			}
			for(ComparableMatureMirHG38 mir : mMatureMirMap.values())
			{
				//"unique\tmir-name\tmirbase-id\tmir-type\tchromosome\tstart-loc\tend-loc\tstrand\tpre-mir-names\tpre-mirbase-ids"
				bw.write(mir.mMatureMimatId + "|" + mir.mMatureMirId + "\t" + mir.mMatureMimatId + "\t" + mir.mMatureMirId + "\t" + "mature-mir" + "\t" + 
						"" + "\t" + "" + "\t" + "" + "\t" +
						mir.getCommaList(mir.mPreMirIds)  + "\t" + mir.getCommaList(mir.mPreMiIds) );
				bw.newLine();
			}
		}
	}

}
