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

package edu.mda.bcb.gdc.api.data;

import edu.mda.bcb.gdc.api.GDCAPI;
import java.util.TreeMap;

/**
 *
 * @author Tod-Casasent
 */
public class DataType implements Comparable<DataType>
{
	public String mName = null;
	public TreeMap<String, Workflow> mWorkflows = null;
	
	public DataType(String theName)
	{
		mName = theName;
		mWorkflows = new TreeMap<>();
	}
			
	@Override
	public int compareTo(DataType o)
	{
		return mName.compareTo(o.mName);
	}
	
	public Workflow addEntry(String theWorkflow, String theLegacyName)
	{
		Workflow workflow = mWorkflows.get(theWorkflow);
		if (null==workflow)
		{
			GDCAPI.printLn("DataType::addEntry - adding new Workflow '" + theWorkflow + "'");
			workflow = new Workflow(theWorkflow, theLegacyName);
			mWorkflows.put(theWorkflow, workflow);
		}
		return workflow;
	}
}
