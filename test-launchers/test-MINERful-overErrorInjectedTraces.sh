#!/bin/bash

# Author:       Claudio Di Ciccio
# Version:      0.8
# Date:         2013/09/20
# Description: This script launches the MinerFulErrorInjectedSimuStarter to test what happens to the mined constraints when the testbed is affected by controlled errors. Parameters for the setup are varied so to cover many possible configurations. The output is stored in several files, where values are listed in whitespace-separated rows.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.MinerFulErrorInjectedSimuStarter"
OUTPUT_DIR="../../MailOfMine-Outcome/SynthLogsWithErrors/nu-data"
HEADER_FILE="header.data"

DEBUGLEVEL='none'
THRESHOLD=0.0
MAX_TEST_REPEATS=50
MIN_STRLEN=0
MAX_STRLEN=30
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
constraintNames=(
 "Participation_a"
 "AtMostOne_a"
 "Init_a"
 "End_a"
 "RespondedExistence_a__b"
 "Response_a__b"
 "AlternateResponse_a__b"
 "ChainResponse_a__b"
 "Precedence_a__b"
 "AlternatePrecedence_a__b"
 "ChainPrecedence_a__b"
 "CoExistence_a__b"
 "Succession_a__b"
 "AlternateSuccession_a__b"
 "ChainSuccession_a__b"
 "NotChainSuccession_a__b"
 "NotSuccession_a__b"
 "NotCoExistence_a__b"
)

alphabetCharacters=("a" "b" "c" "d" "e" "f" "g" "h")

#errorSpreadTypeParams=("collection" "string")
#errorSpreadTypeParamsForHeader=("over collection" "over strings")
errorSpreadTypeParams=("string")
errorSpreadTypeParamsForHeader=("over strings")
errorTypeParams=("ins" "del" "insdel")
errorTypeParamsForHeader=("insertion" "deletion" "insertion/deletion (random proportion)")
errorPercentages=("0" "1" "2" "3" "4" "5" "6" "7" "8" "9" "10" "11" "12" "13" "14" "15" "16" "17" "18" "19" "20" "21" "22" "23" "24" "25" "26" "27" "28" "29" "30")
targetChar="a"

HEADER_INFO_TEMPLATE=`cat <<EOS
# START OF HEADING INFORMATION
#
# Date: %s
# Generating constraint: %s
# Error type: %s
# Error injection spreading policy: %s
#
# END OF HEADING INFORMATION\n
EOS`

## Auxiliary variables
testID=54
overallCounter=0
fileName=''
outputString=''
alphabet=${alphabetCharacters[@]}
alphabet=`echo $alphabet | sed 's/ /:/g'`
alphabetCondensed=`echo $alphabet | sed 's/://g'`
headingInfo=''
headerString=''
supportString=''
now=''


## The game begins!
## Create the subdirectory to store the output files
if [ ! -d $OUTPUT_DIR ]; then
 mkdir $OUTPUT_DIR
fi

## For each constraint
for (( constraintIdx=0; constraintIdx<${#constraints[*]}; constraintIdx++ ))
 do
## For each error spreading policy
  for (( errorSpreadTypeIdx=0; errorSpreadTypeIdx<${#errorSpreadTypeParams[*]}; errorSpreadTypeIdx++ ))
   do
## For each error type
    for (( errorTypeIdx=0; errorTypeIdx<${#errorTypeParams[*]}; errorTypeIdx++ ))
     do
## Decide the name of the output file
      testID=`expr $testID + 1`
## Initialise the file
      fileName="$OUTPUT_DIR/MINERful-errorInjectedTest-${testID}--a-$alphabetCondensed--r-${constraintNames[constraintIdx]}--eS-${errorSpreadTypeParams[errorSpreadTypeIdx]}--eT-${errorTypeParams[errorTypeIdx]}.data"
      now=`date`
      headingInfo=`printf "$HEADER_INFO_TEMPLATE" "$now" "${constraintNames[constraintIdx]}" "${errorSpreadTypeParamsForHeader[errorSpreadTypeIdx]}" "${errorTypeParamsForHeader[errorTypeIdx]}"`
      echo "$headingInfo" > $fileName
## For each error percentage in the range set before
      for (( errorPercentageIdx=0; errorPercentageIdx<${#errorPercentages[*]}; errorPercentageIdx++ ))
       do
## For a given number of times
        for (( testNumber=0; testNumber<$MAX_TEST_REPEATS; testNumber++ ))
         do
          overallCounter=`expr $overallCounter + 1`
## Compute the values
          outputString=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -mR -d $DEBUGLEVEL -t $THRESHOLD -a $alphabet -m $MIN_STRLEN -M $MAX_STRLEN -s $TESTBED_SIZE -eS ${errorSpreadTypeParams[errorSpreadTypeIdx]} -eT ${errorTypeParams[errorTypeIdx]} -eP ${errorPercentages[errorPercentageIdx]} -eC $targetChar -r ${constraints[constraintIdx]}`
          if [ -z "$headerString" ]; then
## Store the heading
           headerString=`echo "$outputString" | grep -n 'Legend' | sed 's/^.*Legend[^:]*: //g' | sed "s/'//g" | sed 's/\;/ /g'`
           echo "ErrorPercentage $headerString" > "$OUTPUT_DIR/$HEADER_FILE"
          fi
          supportString=`echo "$outputString" | grep 'Support' | sed 's/^.*Support[^:]*: //g' | sed 's/\;/ /g'`
## Store the values
          echo "${errorPercentages[errorPercentageIdx]} $supportString" >> $fileName
         done
        echo "${constraintNames[constraintIdx]}: all ${errorPercentages[errorPercentageIdx]}% \"${errorTypeParams[errorTypeIdx]}\" over \"${errorSpreadTypeParams[errorSpreadTypeIdx]}\" tests performed"
       done
      echo "${constraintNames[constraintIdx]}: all \"${errorTypeParams[errorTypeIdx]}\" over \"${errorSpreadTypeParams[errorSpreadTypeIdx]}\" tests performed"
     done
    echo "${constraintNames[constraintIdx]}: all \"${errorSpreadTypeParams[errorSpreadTypeIdx]}\" tests performed"
   done
  echo "${constraintNames[constraintIdx]}: all tests performed"
 done

echo "$overallCounter tests performed"
