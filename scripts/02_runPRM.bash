#!/bin/bash

echo "START 02_runRPM"

set -e

STD_DIR=$1

set -e

echo "run local PlatformReleaseMaps"
cd ${STD_DIR}/apps/PlatformReleaseMaps
java -Xmx8G -Xms8G -Djava.awt.headless=true -classpath "${STD_DIR}/apps/PlatformReleaseMaps/target/*" edu.mda.bcb.prm.PlatformReleaseMaps

echo "START 02_runRPM"


