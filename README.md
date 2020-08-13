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

Also included is a pre-BETA Standardized Data Metabolomics Workbench Tool, for downloading data from the Metabolomics Workbench and converting it to Standardized Data. (Documentation and Docker images available on release.)

Java projects are Netbeans 11 projects.
R packages are RStudio projects.

|Component|Description|
|--|--|
|GDCAPI (app)|download and convert GDC data|
|PlatformReleaseMaps (app)|create HG19 and HG38 gene files for GDCAPI|
|StdMW (app)|HTTP GUI for downloading and converting Metabolomics Workbench data|
|StdMW (docker)|Docker Image for StdMW|

