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
 * GDCAPI class is used to retrieve data from the GDC and convert it to 
 * Standardized Data format.
 * 
 * @author Tod-Casasent
 */
public class GDCAPI
{
	/**
	 * GDC object used to wrap/coordinate processing.
	 */
	static public GDC M_GDC = null;
	
	/**
	 * Timestamp string of format yyyy_MM_dd_HHmm used to mark a particular run.
	 */
	static public String M_TIMESTAMP = null;
	
	/**
	 * The "util" directory, found inside the base directory, and containing 
	 * the HG19 and HG38 gene map files.
	 */
	static public File M_UTIL_DIR = null;
	
	/**
	 * The "tmp" directory, found inside the base directory, and used 
	 * during download and convert for temporary file storage.
	 */
	static public File M_TEMP_DIR = null;
	
	/**
	 * DataIndex object used to store and update the index.tsv for the GDC
	 * data being processed and being processed.
	 */
	static public DataIndex M_QUERY_INDEX = null;
	
	/**
	 * File object pointing to log file to which to perform logging -- if null,
	 * logging does not go to file.
	 */
	static private File M_OUTPUT_FILE = null;
	
	/**
	 * If true, do not download new manifests from internet (from GDC) -- this
	 * is used to complete partial runs or fix bugs.
	 */
	static public boolean M_DISABLE_INTERNET_MANIFEST = false;
	
	/**
	 * If true, do not download new data from internet (from GDC) -- this
	 * is used to complete partial runs or fix bugs.
	 */
	static public boolean M_DISABLE_INTERNET_DOWNLOAD = false;
	
	/**
	 * If true, do not convert new data -- this
	 * is used to complete partial runs or fix bugs.
	 */
	static public boolean M_DISABLE_CONVERT = false;
	
	/**
	 * If true, do not create internal index files for 
	 * data sets -- this is used to complete partial runs or fix bugs.
	 */
	static public boolean M_DISABLE_INDEX_INTERNAL = false;
	
	/**
	 * If true, do not create ZIP files for 
	 * data sets -- this is used to complete partial runs or fix bugs.
	 */
	static public boolean M_DISABLE_ZIP_CREATION = false;
	
	/**
	 * If true, do not update the index file for the GDCAPI process
	 * -- this is used to complete partial runs or fix bugs.
	 */
	static public boolean M_DISABLE_INDEX_EXTERNAL = false;
	
	/**
	 * If true, do not delete the ZIP files created from GDC data
	 * -- this is used to complete partial runs or fix bugs.
	 */
	static public boolean M_DISABLE_ZIP_CLEAN = false;
	
	/**
	 * Main function generally called from a Bash script. Paths and flags are 
	 * set from command line arguments and look for the string "false" for 
	 * flags.
	 * 
	 * @param args 
	 */
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
			// use args timestamp so restarts have same timestamp
			//M_TIMESTAMP = getTimestamp();
			M_TIMESTAMP = args[8];
			GDCAPI.printLn("M_TIMESTAMP=" + M_TIMESTAMP);
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

	/**
	 * Create a timestamp string of format yyyy_MM_dd_HHmm, used for logging
	 * and to version timestamp in PanCanUpdate.
	 * 
	 * @return String of format yyyy_MM_dd_HHmm
	 */
	static public String getTimestamp()
	{
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm");
		return dateFormat.format(calendar.getTime());
	}
	
	/**
	 * If theDir passed in in not null, set the logging output file.
	 * 
	 * @param theDir 
	 */
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
	
	/**
	 * Log theLog string to the log file if file is non-null.
	 * 
	 * @param theLog String to log
	 */
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

