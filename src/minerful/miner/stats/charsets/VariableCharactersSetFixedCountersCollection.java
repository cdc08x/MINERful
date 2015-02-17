package minerful.miner.stats.charsets;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class VariableCharactersSetFixedCountersCollection extends CharactersSetCountersCollection {
	private Integer maximumCharactersSetSize;
	
	public VariableCharactersSetFixedCountersCollection() {
		this(null);
	}

	public VariableCharactersSetFixedCountersCollection(
			Integer maximumCharactersSetSize) {
		this.charactersSetCounterCollection = new TreeSet<CharactersSetCounter>();
		this.singleCharIndexer = new TreeMap<Character, TreeSet<CharactersSetCounter>>();
		this.maximumCharactersSetSize = maximumCharactersSetSize;
	}

	@Override
	protected void reIndex(Set<Character> stuff, CharactersSetCounter indexed) {
		for (Character chr : stuff) {
			if (!this.singleCharIndexer.containsKey(chr))
				this.singleCharIndexer.put(chr, new TreeSet<CharactersSetCounter>());
			this.singleCharIndexer.get(chr).add(indexed);
		}
	}
	
	public boolean hasIndex(Character indexer) {
		return this.getIndexedBy(indexer) != null;
	}
	
	public Set<CharactersSetCounter> getIndexedBy(Character indexer) {
		return this.singleCharIndexer.get(indexer);
	}
	
	public Set<CharactersSetCounter> getIndexedByOrInterleave(Character indexer) {
		this.interleave(indexer);
		return this.singleCharIndexer.get(indexer);
	}
	
	public void storeAndReIndex(CharactersSetCounter nuCharactersSetCounter) {
		this.charactersSetCounterCollection.add(nuCharactersSetCounter);
		this.reIndex(nuCharactersSetCounter.getCharactersSet(), nuCharactersSetCounter);
	}
	
	private boolean interleave(Character indexer) {
		return this.interleave(indexer, false);
	}
	
	private boolean interleave(Character indexer, boolean inheritCountFromExisting) {
		Set<CharactersSetCounter> nuCharSetCounters = null;
		
		if (!this.singleCharIndexer.containsKey(indexer)) {
			nuCharSetCounters = new TreeSet<CharactersSetCounter>();
			// Add the singleton
			CharactersSetCounter nuCharSetCounter = new CharactersSetCounter(indexer);
			Set<Character> nuCharSet = nuCharSetCounter.getCharactersSet();
			this.addAndReIndex(nuCharSet, nuCharSetCounter);
			
			// Combine indexer with existing character sets
			for(CharactersSetCounter existingCharSetCounter : this.charactersSetCounterCollection) {
				if (this.maximumCharactersSetSize == null || existingCharSetCounter.howManyCharactersInSet() < this.maximumCharactersSetSize) {
					nuCharSet = existingCharSetCounter.getCharactersSet();
					nuCharSet.add(indexer);
					nuCharSetCounter = new CharactersSetCounter(nuCharSet);
					if (inheritCountFromExisting)
						nuCharSetCounter.incrementCounter(existingCharSetCounter.getCounter());
					nuCharSetCounters.add(nuCharSetCounter);
				}
			}
			for (CharactersSetCounter nuCharSetCounterToAdd : nuCharSetCounters)
				this.addAndReIndex(nuCharSetCounterToAdd.getCharactersSet(), nuCharSetCounterToAdd);
			
			return true;
		}
		
		return false;
	}
    
    private void mergeAndReindex (CharactersSetCounter chSetCounter) {
        if (this.charactersSetCounterCollection.contains(chSetCounter)) {
            CharactersSetCounter alreadyExisting = this.charactersSetCounterCollection.tailSet(chSetCounter).first();
            alreadyExisting.incrementCounter(chSetCounter.getCounter());
        } else {
            this.addAndReIndex(chSetCounter.getCharactersSet(), chSetCounter);
        }
    }
    
    public void merge (VariableCharactersSetFixedCountersCollection other) {
    	other = this.prepareForMerging(other);
        for (CharactersSetCounter chSetCounter : other.charactersSetCounterCollection) {
            this.mergeAndReindex(chSetCounter);
        }
    }

	private VariableCharactersSetFixedCountersCollection prepareForMerging(
			VariableCharactersSetFixedCountersCollection other) {
		// Check this' single-character indexers. If some are missing, wrt other, it means that some characters did not appear in the previous traces.
		if (!this.singleCharIndexer.keySet().containsAll(other.singleCharIndexer.keySet())) {
			// If some are missing, for each of them you must copy and enlarge the existing sets adding the missing characters, including the counter.
			for (Character otherIndexer : other.singleCharIndexer.keySet()) {
				this.interleave(otherIndexer, true);
			}
		}
		// The other way round, for other and this
		if (!other.singleCharIndexer.keySet().containsAll(this.singleCharIndexer.keySet())) {
			// If some are missing, for each of them you must copy and enlarge the existing sets adding the missing characters, including the counter.
			for (Character thisIndexer : this.singleCharIndexer.keySet()) {
				other.interleave(thisIndexer, true);
			}
		}
		
		return other;
	}
}