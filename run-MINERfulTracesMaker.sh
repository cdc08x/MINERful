#!/bin/bash

# Author:       Claudio Di Ciccio
# Version:      0.8
# Date:         2013/09/20
# Description:  This script launches the MinerFulTracesMakerStarter to create synthetic collections of strings (which can be seen as logs): the (main) input is a set of regular expressions (declarative process model constraints) determining the language (the process model) that generate strings (traces).
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulTracesMakerStarter"

MIN_STRLEN=2
MAX_STRLEN=10
TESTBED_SIZE=10000
MEMORY_MAX="2048m"
OUTPUT_FILE="/home/claudio/Desktop/WhereTheDonkeyFalls-example.xes"

## Global variables
constraints=(
 `Participation a`
 `AtMostOne a`
 `Init a`
 `End a`
 `RespondedExistence a b`
 `Response a b`
 `AlternateResponse a b`
 `ChainResponse a b`
 `Precedence a b`
 `AlternatePrecedence a b`
 `ChainPrecedence a b`
 `CoExistence a b`
 `Succession a b`
 `AlternateSuccession a b`
 `ChainSuccession a b`
 `NotChainSuccession a b`
 `NotSuccession a b`
 `NotCoExistence a b`
)

alphabetCharacters=("a" "b" "c" "d")

## Auxiliary variable
alphabet=`echo ${alphabetCharacters[@]} | sed 's/ /:/g'`

## Run!
# java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $alphabet -m $MIN_STRLEN -M $MAX_STRLEN -s $TESTBED_SIZE -r ${constraints[2]} $*
. ./libs.cfg
java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS -a $alphabet -m $MIN_STRLEN -M $MAX_STRLEN -s $TESTBED_SIZE -oLF $OUTPUT_FILE -oE "xes" -r `End d` `Response a b` `AlternatePrecedence c d`
