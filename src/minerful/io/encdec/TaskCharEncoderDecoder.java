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
import minerful.concept.TaskClass;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.logparser.StringTaskClass;

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

	public static final TaskClass[] TEST_TASK_CLASSES = { new StringTaskClass("deliverable"), new StringTaskClass("package"), new StringTaskClass("wp"),
			new StringTaskClass("meeting"), new StringTaskClass("deadline"), new StringTaskClass("task force"), new StringTaskClass("submission"), new StringTaskClass("report"),
			new StringTaskClass("demo"), new StringTaskClass("contribution"), new StringTaskClass("project"), new StringTaskClass("timeline"), new StringTaskClass("presentation"),
			new StringTaskClass("agenda"), new StringTaskClass("timetable"), new StringTaskClass("slide"), new StringTaskClass("integration"), new StringTaskClass("iteration"),
			new StringTaskClass("release"), new StringTaskClass("requirement"), new StringTaskClass("review"), new StringTaskClass("reviewer"), new StringTaskClass("agreement"),
			new StringTaskClass("interaction"), new StringTaskClass("logistics"), new StringTaskClass("payment"), new StringTaskClass("paper"), new StringTaskClass("video"),
			new StringTaskClass("commitment"), new StringTaskClass("draft"), new StringTaskClass("call"), new StringTaskClass("publication"), new StringTaskClass("proposal"),
			new StringTaskClass("document"), new StringTaskClass("invitation"), new StringTaskClass("update"), new StringTaskClass("status"), new StringTaskClass("cost"), new StringTaskClass("step"),
			new StringTaskClass("version"), new StringTaskClass("frame"), new StringTaskClass("introduction"), new StringTaskClass("finance"), new StringTaskClass("management"),
			new StringTaskClass("form"), new StringTaskClass("comment"), new StringTaskClass("strategy"), new StringTaskClass("final"), new StringTaskClass("periodic"), new StringTaskClass("dow"), new StringTaskClass("note"),
			new StringTaskClass("objective"),
			new StringTaskClass("change"),
			new StringTaskClass("showcase"),
			new StringTaskClass("issue"),
			new StringTaskClass("activity"),
			// here I start inventing!
			new StringTaskClass("invention"), new StringTaskClass("innovation"), new StringTaskClass("html"), new StringTaskClass("css"), new StringTaskClass("xhtml"), new StringTaskClass("jquery"), new StringTaskClass("php"),
			new StringTaskClass("java"), new StringTaskClass("c++"), new StringTaskClass("python"), new StringTaskClass("div"), new StringTaskClass("love"), new StringTaskClass("merry"), new StringTaskClass("christmas"),
			new StringTaskClass("brandybuck"), new StringTaskClass("frodo"), new StringTaskClass("rings"), new StringTaskClass("sonic"), new StringTaskClass("eggman"), new StringTaskClass("kukukukchu"),
			new StringTaskClass("failure"), new StringTaskClass("cover"), new StringTaskClass("rehearsal"), new StringTaskClass("circle"), new StringTaskClass("artist"), new StringTaskClass("wallet"),
			new StringTaskClass("steal"), new StringTaskClass("driving"), new StringTaskClass("license"), new StringTaskClass("avis"), new StringTaskClass("trouble"), new StringTaskClass("avignon"),
			new StringTaskClass("birthday"), new StringTaskClass("princess"), new StringTaskClass("castle") };

	public static final String SPACE_REPLACER = "_";

	public static final String WILDCARD_CHAR = " ";

	private TreeMap<TaskClass, Character> tasksDictionary;
	private TreeMap<Character, TaskClass> inverseTasksDictionary;
	private int charCursor, tasksCursor, boundCursor;

	private static Logger logger;

	public TaskCharEncoderDecoder() {
		this.charCursor = 0;
		this.tasksCursor = 0;
		this.boundCursor = 0;

		this.tasksDictionary = new TreeMap<TaskClass, Character>();
		this.inverseTasksDictionary = new TreeMap<Character, TaskClass>();

		if (logger == null) {
			logger = Logger.getLogger(this.getClass().getCanonicalName());
		}
	}
	
	public Map<Character, TaskClass> getTranslationMap() {
		return new HashMap<Character, TaskClass>(this.inverseTasksDictionary);
	}

	public static NavigableMap<Character, TaskClass> getTranslationMap(TaskCharRelatedConstraintsBag bag) {
		NavigableMap<Character, TaskClass> transMap = new TreeMap<Character, TaskClass>();
		for (TaskChar tChr : bag.getTaskChars()) {
			transMap.put(tChr.identifier, tChr.taskClass);
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

	public Character[] encode(TaskClass[] taskClasses) {
		Character[] encodedTasks = new Character[0];
		while (tasksCursor < taskClasses.length && boundCursor < LOWER_BOUNDS.length
				&& boundCursor < UPPER_BOUNDS.length) {
			charCursor = LOWER_BOUNDS[boundCursor];

			for (; tasksCursor < taskClasses.length
					&& charCursor < UPPER_BOUNDS[boundCursor]; charCursor++, tasksCursor++) {
				tasksDictionary.put(taskClasses[tasksCursor],
						Character.valueOf((char) charCursor));
				inverseTasksDictionary.put(
						Character.valueOf((char) charCursor),
						taskClasses[tasksCursor]);
			}

			if (tasksCursor < taskClasses.length) {
				boundCursor++;
			}
		}

		if (tasksCursor < taskClasses.length)
			throw new UnsupportedOperationException("The method was not able"
					+ " to encode the whole collection of tasks");

		return inverseTasksDictionary.keySet().toArray(encodedTasks);
	}

	public Character encode(TaskClass taskClass) {
		if (taskClass == null) {
			logger.error("A task is identified by a NULL value: skipping this task");
			return null;
		} else if (taskClass.toString().length() == 0) {
			logger.warn("A task is identified by an empty string");
		}

		// If the tasks dictionary already contains this task, skip this!
		if (!this.tasksDictionary.containsKey(taskClass)) {
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

			tasksDictionary.put(taskClass, Character.valueOf((char) charCursor));
			inverseTasksDictionary.put(Character.valueOf((char) charCursor),
					taskClass);
			charCursor++;
		}

		return this.tasksDictionary.get(taskClass);
	}
	
	public String[] encode(List<List<TaskClass>> tasksTraces) {
		String[] stringsTracesArray = new String[0];
		List<String> stringTraces = new ArrayList<String>(tasksTraces.size());
		StringBuilder striTraBuilder = new StringBuilder();
		Character c = null;
		
		for (List<TaskClass> tasksTrace : tasksTraces) {
			striTraBuilder.delete(0, striTraBuilder.length());
			for (TaskClass task : tasksTrace) {
				c = this.encode(task);
				striTraBuilder.append(c);
			}
			stringTraces.add(striTraBuilder.toString());
		}
		
		stringsTracesArray = stringTraces.toArray(stringsTracesArray);
		return stringsTracesArray;
	}

	public TaskClass decode(Character encodedTask) {
		return this.inverseTasksDictionary.get(encodedTask);
	}
	
	public static char encodedCharFromString(String encodedCharString) {
		return
				encodedCharString.startsWith("\\u")
				?	(char)(Integer.parseInt(encodedCharString.substring(2), 16))
				:	encodedCharString.charAt(0);
	}

	public Set<TaskClass> getTaskClasses() {
		return this.tasksDictionary.keySet();
	}

	@Override
	public String toString() {
		StringBuffer sBuf = new StringBuffer();
		for (TaskClass taskClass : tasksDictionary.keySet()) {
			sBuf.append(tasksDictionary.get(taskClass));
			sBuf.append(" <= ");
			sBuf.append(taskClass);
			sBuf.append(" (");
			sBuf.append(taskClass);
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
		taChEnDe.encode(TEST_TASK_CLASSES);
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