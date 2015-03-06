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

import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;

import org.apache.log4j.Logger;

public class OccurrencesStatsBuilder {
	private static final int PROGRESS_BAR_SCALE = 40;
	protected static Logger logger;
    public static final boolean ONWARDS = true;
    public static final boolean BACKWARDS = !ONWARDS;

    private Character[] alphabet;
    private GlobalStatsTable statsTable;
//    private Character contemporaneityDelimiter = null;
    
//    private void commonConstructorOperations(Character[] alphabet, Character contemporaneityDelimiter) {
    private void commonConstructorOperations(Character[] alphabet) {
    	this.alphabet = alphabet;
//    	this.contemporaneityDelimiter = contemporaneityDelimiter;
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getCanonicalName());
        }
    }
    
//  public OccurrencesStatsBuilder(Character[] alphabet, Character contemporaneityDelimiter) {
//	this.commonConstructorOperations(alphabet, contemporaneityDelimiter);
    public OccurrencesStatsBuilder(Character[] alphabet) {
    	this.commonConstructorOperations(alphabet);
    	this.statsTable = new GlobalStatsTable(alphabet);
    }
    
//  public OccurrencesStatsBuilder(Character[] alphabet, Character contemporaneityDelimiter, Integer maximumBranchingFactor) {
//	this.commonConstructorOperations(alphabet, contemporaneityDelimiter);
	public OccurrencesStatsBuilder(Character[] alphabet, Integer maximumBranchingFactor) {
		this.commonConstructorOperations(alphabet);
        this.statsTable = new GlobalStatsTable(alphabet, maximumBranchingFactor);
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
        SortedSet<Character> sortedSetOfCharsInTheAlphabet = new TreeSet<Character>();
        for (Character s: alphabet) {
        	sortedSetOfCharsInTheAlphabet.add(s);
        }

        int counter = 0;
        int analysedPortion = 0;

        Iterator<LogTraceParser> traceParsersIterator = logParser.traceIterator();
        LogTraceParser auxTraceParser = null;
        
        while (traceParsersIterator.hasNext()) {
        	auxTraceParser = traceParsersIterator.next();
            if (!onwards) {
            	auxTraceParser.reverse();
            }
        	auxTraceParser.init();

            SortedSet<Character> occurredEvents = new TreeSet<Character>();
            Character auxEvtIdentifier = null;
            int positionCursor = 0;
//            boolean contemporaneity = false;
            while (!auxTraceParser.isParsingOver()) {
            	auxEvtIdentifier = auxTraceParser.parseSubsequentAndEncode();
//            	if (chr.equals(this.contemporaneityDelimiter)) {
//            		contemporaneity = true;
//            	} else {
                    // for the sake of robustness
//	                if (!contemporaneity) {
	                	positionCursor++;
//	                } else {
//	                	contemporaneity = false;
//	                }
                    if (sortedSetOfCharsInTheAlphabet.contains(auxEvtIdentifier)) {
    	                // record the occurrence of this chr in the current string
    	                occurredEvents.add(auxEvtIdentifier);
    	                for (Character appChr : occurredEvents) {
    	                    // for each already appeared chr, register the new occurrence of the current in its own stats table, at the proper distance.
    	                	this.statsTable.statsTable.get(appChr).newAtPosition(
    	                    		auxEvtIdentifier, 
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
                /* Record the information of which is the last character! */
                if (auxEvtIdentifier != null && sortedSetOfCharsInTheAlphabet.contains(auxEvtIdentifier))
                    this.statsTable.statsTable.get(auxEvtIdentifier).appearancesAsLast += 1;
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
        }
        if (secondPass) { System.out.println(); }
        return this.statsTable;
    }
    
    private void finalizeAnalysisStep(boolean onwards, boolean secondPass) {
        for (Character key: this.alphabet) {
            this.statsTable.statsTable.get(key).finalizeAnalysisStep(onwards, secondPass);
        }
    }
    
    private void setNeverAppearedStuffAtThisStep(Set<Character> appearedChars) {
        List<Character> differenceStuff = new ArrayList<Character>(this.alphabet.length);
        for (Character character : this.alphabet) {
            differenceStuff.add(character);
        }
        Set<Character> neverAppearedStuff = new HashSet<Character>(differenceStuff);
        neverAppearedStuff.removeAll(appearedChars);
        
        if (neverAppearedStuff.size() > 0) {
            for (Character appearedChr : appearedChars) {
            	this.statsTable.statsTable.get(appearedChr).setAsNeverAppeared(neverAppearedStuff);
            }
        }
    }
}