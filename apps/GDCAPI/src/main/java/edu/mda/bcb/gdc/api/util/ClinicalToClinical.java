/*
 *  Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
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
package edu.mda.bcb.gdc.api.util;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.dataframes.Dataframe;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author TDCasasent
 */
public class ClinicalToClinical
{
	File mDataFile = null;
	TreeSet<Path> mClinicalFiles = null;
	File mClinicalFile = null;
	
	public ClinicalToClinical(File theDataFile, TreeSet<Path> theClinicalFiles, File theClinicalFile)
	{
		mDataFile = theDataFile;
		mClinicalFiles = theClinicalFiles;
		mClinicalFile = theClinicalFile;
	};
	
	public void generateClinicalFile() throws IOException
	{
		// get sample list from data file
		TreeSet<String> patients = getPatients(mDataFile.toPath());
		// for each sample, check biospecimen files for newest one with data
		GDCAPI.printLn("Starting clinical file " + mClinicalFile.getAbsolutePath());
		OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
		try (BufferedWriter runWriter = java.nio.file.Files.newBufferedWriter(mClinicalFile.toPath(), Charset.availableCharsets().get("UTF-8"), options))
		{
			// header
			ArrayList<String> headers = getOutputHeaders();
			String headerString = GDCAPI.arrayToString(headers, "\t");
			runWriter.write(headerString);
			runWriter.newLine();
			// data
			for (String pat : patients)
			{
				String line = getClinicalLine(pat);
				if (null!=line)
				{
					runWriter.write(line);
					runWriter.newLine();
				}
				else
				{
					runWriter.write(getClinicalLineNA(pat));
					runWriter.newLine();
					GDCAPI.printWarn("Patient not found, using NAs for " + pat);
				}
			}
		}
		GDCAPI.printLn("Wrote batch file " + mClinicalFile.getAbsolutePath());
	};
	
