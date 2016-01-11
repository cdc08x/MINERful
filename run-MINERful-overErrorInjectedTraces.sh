#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2013/09/20
# Description:  This script launches the MinerFulErrorInjectedSimuStarter to test what happens to the mined constraints when the testbed is affected by controlled errors: the (main) input is a set of regular expressions (declarative process model constraints) determining the language (the process model) that generate strings (traces). Parameters for setting up the error injection are requested as well.
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulErrorInjectedSimuStarter"
OUTPUT_DIR="MINERful-errorTests-out"

DEBUGLEVEL='debug'
THRESHOLD=0.0
MIN_STRLEN=0
MAX_STRLEN=20
TESTBED_SIZE=1000
MEMORY_MAX="2048m"

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
errorSpreadTypeParams=("collection" "string")
errorTypeParams=("ins" "del" "insdel")
errorPercentages=("1" "2" "3" "4" "5" "6" "7" "8" "9" "10" "11" "12" "13" "14" "15" "16" "17" "18" "19" "20")
targetChar="c"

## Auxiliary variable
alphabet=`echo ${alphabetCharacters[@]} | sed 's/ /:/g'`

## Run!
java -Xmx$MEMORY_MAX -jar MINERful.jar $MAINCLASS -d $DEBUGLEVEL -t $THRESHOLD -a $alphabet -mR -m $MIN_STRLEN -M $MAX_STRLEN -s $TESTBED_SIZE -eS ${errorSpreadTypeParams[1]} -eT ${errorTypeParams[0]} -eP 1 -eC $targetChar -r ${constraints[7]} $*
