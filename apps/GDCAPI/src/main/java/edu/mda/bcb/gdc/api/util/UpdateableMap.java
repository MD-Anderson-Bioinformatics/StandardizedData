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

package edu.mda.bcb.gdc.api.util;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class UpdateableMap<T> extends TreeMap<String, Updateable_Mixin<T>>
{
	public UpdateableMap()
	{
	}
	
	public boolean updateFromTreeSets(UpdateableMap<T> theNew)
	{
		UpdateableMap<T> theOld = this;
		boolean updated = false;
		//compare entries from each -- use copies to prevent modifiying original
		TreeSet<String> oldEntries = new TreeSet<>();
		oldEntries.addAll(theOld.keySet());
		TreeSet<String> newEntries = new TreeSet<>();
		newEntries.addAll(theNew.keySet());
		// find new-only entries
		TreeSet<String> newOnly = new TreeSet<>();
		newOnly.addAll(newEntries);
		newOnly.removeAll(oldEntries);
		for (String uuid : newOnly)
		{
			// add these to old
			theOld.put(uuid, theNew.get(uuid));
			updated = true;
		}
		// find old-only file UUIDs
		TreeSet<String> oldOnly = new TreeSet<>();
		oldOnly.addAll(oldEntries);
		oldOnly.removeAll(newEntries);
		for (String uuid : oldOnly)
		{
			// remove these from old
			theOld.remove(uuid);
			updated = true;
		}
		// compare shared file UUIDs
		TreeSet<String> shared = new TreeSet<>();
		shared.addAll(newEntries);
		shared.removeAll(newOnly);
		shared.removeAll(oldOnly);
		for (String uuid : shared)
		{
			// remove these from existing manifest
			Updateable_Mixin<T> oldFile = theNew.get(uuid);
			Updateable_Mixin<T> newFile = theOld.get(uuid);
			if (true==oldFile.updateIfNeeded(newFile))
			{
				updated = true;
			}
		}
		return updated;
	}
}
