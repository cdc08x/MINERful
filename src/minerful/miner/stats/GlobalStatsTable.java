/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package minerful.miner.stats;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.miner.stats.xmlenc.GlobalStatsMapAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalStatsTable {
	
	@XmlTransient
	// TODO To be made user-defined, not a constant within the code
	public static final Boolean INCLUDING_ALTERNATION = true;

	@XmlElement
	@XmlJavaTypeAdapter(value=GlobalStatsMapAdapter.class)
    public Map<Character, LocalStatsWrapper> statsTable;
	@XmlTransient
    public Character[] alphabet;
	@XmlAttribute
    public long logSize;
	@XmlAttribute
	public final Integer maximumBranchingFactor;
	
	private GlobalStatsTable() {
		this.maximumBranchingFactor = null;
	}

	public GlobalStatsTable(Character[] alphabet, long testbedDimension, Integer maximumBranchingFactor) {
		this.alphabet = alphabet;
		this.logSize = testbedDimension;
		this.maximumBranchingFactor = maximumBranchingFactor;
		this.initGlobalStatsTable();
	}

    public GlobalStatsTable(Character[] alphabet) {
        this(alphabet, 0, null);
    }
    public GlobalStatsTable(Character[] alphabet, Integer maximumBranchingFactor) {
    	this(alphabet, 0, maximumBranchingFactor);
    }

    private void initGlobalStatsTable() {
        this.statsTable = new TreeMap<Character, LocalStatsWrapper>();
        if (this.isForBranchedConstraints()) {
        	for (Character letter: alphabet) {
        		if (INCLUDING_ALTERNATION) {
        			this.statsTable.put(letter, new LocalStatsWrapperForCharsetsWAlternation(alphabet, letter, maximumBranchingFactor));
        		}
        		else {
        			this.statsTable.put(letter, new LocalStatsWrapperForCharsetsWOAlternation(alphabet, letter, maximumBranchingFactor));
        		}
	        }
        } else {
        	for (Character letter: alphabet) {
	            this.statsTable.put(letter, new LocalStatsWrapper(alphabet, letter));
	        }
        }
    }
	
    public boolean isForBranchedConstraints() {
		return maximumBranchingFactor != null && maximumBranchingFactor > 1;
	}

    @Override
    public String toString() {
        StringBuilder sBuf = new StringBuilder();
        for(Character key: this.statsTable.keySet()) {
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
                    + "\t aggregate appearances = {"
                    + (aggregateAppearancesBuffer.length() > 0 ? aggregateAppearancesBuffer.substring(2) : "")
                    + "}, for a total amount of "
                    + statsWrapper.getTotalAmountOfAppearances()
                    + " time(/s)\n");
            sBuf.append("\t as the first for " + statsWrapper.getAppearancesAsFirst() + ",");   
            sBuf.append(" as the last for " + statsWrapper.appearancesAsLast + " time(/s)");
            sBuf.append("\t]\n");
            sBuf.append(statsWrapper.toString());

        }
        return sBuf.toString();
    }
}