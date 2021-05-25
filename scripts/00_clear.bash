#!/bin/bash

echo "START 00_clear"

set -e

STD_DIR=$1

echo "Clear build and target files"
rm -rf ${STD_DIR}/apps/*/build/*
rm -rf ${STD_DIR}/apps/*/target/*

echo "clear image components"
rm -f ${STD_DIR}/docker-build/StdMW/docker-compose.*.yml
rm -f ${STD_DIR}/docker-build/StdMW/Dockerfile
rm -f ${STD_DIR}/docker-build/StdMW/installations/StdMW.war

echo "Clear old output data"
rm -rf ${STD_DIR}/data/apps_out/PlatformReleaseMaps/*
rm -rf ${STD_DIR}/data/testing_dynamic/PlatformReleaseMaps/*

#echo "ls build and target files"
#find ${STD_DIR}/apps -name "build" -exec ls -l {} \; 
#find ${STD_DIR}/apps -name "target"  -exec ls -l {} \;

#echo "ls image components"
#ls -lh ${STD_DIR}/docker-build/StdMW/
#ls -lh ${STD_DIR}/docker-build/StdMW/installations/

#echo "ls old output data"
#find ${STD_DIR}/data/apps_out -type f
#find ${STD_DIR}/data/testing_dynamic -type f

echo "FINISH 00_clear"

