package minerful.concept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.StringTaskClass;

@XmlRootElement(name="processAlphabet")
@XmlAccessorType(XmlAccessType.FIELD)
/**
 * An archive of the tasks of a process. For each task, an identifying character is given, as per {@link minerful.concept.TaskChar TaskChar}
 * @author Claudio Di Ciccio
 */
public class TaskCharArchive {
	@XmlElementWrapper(name="tasks")
	@XmlElement(name="task")
	private TreeSet<TaskChar> taskChars;
	@XmlTransient
	private HashMap<Character, TaskChar> taskCharsMapById;
	@XmlTransient
	private HashMap<AbstractTaskClass, TaskChar> taskCharsMapByClass;

	public TaskCharArchive() {
		this.taskChars = new TreeSet<TaskChar>();
		this.taskCharsMapById = null;
		this.taskCharsMapByClass = null;
	}
	
	public void computeIndices() {
		TreeMap<Character, TaskChar>
			fastTmpMapById = new TreeMap<Character, TaskChar>();
		TreeMap<AbstractTaskClass, TaskChar>
			fastTmpMapByName = new TreeMap<AbstractTaskClass, TaskChar>();
		for (TaskChar tChr : this.taskChars) {
			fastTmpMapById.put(tChr.identifier, tChr);
			fastTmpMapByName.put(tChr.taskClass, tChr);
		}
		this.taskCharsMapById = new HashMap<Character, TaskChar>(fastTmpMapById);
		this.taskCharsMapByClass = new HashMap<AbstractTaskClass, TaskChar>(fastTmpMapByName);
	}
	
	public TaskCharArchive(Character[] alphabet) {
		Collection<TaskChar> taskCharsCollection = toTaskChars(Arrays.asList(alphabet));
		this.taskChars = new TreeSet<TaskChar>(taskCharsCollection);
		this.computeIndices();
	}

	public TaskCharArchive(TaskChar... taskChars) {
		this.taskChars = new TreeSet<TaskChar>(Arrays.asList(taskChars));
		this.computeIndices();
	}

	public TaskCharArchive(Collection<TaskChar> taskChars) {
		this.taskChars = new TreeSet<TaskChar>(taskChars);
		this.computeIndices();
	}

	public TaskCharArchive(Map<Character, AbstractTaskClass> roughTaskChars) {
		this.taskChars = new TreeSet<TaskChar>();
		TreeMap<Character, TaskChar> fastTmpMapById = new TreeMap<Character, TaskChar>();
		TreeMap<AbstractTaskClass, TaskChar> fastTmpMapByName = new TreeMap<AbstractTaskClass, TaskChar>();
		for (Character chr : roughTaskChars.keySet()) {
			TaskChar nuTaskChar = new TaskChar(chr, roughTaskChars.get(chr));
			this.taskChars.add(nuTaskChar);
			fastTmpMapById.put(chr, nuTaskChar);
			fastTmpMapByName.put(roughTaskChars.get(chr), nuTaskChar);
		}
		this.taskCharsMapById = new HashMap<Character, TaskChar>(fastTmpMapById);
		this.taskCharsMapByClass = new HashMap<AbstractTaskClass, TaskChar>(fastTmpMapByName);
	}
	
	public boolean isTranslationMapDefined() {
		return (this.taskCharsMapById != null);
	}

	/**
	 * Returns a shallow copy of the translation map by identifier.
	 * @return A shallow copy of the translation map by identifier
	 */
	public Map<Character, TaskChar> getTranslationMapById() {
		return new HashMap<Character, TaskChar>(this.taskCharsMapById);
	}

	/**
	 * Returns a shallow copy of the translation map by task name.
	 * @return A shallow copy of the translation map by task name
	 */
	public Map<AbstractTaskClass, TaskChar> getTranslationMapByName() {
		return new HashMap<AbstractTaskClass, TaskChar>(this.taskCharsMapByClass);
	}

	public TreeSet<TaskChar> getTaskChars() {
		return this.taskChars;
	}

	/**
	 * Returns a shallow copy of the set of TaskChar's.
	 * @return A shallow copy of the set of TaskChar's
	 */
	public TreeSet<TaskChar> getCopyOfTaskChars() {
		return new TreeSet<TaskChar>(this.taskChars);
	}

