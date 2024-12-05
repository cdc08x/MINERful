package minerful.miner.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import minerful.concept.Event;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;


public class LocalStatsWrapper {
	public static final int FIRST_POSITION_IN_TRACE = 1;
	
	protected static Logger logger = Logger.getLogger(LocalStatsWrapper.class);

	// TODO Do not consider this a constant, but rather a user-definable
	// parameter
	/*
	 * It affects the perspective of the distances computing: either
	 * event-centric (i.e., every new occurrence of A lets distances calculation
	 * restart for A w.r.t. the remaining characters) or not (all distances are
	 * summed up)
	 */
	public static final boolean EVENT_CENTRIC_DISTANCE = false;

	protected class AlternatingCounterSwitcher {
		public boolean alternating = false;
		public boolean repetitionInBetweenObserved = false;
		public Integer inBetweenRepetitionsCounter = 0;
		public Integer alternationsCounter = 0;

		public AlternatingCounterSwitcher() {
		}

		public AlternatingCounterSwitcher(Integer counter) {
			this();
			this.inBetweenRepetitionsCounter = counter;
			if (this.inBetweenRepetitionsCounter > 0)
				this.repetitionInBetweenObserved = true;
		}

		/**
		 * Called at the end of the parsing of a trace.
		 * Returns <code>true</code> if alternations with repetitions were observed and to be reported in the trace and resets all counters.
		 */
		public boolean reset() {
			// Notice that if the last task observed was not followed by the alternating one 
			// (e.g., a vs b in xxxaaxbxaxxx)
			// then we should not report the repetition as the last issue seen is a missing alternation
			boolean repetitionsToReport = this.repetitionInBetweenObserved && !this.alternating;
			this.alternating = false;
			this.repetitionInBetweenObserved = false;
			this.inBetweenRepetitionsCounter = 0;
			this.alternationsCounter = 0;
			return repetitionsToReport;
		}

		/**
		 * Invoked when the alternating task is met.
		 * Returns the number of alternations that have occurred and resets its {@link AlternatingCounterSwitcher#inBetweenRepetitionsCounter counter}.
		 * It also updates the {@link AlternatingCounterSwitcher#alternationsCounter alternationsCounter}.
		 * Finally, it sets {@link AlternatingCounterSwitcher#repetitionInBetweenObserved repetitionInBetweenObserved} if repetitions in-between were observed.
		 * @return The number of alternations counted thus far.
		 */
		public int flush() {
			int counter = this.inBetweenRepetitionsCounter;
			if (alternating) {
				this.alternating = false;
				this.alternationsCounter++;
				if (this.inBetweenRepetitionsCounter > 0)
					this.repetitionInBetweenObserved = true;
				this.inBetweenRepetitionsCounter = 0;
				return counter;
			}
			return 0;
		}

		public void charge() {
			if (!this.alternating) {
				this.alternating = true;
			} else {
				this.inBetweenRepetitionsCounter++;
			}
		}

		@Override
		public String toString() {
			return "AlternatingCounterSwitcher{" + "alternating="
					+ this.alternating + ", counter=" + this.inBetweenRepetitionsCounter
					+ ", altrn's-counter=" + this.alternationsCounter + ", rep.observed=" + this.repetitionInBetweenObserved + '}';
		}
	}


	protected TaskChar baseTask;
	protected TaskCharArchive archive;
	protected Integer firstOccurrenceAtThisStep;

	protected SortedSet<Integer> repetitionsAtThisStep;
	public Map<Integer, Integer> repetitions;
	public Map<TaskChar, StatsCell> interplayStatsTable;
	protected Map<TaskChar, Integer> neverMoreCooccurrencesAtThisStep;
	protected Map<TaskChar, Boolean> atLeastOneCooccurrenceInThisTrace;
	protected Map<TaskChar, Integer> adjacentOccurrencesInThisTrace;
	protected Map<TaskChar, Integer> otherTasks;
	protected Map<TaskChar, Boolean> tasksBeforeTheFirst;
	

	protected Map<TaskChar, AlternatingCounterSwitcher> alternatingCntSwAtThisStep;
	public int occurencesAsFirst;
	public int occurrencesAsLast;
	protected long totalAmountOfOccurrences;
	protected long totalAmountOfTracesWithOccurrence;

	protected LocalStatsWrapper() {
	}

	public LocalStatsWrapper(TaskCharArchive archive, TaskChar baseTask) {
		this();
		this.baseTask = baseTask;
		this.archive = archive;
		this.initLocalStatsTable(archive.getTaskChars());
		this.repetitions = new TreeMap<Integer, Integer>();
		this.totalAmountOfOccurrences = 0;
		this.totalAmountOfTracesWithOccurrence = 0;
		this.occurencesAsFirst = 0;
		this.occurrencesAsLast = 0;
	}

