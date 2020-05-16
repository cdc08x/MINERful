/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package minerful.miner.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.miner.stats.xmlenc.GlobalStatsMapAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalStatsTable {
	@XmlTransient
	protected static Logger logger = Logger.getLogger(GlobalStatsTable.class);

	@XmlElement
	@XmlJavaTypeAdapter(value=GlobalStatsMapAdapter.class)
    public Map<TaskChar, LocalStatsWrapper> statsTable;
	@XmlTransient
    public final TaskCharArchive taskCharArchive;
	@XmlAttribute
    public long logSize;
	@XmlAttribute
	public final Integer maximumBranchingFactor;
	
	private GlobalStatsTable() {
		this.maximumBranchingFactor = null;
		this.taskCharArchive = new TaskCharArchive();
	}

	public GlobalStatsTable(TaskCharArchive taskCharArchive, long testbedDimension, Integer maximumBranchingFactor) {
		this.taskCharArchive = taskCharArchive;
		this.logSize = testbedDimension;
		this.maximumBranchingFactor = maximumBranchingFactor;
		this.initGlobalStatsTable();
	}

    public GlobalStatsTable(TaskCharArchive taskCharArchive) {
        this(taskCharArchive, 0, null);
    }
    public GlobalStatsTable(TaskCharArchive taskCharArchive, Integer maximumBranchingFactor) {
    	this(taskCharArchive, 0, maximumBranchingFactor);
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
                    + " time(/s)\n");
            sBuf.append("\t as the first for " + statsWrapper.getAppearancesAsFirst() + ",");   
            sBuf.append(" as the last for " + statsWrapper.occurrencesAsLast + " time(/s)");
            sBuf.append("\t]\n");
            sBuf.append(statsWrapper.toString());

        }
        return sBuf.toString();
    }

	public void mergeAdditively(GlobalStatsTable other) {
		this.logSize += other.logSize;
		
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