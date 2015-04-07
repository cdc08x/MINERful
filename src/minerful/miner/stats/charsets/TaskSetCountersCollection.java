package minerful.miner.stats.charsets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import minerful.concept.TaskChar;

public abstract class TaskSetCountersCollection  implements Cloneable {

	protected TreeSet<TasksSetCounter> tasksSetCounterCollection;
	protected Map<TaskChar, TreeSet<TasksSetCounter>> singleTaskIndexer;

	public TaskSetCountersCollection() {
		super();
	}

	public TasksSetCounter incrementAt(Set<TaskChar> stuff) {
		return this.incrementAt(stuff, 1);
	}
	
	protected abstract void reIndex(Set<TaskChar> stuff, TasksSetCounter indexed);
	
	protected boolean addAndReIndex(Set<TaskChar> charsInNuTaskCharsSetCounter, TasksSetCounter nuTaskCharsSetCounter) {
		if (this.tasksSetCounterCollection.add(nuTaskCharsSetCounter)) {
			reIndex(charsInNuTaskCharsSetCounter, nuTaskCharsSetCounter);
			return true;
		}
		return false;
	}
	
	public TreeSet<TasksSetCounter> getTaskCharsSetCounterCollection() {
		return this.tasksSetCounterCollection;
	}

	public TasksSetCounter incrementAt(TaskChar charInNuTaskCharsSetCounter, int sum) {
		Set<TaskChar> charsInNuTaskCharsSetCounter = new TreeSet<TaskChar>();
		charsInNuTaskCharsSetCounter.add(charInNuTaskCharsSetCounter);
		return this.incrementAt(charsInNuTaskCharsSetCounter, sum);
	}

	public TasksSetCounter incrementAt(Set<TaskChar> charsInNuTaskCharsSetCounter, int sum) {
		// This is the haystack
		TasksSetCounter needle = new TasksSetCounter(charsInNuTaskCharsSetCounter);
		int nuCounter = 0;
		
		if (this.tasksSetCounterCollection.contains(needle)) {
			nuCounter = this.tasksSetCounterCollection.floor(needle).incrementCounter(sum);
		} else {
			this.addAndReIndex(charsInNuTaskCharsSetCounter, needle);
			nuCounter = needle.incrementCounter(sum);
		}
		
		return needle;
	}

	public SortedSet<TasksSetCounter> getTaskCharsSetsOrderedByAscendingCounter() {
		SortedSet<TasksSetCounter> nuCharSetCounter =
				new TreeSet<TasksSetCounter>(
						new TasksSetCounter.TaskSetByAscendingCounterComparator()
				);
		
		nuCharSetCounter.addAll(this.tasksSetCounterCollection);
		
		return nuCharSetCounter;
	}

	@Override
	public String toString() {
		StringBuilder sBuil = new StringBuilder();
		sBuil.append('\n');
		sBuil.append(this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1));
		sBuil.append(": {");
		sBuil.append("\n\tList:");
		for (TasksSetCounter chSCnt : this.tasksSetCounterCollection) {
			sBuil.append(chSCnt.toString().replace("\n", "\n\t\t"));
		}
		sBuil.append("\n\tIndexed:");
		for (TaskChar chr : this.singleTaskIndexer.keySet()) {
			sBuil.append("\n\t\tchr=");
			sBuil.append(chr);
			sBuil.append(" => {");
			sBuil.append(this.singleTaskIndexer.get(chr).toString().replace("\n", "\n\t\t\t"));
			sBuil.append("\n\t\t}");
		}
		sBuil.append("\n}");
		return sBuil.toString();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

    public TasksSetCounter get(Collection<TaskChar> indexer) {
		SortedSet<TasksSetCounter> tailSet = this.tasksSetCounterCollection.tailSet(new TasksSetCounter(indexer));
		if (tailSet == null || tailSet.size() == 0)
			return null;
		return tailSet.first();
	}

    public TasksSetCounter getNearest(Collection<TaskChar> indexer) {
    	TasksSetCounter nearest = this.get(indexer);
    	
    	if (nearest != null) {
    		return nearest;
    	} else {
    		Collection<TaskChar> indexedCharsWithinIndexer = new ArrayList<TaskChar>();
    		for (TaskChar singleIndex : indexer) {
    			if (this.singleTaskIndexer.containsKey(singleIndex))
    				indexedCharsWithinIndexer.add(singleIndex);
    		}
    		
    		return this.get(indexedCharsWithinIndexer);
    	}
	}

	public TasksSetCounter get(TaskChar indexer) {
		return this.tasksSetCounterCollection.tailSet(new TasksSetCounter(indexer)).first();
	}

	public SortedSet<TasksSetCounter> getCharactersSetsOrderedByAscendingCounter() {
		// TODO Auto-generated method stub
		return null;
	}

}