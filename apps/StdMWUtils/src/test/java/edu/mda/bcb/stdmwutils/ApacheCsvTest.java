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

package edu.mda.bcb.stdmwutils;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author tdcasasent
 */
public class ApacheCsvTest
{
	public ApacheCsvTest()
	{
		
	}
	
	@Test
	public void testMain()
	{
		try
		{
			// Ensemble Gene Map
			String compareFile = "../../data/testing_static/StdMWUtils/mwb_batches.tsv";
			try (Reader reader = new FileReader(compareFile))
			{
				CSVFormat format = CSVFormat.DEFAULT.builder()
						.setDelimiter('\t')
						.setHeader()
						.build();
				CSVParser parser = new CSVParser(reader, format);
				List<CSVRecord> memlist = parser.getRecords();
				List<String> headers = parser.getHeaderNames();
				System.out.println("number of entries = " + memlist.size());
				assertEquals(88, memlist.size());
				System.out.println("number of headers = " + headers.size());
				assertEquals(6, headers.size());
			}
		}
		catch(Exception exp)
		{
			exp.printStackTrace(System.err);
			System.err.flush();
			fail(exp.getMessage());
		}
	}
}
