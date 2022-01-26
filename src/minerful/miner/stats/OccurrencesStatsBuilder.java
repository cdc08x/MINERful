/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner.stats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import minerful.concept.Event;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.utils.MessagePrinter;

import org.apache.log4j.Logger;

public class OccurrencesStatsBuilder {
	private static final int PROGRESS_BAR_SCALE = 40;
	protected static Logger logger;
    public static final boolean ONWARDS = true;
    public static final boolean BACKWARDS = !ONWARDS;

    private TaskCharArchive taskCharArchive;
    private GlobalStatsTable statsTable;
//    private Character contemporaneityDelimiter = null;
    
//    private void commonConstructorOperations(Character[] alphabet, Character contemporaneityDelimiter) {
    private void commonConstructorOperations(TaskCharArchive archive) {
    	this.taskCharArchive = archive;
//    	this.contemporaneityDelimiter = contemporaneityDelimiter;
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getCanonicalName());
        }
    }
    
//  public OccurrencesStatsBuilder(Character[] alphabet, Character contemporaneityDelimiter, Integer maximumBranchingFactor) {
//	this.commonConstructorOperations(alphabet, contemporaneityDelimiter);
	public OccurrencesStatsBuilder(TaskCharArchive archive, Integer maximumBranchingFactor) {
		this.commonConstructorOperations(archive);
        this.statsTable = new GlobalStatsTable(archive, maximumBranchingFactor);
    }
    
    public GlobalStatsTable checkThisOut(LogParser logParser) {
        this.statsTable.logSize += logParser.length();
        this.checkThisOut(logParser, ONWARDS);
        this.checkThisOut(logParser, BACKWARDS, true);
        return this.statsTable;
    }
        
    public GlobalStatsTable checkThisOut(LogParser logParser, boolean onwards) {
        return this.checkThisOut(logParser, onwards, false);
    }

    public GlobalStatsTable checkThisOut(LogParser logParser, boolean onwards, boolean secondPass) {
    	// for the sake of robustness
        int counter = 0;
        int analysedPortion = 0;

        Iterator<LogTraceParser> traceParsersIterator = logParser.traceIterator();
        LogTraceParser auxTraceParser = null;
        
        SortedSet<TaskChar> occurredEvents = null;
        Event auxEvent = null;
        TaskChar auxTaskChar = null;

        while (traceParsersIterator.hasNext()) {
        	auxTraceParser = traceParsersIterator.next();
            if (!onwards) {
            	auxTraceParser.reverse();
            }
        	auxTraceParser.init();

            occurredEvents = new TreeSet<TaskChar>();
            auxEvent = null;
            int positionCursor = 0;
//            boolean contemporaneity = false;
            while (!auxTraceParser.isParsingOver()) {
            	auxEvent = auxTraceParser.parseSubsequent().getEvent();

//            	if (chr.equals(this.contemporaneityDelimiter)) {
//            		contemporaneity = true;
//            	} else {
                    // for the sake of robustness
//	                if (!contemporaneity) {
	                	positionCursor++;
//	                } else {
//	                	contemporaneity = false;
//	                }
                    if (this.statsTable.taskCharArchive.containsTaskCharByEvent(auxEvent)) {
                    	auxTaskChar = this.statsTable.taskCharArchive.getTaskCharByEvent(auxEvent);
    	                // record the occurrence of this chr in the current string
    	                occurredEvents.add(auxTaskChar);
    	                for (TaskChar appChr : occurredEvents) {
    	                    // for each already appeared chr, register the new occurrence of the current in its own stats table, at the proper distance.
    	                	this.statsTable.statsTable.get(appChr).newAtPosition(
    	                			auxEvent, 
    	                            (   onwards
    	                                ?   positionCursor
    	                                :   0 - positionCursor
    	                            ),
    	                            onwards
    	                    );
    	                }
                    }
//            	}
            }
            if (!secondPass) {
                /* Record the information about which the last task is! */
                if (auxTaskChar != null)
                    this.statsTable.statsTable.get(auxTaskChar).occurrencesAsLast += 1;
                /* Record which character did not ever appear in the local stats tables! */
                this.setNeverAppearedStuffAtThisStep(occurredEvents);
            }
            /*
             * Reset local stats table counters,
             * increment the appearances of the character at position 1 in the string as the first,
             * record the amount of occurrences AT THIS STEP and increment the total amount OVER ALL OF THE STEPS,
             * reset the switchers for the alternations counters
             */
            this.finalizeAnalysisStep(onwards, secondPass);
            
        	counter++;
        	if ( counter > logParser.length() / PROGRESS_BAR_SCALE * (analysedPortion+1) ) {
        		for (int i = analysedPortion +1;
        				i < ((double)counter / logParser.length() * PROGRESS_BAR_SCALE);
        				i++) {
        			System.out.print("|");
        		}
        		analysedPortion = (int) Math.floor((double)counter / logParser.length() * PROGRESS_BAR_SCALE);
        	}

        	/*
        	 * If the analysis is made backwards, we should toggle the reverse sense of reading again to put the log parser in its initial status
        	 */
            if (!onwards) {
            	auxTraceParser.reverse();
            }
        }
        if (secondPass) { MessagePrinter.printlnOut(""); }
        return this.statsTable;
    }
    
    private void finalizeAnalysisStep(boolean onwards, boolean secondPass) {
        for (TaskChar key: this.taskCharArchive.getTaskChars()) {
            this.statsTable.statsTable.get(key).finalizeAnalysisStep(onwards, secondPass);
        }
    }
    
    private void setNeverAppearedStuffAtThisStep(Set<TaskChar> appearedTasks) {
        List<TaskChar> differenceStuff = new ArrayList<TaskChar>(this.taskCharArchive.size());
        for (TaskChar task : this.taskCharArchive.getTaskChars()) {
            differenceStuff.add(task);
        }
        Set<TaskChar> neverAppearedStuff = new HashSet<TaskChar>(differenceStuff);
        neverAppearedStuff.removeAll(appearedTasks);
        
        if (neverAppearedStuff.size() > 0) {
            for (TaskChar appearedChr : appearedTasks) {
            	this.statsTable.statsTable.get(appearedChr).setAsNeverAppeared(neverAppearedStuff);
            }
        }
    }
}