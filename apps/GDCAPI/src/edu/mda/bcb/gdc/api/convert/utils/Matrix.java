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

package edu.mda.bcb.gdc.api.convert.utils;

import edu.mda.bcb.gdc.api.GDCAPI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class Matrix
{
	public TreeSet<String> mColumns = null;
	public TreeSet<String> mRows = null;
	protected HashMap<String, Integer> mColumnsIndex = null;
	protected HashMap<String, Integer> mRowsIndex = null;
	public double [][] mMatrixDouble = null;
	public int [][] mMatrixInt = null;
	boolean mIntFlag = false;
	
	public Matrix(TreeSet<String> theColumns, TreeSet<String> theRows, boolean theIntFlag)
	{
		mColumns = theColumns;
		mRows = theRows;
		mIntFlag = theIntFlag;
		if (false==mIntFlag)
		{
			GDCAPI.printLn("Matrix::Matrix double mColumns.size()=" + mColumns.size());
			GDCAPI.printLn("Matrix::Matrix double mRows.size()=" + mRows.size());
			mMatrixDouble = new double[mColumns.size()][mRows.size()];
			for (double[] row: mMatrixDouble)
			{
				Arrays.fill(row, Double.NaN);
			}
		}
		else
		{
			GDCAPI.printLn("Matrix::Matrix int mColumns.size()=" + mColumns.size());
			GDCAPI.printLn("Matrix::Matrix int mRows.size()=" + mRows.size());
			mMatrixInt = new int[mColumns.size()][mRows.size()];
			for (int[] row: mMatrixInt)
			{
				Arrays.fill(row, Integer.MIN_VALUE);
			}
		}
		int count = 0;
		mColumnsIndex = new HashMap<>();
		for(String col : mColumns)
		{
			mColumnsIndex.put(col, count);
			count += 1;
		}
		count = 0;
		mRowsIndex = new HashMap<>();
		for(String row : mRows)
		{
			mRowsIndex.put(row, count);
			count += 1;
		}
	}
	
	public void setValue(String theColumn, String theRow, double theValue)
	{
		mMatrixDouble[mColumnsIndex.get(theColumn)][mRowsIndex.get(theRow)] = theValue;
	}
	
	public void setValue(String theColumn, String theRow, int theValue)
	{
		mMatrixInt[mColumnsIndex.get(theColumn)][mRowsIndex.get(theRow)] = theValue;
	}
	
	public String getPrintValueDouble(String theColumn, String theRow)
	{
		double dbVal = mMatrixDouble[mColumnsIndex.get(theColumn)][mRowsIndex.get(theRow)];
		String value = "NA";
		if (!Double.isNaN(dbVal))
		{
			value = Double.toString(dbVal);
		}
		return value;
	}
	
	public String getPrintValueInt(String theColumn, String theRow)
	{
		double dbVal = mMatrixInt[mColumnsIndex.get(theColumn)][mRowsIndex.get(theRow)];
		String value = "NA";
		if (Integer.MIN_VALUE != dbVal)
		{
			value = Long.toString(Double.doubleToRawLongBits(dbVal));
		}
		return value;
	}
	
	public void addValue(String theColumn, String theRow, double theValue)
	{
		int col = mColumnsIndex.get(theColumn);
		int row = mRowsIndex.get(theRow);
		double currentValue = mMatrixDouble[col][row];
		if (Double.NaN != currentValue)
		{
			mMatrixDouble[col][row] = theValue;
		}
		else
		{
			mMatrixDouble[col][row] = currentValue + theValue;
		}
	}
	
	public void addValue(String theColumn, String theRow, int theValue)
	{
		int col = mColumnsIndex.get(theColumn);
		int row = mRowsIndex.get(theRow);
		int currentValue = mMatrixInt[col][row];
		if (Double.NaN != currentValue)
		{
			mMatrixInt[col][row] = theValue;
		}
		else
		{
			mMatrixInt[col][row] = currentValue + theValue;
		}
	}
}
