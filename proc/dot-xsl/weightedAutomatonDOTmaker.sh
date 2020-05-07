#!/bin/bash
MINERFUL_FOLDER="/home/claudio/Code/MINERful"
XSLTS_FOLDER="$MINERFUL_FOLDER/proc/dot-xsl"
USAGE_TEMPLATE="Usage: %s: [-C] [-N] [-W] [-i input-XES-file] <automaton-XML-file>\n"

SUPPORT_THRESHOLD="1.0"
CONFIDENCE_THRESHOLD="0.75"
INTEREST_THRESHOLD="0.125"
FILTERED_OUT_TASKS_LIST_FILE=
DEBUG_LEVEL="debug"

# Initialisation of user inputs with default values
doClustering="TRUE"
doNegInserting="TRUE"
doWeighing="TRUE"
automatonXml=
inputXes=

while getopts "CNWi:" opt
do
  case $opt in
  i)  inputXes="$OPTARG" # To run the miner again
      ;;
  C)  doClustering="FALSE" # To cluster multiple transitions into one arc
      ;;
  N)  doNegInserting="FALSE" # To display a group of very many transitions in the “anything but” form. Say, we have 10 activities and 8 transitions go from the same state to the other same state. Then, rather than having 8 labels, we write: “All except” and then the other two.
      ;;
  W)  doWeighing="FALSE" # To make bolder those transitions that are traversed the most by replaying a log on top of them.
      ;;
  ?)  printf "$USAGE_TEMPLATE" $0 >&2
      exit 2;;
  esac
done

shift $(($OPTIND - 1))
if [ "$*" ]
then
  automatonXml="$*"
else
  printf "$USAGE_TEMPLATE" $0 >&2
  exit 2
fi

currentFolder=$PWD
cd $MINERFUL_FOLDER

#if [ ! -d "$automatonXmlFolder" ]
#then
#  mkdir "$automatonXmlFolder"
#fi

################################################################
#### Discover the process model
################################################################
if [ "$inputXes" ]
then
  echo "Doing the mining..."
  echo "..."
  commandstring="./run-MINERful-unstable.sh -s $SUPPORT_THRESHOLD -c $CONFIDENCE_THRESHOLD -i $INTEREST_THRESHOLD -d $DEBUG_LEVEL"
  commandstring="${commandstring} -iLF ${inputXes} -pXWA ${automatonXml}"
  if [ "$FILTERED_OUT_TASKS_LIST_FILE" ]
  then
    commandstring="${commandstring} -xF ${FILTERED_OUT_TASKS_LIST_FILE}"
  fi
  
  eval "${commandstring}"

  echo "Done"
fi

################################################################
#### Create the XML file that collapses multiple transitions into one, with several labels
################################################################
echo "Collapsing multiple transitions into single multi-labeled ones..."
echo "..."

commandstring="xsltproc $XSLTS_FOLDER/transitionCluster.xsl $automatonXml > ${automatonXml%.*}.clustered.xml"

echo "$commandstring"

eval "${commandstring}"

wait $!
echo "Done"

################################################################
#### Replace multi-labels with ones that exclude the activities not considered in a transition, when the multi-labels are excessively large
################################################################
if [ "$doNegInserting" == "TRUE" ]
then
  echo "Replacing multi-labels with ones that exclude the activities not considered in a transition, when the multi-labels are excessively large..."
  echo "..."
  xsltproc \
    "$XSLTS_FOLDER/transitionFurtherCluster.xsl" \
    "${automatonXml%.*}.clustered.xml" \
    > "${automatonXml%.*}.clustered.withnot.xml"
  wait $!
  echo "Done"
fi

################################################################
#### Create DOT format file
################################################################
echo "Turning into DOT format..."

commandstring="xsltproc"

if [ "$doWeighing" == "TRUE" ]
then
  commandstring="${commandstring} --stringparam DO_WEIGH_LINES 'true()' --stringparam DO_APPLY_TRANSPARENCY_FOR_NOT_TRAVERSED 'true()'"
fi

commandstring="${commandstring} $XSLTS_FOLDER/weightedAutomatonDotter.xsl ${automatonXml%.*}"

if [ "$doNegInserting" == "TRUE" ]
then
  commandstring="${commandstring}.clustered.withnot.xml"
else
  commandstring="${commandstring}.clustered.xml"
fi

commandstring="${commandstring} | sed 's/&gt;/>/g' > ${automatonXml%.*}.dot"

eval "${commandstring}"

echo "Done"

################################################################
#### Create DOT format file displaying the non-conforming paths
################################################################
echo "Turning into DOT format for non-conformance display..."
echo "..."
xsltproc \
  "$XSLTS_FOLDER/weightedAutomatonWithNonConformanceDotter.xsl" \
  "${automatonXml%.*}.clustered.withnot.xml" \
  | sed 's/&gt;/>/g'  \
  > "${automatonXml%.*}.non-conf.dot"
echo "Done"

################################################################
#### Finalising...
################################################################
echo "Opening the DOT automaton..."
xdot "${automatonXml%.*}.dot" &
echo "Saving the DOT automaton as PDF..."
dot -Tpdf "${automatonXml%.*}.dot" > "${automatonXml%.*}.pdf"

cd $currentFolder
wait ${!}
