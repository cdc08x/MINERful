/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner.stats;

import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

//import minerful.miner.stats.xmlenc.DistancesMapAdapter;

/**
 * Wraps the information about co-occurrences with <em>other</em> tasks,
 * considering a pivot is given. Its internal business logic is kept to a
 * minimum. Updates and values to store are for the largest part computed in
 * {@link LocalStatsWrapper #LocalStatsWrapper}.
 */
public class StatsCell implements Cloneable {
    public static final int NEVER_ONWARDS = Integer.MAX_VALUE;
    public static final int NEVER_BACKWARDS = Integer.MIN_VALUE;
    public static final int NEVER_EVER = 0;
    
    protected static Logger logger = Logger.getLogger(StatsCell.class);
    
    public NavigableMap<Integer, Integer> distances;
    public int inBetweenRepsOnwards;
    public int inBetweenRepsBackwards;
 
    // The following five attributes cover the trace-based analysis.
    // The amounts are derived only during the finalisation step,
    // as they add 1 to their counters rather than the number of occurrences
    // of the pivot task they relate to. Indeed, they count frequencies from traces,
    // not events.
    public NavigableMap<Integer, Integer> distancesPerTrace;
    public int tracesWithInBetweenRepsOnwards;
    public int tracesWithInBetweenRepsBackwards;
    public int tracesWithCooccurrenceOnwards;
    public int tracesWithCooccurrenceBackwards;
    public int tracesWithSuccessorCooccurrences;
    public int tracesWithPredecessorCooccurrences;
    public int tracesWithSuccession;
    public int tracesWithAdjacentSuccession;
    public int tracesWithAlternateSuccession;


    private SortedSet<Integer> newDistancesInTrace;

    public StatsCell() {
        this.distances = new TreeMap<Integer, Integer>();
        this.inBetweenRepsOnwards = 0;
        this.inBetweenRepsBackwards = 0;

        this.distancesPerTrace = new TreeMap<Integer, Integer>();
        this.tracesWithInBetweenRepsOnwards = 0;
        this.tracesWithInBetweenRepsBackwards = 0;
        this.tracesWithCooccurrenceOnwards = 0;
        this.tracesWithCooccurrenceBackwards = 0;

        this.tracesWithSuccessorCooccurrences = 0;
        this.tracesWithPredecessorCooccurrences = 0;

        this.tracesWithSuccession = 0;
        this.tracesWithAdjacentSuccession = 0;
        this.tracesWithAlternateSuccession = 0;

        this.newDistancesInTrace = new TreeSet<Integer>();
    }

    void newAtDistance(int distance) {
        this.newAtDistance(distance, 1);
    }

    void newAtDistance(int distance, int quantity) {
    	Integer distanceCounter = this.distances.get(distance);
        distanceCounter = (distanceCounter == null ? quantity : distanceCounter + quantity);
    	this.distances.put(distance, distanceCounter);
    	this.newDistancesInTrace.add(distance);
    }
    
    void setAsNeverCooccurred(int quantity) {
        this.newAtDistance(NEVER_EVER, quantity);
    }

    void setAsNeverCooccurredAnyMore(int quantity, boolean onwards) {
        this.newAtDistance(
                (onwards ? NEVER_ONWARDS : NEVER_BACKWARDS),
                quantity
        );
    }

    public void setAsAdjacent(boolean onwards) {
        if (onwards) {
            this.tracesWithSuccessorCooccurrences += 1;
        } else {
            this.tracesWithPredecessorCooccurrences += 1;
        }

    }

    public void setAsSuccession() {
        this.tracesWithSuccession += 1; 
    }

    public void setAsAdjacentSuccession(boolean onwards) {
        if (onwards){
        	this.tracesWithAdjacentSuccession += 1;
        } 
    }

    public void setAsAlternateSuccession(boolean onwards) {
        if (onwards){
        	this.tracesWithAlternateSuccession += 1;
        } 
    }

