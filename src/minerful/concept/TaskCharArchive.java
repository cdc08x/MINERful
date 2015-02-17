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
	private HashMap<Character, TaskChar> taskCharsMap;

	public TaskCharArchive() {
		this.taskChars = new HashSet<TaskChar>();
		this.taskCharsMap = null;
	}
	
	public TaskCharArchive(Character[] alphabet) {
		Collection<TaskChar> taskCharsCollection = toTaskChars(Arrays.asList(alphabet));
		this.taskChars = new HashSet<TaskChar>(taskCharsCollection);
		this.taskCharsMap = new HashMap<Character, TaskChar>(this.taskChars.size());
		TreeMap<Character, TaskChar> fastTmpMap = new TreeMap<Character, TaskChar>();
		for (TaskChar tChr : taskCharsCollection) {
			fastTmpMap.put(tChr.identifier, tChr);
		}
		this.taskCharsMap = new HashMap<Character, TaskChar>(fastTmpMap);
	}

	public TaskCharArchive(Map<Character, String> roughTaskChars) {
		this.taskChars = new HashSet<TaskChar>();
		TreeMap<Character, TaskChar> fastTmpMap = new TreeMap<Character, TaskChar>();
		for (Character chr : roughTaskChars.keySet()) {
			TaskChar nuTaskChar = new TaskChar(chr, roughTaskChars.get(chr));
			this.taskChars.add(nuTaskChar);
			fastTmpMap.put(chr, nuTaskChar);
		}
		this.taskCharsMap = new HashMap<Character, TaskChar>(fastTmpMap);
	}
	
	public boolean isTranslationMapDefined() {
		return (this.taskCharsMap != null);
	}

	/**
	 * Returns a shallow copy of the translation map.
	 * @return A shallow copy of the translation map
	 */
	public Map<Character, TaskChar> getTranslationMap() {
		return new HashMap<Character, TaskChar>(this.taskCharsMap);
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
		return this.taskCharsMap.keySet();
	}
	
	public TaskChar getTaskChar(Character chr) {
		return this.taskCharsMap.get(chr);
	}

	@Override
	public String toString() {
		return "TaskCharArchive [taskChars=" + taskChars + ", taskCharsMap="
				+ taskCharsMap + "]";
	}

	public int howManyTaskChars() {
		return this.taskChars.size();
	}
}