package minerful.concept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;

import minerful.io.encdec.nusmv.NuSMVEncoder;
import minerful.io.encdec.xml.CharAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskCharSet implements Comparable<TaskCharSet> {
	@XmlElement(name="task")
	private final TaskChar[] taskChars;
	@XmlAttribute
	@XmlID
	private String joinedStringOfIdentifiers;
	@XmlAttribute
	@XmlJavaTypeAdapter(value=CharAdapter.class)
	private Collection<Character> listOfIdentifiers;
	public static final TaskCharSet VOID_TASK_CHAR_SET =
			new TaskCharSet(new TaskChar[0]);
	
	private TaskCharSet() {
		this.taskChars = null;
		this.joinedStringOfIdentifiers = null;
		this.listOfIdentifiers = null;
	}
	
	public TaskCharSet(TaskChar... taskChars) {
		if (taskChars.length < 1) {
			this.taskChars = taskChars;
			this.listOfIdentifiers = new ArrayList<Character>(0);
			this.joinedStringOfIdentifiers = "";
		} else {
			this.taskChars = taskChars;
			this.refreshListOfIdentifiers();
		}
	}
	
	/**
	 * Creates a new instance of TaskCharSet as the join of the input taskCharSets.
	 * If a TaskChar occurs in more than one input TaskCharSet, it will not be recur in the constructed TaskCharSet instance.
	 * @param taskCharSets Instances of TaskCharSet objects
	 */
	public TaskCharSet(TaskCharSet... taskCharSets) {
		if (taskCharSets.length < 1) {
			this.taskChars = new TaskChar[]{};
			this.listOfIdentifiers = new ArrayList<Character>(0);
			this.joinedStringOfIdentifiers = "";
		} else {
			Set<TaskChar> taChaSet = new TreeSet<TaskChar>();
			taChaSet.addAll(taskCharSets[0].getTaskCharsList());
			for (int i = 1; i < taskCharSets.length; i++) {
				taChaSet.addAll(taskCharSets[i].getTaskCharsList());
			}
			this.taskChars = taChaSet.toArray(new TaskChar[taChaSet.size()]);
			this.refreshListOfIdentifiers();
		}
	}

	public TaskCharSet(SortedSet<TaskChar> taskChars) {
		this(taskChars.toArray(new TaskChar[taskChars.size()]));
	}
	public TaskCharSet(List<TaskChar> taskChars) {
		this(new TreeSet<TaskChar>(taskChars));
	}
	public TaskCharSet(Collection<TaskChar> taskChars) {
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
	
	public void refreshListOfIdentifiers() {
		this.listOfIdentifiers = this.toListOfIdentifiers();
		this.joinedStringOfIdentifiers = this.toJoinedStringOfIdentifiers();
	}

	public TaskChar[] getTaskCharsArray() {
		return taskChars;
	}

	public List<TaskChar> getTaskCharsList() {
		return Arrays.asList(taskChars);
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
		for (TaskChar taskChar : this.getTaskCharsArray()) {
			sB.append(taskChar.identifier);
		}
		return sB.toString();
	}
	
	private Collection<Character> toListOfIdentifiers() {
		ArrayList<Character> listOfIdentifiers = new ArrayList<Character>(this.getTaskCharsArray().length);
		for (TaskChar taskChar : this.getTaskCharsArray()) {
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
	public Set<TaskChar> getSetOfTaskChars() {
		return new TreeSet<TaskChar>(Arrays.asList(this.taskChars));
	}
	
	public TaskCharSet pushAtLast(TaskChar taskChar) {
		TaskChar[] arrayOfTaskChars = Arrays.copyOf(this.taskChars, this.size() +1);
		arrayOfTaskChars[this.taskChars.length] = taskChar;
		return new TaskCharSet(arrayOfTaskChars);
	}

	public String toPatternString() {
		return this.toPatternString(true);
	}

	public String toPatternString(boolean positive) {
		if (this.size() == 1)
			return this.taskChars[0].identifier.toString();
		if (positive) {
			return this.joinedStringOfIdentifiers;
		} else { // FIXME Cannot work, really
			return "^" + this.joinedStringOfIdentifiers;
		}
	}

	public String toLTLpfString() {
		if (this.size() == 1)
			return this.taskChars[0].getTaskNumericId();
		String disjunctionOfLiterals = "";
		for (int i = 0; i < this.taskChars.length; i++) {
			disjunctionOfLiterals = disjunctionOfLiterals.concat(NuSMVEncoder.OR).concat(taskChars[i].getTaskNumericId());
		}
		return disjunctionOfLiterals.substring(1);	
	}

	public boolean isPrefixOf(TaskCharSet other) {
		return StringUtils.startsWith(other.getJoinedStringOfIdentifiers(), this.getJoinedStringOfIdentifiers());
	}

	public boolean strictlyIncludes(TaskCharSet other) {
		return
			this.listOfIdentifiers.size() > other.listOfIdentifiers.size()
			&&
			this.listOfIdentifiers.containsAll(other.listOfIdentifiers);
	}
}