    /* Store the information that this task occurred in the same trace as the pivot, either onwards or backwards. */
    void setAsCooccurredInTrace(boolean onwards) {
        if (onwards) {
            this.tracesWithCooccurrenceOnwards += 1;
        } else {
            this.tracesWithCooccurrenceBackwards += 1;
        }
    }

    
    protected void countOneMoreTraceWithDistance(int distance) {
    	Integer distanceCounter = this.distancesPerTrace.get(distance);
        distanceCounter = (distanceCounter == null ? 1 : distanceCounter + 1);
    	this.distancesPerTrace.put(distance, distanceCounter);
    }
    
    void finalizeAnalysisStep(boolean onwards, boolean secondPass) {
    	// Record the distances observed in this trace (including NEVER_EVER, NEVER_ONWARDS and NEVER_BACKWARDS)
    	for (Integer distance: this.newDistancesInTrace) {
    		this.countOneMoreTraceWithDistance(distance);
    	}
    	this.newDistancesInTrace = new TreeSet<Integer>();
    }

    @Override
    public String toString() {
        StringBuilder sBuf = new StringBuilder();
        
        if (this.distances.keySet() == null || this.distances.keySet().size() == 0)
            return "{}\n";
        
        for (Integer key : this.distances.keySet()) {
            sBuf.append(", <");
            switch(key) {
                case NEVER_ONWARDS:
                    sBuf.append("Never more");
                    break;
                case NEVER_BACKWARDS:
                    sBuf.append("Never before");
                    break;
                case NEVER_EVER:
                    sBuf.append("Never");
                    break;
                default:
                    sBuf.append(String.format("%+d", key));
                    break;
            }

            sBuf.append(", "
                    + this.distances.get(key)
                    + " in "
                    + this.distancesPerTrace.get(key)
                    + " tr's>");
        }
        sBuf.append("} time(/s)");
        sBuf.append(", alternating: {onwards = ");
        sBuf.append(this.inBetweenRepsOnwards);
        sBuf.append(" in ");
        sBuf.append(this.tracesWithInBetweenRepsOnwards);
        sBuf.append(" tr's");
        sBuf.append(", backwards = ");
        sBuf.append(this.inBetweenRepsBackwards);
        sBuf.append(" in ");
        sBuf.append(this.tracesWithInBetweenRepsBackwards);
        sBuf.append(" tr's");
        sBuf.append("} time(/s)\n");
        return "{" + sBuf.substring(2);
    }

    @Override
    public Object clone() {
        StatsCell clone = new StatsCell();
        clone.distances = new TreeMap<Integer, Integer>(this.distances);
        return clone;
    }
    
    
    public double howManyTimesItNeverOccurredBackwards() {
        if (this.distances.containsKey(NEVER_BACKWARDS))
            return this.distances.get(NEVER_BACKWARDS);
        return 0;
    }
    
    public double howManyTimesItNeverOccurredOnwards() {
        if (this.distances.containsKey(NEVER_ONWARDS))
            return this.distances.get(NEVER_ONWARDS);
        return 0;
    }
    
    public double howManyTimesItNeverOccurredAtAll() {
        if (this.distances.containsKey(NEVER_EVER))
            return this.distances.get(NEVER_EVER);
        return 0;
    }
    
    public double inHowManyTracesItNeverOccurredBackwards() {
        if (this.distancesPerTrace.containsKey(NEVER_BACKWARDS))
            return this.distancesPerTrace.get(NEVER_BACKWARDS);
        return 0;
    }
    
    public double inHowManyTracesItNeverOccurredOnwards() {
        if (this.distancesPerTrace.containsKey(NEVER_ONWARDS))
            return this.distancesPerTrace.get(NEVER_ONWARDS);
        return 0;
    }
    
    public double inHowManyTracesItNeverOccurredAtAll() {
        if (this.distancesPerTrace.containsKey(NEVER_EVER))
            return this.distancesPerTrace.get(NEVER_EVER);
        return 0;
    }

