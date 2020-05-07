#!/bin/bash
MINERFUL_FOLDER="/home/claudio/Code/MINERfOld/MINERful"
XSLTS_FOLDER="$MINERFUL_FOLDER/proc/dot-xsl"
USAGE_TEMPLATE="Usage: %s: [-C] [-N] [-i input-XES-file] <XML-automata-folder>\n"

SUPPORT_THRESHOLD="1.0"
CONFIDENCE_THRESHOLD="0.0"
INTEREST_THRESHOLD="0.5"
DEBUG_LEVEL="trace"

# Initialisation of user inputs with default values
doClustering="TRUE"
doNegInserting="TRUE"
subAutomXmlFolder=
inputXes=

while getopts "CNi:" opt
do
  case $opt in
  i)  inputXes="$OPTARG"
      ;;
  C)  doClustering="TRUE"
      ;;
  N)  doNegInserting="TRUE"
      ;;
  ?)  printf "$USAGE_TEMPLATE" $0 >&2
      exit 2
      ;;
  esac
done

shift $(($OPTIND - 1))
if [ "$*" ]
then
  subAutomXmlFolder="$*"
else
  printf "$USAGE_TEMPLATE" $0 >&2
  exit 2
fi

# End of initialisation

currentFolder=$PWD
cd $MINERFUL_FOLDER

#if [ ! -d "$subAutomXmlFolder" ]
#then
#  mkdir "$subAutomXmlFolder"
#fi

if [ "$inputXes" ]
then
  echo "Doing the mining..."
  echo "..."
  ./run-MINERful-unstable.sh -t $SUPPORT_THRESHOLD -c $CONFIDENCE_THRESHOLD -i $INTEREST_THRESHOLD -d $DEBUG_LEVEL -R -iLF \
    "$inputXes" \
    -pXWSAF \
    "$subAutomXmlFolder"
  sleep 1
  echo "Done"
fi

if [ "$doClustering" = "TRUE" ]
then
  echo "Collapsing multiple transitions into single multi-labeled ones..."
  echo "..."
  for xmlSubAutofile in "$subAutomXmlFolder"/*.automaton.xml
  do
    xsltproc \
      "$XSLTS_FOLDER/transitionCluster.xsl" \
      "$xmlSubAutofile" \
      > "${xmlSubAutofile%.*}.clustered.xml" 
  done
  sleep 1
  echo "Done"
fi

if [ "$doNegInserting" = "TRUE" ]
then
  echo "Turning transitions with too many labels into transitions negating the excluded ones..."
  echo "..."
  for xmlSubAutofile in "$subAutomXmlFolder"/*.clustered.xml
  do
    xsltproc \
     "$XSLTS_FOLDER/transitionFurtherCluster.xsl" \
      "$xmlSubAutofile" \
      > "${xmlSubAutofile%.*}.clustered.withnot.xml"
  done
  sleep 1
  echo "Done"
fi

echo "Turning into DOT format..."
echo "..."
for xmlSubAutofile in "$subAutomXmlFolder"/*.clustered.withnot.xml
do
  xsltproc \
    "$XSLTS_FOLDER/weightedAutomatonDotter.xsl" \
    "$xmlSubAutofile" \
    | sed 's/&gt;/>/g'  \
    > "${xmlSubAutofile%.*}.dot"
# xdot "${subAutomXmlFolder%.*}.dot" &
done
echo "Done"

#echo "Opening DOT automata..."
#for dotSubAutofile in "$subAutomXmlFolder"/*.dot
#do
#  xdot "$dotSubAutofile"
#  read
#done
#echo "Done"

cd $currentFolder
wait ${!}
