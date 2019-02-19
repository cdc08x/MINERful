#!/bin/bash
# Author:       Claudio Di Ciccio
# Version:      0.8
# Date:         2013/09/20
# Description:  This script launches the MinerFulSimuStarter to test what happens when making the alphabet size grow, slowly.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

clear

MAINCLASS="minerful.MinerFulSimuStarter"

DEBUGLEVEL='none'
THRESHOLD=0.0
MAX_THRESHOLD=1.0
#MAX_TEST_REPEATS=10
MAX_TEST_REPEATS=24
MEMORY_MAX="2048m"
MIN_TESTBED_SIZE=1
#MAX_TESTBED_SIZE=2
#MAX_TESTBED_SIZE=1000
ULTRAMAX_TESTBED_SIZE=10000
MAX_TESTBED_SIZE=$ULTRAMAX_TESTBED_SIZE

OUTPUT_DIR="MINERful-accuracyTest"
OUTPUT_FILE_TEMPLATE="%s-accuracyTest.csv"
SUBSUMPTIONS_FILE_TEMPLATE="%s-subsumptions.csv"
FILENAME_TEMPLATE="$OUTPUT_DIR/$OUTPUT_FILE_TEMPLATE"
FILENAME_FOR_SUBSUMPTIONS_TEMPLATE="$OUTPUT_DIR/$SUBSUMPTIONS_FILE_TEMPLATE"
LEGEND="'Test ID';'Test Number';'Generating constraint';'Number of traces';'Minimum trace size';'Maximum trace size';'Avg. events per trace';'Events read';'Process alphabet size';'Discovered Constraint';'Support';'Confidence Level';'Interest Factor'"

alphabetCharacters=("a" "b" "c" "d" "e" "f" "g" "h")
alphabetCondensed=${alphabetCharacters[@]}
alphabet=`echo $alphabetCondensed | sed 's/ /:/g'`
alphabetCondensed=`echo $alphabetCondensed | sed 's/ //g'`

