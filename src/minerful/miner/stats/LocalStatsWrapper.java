package minerful.miner.stats;

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

import minerful.miner.stats.xmlenc.FirstOccurrencesMapAdapter;
import minerful.miner.stats.xmlenc.LocalStatsMapAdapter;
import minerful.miner.stats.xmlenc.RepetitionsMapAdapter;

@XmlType
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalStatsWrapper {
	public static final int FIRST_POSITION_IN_STRING = 1;

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
	protected Character baseCharacter;
	@XmlTransient
	protected Integer firstOccurrenceInStep;
	@XmlTransient
	protected SortedSet<Integer> repetitionsAtThisStep;
	@XmlElement
	@XmlJavaTypeAdapter(value = RepetitionsMapAdapter.class)
	public Map<Integer, Integer> repetitions;
	@XmlElement(name = "interplayStats")
	@XmlJavaTypeAdapter(value = LocalStatsMapAdapter.class)
	public Map<Character, StatsCell> localStatsTable;
	@XmlTransient
	protected Map<Character, Integer> neverMoreAppearancesAtThisStep;
	@XmlTransient
	protected Map<Character, AlternatingCounterSwitcher> alternatingCntSwAtThisStep;
	@XmlAttribute
	public int appearancesAsFirst;
	@XmlAttribute
	public int appearancesAsLast;
	@XmlAttribute
	protected long totalAmountOfAppearances;
	@XmlElement
	@XmlJavaTypeAdapter(value = FirstOccurrencesMapAdapter.class)
	public Map<Integer, Integer> firstOccurrences;

	protected LocalStatsWrapper() {
	}

	public LocalStatsWrapper(Character[] alphabet, Character baseCharacter) {
		this();
		this.baseCharacter = baseCharacter;
		this.initLocalStatsTable(alphabet);
		this.repetitions = new TreeMap<Integer, Integer>();
		this.firstOccurrences = new TreeMap<Integer, Integer>();
		this.totalAmountOfAppearances = 0;
		this.appearancesAsFirst = 0;
		this.appearancesAsLast = 0;
	}

	protected void initLocalStatsTable(Character[] alphabet) {
		this.localStatsTable = new TreeMap<Character, StatsCell>();
		this.neverMoreAppearancesAtThisStep = new TreeMap<Character, Integer>();
		this.alternatingCntSwAtThisStep = new TreeMap<Character, AlternatingCounterSwitcher>();
		for (Character letter : alphabet) {
			this.localStatsTable.put(letter, new StatsCell());
			if (!letter.equals(this.baseCharacter)) {
				this.neverMoreAppearancesAtThisStep.put(letter, 0);
				this.alternatingCntSwAtThisStep.put(letter,
						new AlternatingCounterSwitcher());
			}
		}
	}

	void newAtPosition(Character character, int position, boolean onwards) {
		/* if the appeared character is equal to this */
		if (character.equals(this.baseCharacter)) {
			for (Character chr : this.neverMoreAppearancesAtThisStep.keySet()) {
				this.neverMoreAppearancesAtThisStep.put(chr,
						this.neverMoreAppearancesAtThisStep.get(chr) + 1);
			}
			/* if this is the first occurrence in the step, record it */
			if (this.firstOccurrenceInStep == null) {
				this.firstOccurrenceInStep = position;
				if (this.firstOccurrences.containsKey(position))
					this.firstOccurrences.put(position, this.firstOccurrences.get(position) + 1);
				else
					this.firstOccurrences.put(position, 1);
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
			AlternatingCounterSwitcher myAltCountSwitcher = this.alternatingCntSwAtThisStep
					.get(character);
			StatsCell statsCell = this.localStatsTable.get(character);
			/* store the info that chr appears after the pivot */
			this.neverMoreAppearancesAtThisStep.put(character, 0);
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

		// if (baseCharacter.equals("a")) {System.out.print("Seen " + character
		// + " by " + baseCharacter); System.out.println(" " +
		// this.alternatingCntSwInStep.get(character)); }

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
				this.localStatsTable.get(character).newAtDistance(
						position - occurredAlsoAt);
			}
		}
		/*
		 * If this is not the first occurrence position, record the distance
		 * equal to (chr.position - firstOccurrenceInStep.position)
		 */
		if (firstOccurrenceInStep != position) {
			/*
			 * START OF: event-centred analysis modification Comment this line
			 * to get back to previous version
			 */
			if (EVENT_CENTRIC) {
				if (repetitionsAtThisStep == null || repetitionsAtThisStep.size() < 1) {
					this.localStatsTable.get(character).newAtDistance(
						position - firstOccurrenceInStep);
				}
			} else {
				/*
				 * END OF: event-centred analysis modification
				 */
				this.localStatsTable.get(character).newAtDistance(
						position - firstOccurrenceInStep);
			}
		}
		/*
		 * If this is the repetition of the pivot, record it (it is needed for
		 * the computation of all the other distances!)
		 */
		if (this.repetitionsAtThisStep != null
				&& character.equals(this.baseCharacter)) {
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

	protected void setAsNeverAppeared(Character neverAppearedChr) {
		if (!neverAppearedChr.equals(this.baseCharacter)) {
			this.localStatsTable
					.get(neverAppearedChr)
					.setAsNeverAppeared(
							((this.repetitionsAtThisStep == null || this.repetitionsAtThisStep
									.size() < 1) ? 1
									: this.repetitionsAtThisStep.size() + 1));
		}
	}

	protected void setAsNeverAppeared(Set<Character> neverAppearedStuff) {
		for (Character chr : neverAppearedStuff) {
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
		if (this.firstOccurrenceInStep != null) {
			/* Record what did not appear in the step, afterwards or backwards */
			this.recordCharactersThatNeverAppearedAnymoreInStep(onwards);
			/* Does NOTHING, at this stage of the implementation */
			for (StatsCell cell : this.localStatsTable.values()) {
				cell.finalizeAnalysisStep(onwards, secondPass);
			}
			/* Resets the switchers for the alternations counter */
			for (AlternatingCounterSwitcher sw : this.alternatingCntSwAtThisStep
					.values()) {
				sw.reset();
			}
			/* Resets the local stats table counters */
			this.firstOccurrenceInStep = null;
			this.repetitionsAtThisStep = null;
		}
	}

	protected void recordCharactersThatNeverAppearedAnymoreInStep(
			boolean onwards) {
		/* For each character, appeared or not in the step */
		for (Character chrNoMore : this.neverMoreAppearancesAtThisStep.keySet()) {
			/* If it appeared no more */
			if (this.neverMoreAppearancesAtThisStep.get(chrNoMore) > 0) {
				/* Set it appeared no more */
				this.localStatsTable.get(chrNoMore).setAsNeverAppearedAnyMore(
						this.neverMoreAppearancesAtThisStep.get(chrNoMore),
						onwards);
				/* Reset the counter! */
				this.neverMoreAppearancesAtThisStep.put(chrNoMore, 0);
			}
		}
		if (this.firstOccurrenceInStep != null
				&& (this.repetitionsAtThisStep == null || this.repetitionsAtThisStep
						.size() == 0)) {
			this.localStatsTable.get(this.baseCharacter)
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
		if (this.firstOccurrenceInStep != null) {
			/* Record the amount of appearances at this step */
			numberOfRepetitions = this.repetitionsAtThisStep == null ? 1
					: this.repetitionsAtThisStep.size() + 1;
			/*
			 * Increment (if needed) the appearances as this character as the
			 * first
			 */
			if (this.firstOccurrenceInStep == FIRST_POSITION_IN_STRING) {
				this.appearancesAsFirst++;
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
		this.totalAmountOfAppearances += numberOfRepetitions;
	}

	public long getTotalAmountOfAppearances() {
		return this.totalAmountOfAppearances;
	}

	public int getAppearancesAsFirst() {
		return this.appearancesAsFirst;
	}

	public int getAppearancesAsLast() {
		return this.appearancesAsLast;
	}

	@Override
	public String toString() {
		if (this.totalAmountOfAppearances == 0)
			return "";

		StringBuilder sBuf = new StringBuilder();
		for (Character key : this.localStatsTable.keySet()) {
			sBuf.append("\t\t[" + key + "] => "
					+ this.localStatsTable.get(key).toString());
		}
		return sBuf.toString();
	}

}