package minerful.io.encdec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.concept.TaskChar;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;

import org.apache.log4j.Logger;

/*
 0030-0039 [numbers]			   		9	
 0041-005A [u.c. basic latin]		+ 25	=  34
 0061-007A [l.c. basic latin]		+ 25	=  59
 00C0-00FF [latin supplement]		+ 63	= 122
 0100-017F [latin extended-a]		+127	= 249
 1E00-1EFF [latin extended-b]		+255	= 504
 2C60-2C7F [latin-extended-c]		+ 31	= 535
 A720-A78E [latin-extended-d-1]		+110	= 645
 A7A0-A7AA [latin-extended-d-2]		+ 10	= 655
 A7F8-A7FF [latin-extended-d-3]		+  7	= 662
 061E-064A [arabic-1]				+ 44	= 706
 0660-066F [arabic-2]				+ 16	= 722
 0671-06D5 [arabic-3]				+100	= 822
 3041-3096 [hiragana]				+ 85	= 907
 30A0-30FF [katakana]				+100	=1007
 16F00-16F44 [miao.. miao??]		+ 68	=1075
 */
public class TaskCharEncoderDecoder {
	public static final char CONTEMPORANEITY_CHARACTER_DELIMITER = '|';
	
	public static final int UNICODE_NUMBERS_LOWER_BOUND = 0x0030;
	public static final int UNICODE_NUMBERS_UPPER_BOUND = 0x0039;
	public static final int UNICODE_BASIC_LATIN_UC_LOWER_BOUND = 0x0041;
	public static final int UNICODE_BASIC_LATIN_UC_UPPER_BOUND = 0x005A;
	public static final int UNICODE_BASIC_LATIN_LC_LOWER_BOUND = 0x0061;
	public static final int UNICODE_BASIC_LATIN_LC_UPPER_BOUND = 0x007A;
	public static final int UNICODE_LATIN_SUPPLEMENT_LOWER_BOUND = 0x00C0;
	public static final int UNICODE_LATIN_SUPPLEMENT_UPPER_BOUND = 0x00FF;
	public static final int UNICODE_EXT_LATIN_A_LOWER_BOUND = 0x0100;
	public static final int UNICODE_EXT_LATIN_A_UPPER_BOUND = 0x017F;
	public static final int UNICODE_EXT_LATIN_B_LOWER_BOUND = 0x1E00;
	public static final int UNICODE_EXT_LATIN_B_UPPER_BOUND = 0x1EFF;
	public static final int UNICODE_EXT_LATIN_C_LOWER_BOUND = 0x2C60;
	public static final int UNICODE_EXT_LATIN_C_UPPER_BOUND = 0x2C7F;
	public static final int UNICODE_EXT_LATIN_D_1_LOWER_BOUND = 0xA720;
	public static final int UNICODE_EXT_LATIN_D_1_UPPER_BOUND = 0xA78E;
	public static final int UNICODE_EXT_LATIN_D_2_LOWER_BOUND = 0xA7A0;
	public static final int UNICODE_EXT_LATIN_D_2_UPPER_BOUND = 0xA7AA;
	public static final int UNICODE_EXT_LATIN_D_3_LOWER_BOUND = 0xA7F8;
	public static final int UNICODE_EXT_LATIN_D_3_UPPER_BOUND = 0xA7FF;
	public static final int UNICODE_ARABIC_1_LOWER_BOUND = 0x061E;
	public static final int UNICODE_ARABIC_1_UPPER_BOUND = 0x064A;
	public static final int UNICODE_ARABIC_2_LOWER_BOUND = 0x0660;
	public static final int UNICODE_ARABIC_2_UPPER_BOUND = 0x066F;	
	public static final int UNICODE_ARABIC_3_LOWER_BOUND = 0x0671;
	public static final int UNICODE_ARABIC_3_UPPER_BOUND = 0x06D5;	
	public static final int UNICODE_ARABIC_4_LOWER_BOUND = 0x0660;
	public static final int UNICODE_ARABIC_4_UPPER_BOUND = 0x066F;	
	public static final int UNICODE_HIRAGANA_LOWER_BOUND = 0x3041;
	public static final int UNICODE_HIRAGANA_UPPER_BOUND = 0x3096;	
	public static final int UNICODE_KATAKANA_LOWER_BOUND = 0x30A0;
	public static final int UNICODE_KATAKANA_UPPER_BOUND = 0x30FF;	
	public static final int UNICODE_MIAO_LOWER_BOUND = 0x16F00;
	public static final int UNICODE_MIAO_UPPER_BOUND = 0x16F44;	

