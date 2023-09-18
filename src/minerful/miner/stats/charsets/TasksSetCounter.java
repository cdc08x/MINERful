package minerful.miner.stats.charsets;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.TaskChar;

public class TasksSetCounter implements Comparable<TasksSetCounter>, Cloneable {

	public static class TaskSetByAscendingCounterComparator implements Comparator<TasksSetCounter> {
		@Override
		public int compare(TasksSetCounter o1, TasksSetCounter o2) {
			int result = Integer.valueOf(o1.counter).compareTo(Integer.valueOf(o2.counter));
			return (
					( result == 0 )
					?	o1.compareTo(o2)
					:	result
			);
		}
	}

	private final String charactersSetString;
	private final TreeSet<TaskChar> taskCharSet;
	private int counter;
	
	public TasksSetCounter(TaskChar task) {
		this.charactersSetString = String.valueOf(task);
		this.taskCharSet = new TreeSet<TaskChar>();
		this.taskCharSet.add(task);
		this.counter = 0;
	}

	public TasksSetCounter(Collection<TaskChar> charactersSet) {
		String charsImplosion = createCharSetString(charactersSet);
		this.charactersSetString = charsImplosion;
		this.taskCharSet = new TreeSet<TaskChar>(charactersSet);
		this.counter = 0;
	}

	private String createCharSetString(Collection<TaskChar> charactersSet) {
		StringBuilder sBuil = new StringBuilder(charactersSet.size());
		for (TaskChar tCh : charactersSet)
			sBuil.append(tCh.identifier);
		return sBuil.toString();
	}
	
	private TasksSetCounter(Collection<TaskChar> taskCharCollection, String charactersSetString, int counter) {
		this.counter = counter;
		this.taskCharSet = new TreeSet<TaskChar>(taskCharCollection);
		this.charactersSetString = createCharSetString(taskCharCollection);
	}

	public TasksSetCounter(Collection<TaskChar> charactersSet, TaskChar characterOnMore) {
		charactersSet.add(characterOnMore);
		String charsImplosion = createCharSetString(charactersSet);
		this.charactersSetString = charsImplosion;
		this.taskCharSet = new TreeSet<TaskChar>(charactersSet);
		this.counter = 0;
	}

	public Set<TaskChar> getTaskCharSet() {
		return this.taskCharSet;
	}

	public Set<TaskChar> getCopyOfCharactersSet() {
		return (Set<TaskChar>)(this.taskCharSet.clone());
	}
	
	public String getCharactersSetString() {
		return charactersSetString;
	}

	public int getCounter() {
		return counter;
	}
	
	public int incrementCounter() {
		return this.incrementCounter(1);
	}
	
	public int incrementCounter(int sum) {
		this.counter += sum;
		return counter;
	}
    
    public int howManyCharactersInSet() {
        return this.taskCharSet.size();
    }
    public boolean isSingleton() {
        return howManyCharactersInSet() == 1;
    }

	@Override
	public int compareTo(TasksSetCounter other) {
		return this.charactersSetString.compareTo(other.charactersSetString);
	}

	@Override
	public boolean equals(Object other) {
		return this.charactersSetString.equals(
				((TasksSetCounter)other).getCharactersSetString()
			);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new TasksSetCounter(taskCharSet, this.charactersSetString, this.counter);
	}

	@Override
	public String toString() {
		StringBuilder sBuil = new StringBuilder();
		sBuil.append('\n');
		sBuil.append(this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1));
		sBuil.append(": {");
		sBuil.append("charactersSetString=");
		sBuil.append(this.charactersSetString);
		sBuil.append(" => ");
		sBuil.append("counter=");
		sBuil.append(this.counter);
		sBuil.append("}");		
		return sBuil.toString();
	}
}