/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner.stats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class OccurrencesStatsBuilder {
	private static final int PROGRESS_BAR_SCALE = 40;
	protected static Logger logger;
    public static final boolean ONWARDS = true;
    public static final boolean BACKWARDS = !ONWARDS;

    private Character[] alphabet;
    private GlobalStatsTable statsTable;
    private Character contemporaneityDelimiter = null;
    
    private void commonConstructorOperations(Character[] alphabet, Character contemporaneityDelimiter) {
    	this.alphabet = alphabet;
    	this.contemporaneityDelimiter = contemporaneityDelimiter;
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getCanonicalName());
        }
    }
    
    public OccurrencesStatsBuilder(Character[] alphabet, Character contemporaneityDelimiter) {
    	this.commonConstructorOperations(alphabet, contemporaneityDelimiter);
    	this.statsTable = new GlobalStatsTable(alphabet);
    }
    
    public OccurrencesStatsBuilder(Character[] alphabet, Character contemporaneityDelimiter, Integer maximumBranchingFactor) {
    	this.commonConstructorOperations(alphabet, contemporaneityDelimiter);
        this.statsTable = new GlobalStatsTable(alphabet, maximumBranchingFactor);
    }
    
    public GlobalStatsTable checkThisOut(String[] testbed) {
        this.statsTable.logSize += testbed.length;
        this.checkThisOut(testbed, ONWARDS);
        this.checkThisOut(testbed, BACKWARDS, true);
        System.out.println();
        return this.statsTable;
    }
        
    public GlobalStatsTable checkThisOut(String[] testbed, boolean onwards) {
        return this.checkThisOut(testbed, onwards, false);
    }
        
    public GlobalStatsTable checkThisOut(String[] testbed, boolean onwards, boolean secondPass) {
        // for sake of robustness
        SortedSet<Character> sortedSetOfCharsInTheAlphabet = new TreeSet<Character>();
        for (Character s: alphabet) {
        	sortedSetOfCharsInTheAlphabet.add(s);
        }

        int counter = 0;
        int analysedPortion = 0;

        for (String testbedString: testbed) {
            if (!onwards) {
                testbedString = new StringBuilder(testbedString).reverse().toString();
            }
            char[] chars = testbedString.toCharArray();
            SortedSet<Character> appearedChars = new TreeSet<Character>();
            Character chr = null;
            int positionCursor = 0;
            boolean contemporaneity = false;
            
            for (int i = 0; i < chars.length; i++) {
                chr = chars[i];
            	if (chr.equals(this.contemporaneityDelimiter)) {
            		contemporaneity = true;
            	} else {
                    // for the sake of robustness
	                if (!contemporaneity) {
	                	positionCursor++;
	                } else {
	                	contemporaneity = false;
	                }
                    if (sortedSetOfCharsInTheAlphabet.contains(chr)) {
    	                // record the occurrence of this chr in the current string
    	                appearedChars.add(chr);
    	                for (Character appChr : appearedChars) {
    	                    // for each already appeared chr, register the new occurrence of the current in its own stats table, at the proper distance.
    	                    this.statsTable.statsTable.get(appChr).newAtPosition(
    	                            chr, 
    	                            (   onwards
    	                                ?   positionCursor
    	                                :   0 - positionCursor
    	                            ),
    	                            onwards
    	                    );
    	                }
                    }
            	}
           	}
            if (!secondPass) {
                /* Record the information of which is the last character! */
                if (chr != null && sortedSetOfCharsInTheAlphabet.contains(chr))
                    this.statsTable.statsTable.get(chr).appearancesAsLast += 1;
                /* Record which character did not ever appear in the local stats tables! */
                this.setNeverAppearedStuffAtThisStep(appearedChars);
            }
            /*
             * Reset local stats table counters,
             * increment the appearances of the character at position 1 in the string as the first,
             * record the amount of occurrences AT THIS STEP and increment the total amount OVER ALL OF THE STEPS,
             * reset the switchers for the alternations counters
             */
            this.finalizeAnalysisStep(onwards, secondPass);
            
        	counter++;
        	if ( counter > (double)testbed.length / PROGRESS_BAR_SCALE * (analysedPortion+1) ) {
        		for (int i = analysedPortion +1;
        				i < ((double)counter / testbed.length * PROGRESS_BAR_SCALE);
        				i++) {
        			System.out.print("|");
        		}
        		analysedPortion = (int) Math.floor((double)counter / testbed.length * PROGRESS_BAR_SCALE);
        	}
        }
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

    public static String[] printAlphabet(String[] testbed) {
        SortedSet<String> alphabet = new TreeSet<String>();
        for (String testbedString: testbed) {
            char[] chars = testbedString.toCharArray();
            for (char character : chars) {
                alphabet.add(String.valueOf(character));
            }
        }
        return alphabet.toArray(new String[alphabet.size()]);
    }
}