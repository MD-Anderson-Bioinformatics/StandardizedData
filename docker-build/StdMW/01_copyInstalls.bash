#!/bin/bash

echo "START 01_copyInstalls"

set -e

STD_DIR=$1

echo "Copy StdMW WAR"
cp ${STD_DIR}/apps/StdMW/target/StdMW-*.war ${STD_DIR}/docker-build/StdMW/installations/StdMW.war

echo "List StdMW Installations"
ls -l ${STD_DIR}/docker-build/StdMW/installations/

echo "FINISH 01_copyInstalls"

