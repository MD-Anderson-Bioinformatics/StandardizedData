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

package edu.mda.bcb.gdc.api;

import edu.mda.bcb.gdc.api.convert.utils.Matrix;
import edu.mda.bcb.gdc.api.indexes.DataIndex;
import edu.mda.bcb.gdc.api.util.GDC;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class GDCAPI
{
	static public GDC M_GDC = null;
	static public String M_TIMESTAMP = null;
	static public File M_UTIL_DIR = null;
	static public File M_TEMP_DIR = null;
	static public DataIndex M_QUERY_INDEX = null;
	static private File M_OUTPUT_FILE = null;
	
	//
	static public boolean M_DISABLE_INTERNET_MANIFEST = false;
	static public boolean M_DISABLE_INTERNET_DOWNLOAD = false;
	static public boolean M_DISABLE_CONVERT = false;
	//
	static public boolean M_DISABLE_INDEX_INTERNAL = false;
	static public boolean M_DISABLE_ZIP_CREATION = false;
	static public boolean M_DISABLE_INDEX_EXTERNAL = false;
	static public boolean M_DISABLE_ZIP_CLEAN = false;
	
	public static void main(String[] args)
	{
		try
		{
			GDCAPI.printLn(getVersion());
			M_DISABLE_INTERNET_MANIFEST = args[0].equalsIgnoreCase("false")?false:true;
			M_DISABLE_INTERNET_DOWNLOAD = args[1].equalsIgnoreCase("false")?false:true;
			M_DISABLE_CONVERT = args[2].equalsIgnoreCase("false")?false:true;
			M_DISABLE_INDEX_INTERNAL = args[3].equalsIgnoreCase("false")?false:true;
			M_DISABLE_ZIP_CREATION = args[4].equalsIgnoreCase("false")?false:true;
			M_DISABLE_INDEX_EXTERNAL = args[5].equalsIgnoreCase("false")?false:true;
			M_DISABLE_ZIP_CLEAN = args[6].equalsIgnoreCase("false")?false:true;
			String baseDir = args[7];
			File legacyDir = new File(baseDir, "legacy");
			File currentDir = new File(baseDir, "current");
			File biospecimenDir = new File(baseDir, "biospecimen");
			File clinicalDir = new File(baseDir, "clinical");
			File utilDir = new File(baseDir, "util");
			File tempDir = new File(baseDir, "tmp");
			File indexesDir = new File(baseDir, "indexes");
			M_UTIL_DIR = utilDir;
			M_TEMP_DIR = tempDir;
			M_TIMESTAMP = getTimestamp();
			if (!M_UTIL_DIR.exists())
			{
				M_UTIL_DIR.mkdirs();
			}
			if (!M_TEMP_DIR.exists())
			{
				M_TEMP_DIR.mkdirs();
			}
			if (!indexesDir.exists())
			{
				indexesDir.mkdirs();
			}
			M_QUERY_INDEX = new DataIndex(new File(indexesDir, "index.tsv"));
			M_GDC = new GDC(legacyDir, currentDir, biospecimenDir, clinicalDir, indexesDir);
			GDCAPI.printLn("----------------------------------------------------");
			GDCAPI.printLn("Process Current Data Manifests");
			M_GDC.currentData();
			GDCAPI.printLn("----------------------------------------------------");
			GDCAPI.printLn("Process Legacy Data Manifests");
			M_GDC.legacyData();
			GDCAPI.printLn("----------------------------------------------------");
			GDCAPI.printLn("Process Biospecimen Manifests");
			M_GDC.biospecimen();
			GDCAPI.printLn("----------------------------------------------------");
			GDCAPI.printLn("Process Clinical Manifests");
			M_GDC.clinical();
			GDCAPI.printLn("----------------------------------------------------");
			GDCAPI.printLn("Process Data Downloads");
			M_GDC.dataDownload();
			GDCAPI.printLn("----------------------------------------------------");
			GDCAPI.printLn("Convert Local Files");
			M_GDC.convertFiles();
			GDCAPI.printLn("----------------------------------------------------");
			GDCAPI.printLn("Update Indexes");
			M_GDC.updateIndexes();
			GDCAPI.printLn("----------------------------------------------------");
			GDCAPI.printLn("----------------------------------------------------");
			GDCAPI.printLn("---- END OF RUN ------------------------------------");
			GDCAPI.printLn("----------------------------------------------------");
		}
		catch (Exception exp)
		{
			GDCAPI.printErr("Error in main", exp);
		}
	}

	static public String getTimestamp()
	{
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm");
		return dateFormat.format(calendar.getTime());
	}
	
	static public void setLogDir(File theDir)
	{
		if (null==theDir)
		{
			M_OUTPUT_FILE = null;
		}
		else
		{
			M_OUTPUT_FILE = new File(theDir, "gdcapi.log");
		}
	}
	
	static public void logIfNotNull(String theLog)
	{
		if (null!=M_OUTPUT_FILE)
		{
			try
			{
				Files.write(M_OUTPUT_FILE.toPath(), (theLog+System.lineSeparator()).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
			catch(Exception theExp)
			{
				System.err.println("Failed wroting to " + M_OUTPUT_FILE.getAbsolutePath());
				theExp.printStackTrace(System.err);
			}
		}
	}

	static public void printWarn(String theLine)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --WRN-- " + theLine;
		System.err.println(out);
		System.err.flush();
		logIfNotNull(out);
	}

	static public void printErr(String theLine, Exception theExp)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --ERR-- " + theLine;
		System.err.println(out);
		System.err.flush();
		logIfNotNull(out);
		if (null != theExp)
		{
			theExp.printStackTrace(System.err);
			System.err.flush();
			theExp.printStackTrace(System.out);
			System.out.flush();
			logIfNotNull(theExp.getMessage());
			logIfNotNull(theExp.toString());
			Throwable cause = theExp.getCause();
			if (null != cause)
			{
				cause.printStackTrace(System.err);
				System.err.flush();
				logIfNotNull(cause.getMessage());
				logIfNotNull(cause.toString());
			}
		}
		else
		{
			printWarn("No exception included in call");
		}
	}

	static public void printErr(String theLine)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --ERR-- " + theLine;
		System.err.println(out);
		System.err.flush();
		logIfNotNull(out);
	}

	static public void printLn(String theLine)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --LOG-- " + theLine;
		System.out.println(out);
		System.out.flush();
		logIfNotNull(out);
	}

	static public String getVersion()
	{
		return "GDCAPI 2020-09-11-1000";
	}

	static public File findNewestTSV(File theDir)
	{
		File newest = null;
		TreeSet<File> fileList = new TreeSet<>();
		File[] tmp = theDir.listFiles();
		if (null != tmp)
		{
			fileList.addAll(Arrays.asList(tmp));
			newest = fileList.descendingSet().first();
		}
		return newest;
	}

	static public void writeDataframe(File theOutputFile, String theHeaders, final Set theSet) throws FileNotFoundException
	{
		try (PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(theOutputFile)), true))
		{
			ps.println(theHeaders);
			for (Object bs : theSet)
			{
				ps.println(bs.toString());
			}
		}
	}

	static public String arrayToString(ArrayList<String> theArray, String theDelimiter)
	{
		String result = null;
		if (null != theArray)
		{
			for (String element : theArray)
			{
				if (null == result)
				{
					result = element;
				}
				else
				{
					result = result + theDelimiter + element;
				}
			}
		}
		else
		{
			result = "";
		}
		return result;
	}

	static public String getColumn(String theColumn, String[] theSplitted, ArrayList<String> theHeaders)
	{
		String result = "";
		int index = theHeaders.indexOf(theColumn);
		if (index >= 0)
		{
			result = theSplitted[index];
		}
		if ("NA".equals(result))
		{
			result = "NaN";
		}
		return result;
	}

	static public void writeMatrixFileStr(File theOutputFile, final Matrix theMatrix) throws FileNotFoundException, Exception
	{
		if (theOutputFile.exists())
		{
			throw new Exception(theOutputFile + " should not exist");
		}
		GDCAPI.printLn("writeMatrixFileStr to " + theOutputFile.getAbsolutePath());
		theOutputFile.getParentFile().mkdirs();
		try (PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(theOutputFile)), true))
		{
			// first headers are sample ids
			for (String sampleId : theMatrix.mColumns)
			{
				ps.print("\t");
				ps.print(sampleId);
			}
			ps.println();
			// the each row is a feature
			for (String feature : theMatrix.mRows)
			{
				// add row header column
				ps.print(feature);
				// and then the values
				for (String sampleId : theMatrix.mColumns)
				{
					ps.print("\t");
					ps.print(theMatrix.getPrintValueDouble(sampleId, feature));
				}
				// end the row
				ps.println();
			}
		}
	}

	static public void writeMatrixFileAsInt(File theOutputFile, final Matrix theMatrix) throws FileNotFoundException, Exception
	{
		if (theOutputFile.exists())
		{
			throw new Exception(theOutputFile + " should not exist");
		}
		GDCAPI.printLn("writeMatrixFileAsInt to " + theOutputFile.getAbsolutePath());
		theOutputFile.getParentFile().mkdirs();
		try (PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(theOutputFile)), true))
		{
			// first headers are sample ids
			for (String sampleId : theMatrix.mColumns)
			{
				ps.print("\t");
				ps.print(sampleId);
			}
			ps.println();
			// the each row is a feature
			for (String feature : theMatrix.mRows)
			{
				// add row header column
				ps.print(feature);
				// and then the values
				for (String sampleId : theMatrix.mColumns)
				{
					ps.print("\t");
					ps.print(theMatrix.getPrintValueInt(sampleId, feature));
				}
				// end the row
				ps.println();
			}
		}
	}

	static public void writeMatrixFileInt(File theOutputFile, SortedSet<String> theFeatures,
			final Map<String, Map<String, Integer>> theSampleToFeatureToValue) throws FileNotFoundException, Exception
	{
		if (theOutputFile.exists())
		{
			throw new Exception(theOutputFile + " should not exist");
		}
		GDCAPI.printLn("writeMatrixFileAsInt to " + theOutputFile.getAbsolutePath());
		theOutputFile.getParentFile().mkdirs();
		try (PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(theOutputFile)), true))
		{
			// first headers are sample ids
			for (String sampleId : new TreeSet<String>(theSampleToFeatureToValue.keySet()))
			{
				ps.print("\t");
				ps.print(sampleId);
			}
			ps.println();
			// the each row is a feature
			for (String feature : theFeatures)
			{
				// add row header column
				ps.print(feature);
				// and then the values
				for (String sampleId : new TreeSet<String>(theSampleToFeatureToValue.keySet()))
				{
					Map<String, Integer> featuresToValue = theSampleToFeatureToValue.get(sampleId);
					Integer value = featuresToValue.get(feature);
					if (null == value)
					{
						value = 0;
					}
					ps.print("\t");
					ps.print(value);
				}
				// end the row
				ps.println();
			}
		}
	}

	static public String getColumnWithAlt(String theColumn, String theAltColumnA, String theAltColumnB, String[] theSplitted, ArrayList<String> theHeaders)
	{
		String val = GDCAPI.getColumn(theColumn, theSplitted, theHeaders);
		if ("".equals(val))
		{
			val = GDCAPI.getColumn(theAltColumnA, theSplitted, theHeaders);
			if (("".equals(val)) && (null != theAltColumnB))
			{
				val = GDCAPI.getColumn(theAltColumnB, theSplitted, theHeaders);
			}
		}
		return val;
	}

	static public String getTwoColumns(String theColumnA, String theColumnB, String[] theSplitted, ArrayList<String> theHeaders)
	{
		return GDCAPI.getColumn(theColumnA, theSplitted, theHeaders) + "-" + GDCAPI.getColumn(theColumnB, theSplitted, theHeaders);
	}
}
