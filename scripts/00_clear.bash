#!/bin/bash

echo "Clear build and dist files"
rm -r ../apps/*/build/*
rm -r ../apps/*/dist/*

echo "Clear old output data"
rm -r ../data/apps_out/PlatformReleaseMaps/EnsemblGeneMap/*
rm -r ../data/apps_out/PlatformReleaseMaps/GDCIdentifiers/*

echo "Clear test output data"
rm -r ../data/testing_dynamic/PlatformReleaseMaps/*

echo "ls build and dist files"
ls ../apps/*/build
ls ../apps/*/dist

echo "ls old output data"
ls -l ../data/apps_out/PlatformReleaseMaps/EnsemblGeneMap
ls -l ../data/apps_out/PlatformReleaseMaps/GDCIdentifiers

echo "ls test output data"
ls ../data/testing_dynamic/PlatformReleaseMaps

echo "done"
