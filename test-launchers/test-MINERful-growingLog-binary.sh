#!/bin/bash
# Author:       Claudio Di Ciccio
# Version:      0.8
# Date:         2013/09/20
# Description:  This script launches the MinerFulSimuStarter, to test what happens when making the size of the log grow, up to 1M entries.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

clear

MAINCLASS="minerful.MinerFulSimuStarter"

STATSFILE="./out/memtime.xagerate.txt"

DEBUGLEVEL='none'
THRESHOLD=0.75

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

stringlenthrangemin[1]=0
stringlenthrangemax[1]=5
stringlenthrangemin[2]=1
stringlenthrangemax[2]=10
stringlenthrangemin[3]=2
stringlenthrangemax[3]=15
stringlenthrangemin[4]=3
stringlenthrangemax[4]=20
stringlenthrangemin[5]=4
stringlenthrangemax[5]=25

alphabet[1]="n"
alphabet[2]=${alphabet[ 1 ]}":p"
alphabet[3]=${alphabet[ 2 ]}":r"
alphabet[4]=${alphabet[ 3 ]}":c"
alphabet[5]=${alphabet[ 4 ]}":e"

testbedsize=( 10 100 200 300 400 500 600 700 800 900 1000 2500 5000 7500 10000 12500 25000 50000 75000 100000 1000000 )

clear
echo "How to read the summary:"
echo "'testId';'testNumber';'numOfConstraints';'numOfCases';'minChrsPerString';'maxChrsPerString';'avgChrsPerString';'totalChrs';'alphabetLength';'totalMiningTime';'kbTabTime';'constraintsMiningTime';'relaConTime';'exiConTime';"
echo

q=0

for (( alph=2; alph<=${#alphabet[*]}; alph++ ))
 do
  for (( min=1; min<=${#stringlenthrangemin[*]} && min<=$alph; min++ ))
   do
    for (( max=1; max<=${#stringlenthrangemax[*]}; max++ ))
     do
      for (( rex=1; rex<=${#testre[*]} && (rex<=$alph || alph > 3); rex++ ))
       do
        for (( num=1; num<${#testbedsize[*]}; num++ ))
         do
          q=`expr $q + 1`
          for (( i=1; i<=5; i++ ))
           do
#           echo
#           echo "Test # $i $j $k $l $h"
#           echo
            echo "$q;$i"
#	    echo "java -classpath $LIBS $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t $THRESHOLD -R -r ${testre[$rex]}"
#            /usr/bin/time -a -o $STATSFILE -v java -classpath $LIBS $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t $THRESHOLD -R -r ${testre[$rex]}
            java -Xmx$MEMORY_MAX -jar MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t $THRESHOLD -R -r ${testre[$rex]}
           done
         done
       done
     done
   done
 done