	public Collection<TaskChar> getTaskCharsIdentifiedByCharacters(Collection<Character> fromCharacters) {
		Collection<TaskChar> taskChars = new ArrayList<TaskChar>(fromCharacters.size());
		for (Character chr : fromCharacters) {
			taskChars.add(this.getTaskChar(chr));
		}
		return taskChars;
	}

	public Collection<TaskChar> getTaskCharsIdentifiedByTaskClasses(Collection<AbstractTaskClass> fromtTaskClasses) {
		Collection<TaskChar> taskChars = new ArrayList<TaskChar>(fromtTaskClasses.size());
		for (AbstractTaskClass chr : fromtTaskClasses) {
			taskChars.add(this.getTaskChar(chr));
		}
		return taskChars;
	}

	public Collection<TaskChar> getTaskCharsIdentifiedByStrings(Collection<String> taskNames) {
		Collection<TaskChar> taskChars = new ArrayList<TaskChar>(taskNames.size());
		for (String taskName : taskNames) {
			taskChars.add(this.getTaskChar(taskName));
		}
		return taskChars;
	}
	
	public void removeAllByClass(Collection<AbstractTaskClass> taskClassesToExclude) {
		Collection<TaskChar> taskCharsToExclude = new ArrayList<TaskChar>(taskClassesToExclude.size()); 
		for (AbstractTaskClass taskClassToExclude : taskClassesToExclude) {
			taskCharsToExclude.add(this.getTaskChar(taskClassToExclude));
		}
		this.taskChars.removeAll(taskCharsToExclude);
		this.computeIndices();
	}
	
	public static Collection<TaskChar> toTaskChars(Collection<Character> characters) {
		Collection<TaskChar> taskChars = new ArrayList<TaskChar>(characters.size());
		for (Character chr: characters) {
			taskChars.add(new TaskChar(chr));
		}
		return taskChars;
	}
	
	public static Collection<Character> toCharacters(Collection<TaskChar> taskChars) {
		Collection<Character> characters = new ArrayList<Character>(taskChars.size());
		for (TaskChar tChr: taskChars) {
			characters.add(tChr.identifier);
		}
		return characters;
	}

	public Character[] getAlphabetArray() {
		return this.getIdentifiersAlphabet().toArray(new Character[this.size()]);
	}
	public Collection<Character> getIdentifiersAlphabet() {
		return this.taskCharsMapById.keySet();
	}
	
	public TaskChar getTaskChar(Character chr) {
		return this.taskCharsMapById.get(chr);
	}

	public TaskChar getTaskChar(String name) {
		return this.taskCharsMapByClass.get(new StringTaskClass(name));
	}

	public TaskChar getTaskChar(AbstractTaskClass taskClass) {
		return this.taskCharsMapByClass.get(taskClass);
	}

	public boolean containsTaskCharByEvent(Event event) {
		return this.taskCharsMapByClass.containsKey(event.taskClass);
	}
	
	public TaskChar getTaskCharByEvent(Event event) {
		return this.taskCharsMapByClass.get(event.taskClass);
	}

	@Override
	public String toString() {
		return "TaskCharArchive [taskChars=" + taskChars + ", taskCharsMap="
				+ taskCharsMapById + "]";
	}

	public int size() {
		return this.taskChars.size();
	}

	public Collection<Set<TaskChar>> splitTaskCharsIntoSubsets(Integer parts) {
		if (parts <= 0)
			throw new IllegalArgumentException("The log cannot be split in " + parts + " parts. Only positive integer values are allowed");
		int taskCharsPerSubset = this.taskChars.size() / parts;
		
		Collection<Set<TaskChar>> taskCharsSubsets = new ArrayList<Set<TaskChar>>(parts);

		Set<TaskChar> taskCharsSubset = null;
		Iterator<TaskChar> taChaIte = this.taskChars.iterator();

		for (int j = 0; j < parts; j++) {
			taskCharsSubset = new TreeSet<TaskChar>();
			for (int i = 0; i < taskCharsPerSubset; i++) {
				taskCharsSubset.add(taChaIte.next());
			}
			taskCharsSubsets.add(taskCharsSubset);
		}
		// Flush remaining taskChars
		while (taChaIte.hasNext()) {
			taskCharsSubset.add(taChaIte.next());
		}
		
		return taskCharsSubsets;
	}
	
	private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		this.computeIndices();
	}
}