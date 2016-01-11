#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2013/09/20
# Description:  This script launches the MinerFulTracesMakerStarter to create synthetic collections of strings (which can be seen as logs): the (main) input is a set of regular expressions (declarative process model constraints) determining the language (the process model) that generate strings (traces).
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Import the libraries and store it into $LIBS
. ./libs.cfg

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulTracesMakerStarter"

MIN_STRLEN=12
MAX_STRLEN=128
TESTBED_SIZE=250
MEMORY_MAX="2048m"
OUTPUT_FILE="/home/claudio/Dropbox/Research-and-programming-stuff/MailOfMine-SW/Test-Logs/simple-synth-log.xes"

## Global variables
alphabetCharacters=("n" "p" "r" "c" "e" "f" "g" "h")
uniquen=`AtMostOne n`
participationn=`Participation n`
endn=`End n`
successionpn=`Succession p n`
responserp=`Response r p`
respondedexistencecp=`RespondedExistence c p`
alternateprecedencerc=`AlternatePrecedence r c`

testre[1]="$uniquen $participationn $endn"
testre[2]="${testre[ 1 ]} $successionpn"
testre[3]="${testre[ 2 ]} $responserp"
testre[4]="${testre[ 3 ]} $respondedexistencecp"
testre[5]="${testre[ 4 ]} $alternateprecedencerc"

## Auxiliary variable
alphabet=`echo ${alphabetCharacters[@]} | sed 's/ /:/g'`

## Run!
java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS -a $alphabet -m $MIN_STRLEN -M $MAX_STRLEN -s $TESTBED_SIZE -oLF $OUTPUT_FILE -oE "xes" -r ${testre[5]}
