#!/bin/bash

VERSION_DATE=`date "+%y%j-%H%M"`

jar cvfm "MINERful-v${VERSION_DATE}.jar" manifest.mf lib src -C ./bin .
cp "MINERful-v${VERSION_DATE}.jar" "MINERful.jar"
zip -r "MINERful-v${VERSION_DATE}" \
    libs.cfg \
    MINERful.jar \
    README.md LICENSE licenses \
    lib bin \
    constraintsFunctions.cfg \
    run-MINERful.sh \
    run-MINERfulSimplifier.sh \
    run-MINERfulSlider.sh \
    run-MINERfulEventLogMaker.sh \
    run-MINERfulFitnessChecker.sh \
    run-XesLogSorter.sh
cp "MINERful-v${VERSION_DATE}.zip" "MINERful.zip"
