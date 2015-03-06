#!/bin/bash
# Author:       Claudio Di Ciccio
# Version:      0.8
# Date:         2013/09/20
# Description:  This script launches the MinerFulSimuStarter to test what happens when making the alphabet size grow.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

clear

MAINCLASS="minerful.MinerFulSimuStarter"

DEBUGLEVEL='none'
THRESHOLD=0.0
MIN_ALPH_SIZE=10
MAX_TEST_REPEATS=10
MEMORY_MAX="2048m"
DO_TEST_MEMORY='-sMS' # Make it an empty string if you don't want any mem. test
OUTPUT_DIR="MINERful-memTest-out"
OUTPUT_FILE="MINERful-memTest-out.csv"

alphabet=("a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z" "A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N" "O" "P" "Q" "R" "S" "T" "U" "W" "X" "Y" "Z" "1" "2" "3" "4" "5" "6" "7" "8" "9" "0")
alphset[1]=${alphabet[0]}
alphrunner=1

for (( alph=1; alph<MIN_ALPH_SIZE; alph++ ))
 do
  alphset[1]="${alphset[1]}:${alphabet[alph]}"
 done

for (( alph=MIN_ALPH_SIZE; alph<${#alphabet[*]}; alph++ ))
 do
#  echo ${alphset[alphrunner]}
  alphrunner=`expr $alphrunner + 1`
  alphset[alphrunner]="${alphset[alphrunner-1]}:${alphabet[alph]}"
 done

stringlenthrangemin[1]=0
stringlenthrangemax[1]=10
stringlenthrangemin[2]=1
stringlenthrangemax[2]=15
stringlenthrangemin[3]=2
stringlenthrangemax[3]=25

testbedsize=(1000 2000 3000 4000 5000 6000 7000 8000 9000 10000 11000 12000 13000 14000 15000 16000)

testrex[1]="`AtMostOne a` `Participation a` `End a`"
testrex[2]="${testrex[1]} `Succession b a`"
testrex[3]="${testrex[2]} `Response d b`"
testrex[4]="${testrex[3]} `RespondedExistence c b`"
testrex[5]="${testrex[4]} `RespondedExistence d c`"

## The game begins!
## Create the subdirectory to store the output files
if [ ! -d $OUTPUT_DIR ]; then
 mkdir $OUTPUT_DIR
fi

## Initialize the file with a legend
echo '"Id";"Progressive number";"Number of traces";"Minimum characters per string";"Maximum characters per string";"Average characters per string";"Total characters read";"Alphabet size";"Total time";"Statistics computation time";"Total mining time";"Relation constraints discovery time";"Existence constraints discovery time";"Memory usage peak";' > "$OUTPUT_DIR/$OUTPUT_FILE"

tid=0
q=0

for (( al=1; al<=${#alphset[*]}; al++ ))
 do
  for (( min=1; min<=${#stringlenthrangemin[*]}; min++ ))
   do
    for (( max=1; max<=${#stringlenthrangemax[*]}; max++ ))
     do
      for (( rex=5; rex<=${#testrex[*]}; rex++ ))
       do
        for (( num=0; num<${#testbedsize[*]}; num++ ))
         do
          tid=`expr $tid + 1`
          for (( i=1; i<=$MAX_TEST_REPEATS; i++ ))
           do
#           echo
#           echo "Test # $i $j $k $l $h"
#           echo
            q=`expr $q + 1`
#           echo "$tid;$i"
#	    echo "java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t $THRESHOLD -R -r ${testrex[$rex]}"
#            /usr/bin/time -a -o $STATSFILE -v java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t $THRESHOLD -R -r ${testrex[$rex]}
            outString=`java -Xmx$MEMORY_MAX -jar MINERful.jar $MAINCLASS -a ${alphset[al]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t $THRESHOLD -R $DO_TEST_MEMORY -r ${testrex[$rex]} | sed -e 's/^[^-]* - //g'`
            echo "$tid;$i;$outString" >> "$OUTPUT_DIR/$OUTPUT_FILE"
            if [ $? -ne 0 ]
             then
              exit 1
            fi
           done
         done
       done
     done
   done
  echo "The test number ${al} over ${#alphset[*]} has been completed. Up to now, ${q} runs have been performed."
# ssmtp dc.claudio@gmail.com <<EOF
# To: "MailOfMine Developers"
# From: "MINERful" <dc.claudio@gmail.com>
# Subject: Intermediate result of the mining process: run number ${tid}

# Hi!
# This is an automatic email, sent to you by yourself so to keep you informed :)
# Luckily, the MINERful testing is going on.
# The test number ${al} over ${#alphset[*]} has been completed.
# Up to now, ${q} runs have been performed.
# Cheers!
# --
# MINERful
# EOF
 done

echo "$q tests"
