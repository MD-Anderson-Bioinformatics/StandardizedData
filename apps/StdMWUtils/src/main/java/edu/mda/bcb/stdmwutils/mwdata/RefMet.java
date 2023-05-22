/*
 *  Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
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
package edu.mda.bcb.stdmwutils.mwdata;

import java.util.ArrayList;

/**
 *
 * @author TDCasasent
 */
public class RefMet implements Comparable<RefMet>
{

	public String name = null;
	public String pubchem_cid = null;
	public String inchi_key = null;
	public String exactmass = null;
	public String formula = null;
	public String super_class = null;
	public String main_class = null;
	public String sub_class = null;

	public RefMet()
	{

	}

	static public String getHeaderString()
	{
		return "refmet_name\tpubchem_cid\tinchi_key\texactmass\tformula\tsuper_class\tmain_class\tsub_class";
	}
    
    public String getRowString()
	{
		return name + "\t" + pubchem_cid + "\t" + inchi_key + "\t" + exactmass + "\t" + formula + "\t" + super_class + "\t" + main_class + "\t" + sub_class;
	}
    
    static public RefMet getFromRowString(ArrayList<String> theHeaders, String theRowString)
	{
		RefMet refmet = new RefMet();
		String[] splitted = theRowString.split("\t", -1);
		refmet.name = splitted[theHeaders.indexOf("refmet_name")];
		refmet.pubchem_cid = splitted[theHeaders.indexOf("pubchem_cid")];
		refmet.inchi_key = splitted[theHeaders.indexOf("inchi_key")];
		refmet.exactmass = splitted[theHeaders.indexOf("exactmass")];
		refmet.formula = splitted[theHeaders.indexOf("formula")];
		refmet.super_class = splitted[theHeaders.indexOf("super_class")];
		refmet.main_class = splitted[theHeaders.indexOf("main_class")];
		refmet.sub_class = splitted[theHeaders.indexOf("sub_class")];
		return refmet;
	}

	@Override
	public int compareTo(RefMet t)
	{
		int cmp = this.name.compareTo(t.name);
		return cmp;
	}
	
	public boolean exactMatch(RefMet theOther)
	{
		boolean match = false;
		if (this.name.equals(theOther.name))
		{
			if (this.exactmass.equals(theOther.exactmass))
			{
				if (this.formula.equals(theOther.formula))
				{
					if (this.inchi_key.equals(theOther.inchi_key))
					{
						if (this.main_class.equals(theOther.main_class))
						{
							if (this.pubchem_cid.equals(theOther.pubchem_cid))
							{
								if (this.sub_class.equals(theOther.sub_class))
								{
									if (this.super_class.equals(theOther.super_class))
									{
										match = true;
									}
								}
							}
						}
					}
				}
			}
		}
		return match;
	}
	

}
