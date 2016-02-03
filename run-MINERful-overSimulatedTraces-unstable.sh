#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2013/09/20
# Description:  This script launches the MinerFulSimuStarter to discover the process model out of a synthetic collection of strings (which can be seen as a log), created on the fly: the (main) input is a set of regular expressions (declarative process model constraints) determining the language (the process model) that generate strings (traces).
#               Run this launcher with "-h" to understand the meaning of options you can pass.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

## Import the libraries and store it into $LIBS
. ./libs.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulSimuStarter"

THRESHOLD=1.0
MIN_STRLEN=2
MAX_STRLEN=15
TESTBED_SIZE=10000
MEMORY_MAX="16G"

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
 `CoExistence a c`              #11
 `Succession a b`               #12
 `AlternateSuccession a b`      #13
 `ChainSuccession a b`          #14
 `NotChainSuccession a b`       #15
 `NotSuccession a b`            #16
 `NotCoExistence a b`           #17
)

# alphabetCharacters=("a" "b" "c" "d") #"e" "f" "g" "h" "i" "j" "k" "l")
# alphabetCharacters=("A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L")
alphabetCharacters=("n" "p" "r" "c")

## Auxiliary variable
alphabet=`echo ${alphabetCharacters[@]} | sed 's/ /:/g'`

## Run!
java -Xmx$MEMORY_MAX -cp $LIBS $MAINCLASS -a $alphabet -m $MIN_STRLEN -M $MAX_STRLEN -t $THRESHOLD -L $TESTBED_SIZE $* -r `End n` `Response r p` `AlternatePrecedence r c` `Participation n` `AtMostOne n` `Succession p n` `RespondedExistence c p` # '[db]c?ec?[db]a?c?e' # -r ${constraints[15]} ${constraints[5]} # -r "[^bc]*(a.*[bc])*[^bc]*" $* # ${constraints[7]} $* #'[^abc]*(a(b|c)[^abc]*)*[^abc]*' $*
