#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2018/09/25
# Description:  This script launches the MinerFulLogMakerStarter to create synthetic event logs.
#               Run this launcher with "-h" to understand the meaning of the options you can pass.

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
MAINCLASS="minerful.MinerFulLogMakerStarter"

## Global variables

## Run!
java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS $*
