#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2015/10/10
# Description:  This script launches the MinerFulErrorInjectedTracesMakerStarter, to create synthetic collections of strings (which can be seen as a log) and then inject them with controlled serrors: the (main) input is a set of regular expressions (declarative process model constraints) determining the language (the process model) that generate strings (traces). Parameters for setting up the error injection are requested as well.
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulErrorInjectedTracesMakerStarter"

MIN_STRLEN=0
MAX_STRLEN=20
TESTBED_SIZE=10
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
targetChar="a"

## Auxiliary variable
alphabet=`echo ${alphabetCharacters[@]} | sed 's/ /:/g'`

## Run!
echo java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $alphabet -m $MIN_STRLEN -M $MAX_STRLEN -L $TESTBED_SIZE -eS ${errorSpreadTypeParams[0]} -eT ${errorTypeParams[2]} -eP 30 -eC $targetChar -r ${constraints[2]} ${constraints[5]} $*
