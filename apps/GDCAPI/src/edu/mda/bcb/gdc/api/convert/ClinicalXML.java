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

package edu.mda.bcb.gdc.api.convert;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.util.UpdateableMap;
import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Tod-Casasent
 */
public class ClinicalXML
{
	public static void processDirectory(UpdateableMap<GDCFile> theFiles, final File theSourceDir, File theOutputFile) throws IOException
	{
		////////////////////////////////////////////////////////////////////////
		//// file array
		////////////////////////////////////////////////////////////////////////
		ArrayList<GDCFile> gdcFiles = new ArrayList<>();
		for (Updateable_Mixin<GDCFile> data : theFiles.values())
		{
			gdcFiles.add((GDCFile)data);
		}
		if (gdcFiles.size()>0)
		{
			if (!theOutputFile.getParentFile().exists())
			{
				theOutputFile.getParentFile().mkdirs();
			}
		}
		else
		{
			GDCAPI.printWarn("ClinicalXML No files to process");
			return;
		}
		final GDCFile [] gdcArray = gdcFiles.toArray(new GDCFile[0]);
		gdcFiles = null;
		////////////////////////////////////////////////////////////////////////
		GDCAPI.printLn("ClinicalXML::processDirectory");
		SortedSet<String> dataList = Collections.synchronizedSortedSet(new TreeSet<>());
		List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
		// note this solution changes the parallelism settings for the entire system
		// so if you have multiple parallel operations going on this could do weird things
		GDCAPI.printLn("PARALLEL-THREADS = 5");
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "5");
		AtomicLong counter = new AtomicLong(0);
		Arrays.asList(gdcArray)
				.parallelStream()
				.forEach((GDCFile myGdcFile) ->
				{
					if (0 == counter.incrementAndGet() % 100)
					{
						GDCAPI.printLn("counter = " + counter + " of " + gdcArray.length);
					}
					try
					{
						dataList.addAll(ClinicalXML.run(myGdcFile, theSourceDir));
					}
					catch (Exception exp)
					{
						errors.add(exp);
					}
				});
		for (Exception err : errors)
		{
			GDCAPI.printErr("ClinicalXML::processDirectory errors " + err.getMessage(), err);
		}
		GDCAPI.printLn("Found " + dataList.size() + " sample ids for " + theOutputFile);
		String headers = "bcr_patient_barcode\tbcr_patient_uuid\tdays_to_birth\theight\tweight\t" +
					"race\tethnicity\tvital_status\tdays_to_last_followup\tdays_to_last_known_alive\tdays_to_death\t" +
					"relative_family_cancer_history\tcancer_first_degree_relative\tclinical_stage\tpathologic_stage\t" +
					"age_at_initial_pathologic_diagnosis\tfollow_up_vital_status\tfollow_up_days_to_last_followup\t" +
					"follow_up_days_to_death\tfollow_up_new_tumor_event_after_initial_treatment";
		GDCAPI.writeDataframe(theOutputFile, headers, dataList);
	}
	
	private ClinicalXML()
	{
	}

	static public TreeSet<String> run(GDCFile theFile, File theSourceDir) throws ParserConfigurationException, SAXException, IOException, Exception
	{
		TreeSet<String> dataList = new TreeSet<>();
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(theFile.getFileObj(theSourceDir, true));
			doc.getDocumentElement().normalize();
			String bcr_patient_barcode = BiospecimenXML.getElementText(doc, "shared:bcr_patient_barcode", 1);
			String bcr_patient_uuid = BiospecimenXML.getElementText(doc, "shared:bcr_patient_uuid", 1);
			String days_to_birth = BiospecimenXML.getElementText(doc, "clin_shared:days_to_birth", "");
			String height = BiospecimenXML.getElementText(doc, "clin_shared:height", "");
			String weight = BiospecimenXML.getElementText(doc, "clin_shared:weight", "");
			ArrayList<String> race = new ArrayList<>();
			NodeList raceList = doc.getElementsByTagName("clin_shared:race");
			for (int nodeIndex = 0; nodeIndex < raceList.getLength(); nodeIndex++)
			{
				Element raceElement = (Element) (raceList.item(nodeIndex));
				String sampleTypeId = raceElement.getNodeValue();
				race.add(sampleTypeId);
			}
			String ethnicity = BiospecimenXML.getElementText(doc, "clin_shared:ethnicity", "");
			String vital_status = BiospecimenXML.getElementText(doc, "clin_shared:vital_status", "");
			String days_to_last_followup = BiospecimenXML.getElementText(doc, "clin_shared:days_to_last_followup", "");
			String days_to_last_known_alive = BiospecimenXML.getElementText(doc, "clin_shared:days_to_last_known_alive", "");
			String days_to_death = BiospecimenXML.getElementText(doc, "clin_shared:days_to_death", "");
			String relative_family_cancer_history = BiospecimenXML.getElementText(doc, "clin_shared:relative_family_cancer_history", "");
			String cancer_first_degree_relative = BiospecimenXML.getElementText(doc, "clin_shared:cancer_first_degree_relative", "");
			String clinical_stage = BiospecimenXML.getElementText(doc, "shared_stage:stage_event", "shared_stage:clinical_stage", "");
			String pathologic_stage = BiospecimenXML.getElementText(doc, "shared_stage:stage_event", "shared_stage:pathologic_stage", "");
			String age_at_initial_pathologic_diagnosis = BiospecimenXML.getElementText(doc, "clin_shared:age_at_initial_pathologic_diagnosis", "");
			String follow_up_vital_status = BiospecimenXML.getElementText(doc, "clin_shared:vital_status", "");
			String follow_up_days_to_last_followup = BiospecimenXML.getElementText(doc, "clin_shared:days_to_last_followup", "");
			String follow_up_days_to_death = BiospecimenXML.getElementText(doc, "clin_shared:days_to_death", "");
			String follow_up_new_tumor_event_after_initial_treatment = BiospecimenXML.getElementText(doc, "nte:new_tumor_event_after_initial_treatment", "");
			dataList.add(bcr_patient_barcode + "\t" + bcr_patient_uuid + "\t" + days_to_birth + "\t" + height + "\t" + weight + "\t" + 
					GDCAPI.arrayToString(race, "|") + "\t" + ethnicity + "\t" + vital_status + "\t" + 
					days_to_last_followup + "\t" + days_to_last_known_alive + "\t" + days_to_death + "\t" + 
					relative_family_cancer_history + "\t" + cancer_first_degree_relative + "\t" + clinical_stage + "\t" + pathologic_stage + "\t" + 
					age_at_initial_pathologic_diagnosis + "\t" + follow_up_vital_status + "\t" + follow_up_days_to_last_followup + "\t" + 
					follow_up_days_to_death + "\t" + follow_up_new_tumor_event_after_initial_treatment);
		}
		catch(ParserConfigurationException exp)
		{
			throw new Exception("For file: " + theFile, exp);
		}
		catch(SAXException exp)
		{
			throw new Exception("For file: " + theFile, exp);
		}
		catch(IOException exp)
		{
			throw new Exception("For file: " + theFile, exp);
		}
		catch(Exception exp)
		{
			throw new Exception("For file: " + theFile, exp);
		}
		return dataList;
	}
}
