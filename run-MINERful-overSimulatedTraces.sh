#!/bin/bash

# Author:       Claudio Di Ciccio
# Version:      0.8
# Date:         2013/09/20
# Description:  This script launches the MinerFulSimuStarter to discover the process model out of a synthetic collection of strings (which can be seen as a log), created on the fly: the (main) input is a set of regular expressions (declarative process model constraints) determining the language (the process model) that generate strings (traces).
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulSimuStarter"
OUTPUT_DIR="out"

THRESHOLD=0.0
MIN_STRLEN=2
MAX_STRLEN=20
TESTBED_SIZE=10000
MEMORY_MAX="2048m"

## Global variables
constraints=(
 `Participation a`              #00
 `AtMostOne a`                  #01
 `Init a`                       #02
 `End a`                        #03
 `RespondedExistence a b`       #04
 `Response a b`                 #05
 `AlternateResponse a b`        #06
 `ChainResponse a b`            #07
 `Precedence a b`               #08
 `AlternatePrecedence a b`      #09
 `ChainPrecedence a b`          #10
 `CoExistence a b`              #11
 `Succession a b`               #12
 `AlternateSuccession a b`      #13
 `ChainSuccession a b`          #14
 `NotChainSuccession a b`       #15
 `NotSuccession a b`            #16
 `NotCoExistence a b`           #17
)

alphabetCharacters=("a" "b" "c" "d")

## Auxiliary variable
alphabet=`echo ${alphabetCharacters[@]} | sed 's/ /:/g'`

## Run!
java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $alphabet -m $MIN_STRLEN -M $MAX_STRLEN -s $TESTBED_SIZE $* -r ${constraints[15]} ${constraints[05]} ${constraints[02]} # -r "[^bc]*(a.*[bc])*[^bc]*" $* # ${constraints[7]} $* #'[^abc]*(a(b|c)[^abc]*)*[^abc]*' $*
