/*
 *  Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
 *  
 *  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
 *  MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

 */
package edu.mda.bcb.stdmwutils.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.mda.bcb.stdmwutils.mwdata.Analysis;
import edu.mda.bcb.stdmwutils.mwdata.MwTable;
import edu.mda.bcb.stdmwutils.mwdata.Summary;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author TDCasasent
 */
public class MwTableUtil
{
	public SummaryUtil mSummary = null;
	public AnalysisUtil mAnalysis = null;
	public MetaboliteUtil mMetabolite = null;
	public RefMetUtil mRefMet = null;
	
	public MwTableUtil(SummaryUtil theSummary, AnalysisUtil theAnalysis, MetaboliteUtil theMetabolite, RefMetUtil theRefMet)
	{
		mSummary = theSummary;
		mAnalysis = theAnalysis;
		mMetabolite = theMetabolite;
		mRefMet = theRefMet;
	}
	
	public TreeSet<String> getApiAsSet()
	{
		TreeSet<String> data = new TreeSet<>();
		for (Analysis analysis : mAnalysis.getAnalysesAll())
		{
			MwTable mwt = combineAnalysisAndStudy(analysis);
			if ((mwt.study.sample_count > 0) && (mwt.metabolite_count > 0))
			{
				// Study <> Analysis <> Title
				String row = mwt.study.study_id + " <> " + mwt.analysis.analysis_id + " <> " + mwt.study.study_title;
				data.add(row);
			}
		}
		return data;
	}
	
	public String getAsJson()
	{
		ArrayList<MwTable> data = new ArrayList<>();
		for (Analysis analysis : mAnalysis.getAnalysesAll())
		{
			data.add(combineAnalysisAndStudy(analysis));
		}
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		//return "{ 'data': " + gson.toJson(data) + "}";
		return gson.toJson(data);
	}
	
	public MwTable combineAnalysisAndStudy(Analysis theAnalysis)
	{
		Summary summary = mSummary.get(theAnalysis.study_hash);
		MwTable mt = new MwTable(theAnalysis, summary);
		mt.init(mMetabolite, mRefMet);
		return mt;
	}
}
