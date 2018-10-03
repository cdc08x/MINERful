package minerful.miner.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;

import minerful.concept.Event;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.miner.stats.xmlenc.LocalStatsMapAdapter;
import minerful.miner.stats.xmlenc.RepetitionsMapAdapter;

@XmlType
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalStatsWrapper {
	@XmlTransient
	public static final int FIRST_POSITION_IN_TRACE = 1;
	
	@XmlTransient
	protected static Logger logger = Logger.getLogger(LocalStatsWrapper.class);

	// TODO Do not consider this a constant, but rather a user-definable
	// parameter
	/*
	 * It affects the perspective of the distances computing: either
	 * event-centric (i.e., every new occurrence of A lets distances calculation
	 * restart for A w.r.t. the remaining characters) or not (all distances are
	 * summed up)
	 */
	public static final boolean EVENT_CENTRIC = false;

	@XmlTransient
	protected class AlternatingCounterSwitcher {
		public boolean alternating = false;
		public Integer counter = 0;
		public Integer alternationsCounter = 0;

		public AlternatingCounterSwitcher() {
		}

		public AlternatingCounterSwitcher(Integer counter) {
			this();
			this.counter = counter;
		}

		public void reset() {
			this.alternating = false;
			this.counter = 0;
			this.alternationsCounter = 0;
		}

		public int flush() {
			int counter = this.counter;
			if (alternating) {
				this.alternating = false;
				this.alternationsCounter++;
				this.counter = 0;
				return counter;
			}
			return 0;
		}

		public void charge() {
			if (!this.alternating) {
				this.alternating = true;
			} else {
				this.counter++;
			}
		}

		@Override
		public String toString() {
			return "AlternatingCounterSwitcher{" + "alternating="
					+ this.alternating + ", counter=" + this.counter
					+ ", altrn's-counter=" + this.alternationsCounter + '}';
		}
	}

	@XmlTransient
	protected TaskChar baseTask;
	@XmlTransient
	protected TaskCharArchive archive;
	@XmlTransient
	protected Integer firstOccurrenceAtThisStep;
	@XmlTransient
	protected SortedSet<Integer> repetitionsAtThisStep;
	@XmlElement
	@XmlJavaTypeAdapter(value = RepetitionsMapAdapter.class)
	public Map<Integer, Integer> repetitions;
	@XmlElement(name = "interplayStats")
	@XmlJavaTypeAdapter(value = LocalStatsMapAdapter.class)
	public Map<TaskChar, StatsCell> interplayStatsTable;
	@XmlTransient
	protected Map<TaskChar, Integer> neverMoreAppearancesAtThisStep;
	@XmlTransient
	protected Map<TaskChar, AlternatingCounterSwitcher> alternatingCntSwAtThisStep;
	@XmlAttribute
	public int occurencesAsFirst;
	@XmlAttribute
	public int occurrencesAsLast;
	@XmlAttribute
	protected long totalAmountOfOccurrences;

	protected LocalStatsWrapper() {
	}

	public LocalStatsWrapper(TaskCharArchive archive, TaskChar baseTask) {
		this();
		this.baseTask = baseTask;
		this.archive = archive;
		this.initLocalStatsTable(archive.getTaskChars());
		this.repetitions = new TreeMap<Integer, Integer>();
		this.totalAmountOfOccurrences = 0;
		this.occurencesAsFirst = 0;
		this.occurrencesAsLast = 0;
	}

	protected void initLocalStatsTable(Set<TaskChar> alphabet) {
		this.interplayStatsTable = new HashMap<TaskChar, StatsCell>(alphabet.size(), (float)1.0);
		this.neverMoreAppearancesAtThisStep = new HashMap<TaskChar, Integer>(alphabet.size(), (float)1.0);
		this.alternatingCntSwAtThisStep = new HashMap<TaskChar, AlternatingCounterSwitcher>(alphabet.size(), (float)1.0);
		for (TaskChar task : alphabet) {
			this.interplayStatsTable.put(task, new StatsCell());
			if (!task.equals(this.baseTask)) {
				this.neverMoreAppearancesAtThisStep.put(task, 0);
				this.alternatingCntSwAtThisStep.put(task,
						new AlternatingCounterSwitcher());
			}
		}
	}

	void newAtPosition(Event event, int position, boolean onwards) {
		if (this.archive.containsTaskCharByEvent(event)) {
			TaskChar tCh = this.archive.getTaskCharByEvent(event);
			/* if the appeared character is equal to this */
			if (tCh.equals(this.baseTask)) {
				for (TaskChar otherTCh : this.neverMoreAppearancesAtThisStep.keySet()) {
					this.neverMoreAppearancesAtThisStep.put(otherTCh,
							this.neverMoreAppearancesAtThisStep.get(otherTCh) + 1);
				}
				/* if this is the first occurrence in the step, record it */
				if (this.firstOccurrenceAtThisStep == null) {
					this.firstOccurrenceAtThisStep = position;
				} else {
					/*
					 * if this is not the first time this chr appears in the step,
					 * initialize the repetitions register
					 */
					if (repetitionsAtThisStep == null) {
						repetitionsAtThisStep = new TreeSet<Integer>();
					}
				}
				
				/*
				 * record the alternation, i.e., the repetition of the chr itself
				 * between its first appearance and the following different
				 * character
				 */
				for (AlternatingCounterSwitcher sw : this.alternatingCntSwAtThisStep
						.values()) {
					sw.charge();
				}
			}
			/* if the appeared character is NOT equal to this */
			else {
				AlternatingCounterSwitcher myAltCountSwitcher = this.alternatingCntSwAtThisStep.get(tCh);
				StatsCell statsCell = this.interplayStatsTable.get(tCh);
				/* store the info that chr appears after the pivot */
				this.neverMoreAppearancesAtThisStep.put(tCh, 0);
				/* is this reading analysis onwards? */
				if (onwards) {// onwards?
					/* If there has been an alternation, record it! */
					// TODO In the next future
					// if (myAltCountSwitcher.alternating)
					// statsCell.alternatedOnwards++;
					/*
					 * Record the repetitions in-between (reading the string from
					 * left to right, i.e., onwards) and restart the counter
					 */
					statsCell.betweenOnwards += myAltCountSwitcher.flush();
				} else {
					/* If there has been an alternation, record it! */
					// TODO In the next future
					// if (myAltCountSwitcher.alternating)
					// statsCell.alternatedBackwards++;
					/*
					 * otherwise, record the repetitions in-between (reading the
					 * string from left to right, i.e., backwards) and restart the
					 * counter
					 */
					statsCell.betweenBackwards += myAltCountSwitcher.flush();
				}
			}
	
			if (repetitionsAtThisStep != null) {
				/*
				 * for each repetition of the same character during the analysis,
				 * record not only the info of the appearance at a distance equal to
				 * (chr.position - firstOccurrenceInStep.position), but also at the
				 * (chr.position - otherOccurrenceInStep.position) for each other
				 * appearance of the pivot!
				 */
				/* THIS IS THE VERY BIG TRICK TO AVOID ANY TRANSITIVE CLOSURE!! */
				for (Integer occurredAlsoAt : repetitionsAtThisStep) {
					this.interplayStatsTable.get(tCh).newAtDistance(position - occurredAlsoAt);
				}
			}
			/*
			 * If this is not the first occurrence position, record the distance
			 * equal to (chr.position - firstOccurrenceInStep.position)
			 */
			if (firstOccurrenceAtThisStep != position) {
				/*
				 * START OF: event-centred analysis modification Comment this line
				 * to get back to previous version
				 */
				if (EVENT_CENTRIC) {
					if (repetitionsAtThisStep == null || repetitionsAtThisStep.size() < 1) {
						this.interplayStatsTable.get(tCh).newAtDistance(
							position - firstOccurrenceAtThisStep);
					}
				} else {
					/*
					 * END OF: event-centred analysis modification
					 */
					this.interplayStatsTable.get(tCh).newAtDistance(
							position - firstOccurrenceAtThisStep);
				}
			}
			/*
			 * If this is the repetition of the pivot, record it (it is needed for
			 * the computation of all the other distances!)
			 */
			if (this.repetitionsAtThisStep != null
					&& tCh.equals(this.baseTask)) {
				/*
				 * START OF: event-centred analysis modification Comment these lines
				 * to get back to previous version
				 */
				if (EVENT_CENTRIC) {
					this.repetitionsAtThisStep.clear();
				}
				/*
				 * END OF: event-centred analysis modification
				 */
				this.repetitionsAtThisStep.add(position);
			}
	
			// if (baseCharacter.equals("f")) {System.out.print("Seen " + character
			// + " by "); System.out.print(baseCharacter + "\t"); for (String chr:
			// neverMoreAppearancesInStep.keySet()) System.out.print(", " + chr +
			// ": " + neverMoreAppearancesInStep.get(chr)); System.out.print("\n");}
		}
	}

	protected void setAsNeverAppeared(TaskChar neverAppearedTask) {
		if (!neverAppearedTask.equals(this.baseTask)) {
			this.interplayStatsTable
					.get(neverAppearedTask)
					.setAsNeverAppeared(
							((this.repetitionsAtThisStep == null || this.repetitionsAtThisStep
									.size() < 1) ? 1
									: this.repetitionsAtThisStep.size() + 1));
		}
	}

	protected void setAsNeverAppeared(Set<TaskChar> neverAppearedStuff) {
		for (TaskChar chr : neverAppearedStuff) {
			this.setAsNeverAppeared(chr);
		}
	}

	void finalizeAnalysisStep(boolean onwards, boolean secondPass) {
		/*
		 * Record the amount of occurrences AT THIS STEP and the total amount
		 * OVER ALL OF THE STEPS
		 */
		if (!secondPass) {
			this.updateAppearancesCounter();
		}
		if (this.firstOccurrenceAtThisStep != null) {
			/* Record what did not appear in the step, afterwards or backwards */
			this.recordCharactersThatNeverAppearedAnymoreInStep(onwards);
			/* Does NOTHING, at this stage of the implementation */
			for (StatsCell cell : this.interplayStatsTable.values()) {
				cell.finalizeAnalysisStep(onwards, secondPass);
			}
			/* Resets the switchers for the alternations counter */
			for (AlternatingCounterSwitcher sw : this.alternatingCntSwAtThisStep.values()) {
				sw.reset();
			}
			/* Resets the local stats table counters */
			this.firstOccurrenceAtThisStep = null;
			this.repetitionsAtThisStep = null;
		}
	}

	protected void recordCharactersThatNeverAppearedAnymoreInStep(
			boolean onwards) {
		/* For each character, appeared or not in the step */
		for (TaskChar tChNoMore : this.neverMoreAppearancesAtThisStep.keySet()) {
			/* If it appeared no more */
			if (this.neverMoreAppearancesAtThisStep.get(tChNoMore) > 0) {
				/* Set it appeared no more */
				this.interplayStatsTable.get(tChNoMore).setAsNeverAppearedAnyMore(
						this.neverMoreAppearancesAtThisStep.get(tChNoMore),
						onwards);
				/* Reset the counter! */
				this.neverMoreAppearancesAtThisStep.put(tChNoMore, 0);
			}
		}
		if (this.firstOccurrenceAtThisStep != null
				&& (this.repetitionsAtThisStep == null || this.repetitionsAtThisStep
						.size() == 0)) {
			this.interplayStatsTable.get(this.baseTask)
					.setAsNeverAppearedAnyMore(1, onwards);
		}
	}

	/**
	 * Increments (if needed) the appearances as this character as the first,
	 * records the amount of occurrences AT THIS STEP and increments the total
	 * amount OVER ALL OF THE STEPS
	 */
	protected void updateAppearancesCounter() {
		Integer numberOfRepetitions = 0;
		if (this.firstOccurrenceAtThisStep != null) {
			/* Record the amount of appearances at this step */
			numberOfRepetitions = this.repetitionsAtThisStep == null ? 1
					: this.repetitionsAtThisStep.size() + 1;
			/*
			 * Increment (if needed) the appearances as this character as the
			 * first
			 */
			if (this.firstOccurrenceAtThisStep == FIRST_POSITION_IN_TRACE) {
				this.occurencesAsFirst++;
			}
		}
		/*
		 * Increment the amount of appearances counter with data gathered at
		 * this step
		 */
		Integer oldNumberOfRepetitionsInFrequencyTable = this.repetitions
				.get(numberOfRepetitions);
		this.repetitions.put(numberOfRepetitions,
				oldNumberOfRepetitionsInFrequencyTable == null ? 1
						: 1 + oldNumberOfRepetitionsInFrequencyTable);
		/* Increment the total amount of appearances */
		this.totalAmountOfOccurrences += numberOfRepetitions;
	}

	public long getTotalAmountOfOccurrences() {
		return this.totalAmountOfOccurrences;
	}

	public int getAppearancesAsFirst() {
		return this.occurencesAsFirst;
	}

	public int getAppearancesAsLast() {
		return this.occurrencesAsLast;
	}

	@Override
	public String toString() {
		if (this.totalAmountOfOccurrences == 0)
			return "";

		StringBuilder sBuf = new StringBuilder();
		for (TaskChar key : this.interplayStatsTable.keySet()) {
			sBuf.append("\t\t[" + key + "] => "
					+ this.interplayStatsTable.get(key).toString());
		}
		return sBuf.toString();
	}

	public void mergeAdditively(LocalStatsWrapper other) {
		this.occurencesAsFirst += other.occurencesAsFirst;
		this.occurrencesAsLast += other.occurrencesAsLast;
		this.totalAmountOfOccurrences += other.totalAmountOfOccurrences;
		
		for (Integer numOfReps : this.repetitions.keySet()) {
			if (other.repetitions.containsKey(numOfReps)) {
				this.repetitions.put(numOfReps, this.repetitions.get(numOfReps) + other.repetitions.get(numOfReps));
			}
		}
		
		for (Integer numOfReps : other.repetitions.keySet()) {
			if (!this.repetitions.containsKey(numOfReps)) {
				this.repetitions.put(numOfReps, other.repetitions.get(numOfReps));
			}
		}
		
		for (TaskChar key : this.interplayStatsTable.keySet()) {
			if (other.interplayStatsTable.containsKey(key)) {
				this.interplayStatsTable.get(key).mergeAdditively(other.interplayStatsTable.get(key));
			}
		}
		
		for (TaskChar key : other.interplayStatsTable.keySet()) {
			if (!this.interplayStatsTable.containsKey(key)) {
				this.interplayStatsTable.put(key, other.interplayStatsTable.get(key));
			}
		}
/*		
		for (Integer firstOcc : this.firstOccurrences.keySet()) {
			if (other.firstOccurrences.containsKey(firstOcc)) {
				this.firstOccurrences.put(firstOcc, this.firstOccurrences.get(firstOcc) + other.firstOccurrences.get(firstOcc));
			}
		}
		for (Integer firstOcc : other.firstOccurrences.keySet()) {
			if (!this.firstOccurrences.containsKey(firstOcc)) {
				this.firstOccurrences.put(firstOcc, other.firstOccurrences.get(firstOcc));
			}
		}
 */
	}

	public void mergeSubtractively(LocalStatsWrapper other) {
		this.occurencesAsFirst -= other.occurencesAsFirst;
		this.occurrencesAsLast -= other.occurrencesAsLast;
		this.totalAmountOfOccurrences -= other.totalAmountOfOccurrences;
		
		for (Integer numOfReps : this.repetitions.keySet()) {
			if (other.repetitions.containsKey(numOfReps)) {
				this.repetitions.put(numOfReps, this.repetitions.get(numOfReps) - other.repetitions.get(numOfReps));
			}
		}
		
		for (Integer numOfReps : other.repetitions.keySet()) {
			if (!this.repetitions.containsKey(numOfReps)) {
				logger.warn("Trying to merge subtractively a number of repetitions that were not included for " + numOfReps);
			}
		}
		
		for (TaskChar key : this.interplayStatsTable.keySet()) {
			if (other.interplayStatsTable.containsKey(key)) {
				this.interplayStatsTable.get(key).mergeSubtractively(other.interplayStatsTable.get(key));
			}
		}
		
		for (TaskChar key : other.interplayStatsTable.keySet()) {
			if (!this.interplayStatsTable.containsKey(key)) {
				logger.warn("Trying to merge subtractively interplay stats that were not included for " + key);
			}
		}
	}
}