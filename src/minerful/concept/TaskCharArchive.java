package minerful.concept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class TaskCharArchive {
	private HashSet<TaskChar> taskChars;
	private HashMap<Character, TaskChar> taskCharsMapById;
	private HashMap<String, TaskChar> taskCharsMapByName;

	public TaskCharArchive() {
		this.taskChars = new HashSet<TaskChar>();
		this.taskCharsMapById = null;
		this.taskCharsMapByName = null;
	}
	
	public TaskCharArchive(Character[] alphabet) {
		Collection<TaskChar> taskCharsCollection = toTaskChars(Arrays.asList(alphabet));
		this.taskChars = new HashSet<TaskChar>(taskCharsCollection);
		this.taskCharsMapById = new HashMap<Character, TaskChar>(this.taskChars.size());
		TreeMap<Character, TaskChar>
			fastTmpMapById = new TreeMap<Character, TaskChar>();
		TreeMap<String, TaskChar>
			fastTmpMapByName = new TreeMap<String, TaskChar>();
		for (TaskChar tChr : taskCharsCollection) {
			fastTmpMapById.put(tChr.identifier, tChr);
			fastTmpMapByName.put(tChr.name, tChr);
		}
		this.taskCharsMapById = new HashMap<Character, TaskChar>(fastTmpMapById);
	}

	public TaskCharArchive(Map<Character, String> roughTaskChars) {
		this.taskChars = new HashSet<TaskChar>();
		TreeMap<Character, TaskChar> fastTmpMap = new TreeMap<Character, TaskChar>();
		for (Character chr : roughTaskChars.keySet()) {
			TaskChar nuTaskChar = new TaskChar(chr, roughTaskChars.get(chr));
			this.taskChars.add(nuTaskChar);
			fastTmpMap.put(chr, nuTaskChar);
		}
		this.taskCharsMapById = new HashMap<Character, TaskChar>(fastTmpMap);
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
	public Map<String, TaskChar> getTranslationMapByName() {
		return new HashMap<String, TaskChar>(this.taskCharsMapByName);
	}

	/**
	 * Returns a shallow copy of the set of TaskChar's.
	 * @return A shallow copy of the set of TaskChar's
	 */
	public TreeSet<TaskChar> getTaskChars() {
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
		return this.getAlphabet().toArray(new Character[this.howManyTaskChars()]);
	}
	public Collection<Character> getAlphabet() {
		return this.taskCharsMapById.keySet();
	}
	
	public TaskChar getTaskChar(Character chr) {
		return this.taskCharsMapById.get(chr);
	}

	public TaskChar getTaskChar(String name) {
		return this.taskCharsMapByName.get(name);
	}

	@Override
	public String toString() {
		return "TaskCharArchive [taskChars=" + taskChars + ", taskCharsMap="
				+ taskCharsMapById + "]";
	}

	public int howManyTaskChars() {
		return this.taskChars.size();
	}
}