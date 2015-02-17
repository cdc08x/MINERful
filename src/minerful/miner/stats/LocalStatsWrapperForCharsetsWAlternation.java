/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner.stats;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlTransient;

import minerful.miner.stats.LocalStatsWrapper.AlternatingCounterSwitcher;
import minerful.miner.stats.charsets.CharactersSetCounter;
import minerful.miner.stats.charsets.FixedCharactersSetIncrementalCountersCollection;
import minerful.miner.stats.charsets.VariableCharactersSetFixedCountersCollection;

public class LocalStatsWrapperForCharsetsWAlternation extends LocalStatsWrapperForCharsets {
	@XmlTransient
	protected Map<Character, Integer> missingAtThisStepBeforeNextRepetition;

    public LocalStatsWrapperForCharsetsWAlternation(Character[] alphabet, Character baseCharacter, Integer maximumCharactersSetSize) {
    	super(alphabet, baseCharacter);
        this.neverAppearedCharacterSets = new FixedCharactersSetIncrementalCountersCollection(alphabet);
        this.neverMoreAppearedAfterCharacterSets = new FixedCharactersSetIncrementalCountersCollection(alphabet);
        this.neverMoreAppearedBeforeCharacterSets = new FixedCharactersSetIncrementalCountersCollection(alphabet);
//		this.orderedAlreadyMetCharsAtThisStep = new ArrayList<Character>();
//		this.alreadyMetCharsAtThisStep = new TreeSet<Character>();
//		this.nearestMatesAtThisStep = new TreeMap<String, Character>();
        this.maximumCharactersSetSize = (
        		maximumCharactersSetSize == null ? null :
        			(maximumCharactersSetSize < alphabet.length ? maximumCharactersSetSize : alphabet.length)
		);
        if (this.maximumCharactersSetSize != null && this.maximumCharactersSetSize < alphabet.length) {
	        this.repetitionsBeforeCharactersAppearingAfter = new FixedCharactersSetIncrementalCountersCollection(alphabet);
	        this.repetitionsAfterCharactersAppearingBefore = new FixedCharactersSetIncrementalCountersCollection(alphabet);
        } else {
	        this.repetitionsBeforeCharactersAppearingAfter = new FixedCharactersSetIncrementalCountersCollection(alphabet);
	        this.repetitionsAfterCharactersAppearingBefore = new FixedCharactersSetIncrementalCountersCollection(alphabet);
        }
    }

    public LocalStatsWrapperForCharsetsWAlternation(Character[] alphabet, Character baseCharacter) {
    	this(alphabet, baseCharacter, null);
    }

    public int getMaximumCharactersSetSize() {
		return this.maximumCharactersSetSize;
	}

	@Override
	protected void initLocalStatsTable(Character[] alphabet) {
    	super.initLocalStatsTable(alphabet);
		this.missingAtThisStepBeforeNextRepetition = new TreeMap<Character, Integer>();
		for (Character letter : alphabet) {
			this.localStatsTable.put(letter, new StatsCell());
			if (!letter.equals(this.baseCharacter)) {
				this.missingAtThisStepBeforeNextRepetition.put(letter, 0);
			}
		}
	}
    
    @Override
	void newAtPosition(Character character, int position, boolean onwards) {
        /* if the appeared character is equal to this */
        if (character.equals(this.baseCharacter)) {
            for (Character chr: this.neverMoreAppearancesAtThisStep.keySet()) {
                this.neverMoreAppearancesAtThisStep.put(chr,
                        this.neverMoreAppearancesAtThisStep.get(chr) + 1
                );
            }
            /* if this is the first occurrence in the step, record it */
            if (this.firstOccurrenceInStep == null) {
                this.firstOccurrenceInStep = position;
            } else {
                /* if this is not the first time this chr appears in the step, initialize the repetitions register */
                if (repetitionsAtThisStep == null) {
                    repetitionsAtThisStep = new TreeSet<Integer>();
                }
            }
            if (onwards) {
            	this.repetitionsBeforeCharactersAppearingAfter.merge(
            			FixedCharactersSetIncrementalCountersCollection.fromNumberedSingletons(missingAtThisStepBeforeNextRepetition)
            	);
            }
            else {
            	this.repetitionsAfterCharactersAppearingBefore.merge(
            			FixedCharactersSetIncrementalCountersCollection.fromNumberedSingletons(missingAtThisStepBeforeNextRepetition)
            	);
            }
            // TODO Document this passage: it is crucial!
            for (Character chr: this.missingAtThisStepBeforeNextRepetition.keySet()) {
                this.missingAtThisStepBeforeNextRepetition.put(chr, 1);
            }
        }
        /* if the appeared character is NOT equal to this */
        else {
            /* store the info that chr appears after the pivot */
            this.neverMoreAppearancesAtThisStep.put(character, 0);
            this.missingAtThisStepBeforeNextRepetition.put(character, 0);
        }

        if (repetitionsAtThisStep != null) {
            /* for each repetition of the same character during the analysis, record not only the info of the appearance at a distance equal to (chr.position - firstOccurrenceInStep.position), but also at the (chr.position - otherOccurrenceInStep.position) for each other appearance of the pivot! */
            /* THIS IS THE VERY BIG TRICK TO AVOID ANY TRANSITIVE CLOSURE!! */
            for (Integer occurredAlsoAt : repetitionsAtThisStep) {
                this.localStatsTable.get(character).newAtDistance(position - occurredAlsoAt);
            }
        }
        /* If this is not the first occurrence position, record the distance equal to (chr.position - firstOccurrenceInStep.position) */
        if (firstOccurrenceInStep != position)
            this.localStatsTable.get(character).newAtDistance(position - firstOccurrenceInStep);
        /* If this is the repetition of the pivot, record it (it is needed for the computation of all the other distances!) */
        if (this.repetitionsAtThisStep != null && character.equals(this.baseCharacter)) {
            this.repetitionsAtThisStep.add(position);
        }

//      this.alreadyMetCharsAtThisStep.add(character);
//    	this.orderedAlreadyMetCharsAtThisStep.remove(character);
//      this.orderedAlreadyMetCharsAtThisStep.add(0, character);
    }

