#!/bin/bash
# Author:       Claudio Di Ciccio
# Version:      1.1
# Date:         2014/03/14
# Description:  This script launches the MinerFulSimuStarter, to test what happens when making the parameters change, during the discovery of target-branched Declare constraints.

## Import the shell functions to create Regular Expressions expressing constraints
. ./constraintsFunctions.cfg

clear

MAINCLASS="minerful.MinerFulSimuStarter"

STATSFILE="./out/memtime.xagerate.txt"

MEMORY_MAX="12G"

DEBUGLEVEL='none'
REPETITIONS_PER_COMBO=10

min_branch=1
max_branch=5
default_branching_factor=3

# Chain Precedence({A,B}, C)
testre[1]="[^C]*([AB][C][^C]*)*[^C]*"
# Alternate Response (A, {B,C})
testre[2]="${testre[ 1 ]} [^A]*([A][^A]*[BC][^A]*)*[^A]*"
# Responded Existence(A, {B,C,D,E})
testre[3]="${testre[ 2 ]} [^A]*(([A].*[BCDE].*)|([BCDE].*[A].*))*[^A]*"
# Response(A, {B,C})
testre[4]="${testre[ 3 ]} [^A]*([A].*[BC])*[^A]*"
# Precedence({A,B,C,D}, E)
testre[5]="${testre[ 4 ]} [^E]*([ABCD].*[E])*[^E]*"

min_rex=5
default_regexps=${testre[ 5 ]}

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

default_tracelength_min=${stringlenthrangemin[ 5 ]}
default_tracelength_max=${stringlenthrangemax[ 5 ]}

alphabet[1]="A"
alphabet[2]=${alphabet[ 1 ]}":B"
alphabet[3]=${alphabet[ 2 ]}":C"
alphabet[4]=${alphabet[ 3 ]}":D"
alphabet[5]=${alphabet[ 4 ]}":E"
alphabet[6]=${alphabet[ 5 ]}":F"
alphabet[7]=${alphabet[ 6 ]}":G"
alphabet[8]=${alphabet[ 7 ]}":H"
alphabet[9]=${alphabet[ 8 ]}":I"
alphabet[10]=${alphabet[ 9 ]}":J"
alphabet[11]=${alphabet[ 10 ]}":K"
alphabet[12]=${alphabet[ 11 ]}":L"
alphabet[13]=${alphabet[ 12 ]}":M"
alphabet[14]=${alphabet[ 13 ]}":N"
alphabet[15]=${alphabet[ 14 ]}":O"
alphabet[16]=${alphabet[ 15 ]}":P"
alphabet[17]=${alphabet[ 16 ]}":Q"
alphabet[18]=${alphabet[ 17 ]}":R"
alphabet[19]=${alphabet[ 18 ]}":S"
alphabet[20]=${alphabet[ 19 ]}":T"
alphabet[21]=${alphabet[ 20 ]}":U"
alphabet[22]=${alphabet[ 21 ]}":V"
alphabet[23]=${alphabet[ 22 ]}":W"
alphabet[24]=${alphabet[ 23 ]}":X"
alphabet[25]=${alphabet[ 24 ]}":Y"
alphabet[26]=${alphabet[ 25 ]}":Z"
alphabet[27]=${alphabet[ 26 ]}":1"
alphabet[28]=${alphabet[ 27 ]}":2"
alphabet[29]=${alphabet[ 28 ]}":3"
alphabet[30]=${alphabet[ 29 ]}":4"


min_alph=5
max_alph=14
default_alphabet=${alphabet[ 8 ]}

supportThreshold=("0.85" "0.90" "0.95" "1.00")
default_support_threshold=${supportThreshold[ 3 ]}

testbedsize=(500 1000 2500 5000 7500 10000 15000 20000 25000 50000 75000 100000)
default_testbed_size=${testbedsize[ 5 ]}

