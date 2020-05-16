#!/bin/bash

# Author:       Claudio Di Ciccio
# Date:         2016/03/20
# Description:  This script launches the ConstraintsRelevanceEvaluator, in order to discover the declarative process model out of a log, provided as input, with custom constraints.

## Import the libraries and store it into $LIBS
. ./libs.cfg

## Clean up the screen
clear

## Runtime environment constants
MAINCLASS="minerful.relevance.ConstraintsRelevanceEvaluator"
MEMORY_MAX="32G"

## Exec-specific parameters
THRESHOLD="1.0"

LOG="logs/Road-traffic-fines/Road_Traffic_Fine_Management_Process.xes.gz"

## Run!
java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS $LOG $THRESHOLD

exit 0


# LOG="/home/claudio/Code/MINERful/logs/BPIC2012/financial_log.xes.gz"
LOG="logs/BPIC2013/bpi_challenge_2013_closed_problems.xes.gz"
# LOG="/home/claudio/Code/MINERful/logs/BPIC2014/BPIC2014-DetailIncidentActivity.xes.gz"
# LOG="logs/Road-traffic-fines/Road_Traffic_Fine_Management_Process.xes.gz"
# LOG="/home/claudio/Desktop/Temp-MINERful/vacucheck/testlog.txt"

OUTLOGFILE="exec-logs/vacuity/`basename ${LOG}`.log.txt"
OUTFILE="models/vacuity/`basename ${LOG}`.model.xml"

## Run!
java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS $LOG $THRESHOLD $OUTFILE &> "$OUTLOGFILE"

## Exec-specific parameters
THRESHOLD="0.50"

# LOG="/home/claudio/Code/MINERful/logs/BPIC2012/financial_log.xes.gz"
# LOG="logs/BPIC2013/bpi_challenge_2013_closed_problems.xes.gz"
# LOG="/home/claudio/Code/MINERful/logs/BPIC2014/BPIC2014-DetailIncidentActivity.xes.gz"
LOG="logs/Road-traffic-fines/Road_Traffic_Fine_Management_Process.xes.gz"
# LOG="/home/claudio/Desktop/Temp-MINERful/vacucheck/testlog.txt"

OUTLOGFILE="exec-logs/vacuity/`basename ${LOG}`.log.txt"
OUTFILE="models/vacuity/`basename ${LOG}`.model.xml"

## Run again!
java -Xmx$MEMORY_MAX -classpath $LIBS $MAINCLASS $LOG $THRESHOLD $OUTFILE &> "$OUTLOGFILE"