	public static final int[] LOWER_BOUNDS = {
			UNICODE_BASIC_LATIN_UC_LOWER_BOUND,
			UNICODE_BASIC_LATIN_LC_LOWER_BOUND,
			UNICODE_LATIN_SUPPLEMENT_LOWER_BOUND,
			UNICODE_EXT_LATIN_A_LOWER_BOUND,
			UNICODE_EXT_LATIN_B_LOWER_BOUND,
			UNICODE_EXT_LATIN_C_LOWER_BOUND,
			UNICODE_EXT_LATIN_D_1_LOWER_BOUND,
			UNICODE_EXT_LATIN_D_2_LOWER_BOUND,
			UNICODE_EXT_LATIN_D_3_LOWER_BOUND,
			UNICODE_ARABIC_1_LOWER_BOUND,
			UNICODE_ARABIC_2_LOWER_BOUND,
			UNICODE_ARABIC_3_LOWER_BOUND,
			UNICODE_ARABIC_4_LOWER_BOUND,
			UNICODE_HIRAGANA_LOWER_BOUND,
			UNICODE_KATAKANA_LOWER_BOUND,
			UNICODE_NUMBERS_LOWER_BOUND,
//			UNICODE_MIAO_LOWER_BOUND,
			};

	public static final int[] UPPER_BOUNDS = {
			UNICODE_BASIC_LATIN_UC_UPPER_BOUND,
			UNICODE_BASIC_LATIN_LC_UPPER_BOUND,
			UNICODE_LATIN_SUPPLEMENT_UPPER_BOUND,
			UNICODE_EXT_LATIN_A_UPPER_BOUND,
			UNICODE_EXT_LATIN_B_UPPER_BOUND,
			UNICODE_EXT_LATIN_C_UPPER_BOUND,
			UNICODE_EXT_LATIN_D_1_UPPER_BOUND,
			UNICODE_EXT_LATIN_D_2_UPPER_BOUND,
			UNICODE_EXT_LATIN_D_3_UPPER_BOUND,
			UNICODE_ARABIC_1_UPPER_BOUND,
			UNICODE_ARABIC_2_UPPER_BOUND,
			UNICODE_ARABIC_3_UPPER_BOUND,
			UNICODE_ARABIC_4_UPPER_BOUND,
			UNICODE_HIRAGANA_UPPER_BOUND,
			UNICODE_KATAKANA_UPPER_BOUND,
			UNICODE_NUMBERS_UPPER_BOUND,
//			UNICODE_MIAO_UPPER_BOUND,
		};
	
	public static final int encodableTasksNumber() {
		int total = 0;
		for (int i = 0; i < UPPER_BOUNDS.length && i < LOWER_BOUNDS.length; i++) {
			total += UPPER_BOUNDS[i] - LOWER_BOUNDS[i];
		}
		return total;
	}

	public static final String[] TEST_TASKS = { "deliverable", "package", "wp",
			"meeting", "deadline", "task force", "submission", "report",
			"demo", "contribution", "project", "timeline", "presentation",
			"agenda", "timetable", "slide", "integration", "iteration",
			"release", "requirement", "review", "reviewer", "agreement",
			"interaction", "logistics", "payment", "paper", "video",
			"commitment", "draft", "call", "publication", "proposal",
			"document", "invitation", "update", "status", "cost", "step",
			"version", "frame", "introduction", "finance", "management",
			"form", "comment", "strategy", "final", "periodic", "dow", "note",
			"objective",
			"change",
			"showcase",
			"issue",
			"activity",
			// here I start inventing!
			"invention", "innovation", "html", "css", "xhtml", "jquery", "php",
			"java", "c++", "python", "div", "love", "merry", "christmas",
			"brandybuck", "frodo", "rings", "sonic", "eggman", "kukukukchu",
			"failure", "cover", "rehearsal", "circle", "artist", "wallet",
			"steal", "driving", "license", "avis", "trouble", "avignon",
			"birthday", "princess", "castle" };