	protected void initLocalStatsTable(Set<TaskChar> alphabet) {
		this.interplayStatsTable = new HashMap<TaskChar, StatsCell>(alphabet.size(), (float)1.0);
		this.neverMoreCooccurrencesAtThisStep = new HashMap<TaskChar, Integer>(alphabet.size(), (float)1.0);
		this.atLeastOneCooccurrenceInThisTrace = new HashMap<TaskChar, Boolean>(alphabet.size(), (float)1.0);
		this.adjacentOccurrencesInThisTrace = new HashMap<TaskChar, Integer>(alphabet.size(), (float)1.0);
		this.otherTasks = new HashMap<TaskChar, Integer>(alphabet.size(), (float)1.0);
		this.tasksBeforeTheFirst = new HashMap<TaskChar, Boolean>(alphabet.size(), (float)1.0);
		this.alternatingCntSwAtThisStep = new HashMap<TaskChar, AlternatingCounterSwitcher>(alphabet.size(), (float)1.0);
		for (TaskChar task : alphabet) {
			this.interplayStatsTable.put(task, new StatsCell());
			this.adjacentOccurrencesInThisTrace.put(task, 0);
			
			if (!task.equals(this.baseTask)) {
				this.otherTasks.put(task, 0);
				this.neverMoreCooccurrencesAtThisStep.put(task, 0);
				this.atLeastOneCooccurrenceInThisTrace.put(task, false);
				this.tasksBeforeTheFirst.put(task,false);
				this.alternatingCntSwAtThisStep.put(task,
						new AlternatingCounterSwitcher());
			}
		}
	}

