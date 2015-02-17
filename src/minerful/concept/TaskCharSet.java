package minerful.concept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskCharSet implements Comparable<TaskCharSet> {
	@XmlElementWrapper(name="taskCharacters")
	private final TaskChar[] taskChars;
	@XmlTransient
	private final String joinedStringOfIdentifiers;
	@XmlTransient
	private final Collection<Character> listOfIdentifiers;
	public static final TaskCharSet VOID_TASK_CHAR_SET =
			new TaskCharSet(new TaskChar[0]);
	
	private TaskCharSet() {
		this.taskChars = null;
		this.joinedStringOfIdentifiers = null;
		this.listOfIdentifiers = null;
	}
	
	private TaskCharSet(TaskChar[] taskChars) {
		if (taskChars.length < 1) {
			this.taskChars = taskChars;
			this.listOfIdentifiers = new ArrayList<Character>(0);
			this.joinedStringOfIdentifiers = "";
		} else {
			this.taskChars = taskChars;
			this.listOfIdentifiers = this.toListOfIdentifiers();
			this.joinedStringOfIdentifiers = this.toJoinedStringOfIdentifiers();
		}
	}
	public TaskCharSet(SortedSet<TaskChar> taskChars) {
		this(taskChars.toArray(new TaskChar[taskChars.size()]));
	}
	public TaskCharSet(List<TaskChar> taskChars) {
		this(new TreeSet<TaskChar>(taskChars));
	}
	public TaskCharSet(
			Collection<TaskChar> taskChars) {
		this(new TreeSet<TaskChar>(taskChars));
	}
	public TaskCharSet(TaskChar taskChar) {
		this.taskChars = new TaskChar[]{taskChar};
		this.listOfIdentifiers = this.toListOfIdentifiers();
		this.joinedStringOfIdentifiers = taskChar.identifier.toString();
	}
	// For cloning purposes
	private TaskCharSet(TaskChar[] taskChars, String joinedStringOfIdentifiers,
			Collection<Character> listOfIdentifiers) {
		this.taskChars = taskChars;
		this.joinedStringOfIdentifiers = joinedStringOfIdentifiers;
		this.listOfIdentifiers = listOfIdentifiers;
	}

	public TaskChar[] getTaskChars() {
		return taskChars;
	}
	
	public TaskChar getTaskChar(int number) {
		if (number < 0 || number > this.taskChars.length - 1)
			throw new IllegalArgumentException("TaskChar #" + number + "not found in set");
		return this.taskChars[number];
	}

	@Override
	public String toString() {
		if (this.size() == 1) {
			return this.taskChars[0].toString();
		}

		StringBuilder builder = new StringBuilder();
		builder.append("{");
		builder.append(StringUtils.join(taskChars, ", "));
		builder.append("}");
		return builder.toString();
	}
	
	@Override
	public int compareTo(TaskCharSet o) {
		return joinedStringOfIdentifiers.compareTo(o.joinedStringOfIdentifiers);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((taskChars == null) ? 0 : taskChars.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskCharSet other = (TaskCharSet) obj;
		if (taskChars == null) {
			if (other.taskChars != null)
				return false;
		} else {
			return this.toJoinedStringOfIdentifiers().equals(other.toJoinedStringOfIdentifiers());
		}
		return true;
	}
	
	private String toJoinedStringOfIdentifiers() {
		StringBuilder sB = new StringBuilder();
		for (TaskChar taskChar : this.getTaskChars()) {
			sB.append(taskChar.identifier);
		}
		return sB.toString();
	}
	
	private Collection<Character> toListOfIdentifiers() {
		ArrayList<Character> listOfIdentifiers = new ArrayList<Character>(this.getTaskChars().length);
		for (TaskChar taskChar : this.getTaskChars()) {
			listOfIdentifiers.add(taskChar.identifier);
		}
		return listOfIdentifiers;
	}

	public Collection<Character> getListOfIdentifiers() {
		return this.listOfIdentifiers;
	}
	
	public String getJoinedStringOfIdentifiers() {
		return joinedStringOfIdentifiers;
	}
	
	public int size() {
		return this.taskChars.length;
	}
	public boolean isSingleton() {
		return this.size() < 2;
	}
	
	public TaskCharSet removeLast() {
		TaskChar[] arrayOfTaskChars = Arrays.copyOf(this.taskChars, this.size() -1);
		return new TaskCharSet(arrayOfTaskChars);
	}
	public TaskChar getLastTaskChar() {
		return this.taskChars[this.taskChars.length-1];
	}
	public TaskCharSet removeFirst() {
		TaskChar[] arrayOfTaskChars = new TaskChar[0];
		if (this.taskChars.length > 1)
			arrayOfTaskChars = Arrays.copyOfRange(this.taskChars, 1, this.size() -1);
		return new TaskCharSet(arrayOfTaskChars);
	}
	public TaskChar getFirstTaskChar() {
		return this.taskChars[0];
	}
	public Collection<TaskChar> getTaskCharsCollection() {
		return Arrays.asList(this.taskChars);
	}
	
	public TaskCharSet pushAtLast(TaskChar taskChar) {
		TaskChar[] arrayOfTaskChars = Arrays.copyOf(this.taskChars, this.size() +1);
		arrayOfTaskChars[this.taskChars.length] = taskChar;
		return new TaskCharSet(arrayOfTaskChars);
	}

	public String toPatternString(boolean positive) {
		if (this.size() == 1)
			return this.taskChars[0].identifier.toString();
		if (positive) {
			return StringUtils.join(this.joinedStringOfIdentifiers, "|");
		} else {
			return "[^" + this.joinedStringOfIdentifiers + "]";
		}
	}

	public boolean isPrefixOf(TaskCharSet other) {
		return StringUtils.startsWith(other.getJoinedStringOfIdentifiers(), this.getJoinedStringOfIdentifiers());
	}
}