#!/bin/bash

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd .. && pwd )"
LOGS_DIR="$BASE_DIR/logs"
OUTPUT_DIR="$BASE_DIR/studies-on-incremental-mining/forecast/specifications"

MINING_CMD="./run-MINERful-unstable.sh"

cd "$BASE_DIR"

LOG_CODES=(
"BPIC2012"
"BPIC2013closed"
"BPIC2013open"
"BPIC2013incidents"
"BPIC2014"
"Fines2015"
"Sepsis2016"
"BPIC2017"
)

declare -A LOGS
LOGS["BPIC2012"]="$LOGS_DIR/BPIC2012/financial_log.xes.gz"
LOGS["BPIC2013closed"]="$LOGS_DIR/BPIC2013/bpi_challenge_2013_closed_problems.xes.gz"
LOGS["BPIC2013open"]="$LOGS_DIR/BPIC2013/bpi_challenge_2013_open_problems.xes.gz"
LOGS["BPIC2013incidents"]="$LOGS_DIR/BPIC2013/bpi_challenge_2013_incidents.xes.gz"
LOGS["BPIC2014"]="$LOGS_DIR/BPIC2014/BPIC2014-DetailIncidentActivity.xes.gz"
LOGS["Fines2015"]="$LOGS_DIR/Fines2015/Road_Traffic_Fine_Management_Process.xes.gz"
LOGS["Sepsis2016"]="$LOGS_DIR/Sepsis2016/SepsisCases.xes.gz"
LOGS["BPIC2017"]="$LOGS_DIR/BPIC2017/BPI-Challenge-2017.xes.gz"

declare -A LOG_LENGTHS
LOG_LENGTHS["BPIC2012"]=13087
LOG_LENGTHS["BPIC2013closed"]=1487
LOG_LENGTHS["BPIC2013open"]=819
LOG_LENGTHS["BPIC2013incidents"]=7554
LOG_LENGTHS["BPIC2014"]=45616
LOG_LENGTHS["Fines2015"]=150370
LOG_LENGTHS["Sepsis2016"]=1050
LOG_LENGTHS["BPIC2017"]=31509

WINDOW_SIZE_FRACTION=5
SHIFT_STEP_PERCENT=3

for logCode in ${LOG_CODES[@]}
do
  loglen=${LOG_LENGTHS[$logCode]}
  echo "Slide-mining files pertaining to $logCode (${LOGS[$logCode]})..."
  # ./run-MINERfulSlider-unstable.sh runs the JAR file with the proper main class
  # -tStart dictates from which trace the sliding stars
  # -subL indicates the window size (how many traces are taken into account to discover the model)
  # -sliBy indicates the step of the shifting window (in traces); if sliBy = subL you have a tumbling window; if sliBy > subL we have a tumbling window with leaps
  # -p indicates how many threads you want to use
  # -s min support
  # -c min confidence
  # -i min interest threshold
  # -ppAT strategy of the pruning of redundant constraints. If set to "none", it implies that, if, e.g., Response(a,b) and ChainResponse(a,b) both hold true, none of those is removed, altough the former would be redundant
  # -shush avoids that the constraints are printed out on your screen
  # -JSON indicates where to save the discovered model in a JSON format. Notice that you will not have 1 JSON model, but as many as the number of shifts you could achieve.
  # -sliOut tells where to store the intermediate results in CSV format.
  # In case of doubt, type ./run-MINERfulSlider-unstable.sh --help (or contact Claudio)
  ./run-MINERfulSlider-unstable.sh -iLF "${LOGS[$logCode]}" -tStart 0 -subL $((loglen / WINDOW_SIZE_FRACTION)) -sliBy $((loglen / 100 * SHIFT_STEP_PERCENT)) -p 2 -s 0.0 -c 0.0 -i 0.0 -ppAT none -shush -JSON "$OUTPUT_DIR/$logCode-metrics-sliding.json" -sliOut "$OUTPUT_DIR/$logCode-metrics-sliding.csv"
  echo "Renaming discovered files pertaining to $logCode (${LOGS[$logCode]})..."
  for jsonFile in $(ls "$OUTPUT_DIR/$logCode-metrics-sliding"* | grep json)
  do
    rename -e 's/\.json(-[0-9]*-[0-9]*)/$1.json/g' "$jsonFile"
  done
  for jsonFile in $(ls "$OUTPUT_DIR/$logCode-metrics-sliding"* | grep json)
  do
    rename -e 's/'"$logCode"'/benchmark§'"$logCode"'/g' "$jsonFile"
  done
done

exit 0
