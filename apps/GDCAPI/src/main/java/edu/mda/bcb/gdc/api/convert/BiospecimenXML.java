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

package edu.mda.bcb.gdc.api.convert;

import edu.mda.bcb.gdc.api.GDCAPI;
import edu.mda.bcb.gdc.api.data.GDCFile;
import edu.mda.bcb.gdc.api.data.Patient;
import edu.mda.bcb.gdc.api.util.UpdateableMap;
import edu.mda.bcb.gdc.api.util.Updateable_Mixin;
import edu.mda.bcb.gdc.api.data.DataType;
import edu.mda.bcb.gdc.api.data.Program;
import edu.mda.bcb.gdc.api.data.Project;
import edu.mda.bcb.gdc.api.data.Workflow;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * BiospecimenXML wraps the processing of the GDC XML files into biospecimen.tsv files.
 * Most function support specific tag types, such as required tags, optional tags,
 * or tags of which there should be specific number.
 * 
 * @author Tod-Casasent
 */
public class BiospecimenXML
{
	/**
	 * processDirectory processes the individual files within a particular directory
	 * to generate a biospecimen.tsv file for that data.
	 * 
	 * @param theFiles	Wrapped TreeMap of GDCFile objects to be processed
	 * @param theSourceDir	File giving directory where GDCFiles reside.
	 * @param theOutputFile File giving biospecimen.tsv file to which to write data.
	 * @param theProgram a Program object, representing the GDC program information.
	 * @param theProject a Project object, representing the GDC Project information.
	 * @param theDataType a DataType object, representing the GDC DataType (one level more general from workflow) information.
	 * @param theWorkflow a Workflow object, representing the GDC Workflow (similar to platform) information.
	 * @throws IOException 
	 */
	public static void processDirectory(UpdateableMap<GDCFile> theFiles, final File theSourceDir, File theOutputFile,
			Program theProgram, Project theProject, DataType theDataType, Workflow theWorkflow) throws IOException
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
			GDCAPI.printWarn("BiospecimenXML No files to process");
			return;
		}
		final GDCFile [] gdcArray = gdcFiles.toArray(new GDCFile[0]);
		gdcFiles = null;
		////////////////////////////////////////////////////////////////////////
		GDCAPI.printLn("BiospecimenXML::processDirectory");
		SortedSet<BiospecimenXMLinst> dataList = Collections.synchronizedSortedSet(new TreeSet<>());
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
						GDCAPI.printLn("BiospecimenXML counter = " + counter + " of " + gdcArray.length);
					}
					try
					{
						dataList.addAll(BiospecimenXML.run(myGdcFile, theSourceDir));
					}
					catch (Exception exp)
					{
						errors.add(exp);
					}
				});
		for (Exception err : errors)
		{
			GDCAPI.printErr("BiospecimenXML::processDirectory errors " + err.getMessage(), err);
		}
		GDCAPI.printLn("Check for duplicate barcodes and UUIDs");
		TreeSet<String> barcodes = new TreeSet<>();
		TreeSet<String> uuid = new TreeSet<>();
		for (BiospecimenXMLinst bs : dataList)
		{
			if (false==barcodes.add(bs.mBarcode))
			{
				GDCAPI.printErr("Duplicate barcode found " + bs.mBarcode);
			}
			if (false==uuid.add(bs.mUuid))
			{
				GDCAPI.printErr("Duplicate UUID found " + bs.mUuid);
			}
		}
		GDCAPI.printLn("Found " + dataList.size() + " sample ids for " + theOutputFile);
		GDCAPI.writeDataframe(theOutputFile, BiospecimenXMLinst.getHeaders(), dataList);
	}
	
	/**
	 * Private constructor -- build in static function run.
	 */
	private BiospecimenXML()
	{
	}

	/**
	 * getElementText returns text for a tag from the XML - or the default value.
	 * 
	 * @param theDoc XML Document object.
	 * @param theTag The tag to grab value from.
	 * @param theDefault Default string to use if tag not found or empty.
	 * @return String value of element text.
	 * @throws java.lang.Exception Throws exception if wrong number of tag elements found
	 */
	static public String getElementText(Document theDoc, String theTag, String theDefault) throws Exception
	{
		String value = theDefault;
		NodeList nl = theDoc.getElementsByTagName(theTag);
		if (nl.getLength() > 0)
		{
			Node myNode = nl.item(0);
			value = myNode.getTextContent();
		}
		return value;
	}

	/**
	 * getElementText returns text for a tag from the XML or throws an exception
	 * if the number of elements with that tag name do not match theCount.
	 * 
	 * @param theDoc XML Document object.
	 * @param theTag The tag to grab value from.
	 * @param theCount Checks length of nodelist for tag and returns item(0) if count matches.
	 * @return String value of element text.
	 * @throws java.lang.Exception Throws exception if wrong number of tag elements found
	 */
	static public String getElementText(Document theDoc, String theTag, int theCount) throws Exception
	{
		NodeList nl = theDoc.getElementsByTagName(theTag);
		if (nl.getLength() != theCount)
		{
			throw new Exception("Incorrect number of '" + theTag + "'. Found " + nl.getLength() + " expected " + theCount);
		}
		Node myNode = nl.item(0);
		return myNode.getTextContent();
	}
	
	/**
	 * getElementCount counts how many tags with given name are available.
	 * 
	 * @param theDoc XML Document object.
	 * @param theTag The tag to grab count from.
	 * @return number of elements for given tag
	 */
	static public int getElementCount(Document theDoc, String theTag)
	{
		NodeList nl = theDoc.getElementsByTagName(theTag);
		return nl.getLength();
	}
	
	/**
	 * getElementText finds element with theParentElement tag, then element in that
	 * with theTag, or give theDefault.
	 * 
	 * @param theDoc XML Document object.
	 * @param theParentElement top level element
	 * @param theTag element belonging to parent element
	 * @param theDefault Default string to use if tag not found or empty.
	 * @return String value of element text.
	 */
	static public String getElementText(Document theDoc, String theParentElement, String theTag, String theDefault)
	{
		String value = theDefault;
		NodeList nl = theDoc.getElementsByTagName(theParentElement);
		if (nl.getLength() > 0)
		{
			ArrayList<Element> tagList = getElements((Element)nl.item(0), theTag);
			if (tagList.size() > 0)
			{
				value = tagList.get(0).getTextContent();
			}
		}
		return value;
	}
	
	/**
	 * getElementText finds element with theParentElement tag, then element in that
	 * with theTag, returns text for a tag from the XML or throws an exception
	 * if the number of elements with that tag name do not match theCount.
	 * 
	 * @param theDoc XML Document object.
	 * @param theParentElement top level element
	 * @param theTag element belonging to parent element
	 * @param theCount Checks length of nodelist for tag and returns item(0) if count matches.
	 * @return String value of element text.
	 * @throws java.lang.Exception Throws exception if wrong number of tag elements found
	 */
	static public String getElementText(Document theDoc, String theParentElement, String theTag, int theCount) throws Exception
	{
		NodeList nl = theDoc.getElementsByTagName(theParentElement);
		if (nl.getLength() != 1)
		{
			throw new Exception("Incorrect number of parent elements '" + theParentElement + "'. Found " + nl.getLength() + " expected " + 1);
		}
		ArrayList<Element> tagList = getElements((Element)nl.item(0), theTag);
		if (tagList.size() != theCount)
		{
			throw new Exception("Incorrect number of tags '" + theTag + "'. Found " + tagList.size() + " expected " + theCount);
		}
		return tagList.get(0).getTextContent();
	}

	/**
	 * Find child of element with node name that equals the tag, then return the text content.
	 * @param theElement Element whose children will be searched.
	 * @param theTag Tag name to match.
	 * @return String in matching tag, or null.
	 */
	static public String getElementText(Element theElement, String theTag)
	{
		String text = null;
		Node myNode = theElement.getFirstChild();
		while (null != myNode)
		{
			if (myNode.getNodeName().equals(theTag))
			{
				text = myNode.getTextContent();
				myNode = null;
			}
			else
			{
				myNode = myNode.getNextSibling();
			}
		}
		return text;
	}

	/**
	 * Get children of element that have given tag name
	 * 
	 * @param theElement Element to search
	 * @param theTag Tag name to match.
	 * @return Unordered list of elements with given tag name
	 */
	static public ArrayList<Element> getElements(Element theElement, String theTag)
	{
		ArrayList<Element> list = new ArrayList<>();
		NodeList nl = theElement.getElementsByTagName(theTag);
		for (int nodeIndex = 0; nodeIndex < nl.getLength(); nodeIndex++)
		{
			Node myNode = nl.item(nodeIndex);
			if (myNode.getNodeName().equals(theTag))
			{
				list.add((Element) myNode);
			}
		}
		return list;
	}

	/**
	 * Static run file to process theGDCFile in theSourceDir.
	 * 
	 * @param theGdcFile File to parse.
	 * @param theSourceDir Directory containing the file.
	 * @return Returns an ordered, unique list of rows built from XML in the file.
	 * @throws ParserConfigurationException XML is bad
	 * @throws SAXException XML is bad
	 * @throws IOException Generally file is missing.
	 * @throws Exception Generally indicates the XML did not match expected parameters, such as required value not found.
	 */
	static public TreeSet<BiospecimenXMLinst> run(GDCFile theGdcFile, File theSourceDir) throws ParserConfigurationException, SAXException, IOException, Exception
	{
		TreeSet<BiospecimenXMLinst> dataList = new TreeSet<>();
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(theGdcFile.getFileObj(theSourceDir, true));
			doc.getDocumentElement().normalize();
			String bcr = getElementText(doc, "admin:admin", "admin:bcr", 1);
			String batch = getElementText(doc, "admin:admin", "admin:batch_number", 1);
			String project = getElementText(doc, "admin:admin", "admin:project_code", 1);
			String disease = getElementText(doc, "admin:admin", "admin:disease_code", 1);
			String tss = getElementText(doc, "shared:tissue_source_site", 1);
			String sex = getElementText(doc, "shared:gender", 1);
			NodeList sampleList = doc.getElementsByTagName("bio:sample");
			for (int nodeIndex = 0; nodeIndex < sampleList.getLength(); nodeIndex++)
			{
				Element sample = (Element) (sampleList.item(nodeIndex));
				String sampleTypeId = getElementText(sample, "bio:sample_type_id");
				String sampleTypeName = getElementText(sample, "bio:sample_type");
				String sampleIsFfpe = getElementText(sample, "bio:is_ffpe");
				ArrayList<Element> portionList = getElements(sample, "bio:shipment_portion");
				for (Element shipPortion : portionList)
				{
					String portionPlate = getElementText(shipPortion, "bio:plate_id");
					String portionCenter = getElementText(shipPortion, "bio:center_id");
					String portionShipDay = getElementText(shipPortion, "bio:day_of_shipment");
					String portionShipMonth = getElementText(shipPortion, "bio:month_of_shipment");
					String portionShipYear = getElementText(shipPortion, "bio:year_of_shipment");
					String portionBarcode = getElementText(shipPortion, "bio:shipment_portion_bcr_aliquot_barcode");
					String portionUuid = getElementText(shipPortion, "bio:bcr_shipment_portion_uuid");
					String portionIsFfpe = getElementText(shipPortion, "bio:is_ffpe");
					dataList.add(new BiospecimenXMLinst(bcr, batch, project, 
							disease, tss, sex, 
							sampleTypeId, sampleTypeName, sampleIsFfpe, 
							portionPlate, portionCenter,
							portionShipDay, portionShipMonth, portionShipYear,
							portionBarcode, portionUuid, null,
							null, null, null,
							null, null, portionIsFfpe, 
							((Patient)theGdcFile.mPatients.firstEntry().getValue()).mUUID, 
							((Patient)theGdcFile.mPatients.firstEntry().getValue()).mBarcode));
				}
				ArrayList<Element> analyteList = getElements(sample, "bio:analyte");
				for (Element analyte : analyteList)
				{
					//String analyteTypeId = getElementText(analyte, "bio:analyte_type_id");
					//String analyteTypeName = getElementText(analyte, "bio:analyte_type");
					//String analyteIsFfpe = getElementText(analyte, "bio:is_derived_from_ffpe");
					ArrayList<Element> aliquotList = getElements(sample, "bio:aliquot");
					for (Element aliquot : aliquotList)
					{
						String aliquotPlate = getElementText(aliquot, "bio:plate_id");
						String aliquotCenter = getElementText(aliquot, "bio:center_id");
						String aliquotShipDay = getElementText(aliquot, "bio:day_of_shipment");
						String aliquotShipMonth = getElementText(aliquot, "bio:month_of_shipment");
						String aliquotShipYear = getElementText(aliquot, "bio:year_of_shipment");
						String aliquotBarcode = getElementText(aliquot, "bio:bcr_aliquot_barcode");
						String aliquotUuid = getElementText(aliquot, "bio:bcr_aliquot_uuid");
						String aliquotSourceCenter = getElementText(aliquot, "bio:source_center");
						String aliquotConcentration = getElementText(aliquot, "bio:concentration"); // convert to range
						String aliquotQuantity = getElementText(aliquot, "bio:quantity"); // convert to range
						String aliquotVolume = getElementText(aliquot, "bio:volume"); // convert to range
						String aliquotPlateRow = getElementText(aliquot, "bio:plate_row");
						String aliquotPlateColumn = getElementText(aliquot, "bio:plate_column");
						String aliquotDerivedFromFfpe = getElementText(aliquot, "bio:is_derived_from_ffpe");
						dataList.add(new BiospecimenXMLinst(bcr, batch, project, 
								disease, tss, sex, 
								sampleTypeId, sampleTypeName, sampleIsFfpe, 
								aliquotPlate, aliquotCenter,
								aliquotShipDay, aliquotShipMonth, aliquotShipYear,
								aliquotBarcode, aliquotUuid, aliquotSourceCenter,
								aliquotConcentration, aliquotQuantity, aliquotVolume,
								aliquotPlateRow, aliquotPlateColumn, aliquotDerivedFromFfpe, 
								((Patient)theGdcFile.mPatients.firstEntry().getValue()).mUUID, 
								((Patient)theGdcFile.mPatients.firstEntry().getValue()).mBarcode));
					}
				}
			}
		}
		catch(ParserConfigurationException exp)
		{
			throw new Exception("For file: " + theGdcFile.getFileObj(theSourceDir, true), exp);
		}
		catch(SAXException exp)
		{
			throw new Exception("For file: " + theGdcFile.getFileObj(theSourceDir, true), exp);
		}
		catch(IOException exp)
		{
			throw new Exception("For file: " + theGdcFile.getFileObj(theSourceDir, true), exp);
		}
		catch(Exception exp)
		{
			throw new Exception("For file: " + theGdcFile.getFileObj(theSourceDir, true), exp);
		}
		return dataList;
	}
}
