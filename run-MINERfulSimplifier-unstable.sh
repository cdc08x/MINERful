#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2016/01/11
# Description:  This script launches the MinerFulSimplifier, in order to simplify a declarative process model, provided as input.
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Import the libraries and store it into $LIBS
. ./libs.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulSimplifier"

DEBUGLEVEL="none"
MEMORY_MAX="16G"
THRESHOLD=0.0

## Run!
java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS $* # -d $DEBUGLEVEL -t $THRESHOLD
