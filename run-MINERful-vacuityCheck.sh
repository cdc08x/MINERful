#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2016/06/10
# Description:  This script launches the MinerFulVacuityChecker, in order to discover the Declare constraints that are non-vacuously satisfied in a given input event log.
# Installation: Please download the following files and directories from the MINERful GitHub Repository (https://github.com/cdc08x/MINERful):
#                   bin/
#                   lib/
#                   src/
#                   libs.cfg
#                   run-MINERful-vacuityCheck.sh (this file)

## Runtime environment constants
MAINCLASS="minerful.MinerFulVacuityChecker"
MEMORY_MAX="16G"

## Exec-specific parametres
THRESHOLD="0.25"
LOG="logs/BPIC2013/bpi_challenge_2013_closed_problems.xes.gz"

## Preliminary checks
if [ ! -f ./libs.cfg ]
then
 echo "Please download the file named libs.cfg from the GitHub repository"
 exit 1
fi

## Import the libraries and store it into $LIBS
. ./libs.cfg

## Clean up the screen
clear


## Run!
java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS $LOG $THRESHOLD

exit 0
