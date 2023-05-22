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
package edu.mda.bcb.samval.matrix;

import java.util.Comparator;

/**
 *
 * @author cjacoby
 */
public class Header<T>
{

	final public String label;          // The headers label
	final public T index;               // The headers index. Generic, but probably int or long.

	public Header(String label, T index)
	{
		this.label = label;
		this.index = index;
	}

	public static Comparator HEADER_SORTED_ORDER = new Comparator<Header>()
	{

		@Override
		public int compare(Header h1, Header h2)
		{
			return h1.label.compareTo(h2.label);
		}

	};

}
