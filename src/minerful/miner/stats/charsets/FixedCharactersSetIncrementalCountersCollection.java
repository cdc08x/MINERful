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

public class FixedCharactersSetIncrementalCountersCollection extends CharactersSetCountersCollection {	
	public FixedCharactersSetIncrementalCountersCollection(Character[] alphabet) {
		this.charactersSetCounterCollection = new TreeSet<CharactersSetCounter>();
		this.singleCharIndexer = new HashMap<Character, TreeSet<CharactersSetCounter>>(alphabet.length);
		this.setupSingleCharIndexer(alphabet);
	}

	private void setupSingleCharIndexer(Character[] alphabet) {
		for (Character chr : alphabet) {
			this.singleCharIndexer.put(chr, new TreeSet<CharactersSetCounter>());
		}
	}
	
	@Override
	protected void reIndex(Set<Character> stuff, CharactersSetCounter indexed) {
		for (Character chr : stuff) {
			this.singleCharIndexer.get(chr).add(indexed);
		}
	}
	
	/**
	 * This is the second best idea applied in this algorithm, after the one avoiding the transitive closure.
	 * @param source A Map connecting a numeric value to single characters
	 * @return An aggregated information concerning subsets of characters, given the set of keys in the <code>source</code> Map
	 */
	public static FixedCharactersSetIncrementalCountersCollection fromNumberedSingletons(Map<Character, Integer> source) {
		// The return value
		FixedCharactersSetIncrementalCountersCollection charSetCountColln = null;
		// Key idea 1: let us revert numbers and characters: we aggregate characters sharing the same numeric value!
		Map<Integer, SortedSet<Character>> reversedMap = new TreeMap<Integer, SortedSet<Character>>();
		// Temporary variable, storing those numeric values that are associated to characters
		int auxSum = 0;
		// This variable will come into play later. Please wait... By now, just remind that it's meant to record the numeric values acting as keys in reversedMap, in ascending order
		Set<Integer> sortedSums = new TreeSet<Integer>();
		// The "local" alphabet of characters
		SortedSet<Character> alphaList = new TreeSet<Character>();
		
		for (Character keyChr : source.keySet()) {
			auxSum = source.get(keyChr);
			// We do not care about 0's
			if (auxSum > 0) {
				if (!reversedMap.containsKey(auxSum)) {
					reversedMap.put(auxSum, new TreeSet<Character>());
				}
				// If it was already there, no problem! It won't be added, actually.
				reversedMap.get(auxSum).add(keyChr);
				// Read above!
				alphaList.add(keyChr);
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
					alphaList.toArray(
							new Character[alphaList.size()]
					)
				);

		return charSetCountColln;
	}

	public static FixedCharactersSetIncrementalCountersCollection fromCountedCharSets(
			Map<Integer, SortedSet<Character>> counterForCharSets, Character[] alphabet) {
		FixedCharactersSetIncrementalCountersCollection charSetConCol = new FixedCharactersSetIncrementalCountersCollection(alphabet);
		SortedSet<Character> auxCharSet = null;
		int difference = 0;
		
		// Ascending order
		for (Integer keyInt : counterForCharSets.keySet()) {
			auxCharSet = counterForCharSets.get(keyInt);
			charSetConCol.incrementAt(auxCharSet, keyInt - difference);
			difference += keyInt - difference;
		}
		
		return charSetConCol;
	}
	
	public void merge(FixedCharactersSetIncrementalCountersCollection other) {
		for (CharactersSetCounter otherCharSetCounter : other.charactersSetCounterCollection) {
			this.incrementAt(otherCharSetCounter.getCharactersSet(), otherCharSetCounter.getCounter());
		}
	}
	
	/**
	 * No idea of what it does :P
	 * @param charactersForIndexing
	 * @return
	 */
	@Deprecated
	public CharactersSetCounter intersectByCharacters(SortedSet<Character> charactersForIndexing) {
		TreeSet<CharactersSetCounter> intersected =
				new TreeSet<CharactersSetCounter>(
						singleCharIndexer.get(charactersForIndexing.first()));
		
		return intersected.tailSet(new CharactersSetCounter(charactersForIndexing)).first();
	}
	
	public SortedSet<CharactersSetCounter> selectCharSetCountersSharedAmong(
            Collection<Character> sharingCharacters) {
		Iterator<Character> charIterator = sharingCharacters.iterator();
		Character chr = null;
		TreeSet<CharactersSetCounter>
			shared =
				new TreeSet<CharactersSetCounter>(),
			tmpShared =
				null;

		if (charIterator.hasNext()) {
			chr = charIterator.next();
			shared = new TreeSet<CharactersSetCounter>(singleCharIndexer.get(chr));
		} else {
			return shared;
		}

		while(charIterator.hasNext()) {
			chr = charIterator.next();
			tmpShared = singleCharIndexer.get(chr);
			shared.retainAll(tmpShared);
		}
		return shared;
	}

	@Override
	public Character[] alphabet() {
		return this.singleCharIndexer.keySet().toArray(new Character[this.singleCharIndexer.keySet().size()]);
	}
	
	@Override
	public SortedSet<CharactersSetCounter> getCharactersSetsOrderedByAscendingCounter() {
		SortedSet<CharactersSetCounter> nuCharSetCounter =
				new TreeSet<CharactersSetCounter>(
						new CharactersSetCounter.CharactersSetByAscendingCounterComparator()
				);
		
		nuCharSetCounter.addAll(this.charactersSetCounterCollection);
		
		return nuCharSetCounter;
	}
}