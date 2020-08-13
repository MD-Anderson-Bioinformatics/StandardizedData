#!/bin/bash

echo "run local PlatformReleaseMaps"
cd ../apps/PlatformReleaseMaps
java -Xmx8G -Xms8G -Djava.awt.headless=true -classpath "./dist/PlatformReleaseMaps.jar" edu.mda.bcb.prm.PlatformReleaseMaps

echo "done"