	public static final String SPACE_REPLACER = "_";

	public static final String WILDCARD_CHAR = " ";

	private TreeMap<String, Character> tasksDictionary;
	private TreeMap<Character, String> inverseTasksDictionary;
	private int charCursor, tasksCursor, boundCursor;

	private static Logger logger;

	public TaskCharEncoderDecoder() {
		this.charCursor = 0;
		this.tasksCursor = 0;
		this.boundCursor = 0;

		this.tasksDictionary = new TreeMap<String, Character>();
		this.inverseTasksDictionary = new TreeMap<Character, String>();

		if (logger == null) {
			logger = Logger.getLogger(this.getClass().getCanonicalName());
		}
	}
	
	public Map<Character, String> getTranslationMap() {
		return new HashMap<Character, String>(this.inverseTasksDictionary);
	}

	public static NavigableMap<Character, String> getTranslationMap(TaskCharRelatedConstraintsBag bag) {
		NavigableMap<Character, String> transMap = new TreeMap<Character, String>();
		for (TaskChar tChr : bag.getTaskChars()) {
			transMap.put(tChr.identifier, tChr.name);
		}
		return transMap;
	}

	@Deprecated
	public static Character[] faultyEncode(String[] tasks) {
		Character[] encodedTasks = new Character[tasks.length];
		int i = 0;
		for (String task : tasks) {
			encodedTasks[i++] = task.charAt(0);
		}
		return encodedTasks;
	}

	public Character[] encode(String[] tasks) {
		Character[] encodedTasks = new Character[0];

		while (tasksCursor < tasks.length && boundCursor < LOWER_BOUNDS.length
				&& boundCursor < UPPER_BOUNDS.length) {
			charCursor = LOWER_BOUNDS[boundCursor];

			for (; tasksCursor < tasks.length
					&& charCursor < UPPER_BOUNDS[boundCursor]; charCursor++, tasksCursor++) {
				tasksDictionary.put(tasks[tasksCursor],
						Character.valueOf((char) charCursor));
				inverseTasksDictionary.put(
						Character.valueOf((char) charCursor),
						tasks[tasksCursor]);
			}

			if (tasksCursor < tasks.length) {
				boundCursor++;
			}
		}

		if (tasksCursor < tasks.length)
			throw new UnsupportedOperationException("The method was not able"
					+ " to encode the whole collection of tasks");

		return inverseTasksDictionary.keySet().toArray(encodedTasks);
	}

	public Character encode(String task) {
		if (task == null) {
			logger.error("A task is identified by a NULL value: skipping this task");
			return null;
		} else if (task.length() == 0) {
			logger.warn("A task is identified by an empty string");
		}

		// If the tasks dictionary already contains this task, skip this!
		if (!this.tasksDictionary.containsKey(task)) {
			// If the bound was not reached for the current translation group,
			// skip this!
			if (charCursor >= UPPER_BOUNDS[boundCursor]) {
				// If we have no more translation groups left, we're in a
				// trouble!
				if (	boundCursor < LOWER_BOUNDS.length -1
					&&	boundCursor < UPPER_BOUNDS.length -1) {
					boundCursor++;
					charCursor = LOWER_BOUNDS[boundCursor];
				} else {
					throw new UnsupportedOperationException(
							"The method was not able " +
									"to encode the whole collection of tasks. " +
									"Currently, only " +
									encodableTasksNumber() +
									" can be encoded.");
				}
			} else if (charCursor == 0) {
				charCursor = LOWER_BOUNDS[boundCursor];
			}

			tasksDictionary.put(task, Character.valueOf((char) charCursor));
			inverseTasksDictionary.put(Character.valueOf((char) charCursor),
					task);
			charCursor++;
		}

		return this.tasksDictionary.get(task);
	}
	
