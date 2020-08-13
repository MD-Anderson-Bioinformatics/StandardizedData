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

package edu.mda.bioinfo.stdmw.utils;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import edu.mda.bioinfo.stdmw.data.MWUrls;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.tomcat.util.http.fileupload.util.Streams;

/**
 *
 * @author Tod-Casasent
 */
public class DatatableUtil
{

	static public void getDatatableRaw(OutputStream theOut, String theAnalysisId) throws MalformedURLException, IOException
	{
		String url = MWUrls.getDatatable(theAnalysisId);
		try (InputStream is = new URL(url).openStream())
		{
			Streams.copy(is, theOut, true);
		}
	}

	static public void getDatatableDC(OutputStream theOut, String theAnalysisId) throws MalformedURLException, IOException
	{
		String url = MWUrls.getDatatable(theAnalysisId);
		try (InputStream is = new URL(url).openStream())
		{
			try (Reader reader = new InputStreamReader(is))
			{
				CSVFormat format = CSVFormat.newFormat('\t').withFirstRecordAsHeader();
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
			}
		}
	}

	static public void getDatatableMSC(OutputStream theOut, String theAnalysisId) throws MalformedURLException, IOException
	{
		String url = MWUrls.getDatatable(theAnalysisId);
		try (InputStream is = new URL(url).openStream())
		{
			try (Reader reader = new InputStreamReader(is))
			{
				CSVFormat format = CSVFormat.newFormat('\t').withFirstRecordAsHeader();
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
				// row headers are first value from each row entry
				// samples
				ArrayList<String> rows = new ArrayList<>();
				for (CSVRecord csvRecord : memlist)
				{
					rows.add(csvRecord.get("Samples"));
				}
				// setup table
				HashMap<String, String> classVals = new HashMap<>();
				Table<String, String, String> dataTable = ArrayTable.create(rows, columns);
				for (CSVRecord csvRecord : memlist)
				{
					String myRow = csvRecord.get("Samples");
					classVals.put(myRow, csvRecord.get("Class"));
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
					theOut.write((row+"-"+classVals.get(row)).getBytes());
					for(String col : dataTable.columnKeySet())
					{
						theOut.write("\t".getBytes());
						theOut.write(dataTable.get(row, col).getBytes());
					}
					theOut.write("\n".getBytes());
					theOut.flush();
				}
			}
		}
	}
}
