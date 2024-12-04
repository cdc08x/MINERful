package minerful.miner.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
//import minerful.miner.stats.xmlenc.GlobalStatsMapAdapter;

/**
 * This class wraps all the information, activity per activity, on its statistics as per the mined event log.
 * The information includes, for all activities, the total amount of occurrences, and the interplay with other activities.
 * @author Claudio Di Ciccio <dc.claudio@gmail.com>
 */
public class GlobalStatsTable {
	protected static Logger logger = Logger.getLogger(GlobalStatsTable.class);

    public Map<TaskChar, LocalStatsWrapper> statsTable;
    public final TaskCharArchive taskCharArchive;
    public long logSize;
    public long numOfEvents;
	public final Integer maximumBranchingFactor;
	
	private GlobalStatsTable() {
		this.maximumBranchingFactor = null;
		this.taskCharArchive = new TaskCharArchive();
	}

	public GlobalStatsTable(TaskCharArchive taskCharArchive, long testbedDimension, long numOfEvents, Integer maximumBranchingFactor) {
		this.taskCharArchive = taskCharArchive;
		this.logSize = testbedDimension;
		this.maximumBranchingFactor = maximumBranchingFactor;
		this.numOfEvents = numOfEvents;
		this.initGlobalStatsTable();
	}

    public GlobalStatsTable(TaskCharArchive taskCharArchive) {
        this(taskCharArchive, 0, 0, null);
    }
    public GlobalStatsTable(TaskCharArchive taskCharArchive, Integer maximumBranchingFactor) {
    	this(taskCharArchive, 0, 0, maximumBranchingFactor);
    }

    private void initGlobalStatsTable() {
        this.statsTable = new HashMap<TaskChar, LocalStatsWrapper>(this.taskCharArchive.getTaskChars().size(), (float)1.0);
        Set<TaskChar> alphabet = this.taskCharArchive.getTaskChars();
        if (this.isForBranchedConstraints()) {
        	for (TaskChar task: this.taskCharArchive.getTaskChars()) {
       			this.statsTable.put(task, new LocalStatsWrapperForCharsetsWAlternation(taskCharArchive, task, maximumBranchingFactor));
	        }
        } else {
        	for (TaskChar task: alphabet) {
	            this.statsTable.put(task, new LocalStatsWrapper(taskCharArchive, task));
	        }
        }
    }
	
    public boolean isForBranchedConstraints() {
		return maximumBranchingFactor != null && maximumBranchingFactor > 1;
	}

    @Override
    public String toString() {
        StringBuilder sBuf = new StringBuilder();
        for(TaskChar key: this.statsTable.keySet()) {
            StringBuilder aggregateAppearancesBuffer = new StringBuilder();
            LocalStatsWrapper statsWrapper = this.statsTable.get(key);
            if (statsWrapper.repetitions != null) {
                for (Integer counter: statsWrapper.repetitions.keySet()) {
                    aggregateAppearancesBuffer.append(", <");
                    aggregateAppearancesBuffer.append(counter);
                    aggregateAppearancesBuffer.append(", ");
                    aggregateAppearancesBuffer.append(statsWrapper.repetitions.get(counter));
                    aggregateAppearancesBuffer.append(">");
                }
            }
            sBuf.append(
                    "\t[" + key + "\n"
                    + "\t aggregate occurrences = {"
                    + (aggregateAppearancesBuffer.length() > 0 ? aggregateAppearancesBuffer.substring(2) : "")
                    + "}, for a total amount of "
                    + statsWrapper.getTotalAmountOfOccurrences()
                    + " time(/s) in "
                    + statsWrapper.totalAmountOfTracesWithOccurrence
                    + " trace(/s) \n");
            sBuf.append("\t as the first for " + statsWrapper.getOccurrencesAsFirst() + ",");   
            sBuf.append(" as the last for " + statsWrapper.occurrencesAsLast + " time(/s)");
            sBuf.append("\t]\n");
            sBuf.append(statsWrapper.toString());

        }
        return sBuf.toString();
    }

	public void mergeAdditively(GlobalStatsTable other) {
		this.logSize += other.logSize;
		this.numOfEvents += other.numOfEvents;
		
		for (TaskChar key : this.statsTable.keySet()) {
			if (other.statsTable.containsKey(key)) {
				logger.trace("Additively merging the statistics tables of " + key);
				this.statsTable.get(key).mergeAdditively(other.statsTable.get(key));
			}
		}
		
		for (TaskChar key : other.statsTable.keySet()) {
			if (!this.statsTable.containsKey(key)) {
				logger.trace("Additively merging the statistics tables of " + key);
				this.statsTable.put(key, other.statsTable.get(key));
			}
		}
	}

	public void mergeSubtractively(GlobalStatsTable other) {
		this.logSize -= other.logSize;
		this.numOfEvents -= other.numOfEvents;
		
		for (TaskChar key : this.statsTable.keySet()) {
			if (other.statsTable.containsKey(key)) {
				logger.trace("Subtractively merging the statistics tables of " + key);
				this.statsTable.get(key).mergeSubtractively(other.statsTable.get(key));
			}
		}
		
		for (TaskChar key : other.statsTable.keySet()) {
			if (!this.statsTable.containsKey(key)) {
				logger.warn("Trying to merge subtractively a part of the stats table that was not included for " + key);
			}
		}
	}
}