	void newAtPosition(Event event, int position, boolean onwards, SortedSet<TaskChar> beforethefirst) {
		
		if (this.archive.containsTaskCharByEvent(event)) {
			TaskChar tCh = this.archive.getTaskCharByEvent(event);
			/* if the occurred character is equal to this */
			if (tCh.equals(this.baseTask)) {
				for (TaskChar otherTCh : this.neverMoreCooccurrencesAtThisStep.keySet()) {
					this.neverMoreCooccurrencesAtThisStep.put(otherTCh,
							this.neverMoreCooccurrencesAtThisStep.get(otherTCh) + 1);
				}
				/* if this is the first occurrence in the step, record it */
				if (this.firstOccurrenceAtThisStep == null) {
					this.firstOccurrenceAtThisStep = position;
					/*record each task presenti in beforthefirst according with the current first occurrence*/
					for (TaskChar before : beforethefirst){
							this.tasksBeforeTheFirst.put(before, true);
						}
				} else {
					/*
					 * if this is not the first time this the task occurs in the step,
					 * initialise the repetitions register
					 */
					if (repetitionsAtThisStep == null) {
						repetitionsAtThisStep = new TreeSet<Integer>();
					}
					/* This passage is a bit redundant as, e.g., NotResponse(a,a) is the same as AtMostOne(a). And NotPrecedence(a,a) is equivalent to both. */
					this.atLeastOneCooccurrenceInThisTrace.put(tCh, true);
				}
				
				/*
				 * record the alternation, i.e., the repetition of the task itself
				 * between its first occurrence and the following different
				 * task
				 */
				for (AlternatingCounterSwitcher sw : this.alternatingCntSwAtThisStep.values()) {
					sw.charge();
				}
			}
			/* if the occurred character is NOT equal to this */
			else {
				/*counter of occurrences for each task different from the current one*/
				this.otherTasks.put(tCh, this.otherTasks.get(tCh) + 1);
				AlternatingCounterSwitcher myAltCountSwitcher = this.alternatingCntSwAtThisStep.get(tCh);
				StatsCell statsCell = this.interplayStatsTable.get(tCh);
				/* store the info that tCh occurs after the pivot */
				this.neverMoreCooccurrencesAtThisStep.put(tCh, 0);
				/* store that in this trace there has been at least an occorrence of tCh after the pivot */
				this.atLeastOneCooccurrenceInThisTrace.put(tCh, true);
				
				
				
				/* is this reading analysis onwards? */
				if (onwards) {// onwards?
					/* If there has been an alternation with an in-between repetition, record it! */
					// TODO In the next future
					// if (myAltCountSwitcher.alternating)
					// statsCell.alternatedOnwards++;
					/*
					 * Record the repetitions in-between (reading the string from
					 * left to right, i.e., onwards) and restart the counter
					 */
					statsCell.inBetweenRepsOnwards += myAltCountSwitcher.flush();
				} else {
					/* If there has been an alternation with an in-between repetition, record it! */
					// TODO In the next future
					// if (myAltCountSwitcher.alternating)
					// statsCell.alternatedBackwards++;
					/*
					 * otherwise, record the repetitions in-between (reading the
					 * string from left to right, i.e., backwards) and restart the
					 * counter
					 */
					
					statsCell.inBetweenRepsBackwards += myAltCountSwitcher.flush();
					
				}
			}

			if (repetitionsAtThisStep != null) {
				/*
				 * for each repetition of the same character during the analysis,
				 * record not only the info of the occurrence at a distance equal to
				 * (chr.position - firstOccurrenceInStep.position), but also at the
				 * (chr.position - otherOccurrenceInStep.position) for every other
				 * occurrence of the pivot!
				 */
				/* THIS IS THE TRICK TO AVOID ANY TRANSITIVE CLOSURE effect!! */
				for (Integer occurredAlsoAt : repetitionsAtThisStep) {
					this.interplayStatsTable.get(tCh).newAtDistance(position - occurredAlsoAt);
					/* 
						If some task B occurs at distance 1, and we are impersoanting task A here, 
						then all other tasks X violate ChainResponse(A,X).
						This is to be recorded for trace-based measure computing:
						one violation in a trace is enough to consider the whole trace as violating the constraint.
						If some task B occurs at distance 1,
						then all other tasks X violate ChainPrecedence(X,A).
					*/
				
					if (Math.abs(position - occurredAlsoAt) == 1) {
						this.adjacentOccurrencesInThisTrace.put(tCh, this.adjacentOccurrencesInThisTrace.get(tCh) + 1);
					}
				}
			}
			
			/* Adjust the counter for adjacences to the first occurrence */
			if  (Math.abs(position - firstOccurrenceAtThisStep) == 1) {
				this.adjacentOccurrencesInThisTrace.put(tCh, this.adjacentOccurrencesInThisTrace.get(tCh) + 1);
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
				if (EVENT_CENTRIC_DISTANCE) {
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
				if (EVENT_CENTRIC_DISTANCE) {
					this.repetitionsAtThisStep.clear();
				}
				/*
				 * END OF: event-centred analysis modification
				 */
				this.repetitionsAtThisStep.add(position);
			}
	
			// if (baseCharacter.equals("f")) {System.out.print("Seen " + character
			// + " by "); System.out.print(baseCharacter + "\t"); for (String chr:
			// neverMoreOccurrencesInStep.keySet()) System.out.print(", " + chr +
			// ": " + neverMoreOccurrencesInStep.get(chr)); System.out.print("\n");}
		}
	}
	

	/**
	 * Records that task <code>neverOccurredTask</code> did not co-occur in this trace.
	 * @param neverOccurredTask The task that did not co-occur in the trace.
	 */
	protected void setAsNeverCooccurred(TaskChar neverOccurredTask) {
		if (!neverOccurredTask.equals(this.baseTask)) {
			this.interplayStatsTable
					.get(neverOccurredTask)
					.setAsNeverCooccurred(
							((this.repetitionsAtThisStep == null || this.repetitionsAtThisStep.size() < 1) ?
									1
									: this.repetitionsAtThisStep.size() + 1));
		}
	}

	protected void setAsNeverCooccurred(Set<TaskChar> neverCooccurredTasks) {
		for (TaskChar chr : neverCooccurredTasks) {
			this.setAsNeverCooccurred(chr);
		}
	}

	void finalizeAnalysisStep(boolean onwards, boolean secondPass) {
		/*
		 * Record the amount of occurrences AT THIS STEP and the total amount
		 * OVER ALL OF THE STEPS
		 */
	
		if (!secondPass) {
			this.updateOccurrencesCounters();
		}
		AlternatingCounterSwitcher sw = null;
		StatsCell cell = null;
		if (this.firstOccurrenceAtThisStep != null) {
			/* Record the alternate tasks that success in the trace*/
			this.recordAlternateSuccessionTasks(onwards);
			/* Record the adjacent tasks that success in the trace*/
			this.recordAdjacentSuccessionTasks(onwards);
			/* Record the tasks that success in the trace*/
			this.recordSuccessionTasks();

			/* Record what did not occur in the step, afterwards or backwards */
			this.recordTasksThatDidntCooccurAnymoreInStep(onwards);
			/* Record the tasks that co-occurred in the trace, afterwards or backwards */
			this.recordTasksThatCooccurredInTrace(onwards);
			/* Record the adjacent tasks, afterwards or backwards */
			this.recordAdjacentTasks(onwards);
			
			/* Stores that in this trace at least a repetition occurred (in case), then 
			 * resets the switchers for the alternations counter */
			for (TaskChar taskChar : this.alternatingCntSwAtThisStep.keySet()) {
				sw = this.alternatingCntSwAtThisStep.get(taskChar);
				cell = this.interplayStatsTable.get(taskChar);
				if (onwards) {
					cell.tracesWithInBetweenRepsOnwards += (sw.reset() ? 1 : 0);
				} else {
					cell.tracesWithInBetweenRepsBackwards += (sw.reset() ? 1 : 0);
				}
			}
			/* Updates the trace counts for recorded distances */
			for (StatsCell finaliCell : this.interplayStatsTable.values()) {
				finaliCell.finalizeAnalysisStep(onwards, secondPass);
			}
			/* Resets the local stats-table counters */
			
			this.firstOccurrenceAtThisStep = null;
			this.repetitionsAtThisStep = null;
			
		}
	}

	protected void recordAdjacentTasks(boolean onwards) {
		int totalAmountOfOccurrences = 0;
		if (this.repetitionsAtThisStep == null) { // In case of no repetitions in this trace beyond the first
			if (this.firstOccurrenceAtThisStep == null) { // In case of no occurrences at all, skip this
				return;
			} else { // Otherwise leave the default value (one) for totalAmountOfOccurrences
				totalAmountOfOccurrences = 1;
			}
		} else {
			totalAmountOfOccurrences = this.repetitionsAtThisStep.size() + 1;
		}
		/* For each character, adjacent or not in the trace */
		for (TaskChar otherTCh : this.adjacentOccurrencesInThisTrace.keySet()) {
			/* If it was adjacent as many times as this task occurred in the trace */
			if (this.adjacentOccurrencesInThisTrace.get(otherTCh) == totalAmountOfOccurrences) {
				/* Set it occurred at least once */
				this.interplayStatsTable.get(otherTCh).setAsAdjacent(onwards);
			}
			/* Reset the counter! */
			this.adjacentOccurrencesInThisTrace.put(otherTCh, 0);
		}
		
	}

	protected void recordSuccessionTasks() {
		/* For each character, preceding or not the first occurrence of the activation in the trace */
		for (TaskChar othertCh : this.tasksBeforeTheFirst.keySet()) {
			/*If the char is before the first activation*/
			if (this.tasksBeforeTheFirst.get(othertCh)) {
				/*If there is at least one occurrence of the target after the last activation*/
				if (this.neverMoreCooccurrencesAtThisStep.get(othertCh)>0){
					this.interplayStatsTable.get(othertCh).setAsSuccession();
				}
				/*Reset the counter*/
				this.tasksBeforeTheFirst.put(othertCh, false);

			}

		}
	}

	protected void recordAdjacentSuccessionTasks(boolean onwards) {
		
		int totalAmountOfOccurrences = 0;
		if (this.repetitionsAtThisStep == null) { // In case of no repetitions in this trace beyond the first
			if (this.firstOccurrenceAtThisStep == null) { // In case of no occurrences at all, skip this
				return;
			} else { // Otherwise leave the default value (one) for totalAmountOfOccurrences
				totalAmountOfOccurrences = 1;
			}
		} else {
			totalAmountOfOccurrences = this.repetitionsAtThisStep.size() + 1;
		}
		
		/* For each character, preceding or not the first occurrence of the activation in the trace */
		for (TaskChar othertCh : this.tasksBeforeTheFirst.keySet()) {
			/*If the char is not before the first activation && all the occurrences are adjacent && target and activation have the same number of occurrences*/
			if (!this.tasksBeforeTheFirst.get(othertCh) && this.adjacentOccurrencesInThisTrace.get(othertCh) == totalAmountOfOccurrences && this.otherTasks.get(othertCh)== totalAmountOfOccurrences){
				this.interplayStatsTable.get(othertCh).setAsAdjacentSuccession(onwards);		
			}
			/*Reset the counter*/
			this.otherTasks.put(othertCh, 0);
			
		}
	}

	protected void recordAlternateSuccessionTasks(boolean onwards) {
		
		int totalAmountOfOccurrences = 0;
		if (this.repetitionsAtThisStep == null) { // In case of no repetitions in this trace beyond the first
			if (this.firstOccurrenceAtThisStep == null) { // In case of no occurrences at all, skip this
				return;
			} else { // Otherwise leave the default value (one) for totalAmountOfOccurrences
				totalAmountOfOccurrences = 1;
			}
		} else {
			totalAmountOfOccurrences = this.repetitionsAtThisStep.size() + 1;
		}
		/* For each character, preceding or not the first occurrence of the activation in the trace */
		for (TaskChar othertCh : this.tasksBeforeTheFirst.keySet()) {
			/*If the char is not before the first activation && target and activation have the same number of occurrences*/
			if (!this.tasksBeforeTheFirst.get(othertCh) && (this.otherTasks.get(othertCh)== totalAmountOfOccurrences)) {
				/*If there are no occurrences of the target after the last activation*/
				if (!(this.neverMoreCooccurrencesAtThisStep.get(othertCh)>0)){
					this.interplayStatsTable.get(othertCh).setAsAlternateSuccession(onwards);
				}
			
			}

		}
	}


	protected void recordTasksThatCooccurredInTrace(boolean onwards) {
		/* For each character, occurred or not in the step */
		for (TaskChar tChOnce : this.atLeastOneCooccurrenceInThisTrace.keySet()) {
			/* If it occurred at least once */
			if (this.atLeastOneCooccurrenceInThisTrace.get(tChOnce)) {
				/* Set it occurred at least once */
				this.interplayStatsTable.get(tChOnce).setAsCooccurredInTrace(onwards);
				/* Reset the counter! */
				this.atLeastOneCooccurrenceInThisTrace.put(tChOnce, false);
			}
		}
	}

	protected void recordTasksThatDidntCooccurAnymoreInStep(boolean onwards) {
		/* For each character, occurred or not in the step */
		for (TaskChar tChNoMore : this.neverMoreCooccurrencesAtThisStep.keySet()) {
			/* If it occurred no more */
			if (this.neverMoreCooccurrencesAtThisStep.get(tChNoMore) > 0) {
				/* Set it occurred no more… */
				this.interplayStatsTable.get(tChNoMore).setAsNeverCooccurredAnyMore(
						this.neverMoreCooccurrencesAtThisStep.get(tChNoMore),
						onwards);
				/* Reset the counter! */
				this.neverMoreCooccurrencesAtThisStep.put(tChNoMore, 0);
			}
		}
		/* Count the number of cases in which the base task occurred only once (i.e., it did not repeat) */ 
		if (this.firstOccurrenceAtThisStep != null
				&& (this.repetitionsAtThisStep == null || this.repetitionsAtThisStep.size() == 0)) {
			this.interplayStatsTable.get(this.baseTask)
					.setAsNeverCooccurredAnyMore(1, onwards);
		}
	}

	/**
	 * Increments (if needed) the occurrences as this character as the first,
	 * records the amount of occurrences AT THIS STEP and increments the total
	 * amount OVER ALL OF THE STEPS
	 */
	protected int updateOccurrencesCounters() {
		Integer numberOfRepetitions = 0;
		if (this.firstOccurrenceAtThisStep != null) {
			/* Record the amount of occurrences at this step */
			numberOfRepetitions = this.repetitionsAtThisStep == null ? 1
					: this.repetitionsAtThisStep.size() + 1;
			/*
			 * Increment (if needed) the occurrences of this character as the
			 * first
			 */
			if (this.firstOccurrenceAtThisStep == FIRST_POSITION_IN_TRACE) {
				this.occurencesAsFirst++;
			}
		}
		/*
		 * Increment the amount of occurrences counter with data gathered at
		 * this step
		 */
		Integer oldNumberOfRepetitionsInFrequencyTable = this.repetitions
				.get(numberOfRepetitions);
		this.repetitions.put(numberOfRepetitions,
				oldNumberOfRepetitionsInFrequencyTable == null ? 1
						: 1 + oldNumberOfRepetitionsInFrequencyTable);
		/* Increment the total amount of occurrences */
		this.totalAmountOfOccurrences += numberOfRepetitions;
		/* Increment the total amount of traces having at least an occurrence of this task */
		this.totalAmountOfTracesWithOccurrence += ((numberOfRepetitions > 0) ? 1 : 0);

		return numberOfRepetitions;
	}

	public long getTotalAmountOfOccurrences() {
		return this.totalAmountOfOccurrences;
	}

	public long getTotalAmountOfTracesWithOccurrence() {
		return this.totalAmountOfTracesWithOccurrence;
	}

	public int getOccurrencesAsFirst() {
		return this.occurencesAsFirst;
	}

	public int getOccurrencesAsLast() {
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
		this.totalAmountOfTracesWithOccurrence += other.totalAmountOfTracesWithOccurrence;
		
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
		this.totalAmountOfTracesWithOccurrence -= other.totalAmountOfTracesWithOccurrence;

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