	protected String getClinicalLine(String thePatient) throws IOException
	{
		String line = null;
		for (Path file : mClinicalFiles)
		{
			if (null==line)
			{
				Dataframe df = new Dataframe();
				df.readDataFrame(file.toFile());
				ArrayList<String> row = df.getRowFromColumnValueNoWarn("bcr_patient_barcode", thePatient);
				if (null!=row)
				{
					if (row.size()>0)
					{
						// Source -> Destination
						// bcr_patient_barcode                                Patient
						// days_to_birth                                      days_to_birth
						// height                                             height
						// weight                                             weight
						// race                                               race
						// ethnicity                                          ethnicity
						// vital_status                                       vital_status
						// days_to_last_followup                              days_to_last_followup
						// days_to_last_known_alive                           days_to_last_known_alive
						// days_to_death                                      days_to_death
						// relative_family_cancer_history                     relative_family_cancer_history
						// cancer_first_degree_relative                       cancer_first_degree_relative
						// clinical_stage                                     clinical_stage
						// pathologic_stage                                   pathologic_stage
						// age_at_initial_pathologic_diagnosis                age_at_initial_pathologic_diagnosis
						// follow_up_vital_status                             follow_up_vital_status
						// follow_up_days_to_last_followup                    follow_up_days_to_last_followup
						// follow_up_days_to_death                            follow_up_days_to_death
						// follow_up_new_tumor_event_after_initial_treatment  follow_up_new_tumor_event_after_initial_treatment
						line = row.get(df.mHeaders.indexOf("bcr_patient_barcode")) + 
								"\t" + row.get(df.mHeaders.indexOf("days_to_birth")) + 
								"\t" + row.get(df.mHeaders.indexOf("height")) + 
								"\t" + row.get(df.mHeaders.indexOf("weight")) + 
								"\t" + row.get(df.mHeaders.indexOf("race")) + 
								"\t" + row.get(df.mHeaders.indexOf("ethnicity")) + 
								"\t" + row.get(df.mHeaders.indexOf("vital_status")) + 
								"\t" + row.get(df.mHeaders.indexOf("days_to_last_followup")) + 
								"\t" + row.get(df.mHeaders.indexOf("days_to_last_known_alive")) + 
								"\t" + row.get(df.mHeaders.indexOf("days_to_death")) + 
								"\t" + row.get(df.mHeaders.indexOf("relative_family_cancer_history")) + 
								"\t" + row.get(df.mHeaders.indexOf("cancer_first_degree_relative")) + 
								"\t" + row.get(df.mHeaders.indexOf("clinical_stage")) + 
								"\t" + row.get(df.mHeaders.indexOf("pathologic_stage")) + 
								"\t" + row.get(df.mHeaders.indexOf("age_at_initial_pathologic_diagnosis")) + 
								"\t" + row.get(df.mHeaders.indexOf("follow_up_vital_status")) + 
								"\t" + row.get(df.mHeaders.indexOf("follow_up_days_to_last_followup")) + 
								"\t" + row.get(df.mHeaders.indexOf("follow_up_days_to_death")) + 
								"\t" + row.get(df.mHeaders.indexOf("follow_up_new_tumor_event_after_initial_treatment"));
					}
				}
			}
		}
		return line;
	};
	
	
	protected String getClinicalLineNA(String thePatient) throws IOException
	{
		String line = null;
		// Source -> Destination
		// bcr_patient_barcode                                Patient
		// days_to_birth                                      days_to_birth
		// height                                             height
		// weight                                             weight
		// race                                               race
		// ethnicity                                          ethnicity
		// vital_status                                       vital_status
		// days_to_last_followup                              days_to_last_followup
		// days_to_last_known_alive                           days_to_last_known_alive
		// days_to_death                                      days_to_death
		// relative_family_cancer_history                     relative_family_cancer_history
		// cancer_first_degree_relative                       cancer_first_degree_relative
		// clinical_stage                                     clinical_stage
		// pathologic_stage                                   pathologic_stage
		// age_at_initial_pathologic_diagnosis                age_at_initial_pathologic_diagnosis
		// follow_up_vital_status                             follow_up_vital_status
		// follow_up_days_to_last_followup                    follow_up_days_to_last_followup
		// follow_up_days_to_death                            follow_up_days_to_death
		// follow_up_new_tumor_event_after_initial_treatment  follow_up_new_tumor_event_after_initial_treatment
		line = thePatient + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" +  
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA" + 
				"\t" + "NA";
		return line;
	};
	
	// TODO: duplicated from ClinicalDF.java, de-duplicate in future
	private ArrayList<String> getOutputHeaders()
	{
		ArrayList<String> headers = new ArrayList<>();
		headers.add("Patient");
		headers.add("days_to_birth");
		headers.add("height");
		headers.add("weight");
		headers.add("race");
		headers.add("ethnicity");
		headers.add("vital_status");
		headers.add("days_to_last_followup");
		headers.add("days_to_last_known_alive");
		headers.add("days_to_death");
		headers.add("relative_family_cancer_history");
		headers.add("cancer_first_degree_relative");
		headers.add("clinical_stage");
		headers.add("pathologic_stage");
		headers.add("age_at_initial_pathologic_diagnosis");
		headers.add("follow_up_vital_status");
		headers.add("follow_up_days_to_last_followup");
		headers.add("follow_up_days_to_death");
		headers.add("follow_up_new_tumor_event_after_initial_treatment");
		return headers;
	}
	
	static public TreeSet<String> getPatients(Path theDataFile) throws IOException
	{
		TreeSet<String> patients = new TreeSet<>();
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(theDataFile, Charset.availableCharsets().get("UTF-8")))
		{
			String line = br.readLine();
			String [] tokens = line.split("\t", -1);
			for (String val : tokens)
			{
				if (!"".equals(val))
				{
					String [] splitted = val.split("-", -1);
					String patient = splitted[0] + "-" + splitted[1] + "-" + splitted[2];
					patients.add(patient);
				}
			}
		}
		return patients;
	}
}