	public String[] encode(List<List<String>> tasksTraces) {
		String[] stringsTracesArray = new String[0];
		List<String> stringTraces = new ArrayList<String>(tasksTraces.size());
		StringBuilder striTraBuilder = new StringBuilder();
		Character c = null;
		
		for (List<String> tasksTrace : tasksTraces) {
			striTraBuilder.delete(0, striTraBuilder.length());
			for (String task : tasksTrace) {
				c = this.encode(task);
				striTraBuilder.append(c);
			}
			stringTraces.add(striTraBuilder.toString());
		}
		
		stringsTracesArray = stringTraces.toArray(stringsTracesArray);
		return stringsTracesArray;
	}

	public String decode(Character encodedTask) {
		return this.decode(encodedTask, true);
	}

	public String decode(Character encodedTask, boolean removeNonWordCharacters) {
		if (!removeNonWordCharacters)
			return this.inverseTasksDictionary.get(encodedTask);
		else {
			return replaceNonWordCharacters(
					this.inverseTasksDictionary.get(encodedTask)
					);
		}
	}
	
	public static char encodedCharFromString(String encodedCharString) {
		return
				encodedCharString.startsWith("\\u")
				?	(char)(Integer.parseInt(encodedCharString.substring(2), 16))
				:	encodedCharString.charAt(0);
	}

	public Set<String> getTasks() {
		return this.getTasks(true);
	}

	public Set<String> getTasks(boolean removeSpaces) {
		Set<String> tasks = this.tasksDictionary.keySet();
		if (!removeSpaces)
			return tasks;

		Set<String> tasksWoSpaces = new TreeSet<String>();
		for (String task : tasks) {
			tasksWoSpaces.add(replaceNonWordCharacters(task));
		}
		return tasksWoSpaces;
	}

	@Override
	public String toString() {
		StringBuffer sBuf = new StringBuffer();
		for (String task : tasksDictionary.keySet()) {
			sBuf.append(tasksDictionary.get(task));
			sBuf.append(" <= ");
			sBuf.append(task);
			sBuf.append(" (");
			sBuf.append(replaceNonWordCharacters(task));
			sBuf.append(")\n");
		}
		return sBuf.toString();
	}
	
	public Character[] encodedTasks() {
		return this.inverseTasksDictionary.keySet().toArray(new Character[0]);
	}
	
	public String[] decodedTasks() {
		return this.tasksDictionary.keySet().toArray(new String[0]);
	}
	
	public static void main(String[] args) {
		TaskCharEncoderDecoder taChEnDe = new TaskCharEncoderDecoder();
		taChEnDe.encode(TEST_TASKS);
		logger.debug(taChEnDe);
	}

	public static String replaceNonWordCharacters(String originalString) {
		return originalString.replaceAll("\\W", "_");
	}

	public void excludeThese(Collection<String> activitiesToExcludeFromResult) {
		if (activitiesToExcludeFromResult != null) {
			for (String activityToExclude : activitiesToExcludeFromResult) {
				this.removeFromTranslationMap(activityToExclude);
			}
		}
	}

	private void removeFromTranslationMap(String activityToExclude) {
		Character charToRemove = null;
		activityToExclude = XesDecoder.cleanEvtIdentifierTransitionStatus(activityToExclude);
System.err.println("Ma stramannaggia il porco di un maiale. Voglio che togli QUESTO: " + activityToExclude);
		for (String key : this.tasksDictionary.keySet().toArray(new String[0])) {
			if (XesDecoder.matchesEvtIdentifierWithTransitionStatus(key, activityToExclude)) {
				charToRemove = tasksDictionary.remove(key);
				inverseTasksDictionary.remove(charToRemove);
			}
		}


/*
		if (this.tasksDictionary.containsKey(activityToExclude)) {
			charToRemove = tasksDictionary.remove(activityToExclude);
			inverseTasksDictionary.remove(charToRemove);
		} else {
			logger.warn("A non-existing activity was requested to be removed from the alphabet: " + activityToExclude);
System.err.println("A non-existing activity was requested to be removed from the alphabet: " + activityToExclude);
		}
 */	
	}
}