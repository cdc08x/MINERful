#!/bin/bash
# Author:       Claudio Di Ciccio
# Version:      1.1
# Date:         2014/03/14
# Description:  This script launches the MinerFulSimuStarter, to test what happens when making the parameters change, during the discovery of target-branched Declare constraints.

MAINCLASS="minerful.MinerFulMinerStarter"
BPIC_FILE_PATH="financial_log.xes.gz"

MEMORY_MAX="12G"

DEBUGLEVEL='none'

confidences=(0.85 0.90 0.95 1.00)
supports=(0.85 0.90 0.95 1.00)
default_branching_factor=5

echo "# How to read the summary:"
echo "#### 'Test ID';'Test number';\
'Branching factor';'Support threshold';'Confidence threshold';\
'Number of traces';'Minimum characters per string';'Maximum characters per string';'Average characters per string';'Total characters read';'Alphabet size';\
'Total time';'Statistics computation time';'Total mining time';'Relation constraints discovery time';'Existence constraints discovery time';'Maximum memory usage';\
'Total number of discoverable constraints';'Total number of discoverable existence constraints';'Total number of discoverable relation constraints';\
'Total number of discovered constraints above thresholds';'Total number of discovered existence constraints above thresholds';'Total number of discovered relation constraints above thresholds';\
'Constraints before hierarchy-based pruning';'Existence constraints before hierarchy-based pruning';'Relation constraints before hierarchy-based pruning';\
'Constraints before threshold-based pruning';'Existence constraints before threshold-based pruning';'Relation onstraints before threshold-based pruning';\
'Constraints after pruning';'Existence constraints after pruning';'Relation constraints after pruning'"
echo "#"
echo "#"
echo "#"
echo "'testId';'testNum';\
'branchingFactor';'support';'confidence'\
'numOfCases';'minChrsPerString';'maxChrsPerString';'avgChrsPerString';'totalChrs';'alphabetLength';\
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
echo "#### branch: $default_branching_factor"
echo "#"
echo "#"

q=1
n=0
j=1

echo "#"
echo "# Test the impact of support and confidence thresholds"
echo "# Support thresholds: ${supports[*]}"
echo "# Confidence thresholds: ${confidences[*]} starting with ${confidences[1]}"
echo "#"
for (( s=0; s<${#supports[*]}; s++ ))
 do
  for (( c=1; c<${#confidences[*]}; c++ ))
   do
    n=`expr $n + 1`
   echo java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -iLF $BPIC_FILE_PATH -d $DEBUGLEVEL -t ${supports[s]} -c ${confidences[c]} -b $default_branching_factor -R
exit
#   csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -iLF $BPIC_FILE_PATH -d $DEBUGLEVEL -t ${supports[s]} -c ${confidences[c]} -b $default_branching_factor -R | grep ';0;'`
#   echo "$q;$n;$default_branching_factor;${supports[s]};${confidences[c]};$csvout"
  done
  j=`expr $j + 1`
done

echo "#"
echo "# Test the impact of support and confidence thresholds. Recovering apparent problems with arithmetics of the developer."
echo "# Support thresholds: ${supports[*]}"
echo "# Confidence thresholds: ${confidences[0]}"
echo "#"
for (( s=0; s<${#supports[*]}; s++ ))
 do
  for (( c=0; c<1; c++ ))
   do
    n=`expr $n + 1`
#   echo java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -iLF $BPIC_FILE_PATH -d $DEBUGLEVEL -t ${supports[s]} -c ${confidences[c]} -b $default_branching_factor -R
    csvout=`java -Xmx$MEMORY_MAX -cp MINERful.jar $MAINCLASS -iLF $BPIC_FILE_PATH -d $DEBUGLEVEL -t ${supports[s]} -c ${confidences[c]} -b $default_branching_factor -R | grep ';0;'`
    echo "$q;$n;$default_branching_factor;${supports[s]};${confidences[c]};$csvout"
  done
  j=`expr $j + 1`
done


echo "# $n tests performed"
echo "# Cheers from MINERful!"