	/**
	 * Log theLine string to System.err and the log file if file is non-null.
	 * 
	 * @param theLine String to log
	 */
	static public void printWarn(String theLine)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --WRN-- " + theLine;
		System.err.println(out);
		System.err.flush();
		logIfNotNull(out);
	}

	/**
	 * Log theLine and theExp to System.err and the log file if file is non-null.
	 * 
	 * @param theLine String to log
	 * @param theExp Exception to log (with stack trace).
	 */
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

	/**
	 * Log theLine to System.err and the log file if file is non-null.
	 * 
	 * @param theLine String to log
	 */
	static public void printErr(String theLine)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --ERR-- " + theLine;
		System.err.println(out);
		System.err.flush();
		logIfNotNull(out);
	}

	/**
	 * Log theLine to System.out and the log file if file is non-null.
	 * 
	 * @param theLine String to log
	 */
	static public void printLn(String theLine)
	{
		String out = getTimestamp() + " " + String.format("%06d", Thread.currentThread().getId()) + " --LOG-- " + theLine;
		System.out.println(out);
		System.out.flush();
		logIfNotNull(out);
	}

	/**
	 * Return version string -- BEA_VERSION_TIMESTAMP is replaced by external
	 * script when building application.
	 * 
	 * @return GDCAPI name and version string.
	 */
	static public String getVersion()
	{
		return "GDCAPI 2021-10-18-0910";
		//return "GDCAPI BEA_VERSION_TIMESTAMP";
	}

	/**
	 * TSV files are versioned, so find the newest version.
	 * 
	 * @param theDir where to look for TSV files
	 * @return newest TSV file in theDir or null
	 */
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

	/**
	 * Write a dataframe to a file
	 * 
	 * @param theOutputFile File for where to write
	 * @param theHeaders Tab-delimited string for headers
	 * @param theSet A Set using toString to write data
	 * @throws FileNotFoundException 
	 */
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

	/**
	 * A common activity is converting a String Array to a string. This does it.
	 * 
	 * @param theArray String ArrayList of convert
	 * @param theDelimiter Delimiter String to use
	 * @return String of the array converted to a string, maybe of 0 length. Not null.
	 */
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

	/**
	 * Given a column (String), find the index in the list of headers, and return
	 * that value from the String array. Also, convert NA to NaN (for use with
	 * Java).
	 * 
	 * @param theColumn Header string to find
	 * @param theSplitted Data String array
	 * @param theHeaders Array of header strings
	 * @return Value string -- possibly zero length, but not null
	 */
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

	/**
	 * Write matrix to a file as a TSV of unquoted strings. 
	 * Throws an exception if the file 
	 * already exists, since for GDCAPI, this should not happen.
	 * 
	 * @param theOutputFile Output file location
	 * @param theMatrix Matrix object to write
	 * @throws FileNotFoundException
	 * @throws Exception 
	 */
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

	/**
	 * Write matrix to a file as a TSV of integers -- with NA if integer
	 * conversion fails.
	 * Throws an exception if the file 
	 * already exists, since for GDCAPI, this should not happen.
	 * 
	 * @param theOutputFile Output file location
	 * @param theMatrix Matrix object to write
	 * @throws FileNotFoundException
	 * @throws Exception 
	 */
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

	/**
	 * Write data to a matrix file as a TSV of integers -- with NA if integer
	 * conversion fails. Needed since mutations do not have a predetermined
	 * matrix to build.
	 * Throws an exception if the file 
	 * already exists, since for GDCAPI, this should not happen.
	 * 
	 * @param theOutputFile Output file location
	 * @param theFeatures List of feature (row) names (probes, etc).
	 * @param theSampleToFeatureToValue Sample ids (col) mapped to features to integer count
	 * @throws FileNotFoundException
	 * @throws Exception 
	 */
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

	/**
	 * Some conversion techniques are the same except for the source column, 
	 * which can be one of three values. This checks the column options and
	 * returns the first value found.
	 * 
	 * @param theColumn First column (header) to try
	 * @param theAltColumnA Second column (header) to try (null skips)
	 * @param theAltColumnB Third column (header) to try (null skips)
	 * @param theSplitted String array
	 * @param theHeaders ArrayList of header strings
	 * @return String (may be zero length)
	 */
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

	/**
	 * Combine two columns separated by a dash 
	 * 
	 * @param theColumnA First column (header)
	 * @param theColumnB Second column (header)
	 * @param theSplitted Array string of values
	 * @param theHeaders Array List of headers
	 * @return a string of the two values separated by a dash
	 */
	static public String getTwoColumns(String theColumnA, String theColumnB, String[] theSplitted, ArrayList<String> theHeaders)
	{
		return GDCAPI.getColumn(theColumnA, theSplitted, theHeaders) + "-" + GDCAPI.getColumn(theColumnB, theSplitted, theHeaders);
	}
}
