#!/bin/bash

#PARALLEL_THREADS=2
DO_MINING=1
DO_POST_PROCESSING=1
DO_THRESHOLDS_CUT=1
TIMEOUT=`expr 60 \* 30` # Maximum computation time allowed

TEST_LAUNCH_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"
LOGS_DIR="$BASE_DIR/logs"
MINED_MODELS_DIR="$BASE_DIR/models/mined"
MINED_MODELS_MINIMISED_DIR="$BASE_DIR/models/mined/min"
EXEC_LOGS_DIR="$BASE_DIR/exec-logs"

MINING_CMD="./run-MINERful-unstable.sh"
PRUNING_CMD="./run-MINERfulSimplifier-unstable.sh"

SUPPORT_THRESHOLD="0.75"
CONFIDENCE_THRESHOLD="0.125"
INTEREST_FACTOR_THRESHOLD="0.0625"

PPAT_OPTIONS=(
  'hierarchyconflictredundancy'        #0
  'hierarchyconflictredundancydouble'  #1
)

PPPP_OPTIONS=(
'activationtargetbonds:familyhierarchy:supportconfidenceinterestfactor'  #0
'activationtargetbonds:supportconfidenceinterestfactor:familyhierarchy'  #1
'familyhierarchy:activationtargetbonds:supportconfidenceinterestfactor'  #2
'familyhierarchy:supportconfidenceinterestfactor:activationtargetbonds'  #3
'supportconfidenceinterestfactor:activationtargetbonds:familyhierarchy'  #4
'supportconfidenceinterestfactor:familyhierarchy:activationtargetbonds'  #5
'supportconfidenceinterestfactor:familyhierarchy'                        #6
'supportconfidenceinterestfactor:activationtargetbonds'                  #7
'familyhierarchy:supportconfidenceinterestfactor'                        #8
'familyhierarchy:activationtargetbonds'                                  #9
'activationtargetbonds:supportconfidenceinterestfactor'                  #10
'activationtargetbonds:familyhierarchy'                                  #11
'supportconfidenceinterestfactor'                                        #12
'familyhierarchy'                                                        #13
'activationtargetbonds'                                                  #14
'random'                                                                 #15
)

LOGS=(
  "${LOGS_DIR}/BPIC2012/financial_log.xes.gz"                                   #0
  "${LOGS_DIR}/Road-traffic-fines/Road_Traffic_Fine_Management_Process.xes.gz"  #1
  "${LOGS_DIR}/Synthetic/EOMAS2015/alpha_16_minlen_24_maxlen_24_size_800.xes"   #2
  "${LOGS_DIR}/BPIC2013/bpi_challenge_2013_incidents.xes.gz"                    #3
  "${LOGS_DIR}/BPIC2013/bpi_challenge_2013_open_problems.xes.gz"                #4
  "${LOGS_DIR}/BPIC2013/bpi_challenge_2013_closed_problems.xes.gz"              #5
  "${LOGS_DIR}/BPIC2014/DetailIncidentActivity.xes.gz"                          #6
  "${LOGS_DIR}/BPIC2015/BPIC15_1.xes"                                           #7
  "${LOGS_DIR}/BPIC2015/BPIC15_2.xes"                                           #8
  "${LOGS_DIR}/BPIC2015/BPIC15_3.xes"                                           #9
  "${LOGS_DIR}/BPIC2015/BPIC15_4.xes"                                           #10
  "${LOGS_DIR}/BPIC2015/BPIC15_5.xes"                                           #11
)
MINED_PROCESSES=()

