/*
 *  Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
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
package edu.mda.bcb.samval.matrix;

import java.io.IOException;

/**
 * Static Builder class to build a Matrix with optioned parameters.
 */
public class Builder
{

	// Defualt parameters for Matrix. Override with builder methods.
	private String path;
	String newline = "\n";
	String delim = "\t";
	boolean allowNonRectangle = false;
	String standIn = "Unknown";

	public Builder(String path)
	{
		this.path = path;
	}

	// public Builder withNewline(String newline) {
	//     this.newline = newline;
	//     return this;
	// }
	public Builder withDelimiter(String delim)
	{
		this.delim = delim;
		return this;
	}

	public Builder allowNonRectangle(boolean allow)
	{
		this.allowNonRectangle = allow;
		return this;
	}

	public Builder withStandIn(String standIn)
	{
		this.standIn = standIn;
		return this;
	}

	public Matrix build() throws IOException, Exception
	{
		Matrix m = new Matrix(this.path, this.newline, this.delim, this.allowNonRectangle, this.standIn);
		return m;
	}
	
}
