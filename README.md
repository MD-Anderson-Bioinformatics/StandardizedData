# Standardized Data

 * Bradley Broom (owner)
 * John Weinstein
 * Rehan Akbani
 * Tod Casasent (developer)

This is for educational and research purposes only.

Samples from large research projects are often processed and run in multiple batches at different times. Because the samples are processed in batches rather than all at once, the data can be vulnerable to systematic noise such as batch effects (unwanted variation between batches) and trend effects (unwanted variation over time), which can lead to misleading analysis results.

Standardized Data is a project for converting large public datasets into matrix and dataframe files for simple, quick use in analyses.

Additional information can be found at http://bioinformatics.mdanderson.org/main/TCGABatchEffects:Overview

Standardized Data is a project for updating and maintaining data from the TCGA project, which has been turned into a simple matrix format for easier use.

This includes an application for downloading and converting data from the Genomic Data Commons (GDC).

Also included is the MDACC Standardized Data Metabolomics Workbench Tool, for downloading data from the Metabolomics Workbench and converting it to Standardized Data.
Documentation and Docker Hub image available from SMW_01_InstallExtImageLinux.pdf in docs directory.

Java projects are Netbeans 11 projects.
R packages are RStudio projects.

|Component|Description|
|--|--|
|GDCAPI (app)|download and convert GDC data|
|PlatformReleaseMaps (app)|create HG19 and HG38 gene files for GDCAPI|
|StdMW (app)|HTTP GUI for downloading and converting Metabolomics Workbench data|
|StdMWUtils (app)|Library for downloading and converting Metabolomics Workbench data|
|StdMW (docker)|Docker Image for StdMW|

# Standardized Metabolomics Workbench Tool Docker Quick Start

Download the docker-compose.yml file at the root of this repository. This file is setup for use on Linux.

Make the following directories.
 - /SMW/MW_CACHE
 - /SMW/MW_ZIP
 - /SMW/MW_LOGS

Copy the contents of apps/StdMW/data/testing_static/MW_CACHE into /SMW/MW_CACHE

Permissions or ownership of the directories may need to be changed or matched to the Docker image user 2002.

In the directory with the docker-compose.yml file run:

	docker-compose -p stdmwhubtest -f docker-compose.yml up --no-build -d

You can stop it with:

	docker-compose -p stdmwhubtest -f docker-compose.yml down

Use URL:

   localhost:8080/StdMW

**For educational and research purposes only.**

**Funding** 
This work was supported in part by U.S. National Cancer Institute (NCI) grant: Weinstein, Mills, Akbani. Batch effects in molecular profiling data on cancers: detection, quantification, interpretation, and correction, 5U24CA210949
This work was supported in part by U.S. National Cancer Institute (NCI) grant: Weinstein, Broom, Akbani. Computational Tools for Analysis and Visualization of Quality Control Issues in Metabolomic Data, U01CA235510