clear
echo "# How to read the summary:"
echo "#### 'Test ID';'Test number';'Test repetition';'Independent parameter'\
'Branching factor';'Support threshold';'Number of traces';'Minimum characters per string';'Maximum characters per string';'Average characters per string';'Total characters read';'Alphabet size';\
'Total time';'Statistics computation time';'Total mining time';'Relation constraints discovery time';'Existence constraints discovery time';'Maximum memory usage';\
'Total number of discoverable constraints';'Total number of discoverable existence constraints';'Total number of discoverable relation constraints';\
'Total number of discovered constraints above thresholds';'Total number of discovered existence constraints above thresholds';'Total number of discovered relation constraints above thresholds';\
'Constraints before hierarchy-based pruning';'Existence constraints before hierarchy-based pruning';'Relation constraints before hierarchy-based pruning';\
'Constraints before threshold-based pruning';'Existence constraints before threshold-based pruning';'Relation onstraints before threshold-based pruning';\
'Constraints after pruning';'Existence constraints after pruning';'Relation constraints after pruning'"
echo "#"
echo "#"
echo "#"
echo "'testId';'testNum';'testRep';'param';\
'branchingFactor';'threshold';'numOfCases';'minChrsPerString';'maxChrsPerString';'avgChrsPerString';'totalChrs';'alphabetLength';\
'totalMiningTime';'kbTabTime';'constraintsMiningTime';'relaConTime';'exiConTime';'maxMemUsage';\
'discoverableCns';'discoverableExiCns';'discoverableRelCns';\
'aboveThresholdCns';'aboveThresholdExiCns';'aboveThresholdRelCns';\
'afterSetContainmentPruningCns';'afterSetContainmentPruningExiCns';'afterSetContainmentPruningRelCns';\
'afterHierarchyPruningCns';'afterHierarchyPruningExiCns';'afterHierarchyPruningRelCns';\
'afterThresholdPruningCns';'afterThresholdExiCns';'afterThresholdRelCns';\
"
echo "#"
echo "#"
echo "#"
# 'Number of traces';'Minimum characters per string';'Maximum characters per string';'Average characters per string';'Total characters read';'Alphabet size';'Total time';'Statistics computation time';'Total mining time';'Relation constraints discovery time';'Existence constraints discovery time';'Maximum memory usage';'Total number of discoverable constraints';'Total number of discoverable existence constraints';'Total number of discoverable relation constraints';'Total number of discovered constraints above thresholds';'Total number of discovered existence constraints above thresholds';'Total number of discovered relation constraints above thresholds';'Constraints before hierarchy-based pruning';'Existence constraints before hierarchy-based pruning';'Relation constraints before hierarchy-based pruning';'Constraints before threshold-based pruning';'Existence constraints before threshold-based pruning';'Relation onstraints before threshold-based pruning';'Constraints after pruning';'Existence constraints after pruning';'Relation constraints after pruning'
echo "# Defaults:"
echo "#### alphabet: $default_alphabet"
echo "#### trace: $default_tracelength_min -- $default_tracelength_max"
echo "#### log: $default_testbed_size"
echo "#### branch: $default_branching_factor"
echo "#### threshold: $default_support_threshold"
echo "#"
echo "#"

q=1
n=0
j=1
p="'alphabet'"

