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

import minerful.logparser.StringTaskClass;

public class TaskCharArchive {
	private TreeSet<TaskChar> taskChars;
	private HashMap<Character, TaskChar> taskCharsMapById;
	private HashMap<TaskClass, TaskChar> taskCharsMapByClass;

	public TaskCharArchive() {
		this.taskChars = new TreeSet<TaskChar>();
		this.taskCharsMapById = null;
		this.taskCharsMapByClass = null;
	}
	
	public TaskCharArchive(Character[] alphabet) {
		Collection<TaskChar> taskCharsCollection = toTaskChars(Arrays.asList(alphabet));
		this.taskChars = new TreeSet<TaskChar>(taskCharsCollection);
		TreeMap<Character, TaskChar>
			fastTmpMapById = new TreeMap<Character, TaskChar>();
		TreeMap<TaskClass, TaskChar>
			fastTmpMapByName = new TreeMap<TaskClass, TaskChar>();
		for (TaskChar tChr : taskCharsCollection) {
			fastTmpMapById.put(tChr.identifier, tChr);
			fastTmpMapByName.put(tChr.taskClass, tChr);
		}
		this.taskCharsMapById = new HashMap<Character, TaskChar>(fastTmpMapById);
		this.taskCharsMapByClass = new HashMap<TaskClass, TaskChar>(fastTmpMapByName);
	}

	public TaskCharArchive(Map<Character, TaskClass> roughTaskChars) {
		this.taskChars = new TreeSet<TaskChar>();
		TreeMap<Character, TaskChar> fastTmpMapById = new TreeMap<Character, TaskChar>();
		TreeMap<TaskClass, TaskChar> fastTmpMapByName = new TreeMap<TaskClass, TaskChar>();
		for (Character chr : roughTaskChars.keySet()) {
			TaskChar nuTaskChar = new TaskChar(chr, roughTaskChars.get(chr));
			this.taskChars.add(nuTaskChar);
			fastTmpMapById.put(chr, nuTaskChar);
			fastTmpMapByName.put(roughTaskChars.get(chr), nuTaskChar);
		}
		this.taskCharsMapById = new HashMap<Character, TaskChar>(fastTmpMapById);
		this.taskCharsMapByClass = new HashMap<TaskClass, TaskChar>(fastTmpMapByName);
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
	public Map<TaskClass, TaskChar> getTranslationMapByName() {
		return new HashMap<TaskClass, TaskChar>(this.taskCharsMapByClass);
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

	public Collection<TaskChar> getTaskChars(Collection<Character> fromCharacters) {
		Collection<TaskChar> taskChars = new ArrayList<TaskChar>(fromCharacters.size());
		for (Character chr : fromCharacters) {
			taskChars.add(this.getTaskChar(chr));
		}
		return taskChars;
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
		return this.getAlphabet().toArray(new Character[this.size()]);
	}
	public Collection<Character> getAlphabet() {
		return this.taskCharsMapById.keySet();
	}
	
	public TaskChar getTaskChar(Character chr) {
		return this.taskCharsMapById.get(chr);
	}

	public TaskChar getTaskChar(String name) {
		return this.taskCharsMapByClass.get(new StringTaskClass(name));
	}

	public TaskChar getTaskChar(TaskClass taskClass) {
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
			taskCharsSubsets.add(taskCharsSubset);
			for (int i = 0; i < taskCharsPerSubset; i++) {
				taskCharsSubset.add(taChaIte.next());
			}
		}
		// Flush remaining taskChars
		while (taChaIte.hasNext()) {
			taskCharsSubset.add(taChaIte.next());
		}
		
		return taskCharsSubsets;
	}
}