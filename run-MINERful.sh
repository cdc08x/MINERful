#!/bin/bash

# Author:       Claudio Di Ciccio
# Version:      0.8
# Date:         2013/09/20
# Description:  This script launches the MinerFulMinerStarter, in order to discover the declarative process model out of a log, provided as input.
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="it.uniroma1.dis.minerful.MinerFulMinerStarter"

DEBUGLEVEL="none"
MEMORY_MAX="8096m"
THRESHOLD=0.0

## Run!
java -Xmx$MEMORY_MAX -jar MINERful.jar $MAINCLASS $* # -d $DEBUGLEVEL -t $THRESHOLD
