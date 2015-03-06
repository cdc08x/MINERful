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
THRESHOLD=0.66
MAX_THRESHOLD=1.0
#MAX_TEST_REPEATS=10
MAX_TEST_REPEATS=10
MEMORY_MAX="2048m"
MIN_TESTBED_SIZE=1
#MAX_TESTBED_SIZE=2
#MAX_TESTBED_SIZE=1000
ULTRAMAX_TESTBED_SIZE=1000
MAX_TESTBED_SIZE=$ULTRAMAX_TESTBED_SIZE

OUTPUT_DIR="MINERful-accuracyTest-givenProcess"
OUTPUT_FILE_TEMPLATE="%s-accuracyTest.csv"
SUBSUMPTIONS_FILE_TEMPLATE="%s-subsumptions.csv"
FILENAME_TEMPLATE="$OUTPUT_DIR/$OUTPUT_FILE_TEMPLATE"
FILENAME_FOR_SUBSUMPTIONS_TEMPLATE="$OUTPUT_DIR/$SUBSUMPTIONS_FILE_TEMPLATE"
LEGEND="'Test ID';'Test Number';'Generating constraint';'Number of traces';'Minimum trace size';'Maximum trace size';'Avg. events per trace';'Events read';'Process alphabet size';'Discovered Constraint';'Support';'Confidence Level';'Interest Factor'"

alphabetCharacters=("n" "p" "r" "c" "e" "f" "g" "h")
alphabetCondensed=${alphabetCharacters[@]}
alphabet=`echo $alphabetCondensed | sed 's/ /:/g'`
alphabetCondensed=`echo $alphabetCondensed | sed 's/ //g'`

stringlenthrangemin[0]=$(( 0 * ${#alphabetCharacters[*]} ))
stringlenthrangemax[0]=$(( 1 * 2 * ${#alphabetCharacters[*]} ))
stringlenthrangemin[1]=$(( 1 * ${#alphabetCharacters[*]} ))
stringlenthrangemax[1]=$(( 1 * 2 * 4 * ${#alphabetCharacters[*]} ))
stringlenthrangemin[2]=$(( 1 * 2 * ${#alphabetCharacters[*]} ))
stringlenthrangemax[2]=$(( 1 * 2 * 4 * 8 * ${#alphabetCharacters[*]} ))

uniquen=`AtMostOne n`
participationn=`Participation n`
endn=`End n`
successionpn=`Succession p n`
responserp=`Response r p`
respondedexistencecp=`RespondedExistence c p`
alternateprecedencerc=`AlternatePrecedence r c`

constraints[0]="$uniquen $participationn $endn"
constraints[1]="${constraints[ 0 ]} $successionpn"
constraints[2]="${constraints[ 1 ]} $responserp"
constraints[3]="${constraints[ 2 ]} $respondedexistencecp"
constraints[4]="${constraints[ 3 ]} $alternateprecedencerc"

constraintNames=(
 'AtMostOne_n-Participation_n-End_n'
 'AtMostOne_n-Participation_n-End_n-Succession_p__n'
 'AtMostOne_n-Participation_n-End_n-Succession_p__n-Respose_r__p'
 'AtMostOne_n-Participation_n-End_n-Succession_p__n-Respose_r__p-RespondedExistence_c__p'
 'AtMostOne_n-Participation_n-End_n-Succession_p__n-Respose_r__p-RespondedExistence_c__p-AlternatePrecedence_r__c'
)

constraintSerifNames=(
 '{AtMostOne(n), Participation(n), End(n)}'
 '{AtMostOne(n), Participation(n), End(n), Succession(p, n)}'
 '{AtMostOne(n), Participation(n), End(n), Succession(p, n), Respose(r, p)}'
 '{AtMostOne(n), Participation(n), End(n), Succession(p, n), Respose(r, p), RespondedExistence(c, p)}'
 '{AtMostOne(n), Participation(n), End(n), Succession(p, n), Respose(r, p), RespondedExistence(c, p), AlternatePrecedence(r, c)}'
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
