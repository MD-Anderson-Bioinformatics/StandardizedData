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

package edu.mda.bcb.gdc.api.endpoints.legacy;

/**
 *
 * @author Tod-Casasent
 */
public class LegacyWorkflowStrings
{
	static public String [] M_WORKFLOW_NAMES =
	{
		"RNASeq-GeneV1",
		"RNASeq-GeneV2",
		"RNASeq-IsoformV2",
		"Methylation27",
		"Methylation450",
		"RPPA",
		"SNP6",
		"miRNA-gene",
		"miRNA-isoform",
		"mutations",
	};
	
	static public String [] M_WORKFLOW_JSON =
	{
		// RNASeq-Gene V1
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"RNA-Seq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"platform\",\n" +
"						\"value\":\"Illumina HiSeq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"Gene expression quantification\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"in\",\n" +
"				\"content\":\n" +
"				{\n" +
"					\"field\":\"tags\",\n" +
"					\"value\":[\"v1\"]\n" +
"				}\n" +
"			}",
		// RNASeq-Gene
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"RNA-Seq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"platform\",\n" +
"						\"value\":\"Illumina HiSeq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"Gene expression quantification\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"in\",\n" +
"				\"content\":\n" +
"				{\n" +
"					\"field\":\"tags\",\n" +
"					\"value\":[\"unnormalized\"]\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"in\",\n" +
"				\"content\":\n" +
"				{\n" +
"					\"field\":\"tags\",\n" +
"					\"value\":[\"v2\"]\n" +
"				}\n" +
"			}",
		// RNASeq-Isoform
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"RNA-Seq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"platform\",\n" +
"						\"value\":\"Illumina HiSeq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"Isoform expression quantification\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"in\",\n" +
"				\"content\":\n" +
"				{\n" +
"					\"field\":\"tags\",\n" +
"					\"value\":[\"unnormalized\"]\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"in\",\n" +
"				\"content\":\n" +
"				{\n" +
"					\"field\":\"tags\",\n" +
"					\"value\":[\"v2\"]\n" +
"				}\n" +
"			}",
		//"Methylation27",
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"Methylation array\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"platform\",\n" +
"						\"value\":\"Illumina Human Methylation 27\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"Methylation beta value\"\n" +
"				}\n" +
"			}",
		//"Methylation450",
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"Methylation array\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"platform\",\n" +
"						\"value\":\"Illumina Human Methylation 450\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"Methylation beta value\"\n" +
"				}\n" +
"			}",
		//"RPPA",
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"Protein expression array\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"platform\",\n" +
"						\"value\":\"MDA_RPPA_Core\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"Protein expression quantification\"\n" +
"				}\n" +
"			}",
		//"SNP6",
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"Genotyping array\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"platform\",\n" +
"						\"value\":\"Affymetrix SNP Array 6.0\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"Copy number segmentation\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"in\",\n" +
"				\"content\":\n" +
"				{\n" +
"					\"field\":\"tags\",\n" +
"					\"value\":[\"hg19\"]\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"in\",\n" +
"				\"content\":\n" +
"				{\n" +
"					\"field\":\"tags\",\n" +
"					\"value\":[\"nocnv\"]\n" +
"				}\n" +
"			}",
		//"miRNA-gene",
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"miRNA-Seq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"platform\",\n" +
"						\"value\":\"Illumina HiSeq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"miRNA gene quantification\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"in\",\n" +
"				\"content\":\n" +
"				{\n" +
"					\"field\":\"tags\",\n" +
"					\"value\":[\"hg19\"]\n" +
"				}\n" +
"			}",
		//"miRNA-isoform",
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"miRNA-Seq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"platform\",\n" +
"						\"value\":\"Illumina HiSeq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"miRNA isoform quantification\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"in\",\n" +
"				\"content\":\n" +
"				{\n" +
"					\"field\":\"tags\",\n" +
"					\"value\":[\"hg19\"]\n" +
"				}\n" +
"			}",
		//"mutations",
		",\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"experimental_strategy\",\n" +
"						\"value\":\"DNA-Seq\"\n" +
"				}\n" +
"			},\n" +
"			{\n" +
"				\"op\":\"=\",\n" +
"				\"content\":\n" +
"				{\n" +
"						\"field\":\"data_type\",\n" +
"						\"value\":\"Simple somatic mutation\"\n" +
"				}\n" +
"			}",
	};
	
	static public String [] M_WORKFLOW_DATATYPE =
	{
		//"RNASeq-Genev1",
		"data_category",
		//"RNASeq-Gene",
		"data_category",
		//"RNASeq-Isoform"
		"data_category",
		//"Methylation27",
		"data_category",
		//"Methylation450",
		"data_category",
		//"RPPA",
		"data_category",
		//"SNP6",
		"data_category",
		//"miRNA-gene",
		"experimental_strategy",
		//"miRNA-isoform",
		"experimental_strategy",
		//"mutations",
		"data_type",
	};
	
	static public String [] M_WORKFLOW_PLATFORM =
	{
		//"RNASeq-Gene v1",
		"experimental_strategy|tags",
		//"RNASeq-Gene",
		"experimental_strategy|tags",
		//"RNASeq-Isoform"
		"experimental_strategy|tags",
		//"Methylation27",
		"platform",
		//"Methylation450",
		"platform",
		//"RPPA",
		"platform",
		//"SNP6",
		"platform|tags",
		//"miRNA-gene",
		"data_type|tags",
		//"miRNA-isoform",
		"data_type|tags",
		//"mutations",
		"experimental_strategy|platform",
	};
}