echo "#"
echo "# Test the impact of the alphabet size"
echo "# ${alphabet[*]}"
echo "# From ${alphabet[min_alph]}"
echo "# To ${alphabet[max_alph]}"
echo "#"
for (( alph=$min_alph; alph<=$max_alph; alph++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#    echo "$q;$i;$j;$p;$default_branching_factor;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done


q=`expr $q + 1`
p="'trace'"

echo "#"
echo '# Test the impact of the trace size'
echo "# Min: ${stringlenthrangemin[*]}"
echo "# Max: ${stringlenthrangemax[*]}"
echo "#"
for (( min=1; min<=${#stringlenthrangemin[*]} && min<=$alph; min++ ))
 do
  for (( max=1; max<=${#stringlenthrangemax[*]}; max++ ))
   do
    for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
     do
      n=`expr $n + 1`
#      csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $default_alphabet -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s $default_testbed_size -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#     echo "$q;$i;$j;$p;$default_branching_factor;$default_support_threshold;$csvout"
    done
    j=`expr $j + 1`
  done
done


q=`expr $q + 1`
p="'log'"

echo "#"
echo '# Test the impact of the log size'
echo "# ${testbedsize[*]}"
echo "#"
for (( num=0; num<${#testbedsize[*]}; num++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $default_alphabet -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s ${testbedsize[$num]} -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#   echo "$q;$i;$j;$p;$default_branching_factor;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done


q=`expr $q + 1`
p="'branch'"

echo "#"
echo '# Test the impact of the branching factor'
echo "# From $min_branch to $max_branch"
echo "#"
for (( b=$min_branch; b<=$max_branch; b++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $default_alphabet -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $b -R -r $default_regexps | grep ';0;'`
#   echo "$q;$i;$j;$p;$b;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done


q=`expr $q + 1`
p="'threshold'"

echo "#"
echo "# Test the impact of the support threshold"
echo "# ${supportThreshold[*]}"
echo "#"
for (( sThre=0; sThre<${#supportThreshold[*]}; sThre++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $default_alphabet -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t ${supportThreshold[sThre]} -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#   echo "$q;$i;$j;$p;$b;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done

q=`expr $q + 1`
p="'alphabet'"

new_min_alph=`expr $max_alph + 1`
new_max_alph=`expr $max_alph + 2`
echo "#"
echo "# Test the impact of the alphabet size: extended"
echo "# ${alphabet[*]}"
echo "# From ${alphabet[new_min_alph]}"
echo "#"
for (( alph=$new_min_alph; alph<=$new_max_alph; alph++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#    echo "$q;$i;$j;$p;$default_branching_factor;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done


q=`expr $q + 1`
p="'branch'"

nu_min_branch=`expr $max_branch + 3`
nu_max_branch=`expr $max_branch + 3`
echo "#"
echo '# Test the impact of higher branching factors'
echo "# From $nu_min_branch to $nu_max_branch"
echo "#"
for (( b=$nu_min_branch; b<=$nu_max_branch; b++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $default_alphabet -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $b -R -r $default_regexps | grep ';0;'`
#   echo "$q;$i;$j;$p;$b;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done



q=`expr $q + 1`
p="'branch'"

nu_min_branch=`expr $max_branch + 3`
nu_max_branch=`expr $max_branch + 3`
echo "#"
echo '# Test the impact of higher branching factors'
echo "# From $nu_min_branch to $nu_max_branch"
echo "#"
for (( b=$nu_min_branch; b<=$nu_max_branch; b++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $default_alphabet -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $b -R -r $default_regexps | grep ';0;'`
#   echo "$q;$i;$j;$p;$b;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done


q=`expr $q + 1`
p="'alphabet'"

new_min_alph=`expr $new_max_alph + 1`
new_max_alph=23
echo "#"
echo "# Test the impact of the alphabet size: extended"
echo "# ${alphabet[*]}"
echo "# From ${alphabet[new_min_alph]}"
echo "#"
for (( alph=$new_min_alph; alph<=$new_max_alph; alph++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#    echo "$q;$i;$j;$p;$default_branching_factor;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done


q=`expr $q + 1`
p="'alphabet'"

new_min_alph=15
new_max_alph=16
echo "#"
echo "# Test the impact of the alphabet size: extended"
echo "# ${alphabet[*]}"
echo "# From ${alphabet[new_min_alph]}"
echo "#"
for (( alph=$new_min_alph; alph<=$new_max_alph; alph++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#   csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#   echo "$q;$i;$j;$p;$default_branching_factor;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done


q=`expr $q + 1`
p="'alphabet'"

new_min_alph=24
new_max_alph=${#alphabet[*]}
echo "#"
echo "# Test the impact of the alphabet size: extended"
echo "# ${alphabet[*]}"
echo "# From ${alphabet[new_min_alph]}"
echo "#"
for (( alph=$new_min_alph; alph<=$new_max_alph; alph++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#   csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#   echo "$q;$i;$j;$p;$default_branching_factor;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done

q=`expr $q + 1`
p="'alphabet'"

new_min_alph=30
new_max_alph=${#alphabet[*]}
echo "#"
echo "# Test the impact of the alphabet size: extended"
echo "# ${alphabet[*]}"
echo "# From ${alphabet[new_min_alph]}"
echo "#"
for (( alph=$new_min_alph; alph<=$new_max_alph; alph++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
#    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#    echo "$q;$i;$j;$p;$default_branching_factor;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done



echo "#"
echo "# Test the impact of the alphabet size, altogether"
echo "# ${alphabet[*]}"
echo "# From ${alphabet[min_alph]}"
echo "#"
for (( alph=$min_alph; alph<=${#alphabet[*]}; alph++ ))
 do
  for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
   do
    n=`expr $n + 1`
    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
    echo "$q;$i;$j;$p;$default_branching_factor;$default_support_threshold;$csvout"
  done
  j=`expr $j + 1`
done



echo "# $n tests performed"
echo "# Cheers from MINERful!"

# Naïve version follows: test every combination. Too much!
exit
# Naïve version follows: test every combination. Too much!

for (( alph=$min_alph; alph<=${#alphabet[*]}; alph++ ))
 do
  for (( min=1; min<=${#stringlenthrangemin[*]} && min<=$alph; min++ ))
   do
    for (( max=1; max<=${#stringlenthrangemax[*]}; max++ ))
     do
      for (( rex=$min_rex; rex<=${#testre[*]}; rex++ ))
       do
        for (( num=0; num<${#testbedsize[*]}; num++ ))
         do
          for (( b=$min_branch; b<=$max_branch; b++ ))
           do
            for (( sThre=0; sThre<${#supportThreshold[*]}; sThre++ ))
             do
              q=`expr $q + 1`
              for (( i=1; i<=$REPETITIONS_PER_COMBO; i++ ))
               do
#               echo
#               echo "Test # $i $j $k $l $h"
#               echo
#               echo "$q;$i;"
#	            echo "java -classpath $LIBS $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t $THRESHOLD -R -r ${testre[$rex]}"
#               /usr/bin/time -a -o $STATSFILE -v java -classpath $LIBS $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t $THRESHOLD -R -r ${testre[$rex]}
                csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t ${supportThreshold[sThre]} -b $b -R -r ${testre[$rex]} | grep ';0;'`
                echo "$q;$i;$b;${supportThreshold[sThre]};$csvout"
              done
            done
           done
         done
       done
     done
   done
 done

#   csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a $default_alphabet -d $DEBUGLEVEL -m $default_tracelength_min -M $default_tracelength_max -s $default_testbed_size -t $default_support_threshold -b $default_branching_factor -R -r $default_regexps | grep ';0;'`
#   csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -a ${alphabet[alph]} -d $DEBUGLEVEL -m ${stringlenthrangemin[min]} -M ${stringlenthrangemax[max]} -s ${testbedsize[$num]} -t ${supportThreshold[sThre]} -b $b -R -r ${testre[$rex]} | grep ';0;'`
