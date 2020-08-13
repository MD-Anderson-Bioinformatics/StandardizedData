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

package edu.mda.bcb.gdc.api.dataframes;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.data.Patient;
import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class ClinicalDF
{
	public GDCFile [] mFiles = null;
	public File mConvertDir = null;
	public String mProgram = null;
	public String mProject = null;
	
	public ClinicalDF(GDCFile [] theFiles, File theConvertDir,
			String theProgram, String theProject)
	{
		mFiles = theFiles;
		mConvertDir = theConvertDir;
		mProgram = theProgram;
		mProject = theProject;
	}
	
	public void writeClinicalFile(File theClinicalDir) throws IOException
	{
		GDCAPI.printLn("Clinical::writeClinicalFile called");
		GDCAPI.printLn("Clinical::writeClinicalFile - getSamples");
		TreeSet<Patient> patients = getPatients();
		GDCAPI.printLn("Clinical::writeClinicalFile - getDataframe");
		Dataframe df = getDataframe(theClinicalDir);
		File outFile = new File(mConvertDir, "clinical.tsv");
		GDCAPI.printLn("Clinical::writeClinicalFile - collects header info");
		Integer [] headerIndexes = getUsableHeaderIndexes(df.mHeaders);
		ArrayList<String> sourceHeaders = getSourceHeaders();
		ArrayList<String> outputHeaders = getOutputHeaders();
		GDCAPI.printLn("Clinical::writeClinicalFile - write " + outFile.getAbsolutePath());
		try (PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)), true))
		{
			boolean first = true;
			////////////////////////////////////////////
			// header
			for (Integer index : headerIndexes)
			{
				if (false==first)
				{
					ps.print("\t");
				}
				else
				{
					first = false;
				}
				ps.print(outputHeaders.get(index));
			}
			ps.println();
			////////////////////////////////////////////
			// samples
			for (Patient myPatient : patients)
			{
				first = true;
				for (Integer index : headerIndexes)
				{
					if (false==first)
					{
						ps.print("\t");
					}
					else
					{
						first = false;
					}
					ps.print(df.getColumnFromColumnValue(sourceHeaders.get(index), getUUIDHeader(), myPatient.mUUID));
				}
				ps.println();
			}
		}
	}
	
	private Dataframe getDataframe(File theClinicalDir) throws IOException
	{
		Dataframe df = new Dataframe();
		if (null!=theClinicalDir)
		{
			File myFile = new File(theClinicalDir, "clinical.tsv");
			df.readDataFrame(myFile);
		}
		return df;
	}
	
	private TreeSet<Patient> getPatients()
	{
		// collect set of samples
		TreeSet<Patient> patients = new TreeSet<>();
		for (GDCFile gdcFile : mFiles)
		{
			for (Updateable_Mixin<Patient> myPatient : gdcFile.mPatients.values())
			{
				Patient objPatient = ((Patient)myPatient);
				patients.add(objPatient);
			}
		}
		return patients;
	}
	
	private ArrayList<String> getSourceHeaders()
	{
		ArrayList<String> headers = new ArrayList<>();
		headers.add("bcr_patient_barcode");
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
	
	private String getUUIDHeader()
	{
		return "bcr_patient_uuid";
	}
	
	private Integer [] getUsableHeaderIndexes(ArrayList<String> theAvailableHeaders)
	{
		ArrayList<String> sourceHeaders = getSourceHeaders();
		ArrayList<Integer> usable = new ArrayList<>();
		usable.add(0);
		usable.add(1);
		for (int test=2 ; test<sourceHeaders.size(); test++)
		{
			if (theAvailableHeaders.contains(sourceHeaders.get(test)))
			{
				usable.add(test);
			}
		}
		return usable.toArray(new Integer[0]);
	}
}
