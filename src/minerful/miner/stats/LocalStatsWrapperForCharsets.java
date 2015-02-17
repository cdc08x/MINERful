/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner.stats;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;

import minerful.miner.stats.charsets.FixedCharactersSetIncrementalCountersCollection;

public abstract class LocalStatsWrapperForCharsets extends LocalStatsWrapper {
    protected FixedCharactersSetIncrementalCountersCollection neverAppearedCharacterSets;
    protected FixedCharactersSetIncrementalCountersCollection neverMoreAppearedAfterCharacterSets;
    protected FixedCharactersSetIncrementalCountersCollection neverMoreAppearedBeforeCharacterSets;
    protected FixedCharactersSetIncrementalCountersCollection repetitionsBeforeCharactersAppearingAfter;
    protected FixedCharactersSetIncrementalCountersCollection repetitionsAfterCharactersAppearingBefore;
    protected Map<String, Character> nearestMatesAtThisStep;
    protected ArrayList<Character> orderedAlreadyMetCharsAtThisStep;
    protected TreeSet<Character> alreadyMetCharsAtThisStep;
    protected Integer maximumCharactersSetSize;

    public LocalStatsWrapperForCharsets(Character[] alphabet, Character baseCharacter) {
		super(alphabet, baseCharacter);
	}

	public FixedCharactersSetIncrementalCountersCollection getNeverAppearedCharacterSets() {
		return neverAppearedCharacterSets;
	}
	public FixedCharactersSetIncrementalCountersCollection getNeverMoreAppearedAfterCharacterSets() {
		return neverMoreAppearedAfterCharacterSets;
	}
	public FixedCharactersSetIncrementalCountersCollection getNeverMoreAppearedBeforeCharacterSets() {
		return neverMoreAppearedBeforeCharacterSets;
	}
	public FixedCharactersSetIncrementalCountersCollection getRepetitionsBeforeCharactersAppearingAfter() {
		return repetitionsBeforeCharactersAppearingAfter;
	}
	public FixedCharactersSetIncrementalCountersCollection getRepetitionsAfterCharactersAppearingBefore() {
		return repetitionsAfterCharactersAppearingBefore;
	}
}