#!/bin/bash

./00_clear.bash

echo "move to app dir"
cd ../apps

echo "compile PlatformReleaseMaps"
cd PlatformReleaseMaps
ant clean jar
cd ..

echo "compile GDCAPI"
cd GDCAPI
ant clean jar
cd ..

echo "compile StdMW"
cd StdMW
ant -f build.xml
cd ..

echo "list ./PlatformReleaseMaps/dist"
ls -l ./PlatformReleaseMaps/dist
echo "list ./GDCAPI/dist"
ls -l ./GDCAPI/dist
echo "list ./StdMW/dist"
ls -l ./StdMW/dist

echo "done"
