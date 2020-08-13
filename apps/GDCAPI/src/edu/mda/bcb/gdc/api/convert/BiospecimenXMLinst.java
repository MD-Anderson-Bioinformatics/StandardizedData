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

/**
 *
 * @author Tod-Casasent
 */
public class BiospecimenXMLinst implements Comparable<BiospecimenXMLinst>
{
	public String mBcr = null;
	public String mBatch = null;
	public String mProject = null;
	public String mDisease = null;
	public String mTss = null;
	public String mSex = null;
	public String mSampleTypeId = null;
	public String mSampleTypeName = null;
	public boolean mIsFfpe = false;
	public String mPlateId = null;
	public String mAliquotCenter = null;
	public String mShipDate = null;
	public String mBarcode = null;
	public String mUuid = null;
	public String mSourceCenter = null;
	public String mAliquotConcentration = null;
	public String mAliquotQuantity = null;
	public String mAliquotVolume = null;
	public String mPlateRow = null;
	public String mPlateColumn = null;
	public String mPatientUuid = null;
	public String mPatientBarcode = null;

	public BiospecimenXMLinst(String theBcr, String theBatch, String theProject,
			String theDisease, String theTss, String theSex,
			String theSampleTypeId, String theSampleTypeName, String theSampleIsFfpe,
			String thePlateId, String theAliquotCenter,
			String theShipDateDay, String theShipDateMonth, String theShipDateYear,
			String theBarcode, String theUuid, String theSourceCenter,
			String theAliquotConcentration, String theAliquotQuantity, String theAliquotVolume,
			String thePlateRow, String thePlateColumn, String theAliquotIsFfpe,
			String thePatientUUID, String thePatientBarcode)
	{
		mUuid = theUuid;
		mBarcode = theBarcode;
		mPatientUuid = thePatientUUID;
		mPatientBarcode = thePatientBarcode;
		//
		mBcr = theBcr;
		mBatch = theBatch;
		mProject = theProject;
		mDisease = theDisease;
		mTss = theTss;
		mSex = theSex;
		mSampleTypeId = theSampleTypeId;
		mSampleTypeName = theSampleTypeName;
		if (("yes".equalsIgnoreCase(theSampleIsFfpe)) || ("yes".equalsIgnoreCase(theAliquotIsFfpe)))
		{
			mIsFfpe = true;
		}
		else
		{
			mIsFfpe = false;
		}
		mPlateId = thePlateId;
		mAliquotCenter = theAliquotCenter;
		//
		mShipDate = "";
		if ((null != theShipDateYear) && (!"".equals(theShipDateYear)))
		{
			mShipDate = mShipDate + theShipDateYear;
		}
		if ((null != theShipDateMonth) && (!"".equals(theShipDateMonth)))
		{
			mShipDate = mShipDate + "-" + theShipDateMonth;
		}
		if ((null != theShipDateDay) && (!"".equals(theShipDateDay)))
		{
			mShipDate = mShipDate + "-" + theShipDateDay;
		}
		mSourceCenter = theSourceCenter;
		mAliquotConcentration = theAliquotConcentration;
		mAliquotQuantity = theAliquotQuantity;
		mAliquotVolume = theAliquotVolume;
		mPlateRow = thePlateRow;
		mPlateColumn = thePlateColumn;
		//
		//
		if ((null == mBcr) || ("".equals(mBcr)))
		{
			mBcr = "Unknown";
		}
		if ((null == mBatch) || ("".equals(mBatch)))
		{
			mBatch = "Unknown";
		}
		if ((null == mDisease) || ("".equals(mDisease)))
		{
			mDisease = "Unknown";
		}
		if ((null == mTss) || ("".equals(mTss)))
		{
			mTss = "Unknown";
		}
		if ((null == mSex) || ("".equals(mSex)))
		{
			mSex = "Unknown";
		}
		if ((null == mSampleTypeId) || ("".equals(mSampleTypeId)))
		{
			mSampleTypeId = "Unknown";
		}
		if ((null == mSampleTypeName) || ("".equals(mSampleTypeName)))
		{
			mSampleTypeName = "Unknown";
		}
		if ((null == mPlateId) || ("".equals(mPlateId)))
		{
			mPlateId = "Unknown";
		}
		if ((null == mAliquotCenter) || ("".equals(mAliquotCenter)))
		{
			mAliquotCenter = "Unknown";
		}
		if ((null == mShipDate) || ("".equals(mShipDate)))
		{
			mShipDate = "Unknown";
		}
		if ((null == mBarcode) || ("".equals(mBarcode)))
		{
			mBarcode = "Unknown";
		}
		if ((null == mUuid) || ("".equals(mUuid)))
		{
			mUuid = "Unknown";
		}
		else
		{
			mUuid = mUuid.toUpperCase();
		}
		if ((null == mSourceCenter) || ("".equals(mSourceCenter)))
		{
			mSourceCenter = "Unknown";
		}
		if ((null == mAliquotConcentration) || ("".equals(mAliquotConcentration)))
		{
			mAliquotConcentration = "Unknown";
		}
		if ((null == mAliquotQuantity) || ("".equals(mAliquotQuantity)))
		{
			mAliquotQuantity = "Unknown";
		}
		if ((null == mAliquotVolume) || ("".equals(mAliquotVolume)))
		{
			mAliquotVolume = "Unknown";
		}
		if ((null == mPlateRow) || ("".equals(mPlateRow)))
		{
			mPlateRow = "Unknown";
		}
		if ((null == mPlateColumn) || ("".equals(mPlateColumn)))
		{
			mPlateColumn = "Unknown";
		}
		if ((null == mProject) || ("".equals(mProject)))
		{
			if (mBarcode.contains("-"))
			{
				mProject = mBarcode.split("-", -1)[0];
			}
			else
			{
				mProject = "Unknown";
			}
		}
	}

