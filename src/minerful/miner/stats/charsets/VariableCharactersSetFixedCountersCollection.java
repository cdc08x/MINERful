package minerful.miner.stats.charsets;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.concept.TaskChar;

public class VariableCharactersSetFixedCountersCollection extends TaskSetCountersCollection {
	private Integer maximumCharactersSetSize;
	
	public VariableCharactersSetFixedCountersCollection() {
		this(null);
	}

	public VariableCharactersSetFixedCountersCollection(
			Integer maximumCharactersSetSize) {
		this.tasksSetCounterCollection = new TreeSet<TasksSetCounter>();
		this.singleTaskIndexer = new TreeMap<TaskChar, TreeSet<TasksSetCounter>>();
		this.maximumCharactersSetSize = maximumCharactersSetSize;
	}

	@Override
	protected void reIndex(Set<TaskChar> stuff, TasksSetCounter indexed) {
		for (TaskChar chr : stuff) {
			if (!this.singleTaskIndexer.containsKey(chr))
				this.singleTaskIndexer.put(chr, new TreeSet<TasksSetCounter>());
			this.singleTaskIndexer.get(chr).add(indexed);
		}
	}
	
	public boolean hasIndex(TaskChar indexer) {
		return this.getIndexedBy(indexer) != null;
	}
	
	public Set<TasksSetCounter> getIndexedBy(TaskChar indexer) {
		return this.singleTaskIndexer.get(indexer);
	}
	
	public Set<TasksSetCounter> getIndexedByOrInterleave(TaskChar indexer) {
		this.interleave(indexer);
		return this.singleTaskIndexer.get(indexer);
	}
	
	public void storeAndReIndex(TasksSetCounter nuCharactersSetCounter) {
		this.tasksSetCounterCollection.add(nuCharactersSetCounter);
		this.reIndex(nuCharactersSetCounter.getTaskCharSet(), nuCharactersSetCounter);
	}
	
	private boolean interleave(TaskChar indexer) {
		return this.interleave(indexer, false);
	}
	
	private boolean interleave(TaskChar indexer, boolean inheritCountFromExisting) {
		Set<TasksSetCounter> nuCharSetCounters = null;
		
		if (!this.singleTaskIndexer.containsKey(indexer)) {
			nuCharSetCounters = new TreeSet<TasksSetCounter>();
			// Add the singleton
			TasksSetCounter nuCharSetCounter = new TasksSetCounter(indexer);
			Set<TaskChar> nuCharSet = nuCharSetCounter.getTaskCharSet();
			this.addAndReIndex(nuCharSet, nuCharSetCounter);
			
			// Combine indexer with existing character sets
			for(TasksSetCounter existingCharSetCounter : this.tasksSetCounterCollection) {
				if (this.maximumCharactersSetSize == null || existingCharSetCounter.howManyCharactersInSet() < this.maximumCharactersSetSize) {
					nuCharSet = existingCharSetCounter.getTaskCharSet();
					nuCharSet.add(indexer);
					nuCharSetCounter = new TasksSetCounter(nuCharSet);
					if (inheritCountFromExisting)
						nuCharSetCounter.incrementCounter(existingCharSetCounter.getCounter());
					nuCharSetCounters.add(nuCharSetCounter);
				}
			}
			for (TasksSetCounter nuCharSetCounterToAdd : nuCharSetCounters)
				this.addAndReIndex(nuCharSetCounterToAdd.getTaskCharSet(), nuCharSetCounterToAdd);
			
			return true;
		}
		
		return false;
	}
    
    private void mergeAndReindex (TasksSetCounter chSetCounter) {
        if (this.tasksSetCounterCollection.contains(chSetCounter)) {
            TasksSetCounter alreadyExisting = this.tasksSetCounterCollection.tailSet(chSetCounter).first();
            alreadyExisting.incrementCounter(chSetCounter.getCounter());
        } else {
            this.addAndReIndex(chSetCounter.getTaskCharSet(), chSetCounter);
        }
    }
    
    public void merge (VariableCharactersSetFixedCountersCollection other) {
    	other = this.prepareForMerging(other);
        for (TasksSetCounter chSetCounter : other.tasksSetCounterCollection) {
            this.mergeAndReindex(chSetCounter);
        }
    }

	private VariableCharactersSetFixedCountersCollection prepareForMerging(
			VariableCharactersSetFixedCountersCollection other) {
		// Check this' single-character indexers. If some are missing, wrt other, it means that some characters did not appear in the previous traces.
		if (!this.singleTaskIndexer.keySet().containsAll(other.singleTaskIndexer.keySet())) {
			// If some are missing, for each of them you must copy and enlarge the existing sets adding the missing characters, including the counter.
			for (TaskChar otherIndexer : other.singleTaskIndexer.keySet()) {
				this.interleave(otherIndexer, true);
			}
		}
		// The other way round, for other and this
		if (!other.singleTaskIndexer.keySet().containsAll(this.singleTaskIndexer.keySet())) {
			// If some are missing, for each of them you must copy and enlarge the existing sets adding the missing characters, including the counter.
			for (TaskChar thisIndexer : this.singleTaskIndexer.keySet()) {
				other.interleave(thisIndexer, true);
			}
		}
		
		return other;
	}
}