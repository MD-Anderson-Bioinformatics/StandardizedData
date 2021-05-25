#!/bin/bash

echo "START 01_compile"

set -e

STD_DIR=$1

echo "compile SamplesValidation"
cd ${STD_DIR}/apps/SamplesValidation
mvn clean install dependency:copy-dependencies

echo "compile PlatformReleaseMaps"
cd ${STD_DIR}/apps/PlatformReleaseMaps
mvn clean install dependency:copy-dependencies

echo "compile GDCAPI"
cd ${STD_DIR}/apps/GDCAPI
mvn clean install dependency:copy-dependencies

echo "compile StdMWUtils"
cd ${STD_DIR}/apps/StdMWUtils
mvn -e clean install dependency:copy-dependencies

echo "compile StdMW"
cd ${STD_DIR}/apps/StdMW
mvn clean install dependency:copy-dependencies

echo "list PlatformReleaseMaps/target/*.jar"
ls -lh ${STD_DIR}/apps/PlatformReleaseMaps/target/*.jar
echo "list GDCAPI/target/*.jar"
ls -lh ${STD_DIR}/apps/GDCAPI/target/*.jar
echo "list StdMWUtils/target/*.jar"
ls -lh ${STD_DIR}/apps/StdMWUtils/target/*.jar
echo "list StdMW/target/*.war"
ls -lh ${STD_DIR}/apps/StdMW/target/*.war

echo "FINISH 01_compile"