	@Override
	public int compareTo(BiospecimenXMLinst o)
	{
		return this.toString().compareTo(o.toString());
	}

	@Override
	public String toString()
	{
		return mPatientUuid + "\t"
				+ mPatientBarcode + "\t"
				+ mUuid + "\t"
				+ mBarcode + "\t"
				+ mProject + "\t"
				+ mDisease + "\t"
				+ mBatch + "\t"
				+ mBcr + "\t"
				+ mTss + "\t"
				+ mPlateId + "\t"
				+ mAliquotCenter + "\t"
				+ mShipDate + "\t"
				+ mSourceCenter + "\t"
				+ mSex + "\t"
				+ mSampleTypeId + "\t"
				+ mSampleTypeName + "\t"
				+ mIsFfpe + "\t"
				+ mAliquotConcentration + "\t"
				+ mAliquotQuantity + "\t"
				+ mAliquotVolume + "\t"
				+ mPlateRow + "\t"
				+ mPlateColumn;
	}

	static public String getHeaders()
	{
		return "PatientUUID" + "\t"
				+"PatientBarcode" + "\t"
				+ "Uuid" + "\t"
				+ "Barcode" + "\t"
				+"Project" + "\t"
				+ "Disease" + "\t"
				+ "Batch" + "\t"
				+ "Bcr" + "\t"
				+ "Tss" + "\t"
				+ "PlateId" + "\t"
				+ "AliquotCenter" + "\t"
				+ "ShipDate" + "\t"
				+ "SourceCenter" + "\t"
				+ "Sex" + "\t"
				+ "SampleTypeId" + "\t"
				+ "SampleTypeName" + "\t"
				+ "IsFfpe" + "\t"
				+ "AliquotConcentration" + "\t"
				+ "AliquotQuantity" + "\t"
				+ "AliquotVolume" + "\t"
				+ "PlateRow" + "\t"
				+ "PlateColumn";
	}

}