stringlenthrangemin[0]=$(( 0 * ${#alphabetCharacters[*]} ))
stringlenthrangemax[0]=$(( 1 * 2 * ${#alphabetCharacters[*]} ))
stringlenthrangemin[1]=$(( 1 * ${#alphabetCharacters[*]} ))
stringlenthrangemax[1]=$(( 1 * 2 * 4 * ${#alphabetCharacters[*]} ))
stringlenthrangemin[2]=$(( 1 * 2 * ${#alphabetCharacters[*]} ))
stringlenthrangemax[2]=$(( 1 * 2 * 4 * 8 * ${#alphabetCharacters[*]} ))

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
constraintNames=(
 "Participation_a"              #00
 "AtMostOne_a"                  #01
 "Init_a"                       #02
 "End_a"                        #03
 "RespondedExistence_a__b"      #04
 "Response_a__b"                #05
 "AlternateResponse_a__b"       #06
 "ChainResponse_a__b"           #07
 "Precedence_a__b"              #08
 "AlternatePrecedence_a__b"     #09
 "ChainPrecedence_a__b"         #10
 "CoExistence_a__b"             #11
 "Succession_a__b"              #12
 "AlternateSuccession_a__b"     #13
 "ChainSuccession_a__b"         #14
 "NotChainSuccession_a__b"      #15
 "NotSuccession_a__b"           #16
 "NotCoExistence_a__b"          #17
)
constraintSerifNames=(
 "'Participation(a)'"           #00
 "'AtMostOne(a)'"              #01
 "'Init(a)'"                    #02
 "'End(a)'"                     #03
 "'RespondedExistence(a, b)'"   #04
 "'Response(a, b)'"             #05
 "'AlternateResponse(a, b)'"    #06
 "'ChainResponse(a, b)'"        #07
 "'Precedence(a, b)'"           #08
 "'AlternatePrecedence(a, b)'"  #09
 "'ChainPrecedence(a, b)'"      #10
 "'CoExistence(a, b)'"          #11
 "'Succession(a, b)'"           #12
 "'AlternateSuccession(a, b)'"  #13
 "'ChainSuccession(a, b)'"      #14
 "'NotChainSuccession(a, b)'"   #15
 "'NotSuccession(a, b)'"        #16
 "'NotCoExistence(a, b)'"       #17
)

## The game begins!
## Create the subdirectory to store the output files
if [ ! -d $OUTPUT_DIR ]; then
 mkdir $OUTPUT_DIR
fi

## For each constraint
#for (( constraintIdx=0; constraintIdx<${#constraints[*]}; constraintIdx++ ))
# do
### Empirically find and store the graph of subsumptions, referred to the current constraint
#      outputString=`java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS -mR -d $DEBUGLEVEL -t $MAX_THRESHOLD -a $alphabet -m ${stringlenthrangemin[1]} -M ${stringlenthrangemax[2]} -s $ULTRAMAX_TESTBED_SIZE -r ${constraints[constraintIdx]}`
#      echo `echo "$outputString" | grep -n 'Legend' | sed 's/^.*Legend[^:]*: //g'` > `printf "$FILENAME_FOR_SUBSUMPTIONS_TEMPLATE" "${constraintNames[constraintIdx]}"`
#      echo "Successfully stored the net of subsumptions for ${constraintNames[constraintIdx]}"
#done

## Temporary variables
# headerString=`java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS -mR -d none -t 0.0 -a $alphabet -m ${#alphabetCharacters[*]} -M ${#alphabetCharacters[*]} -s 1 -r $alphabetCondensed`
# headerString=`echo "$headerString" | grep -n 'Legend' | sed 's/^.*Legend[^:]*: //g' | sed "s/'//g"`
# headerString=`echo "$LEGEND_INIT$headerString"`

filename=''

tid=0
q=0

## For each constraint
for (( constraintIdx=0; constraintIdx<${#constraints[*]}; constraintIdx++ ))
 do
  filename=`printf "$FILENAME_TEMPLATE" "${constraintNames[constraintIdx]}"`
  echo "$LEGEND" > $filename
## For each combo of minimum string legth...
  for (( min=0; min<${#stringlenthrangemin[*]}; min++ ))
   do
## ... and max length
    for (( max=0; max<${#stringlenthrangemax[*]}; max++ ))
     do
    
## For each possible length of a string, from $MIN_TESTBED_SIZE to $MAX_TESTBED_SIZE
      for (( num=$MIN_TESTBED_SIZE; num<=$MAX_TESTBED_SIZE; num++ ))
       do
        tid=`expr $tid + 1`
        
        for (( i=1; i<=$MAX_TEST_REPEATS; i++ ))
         do
          q=`expr $q + 1`
          
          outputString=`java -Xmx$MEMORY_MAX -jar MINERful.jar $MAINCLASS -CSV -d $DEBUGLEVEL -t $THRESHOLD -a $alphabet -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s $num -r ${constraints[constraintIdx]}`
          
          if [ $? -ne 0 ]
           then
            exit 1
          fi

          setupString=`echo "$outputString" | grep 'printComputationStats' | sed 's/.*- \(\([0-9\.]*;\)\{6\}\).*/\1/g'`
          outputString=`echo "$outputString" | grep "^'" | sed "s/^'/$tid;$q;${constraintSerifNames[constraintIdx]};$setupString'/g"`
          echo "$outputString" >> $filename
        done
        echo "$(($MAX_TESTBED_SIZE - $MIN_TESTBED_SIZE +1)) tests for ${constraintSerifNames[constraintIdx]} on [ ${stringlenthrangemin[min]} - ${stringlenthrangemax[max]} ] long strings done, on a testbed comprising $num strings"
      done
      echo "Tests for ${constraintSerifNames[constraintIdx]} on [ ${stringlenthrangemin[min]} - ${stringlenthrangemax[max]} ] long strings done"
    done
  done
done

echo "$q tests"

exit 0
