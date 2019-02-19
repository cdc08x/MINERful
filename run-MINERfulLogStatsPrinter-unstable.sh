#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2019/01/22
# Description:  This script launches the MinerFulLogStatsPrinter, in order to show information about a given event log.
#               Run this launcher with "-h" to understand the meaning of options you can pass.
# Installation: Please download the following files and directories from the MINERful GitHub Repository (https://github.com/cdc08x/MINERful):
#                   bin/
#                   lib/
#                   src/
#                   libs.cfg


## Exec-specific parametres
DEBUGLEVEL="none"
MEMORY_MAX="16G"
THRESHOLD=1.0

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

## Runtime environment constants
MAINCLASS="minerful.MinerFulLogStatsPrinter"

## Run!
java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS $* # -d $DEBUGLEVEL -t $THRESHOLD