    @Override
	void finalizeAnalysisStep(boolean onwards, boolean secondPass) {
    	// Step 1: as in the single-char case:
    	/* Record the amount of occurrences AT THIS STEP and the total amount OVER ALL OF THE STEPS */
    	/* Record what did not appear in the step, afterwards or backwards */
    	/* Resets the switchers for the alternations counter */
    	/* Resets the local stats table counters */
        super.finalizeAnalysisStep(onwards, secondPass);
        for (Character chr: this.missingAtThisStepBeforeNextRepetition.keySet()) {
            this.missingAtThisStepBeforeNextRepetition.put(chr, 0);
        }
    }

    @Override
	protected void setAsNeverAppeared(Set<Character> neverAppearedStuff) {
    	if (neverAppearedStuff.size() < 1) {
    		return;
    	}
    	// Step 1: each character in neverAppearedStuff must be recorded as never appearing in the current string 
    	for (Character neverAppearedChr : neverAppearedStuff) {
    		super.setAsNeverAppeared(neverAppearedChr);
    	}
    	// Step 2: the whole set of characters has to be recorded at once
    	// Step 2 is needed because Step 1 loses the information that all of the char's are not read at once all together in a string
    	addSetToNeverAppearedCharSets(
    			neverAppearedStuff,
				(   (this.repetitionsAtThisStep == null || this.repetitionsAtThisStep.size() < 1) 
                    ? 1
                    : this.repetitionsAtThisStep.size() + 1
                )
            );
    }
    
    protected void addSetToNeverAppearedCharSets(Set<Character> neverAppearedStuff, int sum) {
    	this.neverAppearedCharacterSets.incrementAt(neverAppearedStuff, sum);
    }
     
    @Override
	protected void recordCharactersThatNeverAppearedAnymoreInStep(boolean onwards) {
    	// Step 1: aggregate this.neverMoreAppearancesInStep and record
    	if (onwards) {
	    	this.neverMoreAppearedAfterCharacterSets.merge(
	    			FixedCharactersSetIncrementalCountersCollection.fromNumberedSingletons(neverMoreAppearancesAtThisStep)
	    	);
    	} else {
    		this.neverMoreAppearedBeforeCharacterSets.merge(
	    			FixedCharactersSetIncrementalCountersCollection.fromNumberedSingletons(neverMoreAppearancesAtThisStep)
	    	);
    	}
    	// Step 2: update singletons
        super.recordCharactersThatNeverAppearedAnymoreInStep(onwards);
    }
   
    @Override
    public String toString() {
        if (this.totalAmountOfAppearances == 0)
            return "";

        StringBuilder sBuf = new StringBuilder();
        for (Character key : this.localStatsTable.keySet()) {
            sBuf.append("\t\t[" + key + "] => " + this.localStatsTable.get(key).toString());
        }
        sBuf.append("\n\t\t\tnever's " + this.neverAppearedCharacterSets.toString().replace("\n", "\n\t\t\t\t"));
        sBuf.append("\n\t\t\tnever-after's " + this.neverMoreAppearedAfterCharacterSets.toString().replace("\n", "\n\t\t\t\t"));
        sBuf.append("\n\t\t\tnever-before's " + this.neverMoreAppearedBeforeCharacterSets.toString().replace("\n", "\n\t\t\t\t"));
        sBuf.append("\n\t\t\trepetitions-in-between-after's " + this.repetitionsBeforeCharactersAppearingAfter.toString().replace("\n", "\n\t\t\t\t"));
        sBuf.append("\n\t\t\trepetitions-in-between-before's " + this.repetitionsAfterCharactersAppearingBefore.toString().replace("\n", "\n\t\t\t\t"));
        sBuf.append("\n");

        return sBuf.toString();
    }

}