for (( logscnt=0; logscnt<${#LOGS[@]}; logscnt++ ))
do
  MINED_PROCESSES[$logscnt]=`basename ${LOGS[$logscnt]}`
  MINED_PROCESSES[$logscnt]="${MINED_PROCESSES[$logscnt]%%.*}-model-s075.xml"
#  echo "${MINED_PROCESSES[$logscnt]}"
done

MINED_PROCESSES_CONDEC=()
for (( logscnt=0; logscnt<${#LOGS[@]}; logscnt++ ))
do
  MINED_PROCESSES_CONDEC[$logscnt]="${MINED_PROCESSES[$logscnt]%%.*}-model-s075_CONDEC.xml"
#  echo "${MINED_PROCESSES[$logscnt]}"
done

MINING_OPTIONS=(
#  "-iLF ${LOGS[0]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[0]}  -condec $MINED_MODELS_DIR/${MINED_PROCESSES_CONDEC[0]}  -s $SUPPORT_THRESHOLD -c $CONFIDENCE_THRESHOLD -i $INTEREST_FACTOR_THRESHOLD"                #0
  "-iLF ${LOGS[0]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[0]}  -s $SUPPORT_THRESHOLD"                #0
  "-iLF ${LOGS[1]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[1]}  -s $SUPPORT_THRESHOLD -eC 'logspec'"  #1
  "-iLF ${LOGS[2]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[2]}  -s $SUPPORT_THRESHOLD"                #2
  "-iLF ${LOGS[3]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[3]}  -s $SUPPORT_THRESHOLD -eC 'logspec'"  #3
  "-iLF ${LOGS[4]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[4]}  -s $SUPPORT_THRESHOLD -eC 'logspec'"  #4
  "-iLF ${LOGS[5]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[5]}  -s $SUPPORT_THRESHOLD -eC 'logspec'"  #5
  "-iLF ${LOGS[6]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[6]}  -s $SUPPORT_THRESHOLD -eC 'logspec'"  #6
  "-iLF ${LOGS[7]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[7]}  -s $SUPPORT_THRESHOLD"                #7
  "-iLF ${LOGS[8]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[8]}  -s $SUPPORT_THRESHOLD"                #8
  "-iLF ${LOGS[8]}  -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[9]}  -s $SUPPORT_THRESHOLD"                #9
  "-iLF ${LOGS[10]} -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[10]} -s $SUPPORT_THRESHOLD"                #10
  "-iLF ${LOGS[11]} -oMF $MINED_MODELS_DIR/${MINED_PROCESSES[11]} -s $SUPPORT_THRESHOLD"                #11
)

PRUNED_PROCESSES_PATTERNS=()
for (( logscnt=0; logscnt<${#LOGS[@]}; logscnt++ ))
do
  PRUNED_PROCESSES_PATTERNS[$logscnt]=`basename ${LOGS[$logscnt]}`
  PRUNED_PROCESSES_PATTERNS[$logscnt]="${PRUNED_PROCESSES_PATTERNS[$logscnt]%%.*}-model-min-s%s-c%s-i%s_ppAT-%02d_ppPP-%02d.xml"
#echo "${PRUNED_PROCESSES_PATTERNS[$logscnt]}"
done

PRUNED_PROCESSES_PATTERNS_CONDEC=()
for (( logscnt=0; logscnt<${#LOGS[@]}; logscnt++ ))
do
  PRUNED_PROCESSES_PATTERNS_CONDEC[$logscnt]="${PRUNED_PROCESSES_PATTERNS[$logscnt]%%.*}_CONDEC.xml"
#echo "${PRUNED_PROCESSES_PATTERNS_CONDEC[$logscnt]}"
done

OUTPUT_EXEC_LOGS_PATTERNS=()
for (( logscnt=0; logscnt<${#LOGS[@]}; logscnt++ ))
do
  OUTPUT_EXEC_LOGS_PATTERNS[$logscnt]="${PRUNED_PROCESSES_PATTERNS[$logscnt]%%.*}.log"
#echo "${PRUNED_PROCESSES_PATTERNS[$logscnt]}"
done

OUTPUT_CSV_PATTERNS=()
for (( logscnt=0; logscnt<${#LOGS[@]}; logscnt++ ))
do
  OUTPUT_CSV_PATTERNS[$logscnt]="${PRUNED_PROCESSES_PATTERNS[$logscnt]%%.*}.csv"
#echo "${PRUNED_PROCESSES_PATTERNS[$logscnt]}"
done

cd "$BASE_DIR"
i=0

# First part: mine models
if [ "$DO_MINING" = 1 ] 
then
  for (( logscnt=0; logscnt<${#MINING_OPTIONS[@]}; logscnt++ ))
  do
    CMD="$MINING_CMD ${MINING_OPTIONS[$logscnt]}"
    echo "Executing ${CMD}..."
    eval "$CMD"
    i=`expr $i + 1`
  done
fi

echo "$i performed mining tasks"

i=0

if [ "$DO_POST_PROCESSING" = 1 ]
then
# Second part: post-process models
#for (( logscnt=0; logscnt<=1; logscnt++ ))
  for (( logscnt=0; logscnt<${#MINED_PROCESSES[@]}; logscnt++ ))
  do
#    for (( ppatcnt=0; ppatcnt<${#PPAT_OPTIONS[@]}; ppatcnt++ ))
    for (( ppatcnt=0; ppatcnt<${#PPAT_OPTIONS[@]}; ppatcnt++ ))
    do
#      for (( ppppcnt=0; ppppcnt<${#PPPP_OPTIONS[@]}; ppppcnt++ ))
      for (( ppppcnt=0; ppppcnt<${#PPPP_OPTIONS[@]}; ppppcnt++ ))
      do
        case "${MINED_PROCESSES[$logscnt]}" in
        *financial*)
          customsupport="0.75"
          customconfidence="0.25"
          custominterestfactor="0.125"
          ;;
        *alpha*)
          customsupport="0.75"
          customconfidence="0.25"
          custominterestfactor="0.125"
          ;;
        *BPIC15*)
          customsupport="0.75"
          customconfidence="0.75"
          custominterestfactor="0.375"
          ;;
        *)
          customsupport="$SUPPORT_THRESHOLD"
          customconfidence="$CONFIDENCE_THRESHOLD"
          custominterestfactor="$INTEREST_FACTOR_THRESHOLD"
          ;;
        esac
        outfile=`printf -- "${PRUNED_PROCESSES_PATTERNS[$logscnt]}" "${customsupport//\./}" "${customconfidence//\./}" "${custominterestfactor//\./}" "$ppatcnt" "$ppppcnt"`
        outfilecondec=`printf -- ${PRUNED_PROCESSES_PATTERNS_CONDEC[$logscnt]} "${customsupport//\./}" "${customconfidence//\./}" "${custominterestfactor//\./}" "$ppatcnt" "$ppppcnt"`
        execlogfile=`printf -- ${OUTPUT_EXEC_LOGS_PATTERNS[$logscnt]} "${customsupport//\./}" "${customconfidence//\./}" "${custominterestfactor//\./}" "$ppatcnt" "$ppppcnt"`
        csvfile=`printf -- ${OUTPUT_CSV_PATTERNS[$logscnt]} "${customsupport//\./}" "${customconfidence//\./}" "${custominterestfactor//\./}" "$ppatcnt" "$ppppcnt"`
        # Print the preamble in the exec-log file
        echo "Invocation parameters: " > "${EXEC_LOGS_DIR}/${execlogfile}"
        echo "'Process model';'ppPP';'ppAA';'support';'confidence';'interestfactor'" >> "${EXEC_LOGS_DIR}/${execlogfile}"
        echo "'"`basename ${MINED_MODELS_DIR}/${MINED_PROCESSES[$logscnt]}`"';'"${PPAT_OPTIONS[$ppatcnt]}"';'"${PPPP_OPTIONS[$ppppcnt]}"';'"${customsupport}"';'"${customconfidence}"';'"${custominterestfactor}"'" >> "${EXEC_LOGS_DIR}/${execlogfile}"
        echo >> "${EXEC_LOGS_DIR}/${execlogfile}"
        CMD="$PRUNING_CMD -iMF ${MINED_MODELS_DIR}/${MINED_PROCESSES[$logscnt]} -s ${customsupport} -c ${customconfidence} -i ${custominterestfactor} -ppAT ${PPAT_OPTIONS[$ppatcnt]} -ppPP ${PPPP_OPTIONS[$ppppcnt]} -oMF ${MINED_MODELS_MINIMISED_DIR}/${outfile} -CSV ${MINED_MODELS_MINIMISED_DIR}/${csvfile} &>> ${EXEC_LOGS_DIR}/${execlogfile}"
        echo "Executing timeout ${TIMEOUT} ${CMD}..."
        eval "timeout ${TIMEOUT} ${CMD}"
        i=`expr $i + 1`
      done
    done
  done
fi

echo "$i performed pruning tasks"

if [ "$DO_THRESHOLDS_CUT" = 1 ]
then
  for (( logscnt=0; logscnt<${#MINED_PROCESSES[@]}; logscnt++ ))
  do
    case "${MINED_PROCESSES[$logscnt]}" in
    *financial*)
      customsupport="0.75"
      customconfidence="0.25"
      custominterestfactor="0.125"
      ;;
    *alpha*)
      customsupport="0.75"
      customconfidence="0.25"
      custominterestfactor="0.125"
      ;;
    *BPIC15*)
      customsupport="0.75"
      customconfidence="0.75"
      custominterestfactor="0.375"
      ;;
    *)
      customsupport="$SUPPORT_THRESHOLD"
      customconfidence="$CONFIDENCE_THRESHOLD"
      custominterestfactor="$INTEREST_FACTOR_THRESHOLD"
      ;;
    esac
    ppat='hierarchy'
    pppp='random'
    ppatcnt='99'
    ppppcnt='99'
    outfile=`printf -- "${PRUNED_PROCESSES_PATTERNS[$logscnt]}" "${customsupport//\./}" "${customconfidence//\./}" "${custominterestfactor//\./}" "$ppatcnt" "$ppppcnt"`
    outfilecondec=`printf -- ${PRUNED_PROCESSES_PATTERNS_CONDEC[$logscnt]} "${customsupport//\./}" "${customconfidence//\./}" "${custominterestfactor//\./}" "$ppatcnt" "$ppppcnt"`
    execlogfile=`printf -- ${OUTPUT_EXEC_LOGS_PATTERNS[$logscnt]} "${customsupport//\./}" "${customconfidence//\./}" "${custominterestfactor//\./}" "$ppatcnt" "$ppppcnt"`
    csvfile=`printf -- ${OUTPUT_CSV_PATTERNS[$logscnt]} "${customsupport//\./}" "${customconfidence//\./}" "${custominterestfactor//\./}" "$ppatcnt" "$ppppcnt"`
    # Print the preamble in the exec-log file
    echo "Invocation parameters: " > "${EXEC_LOGS_DIR}/${execlogfile}"
    echo "'Process model';'ppPP';'ppAA';'support';'confidence';'interestfactor'" >> "${EXEC_LOGS_DIR}/${execlogfile}"
    echo "'"`basename ${MINED_MODELS_DIR}/${MINED_PROCESSES[$logscnt]}`"';'"${ppat}"';'"${pppp}"';'"${customsupport}"';'"${customconfidence}"';'"${custominterestfactor}"'" >> "${EXEC_LOGS_DIR}/${execlogfile}"
    echo >> "${EXEC_LOGS_DIR}/${execlogfile}"
    CMD="$PRUNING_CMD -iMF ${MINED_MODELS_DIR}/${MINED_PROCESSES[$logscnt]} -s ${customsupport} -c ${customconfidence} -i ${custominterestfactor} -ppAT ${ppat} -ppPP ${pppp} -oMF ${MINED_MODELS_MINIMISED_DIR}/${outfile} -CSV ${MINED_MODELS_MINIMISED_DIR}/${csvfile} &>> ${EXEC_LOGS_DIR}/${execlogfile}"
    echo "Executing timeout ${TIMEOUT} ${CMD}..."
    eval "timeout ${TIMEOUT} ${CMD}"
    i=`expr $i + 1`
  done
fi

echo "$i performed threshold cuts"
