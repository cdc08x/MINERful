/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner.stats;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.miner.stats.charsets.FixedTaskSetIncrementalCountersCollection;

public abstract class LocalStatsWrapperForCharsets extends LocalStatsWrapper {
    protected FixedTaskSetIncrementalCountersCollection neverAppearedCharacterSets;
    protected FixedTaskSetIncrementalCountersCollection neverMoreAppearedAfterCharacterSets;
    protected FixedTaskSetIncrementalCountersCollection neverMoreAppearedBeforeCharacterSets;
    protected FixedTaskSetIncrementalCountersCollection repetitionsBeforeCharactersAppearingAfter;
    protected FixedTaskSetIncrementalCountersCollection repetitionsAfterCharactersAppearingBefore;
//    protected Map<String, TaskChar> nearestMatesAtThisStep;
//    protected ArrayList<TaskChar> orderedAlreadyMetCharsAtThisStep;
//    protected TreeSet<TaskChar> alreadyMetCharsAtThisStep;
    protected Integer maximumTasksSetSize;

    public LocalStatsWrapperForCharsets(TaskCharArchive archive, TaskChar baseTask) {
		super(archive, baseTask);
	}

	public FixedTaskSetIncrementalCountersCollection getNeverAppearedCharacterSets() {
		return neverAppearedCharacterSets;
	}
	public FixedTaskSetIncrementalCountersCollection getNeverMoreAppearedAfterCharacterSets() {
		return neverMoreAppearedAfterCharacterSets;
	}
	public FixedTaskSetIncrementalCountersCollection getNeverMoreAppearedBeforeCharacterSets() {
		return neverMoreAppearedBeforeCharacterSets;
	}
	public FixedTaskSetIncrementalCountersCollection getRepetitionsBeforeCharactersAppearingAfter() {
		return repetitionsBeforeCharactersAppearingAfter;
	}
	public FixedTaskSetIncrementalCountersCollection getRepetitionsAfterCharactersAppearingBefore() {
		return repetitionsAfterCharactersAppearingBefore;
	}
}