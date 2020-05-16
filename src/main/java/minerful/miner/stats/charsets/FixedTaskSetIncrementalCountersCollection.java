package minerful.miner.stats.charsets;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.concept.TaskChar;

public class FixedTaskSetIncrementalCountersCollection extends TaskSetCountersCollection {	
	public FixedTaskSetIncrementalCountersCollection(Set<TaskChar> alphabet) {
		this.tasksSetCounterCollection = new TreeSet<TasksSetCounter>();
		this.singleTaskIndexer = new HashMap<TaskChar, TreeSet<TasksSetCounter>>(alphabet.size());
		this.setupSingleCharIndexer(alphabet);
	}

	private void setupSingleCharIndexer(Set<TaskChar> alphabet) {
		for (TaskChar chr : alphabet) {
			this.singleTaskIndexer.put(chr, new TreeSet<TasksSetCounter>());
		}
	}
	
	@Override
	protected void reIndex(Set<TaskChar> stuff, TasksSetCounter indexed) {
		for (TaskChar chr : stuff) {
			this.singleTaskIndexer.get(chr).add(indexed);
		}
	}
	
	/**
	 * This is the second best idea applied in this algorithm, after the one avoiding the transitive closure.
	 * @param source A Map connecting a numeric value to single characters
	 * @return An aggregated information concerning subsets of characters, given the set of keys in the <code>source</code> Map
	 */
	public static FixedTaskSetIncrementalCountersCollection fromNumberedSingletons(Map<TaskChar, Integer> source) {
		// The return value
		FixedTaskSetIncrementalCountersCollection charSetCountColln = null;
		// Key idea 1: let us revert numbers and characters: we aggregate characters sharing the same numeric value!
		Map<Integer, SortedSet<TaskChar>> reversedMap = new TreeMap<Integer, SortedSet<TaskChar>>();
		// Temporary variable, storing those numeric values that are associated to characters
		int auxSum = 0;
		// This variable will come into play later. Please wait... By now, just remind that it's meant to record the numeric values acting as keys in reversedMap, in ascending order
		Set<Integer> sortedSums = new TreeSet<Integer>();
		// The "local" alphabet of characters
		SortedSet<TaskChar> alphaList = new TreeSet<TaskChar>();
		
		for (TaskChar key : source.keySet()) {
			auxSum = source.get(key);
			// We do not care about 0's
			if (auxSum > 0) {
				if (!reversedMap.containsKey(auxSum)) {
					reversedMap.put(auxSum, new TreeSet<TaskChar>());
				}
				// If it was already there, no problem! It won't be added, actually.
				reversedMap.get(auxSum).add(key);
				// Read above!
				alphaList.add(key);
				sortedSums.add(auxSum);
			}
		}
		
		/*
		 * Refactoring phase! E.g., say
		 * a => 2, b => 3, c => 2.
		 * This means that
		 * {b} => 3 and {a, b, c} => 2
		 * Up to this point, we would have
		 * {a, c} => 2 and
		 * {b} => 3
		 * This means, in turn, that you have to iteratively "propagate"
		 * characters from the top-rated (in terms of count) sets to the
		 * lower-rated sets. In the example, you have to
		 * add {b} (rated 3) into
		 * {a, c} (rated 2)
		 * so to have {a, b, c} rated 2.
		 */
		Integer[] sortedSumsArray = sortedSums.toArray(new Integer[0]);
		// Here we sort the array. Well, it might be useless, as the TreeSet already uses an ascending order over stored values, but you can never know...
		Arrays.sort(sortedSumsArray);
		// From the highest numeric value, to the lowest...
		for (int i = sortedSumsArray.length -1; i > 0; i--) {
			// Get the numeric value currently below this and add all its associated characters
			reversedMap.get(sortedSumsArray[i-1])
			.addAll(reversedMap.get(sortedSumsArray[i]));
		}
		
		/*
		 * Now we have:
		 * {a, b, c} = 2
		 * {b} = 3
		 * Which is fine. But we want to consider the *delta* values.
		 * Now we know for sure that the numbers are associated to sets for which a STRICT DESCENDING containment order holds as numbers grow, due to the "propagation" technique that we adopted.
		 * Therefore, we want something like this now:
		 * {a, b, c} = 2
		 * {b} = 1
		 */
		charSetCountColln = fromCountedCharSets(
					reversedMap,
					alphaList
				);

		return charSetCountColln;
	}

	public static FixedTaskSetIncrementalCountersCollection fromCountedCharSets(
			Map<Integer, SortedSet<TaskChar>> counterForCharSets, Set<TaskChar> alphabet) {
		FixedTaskSetIncrementalCountersCollection charSetConCol = new FixedTaskSetIncrementalCountersCollection(alphabet);
		SortedSet<TaskChar> auxCharSet = null;
		int difference = 0;
		
		// Ascending order
		for (Integer keyInt : counterForCharSets.keySet()) {
			auxCharSet = counterForCharSets.get(keyInt);
			charSetConCol.incrementAt(auxCharSet, keyInt - difference);
			difference += keyInt - difference;
		}
		
		return charSetConCol;
	}
	
	public void merge(FixedTaskSetIncrementalCountersCollection other) {
		for (TasksSetCounter otherCharSetCounter : other.tasksSetCounterCollection) {
			this.incrementAt(otherCharSetCounter.getTaskCharSet(), otherCharSetCounter.getCounter());
		}
	}
	
	public SortedSet<TasksSetCounter> selectCharSetCountersSharedAmong(
            Collection<TaskChar> sharingTasks) {
		Iterator<TaskChar> taskIterator = sharingTasks.iterator();
		TaskChar currTask = null;
		TreeSet<TasksSetCounter>
			shared =
				new TreeSet<TasksSetCounter>(),
			tmpShared =
				null;

		if (taskIterator.hasNext()) {
			currTask = taskIterator.next();
			shared = new TreeSet<TasksSetCounter>(singleTaskIndexer.get(currTask));
		} else {
			return shared;
		}

		while(taskIterator.hasNext()) {
			currTask = taskIterator.next();
			tmpShared = singleTaskIndexer.get(currTask);
			shared.retainAll(tmpShared);
		}
		return shared;
	}
	
	public SortedSet<TasksSetCounter> selectCharSetCountersSharedAmong(
            TaskChar[] sharingTasks) {
		TreeSet<TasksSetCounter>
			shared =
				null,
			tmpShared =
				null;

		if (sharingTasks.length > 0) {
			for (TaskChar currTask : sharingTasks) {
				if (shared == null) {
					shared = new TreeSet<TasksSetCounter>(singleTaskIndexer.get(currTask));
				} else {
					tmpShared = singleTaskIndexer.get(currTask);
					shared.retainAll(tmpShared);
				}
			}
		}

		return shared;
	}
	
	@Override
	public SortedSet<TasksSetCounter> getCharactersSetsOrderedByAscendingCounter() {
		SortedSet<TasksSetCounter> nuCharSetCounter =
				new TreeSet<TasksSetCounter>(
						new TasksSetCounter.TaskSetByAscendingCounterComparator()
				);
		
		nuCharSetCounter.addAll(this.tasksSetCounterCollection);
		
		return nuCharSetCounter;
	}
}