	public void mergeAdditively(StatsCell other) {
		this.inBetweenRepsBackwards += other.inBetweenRepsBackwards;
		this.inBetweenRepsOnwards += other.inBetweenRepsOnwards;
		this.tracesWithInBetweenRepsBackwards += other.tracesWithInBetweenRepsBackwards;
		this.tracesWithInBetweenRepsOnwards += other.tracesWithInBetweenRepsOnwards;
		this.tracesWithCooccurrenceOnwards += other.tracesWithCooccurrenceOnwards;
		this.tracesWithCooccurrenceBackwards += other.tracesWithCooccurrenceBackwards;
		this.tracesWithSuccessorCooccurrences += other.tracesWithSuccessorCooccurrences;
		this.tracesWithPredecessorCooccurrences += other.tracesWithPredecessorCooccurrences;
        this.tracesWithSuccession += other.tracesWithSuccession;
        this.tracesWithAdjacentSuccession += other.tracesWithAdjacentSuccession;
        this.tracesWithAlternateSuccession += other.tracesWithAlternateSuccession;

		for (Integer distance : this.distances.keySet()) {
			if (other.distances.containsKey(distance)) {
				this.distances.put(distance, this.distances.get(distance) + other.distances.get(distance));
			}
		}
		
		for (Integer distance : other.distances.keySet()) {
			if (!this.distances.containsKey(distance)) {
				this.distances.put(distance, other.distances.get(distance));
			}
		}
		
		for (Integer distance : this.distancesPerTrace.keySet()) {
			if (other.distancesPerTrace.containsKey(distance)) {
				this.distancesPerTrace.put(distance, this.distancesPerTrace.get(distance) + other.distancesPerTrace.get(distance));
			}
		}
		
		for (Integer distance : other.distancesPerTrace.keySet()) {
			if (!this.distancesPerTrace.containsKey(distance)) {
				this.distancesPerTrace.put(distance, other.distancesPerTrace.get(distance));
			}
		}
	}

	public void mergeSubtractively(StatsCell other) {
		this.inBetweenRepsBackwards -= other.inBetweenRepsBackwards;
		this.inBetweenRepsOnwards -= other.inBetweenRepsOnwards;
		this.tracesWithInBetweenRepsBackwards -= other.tracesWithInBetweenRepsBackwards;
		this.tracesWithInBetweenRepsOnwards -= other.tracesWithInBetweenRepsOnwards;
		this.tracesWithCooccurrenceOnwards -= other.tracesWithCooccurrenceOnwards;
		this.tracesWithCooccurrenceBackwards -= other.tracesWithCooccurrenceBackwards;
		this.tracesWithSuccessorCooccurrences -= other.tracesWithSuccessorCooccurrences;
		this.tracesWithPredecessorCooccurrences -= other.tracesWithPredecessorCooccurrences;
		this.tracesWithSuccession -= other.tracesWithSuccession;
        this.tracesWithAdjacentSuccession -= other.tracesWithAdjacentSuccession;
        this.tracesWithAlternateSuccession -= other.tracesWithAlternateSuccession;
      

		for (Integer distance : this.distances.keySet()) {
			if (other.distances.containsKey(distance)) {
				this.distances.put(distance, this.distances.get(distance) - other.distances.get(distance));
			}
		}
		
		for (Integer distance : other.distances.keySet()) {
			if (!this.distances.containsKey(distance)) {
				logger.warn("Trying to merge subtractively distance stats that were not included for " + distance);
			}
		}
		
		for (Integer distance : this.distancesPerTrace.keySet()) {
			if (other.distancesPerTrace.containsKey(distance)) {
				this.distancesPerTrace.put(distance, this.distancesPerTrace.get(distance) - other.distancesPerTrace.get(distance));
			}
		}
		
		for (Integer distance : other.distancesPerTrace.keySet()) {
			if (!this.distancesPerTrace.containsKey(distance)) {
				logger.warn("Trying to merge subtractively distance stats that were not included for " + distance);
			}
		}
	}
}