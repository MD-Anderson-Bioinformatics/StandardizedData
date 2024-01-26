// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.stdmwutils.utils;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import edu.mda.bcb.stdmwutils.StdMwDownload;
import edu.mda.bcb.stdmwutils.mwdata.MWUrls;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.fileupload.util.Streams;

/**
 *
 * @author Tod-Casasent
 */
public class DatatableUtil
{
	static public String getDatatableRaw(OutputStream theOut, String theAnalysisId) throws MalformedURLException, IOException
	{
		String success = null;
		String url = MWUrls.getDatatable(theAnalysisId);
		try (InputStream is = new URL(url).openStream())
		{
			Streams.copy(is, theOut, true);
			success = url;
		}
		catch(Exception exp)
		{
			StdMwDownload.printErr("Error in getDatatableRaw (1)", exp);
		}
		return success;
	}

	static public String getDatatableDC(OutputStream theOut, String theAnalysisId) throws MalformedURLException, IOException
	{
		String success = null;
		String url = MWUrls.getDatatable(theAnalysisId);
		try (InputStream is = new URL(url).openStream())
		{
			try (Reader reader = new InputStreamReader(is))
			{
				CSVFormat format = CSVFormat.DEFAULT.builder().setDelimiter('\t').setHeader().build();
				CSVParser parser = new CSVParser(reader, format);
				List<CSVRecord> memlist = parser.getRecords();
				// column headers are all headers except first
				// features
				ArrayList<String> columns = new ArrayList<>();
				for (String hdr : parser.getHeaderNames())
				{
					columns.add(hdr);
				}
				// remove sample from list of headers
				columns.remove("Samples");
				// remove class from list of headers
				columns.remove("Class");
				if (columns.size() > 0)
				{
					// row headers are first value from each row entry
					// samples
					ArrayList<String> rows = new ArrayList<>();
					for (CSVRecord csvRecord : memlist)
					{
						rows.add(csvRecord.get("Samples"));
					}
					// setup table
					Table<String, String, String> dataTable = ArrayTable.create(rows, columns);
					for (CSVRecord csvRecord : memlist)
					{
						String myRow = csvRecord.get(0);
						for (String col : columns)
						{
							dataTable.put(myRow, col, csvRecord.get(col));
						}
					}
					dataTable = Tables.transpose(dataTable);
					// write headers to theOut
					for(String hdr : dataTable.columnKeySet())
					{
						theOut.write(("\t"+hdr).getBytes());
					}
					theOut.write("\n".getBytes());
					theOut.flush();
					// write each row to theOut
					for(String row : dataTable.rowKeySet())
					{
						theOut.write(row.getBytes());
						for(String col : dataTable.columnKeySet())
						{
							theOut.write("\t".getBytes());
							theOut.write(dataTable.get(row, col).getBytes());
						}
						theOut.write("\n".getBytes());
						theOut.flush();
					}
					success = url;
				}
				else
				{
					success = null;
					StdMwDownload.printWarn("getDatatableDC no columns in theAnalysisId=" + theAnalysisId);
				}
			}
			catch(Exception exp)
			{
				StdMwDownload.printErr("Error in getDatatableDC (1)", exp);
			}
		}
		catch(Exception exp)
		{
			StdMwDownload.printErr("Error in getDatatableDC (2)", exp);
		}
		return success;
	}

	static public String getDatatableMSC(OutputStream theOut, String theAnalysisId) throws MalformedURLException, IOException
	{
		String success = null;
		String url = MWUrls.getDatatable(theAnalysisId);
		try (InputStream is = new URL(url).openStream())
		{
			try (Reader reader = new InputStreamReader(is))
			{
				CSVFormat format = CSVFormat.DEFAULT.builder().setDelimiter('\t').setHeader().build();
				CSVParser parser = new CSVParser(reader, format);
				List<CSVRecord> memlist = parser.getRecords();
				// column headers are Samples, Class, and then feature names
				// features
				ArrayList<String> featureNames = new ArrayList<>();
				for (String hdr : parser.getHeaderNames())
				{
					featureNames.add(hdr);
				}
				StdMwDownload.printLn("Number of headers = " + featureNames.size());
				// remove sample from list of features
				featureNames.remove("Samples");
				// remove class from list of features
				featureNames.remove("Class");
				if (featureNames.size()>0)
				{
					// sample id and sample class are first two values from each row entry
					// sample+class
					ArrayList<String> sampleLabels = new ArrayList<>();
					for (CSVRecord csvRecord : memlist)
					{
						sampleLabels.add(csvRecord.get("Samples") + "-" + csvRecord.get("Class"));
					}
					// ArrayTable.create(row-index-list-features, column-index-list-samples
					Table<String, String, String> dataTable = ArrayTable.create(featureNames, sampleLabels);
					for (CSVRecord csvRecord : memlist)
					{
						String colIndex = csvRecord.get("Samples") + "-" + csvRecord.get("Class");
						for (String rowIndex : featureNames)
						{
							dataTable.put(rowIndex, colIndex, csvRecord.get(rowIndex));
						}
					}
					dataTable = Tables.transpose(dataTable);
					// write headers to theOut
					for(String hdr : dataTable.columnKeySet())
					{
						theOut.write(("\t"+hdr).getBytes());
					}
					theOut.write("\n".getBytes());
					theOut.flush();
					// write each row to theOut
					// ArrayTable.create(row-index-list-features, column-index-list-samples
					for(String featureId : dataTable.rowKeySet())
					{
						theOut.write(featureId.getBytes());
						for(String sampleId : dataTable.columnKeySet())
						{
							theOut.write("\t".getBytes());
							theOut.write(dataTable.get(featureId, sampleId).getBytes());
						}
						theOut.write("\n".getBytes());
						theOut.flush();
					}
					success = url;
				}
				else
				{
					StdMwDownload.printWarn("getDatatableMSC was unable to get feature list for theAnalysisId=" + theAnalysisId);
					success = null;
				}
			}
			catch(Exception exp)
			{
				StdMwDownload.printErr("Error in getDatatableMSC (1)", exp);
			}
		}
		catch(Exception exp)
		{
			StdMwDownload.printErr("Error in getDatatableMSC (2)", exp);
		}
		return success;
	}
}
