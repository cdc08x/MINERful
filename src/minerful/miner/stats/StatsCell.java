/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner.stats;

import java.util.NavigableMap;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;

import minerful.miner.stats.xmlenc.DistancesMapAdapter;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class StatsCell implements Cloneable {
    @XmlTransient
    public static final int NEVER_ONWARDS = Integer.MAX_VALUE;
    @XmlTransient
    public static final int NEVER_BACKWARDS = Integer.MIN_VALUE;
    @XmlTransient
    public static final int NEVER_EVER = 0;
    
    @XmlTransient
    protected static Logger logger = Logger.getLogger(StatsCell.class);
    
    @XmlJavaTypeAdapter(value=DistancesMapAdapter.class)
    public NavigableMap<Integer, Integer> distances;
    @XmlElement(name="repetitionsInBetweenOnwards")
    public int betweenOnwards;
    @XmlElement(name="repetitionsInBetweenBackwards")
    public int betweenBackwards;

    public StatsCell() {
        this.distances = new TreeMap<Integer, Integer>();
        this.betweenOnwards = 0;
        this.betweenBackwards = 0;
    }

    void newAtDistance(int distance) {
        this.newAtDistance(distance, 1);
    }

    void newAtDistance(int distance, int quantity) {
    	Integer distanceCounter = this.distances.get(distance);
        distanceCounter = (distanceCounter == null ? quantity : distanceCounter + quantity);
    	this.distances.put(distance, distanceCounter);
    }
    
    void setAsNeverAppeared(int quantity) {
        this.newAtDistance(NEVER_EVER, quantity);
    }

    void setAsNeverAppearedAnyMore(int quantity, boolean onwards) {
        this.newAtDistance(
                (onwards ? NEVER_ONWARDS : NEVER_BACKWARDS),
                quantity
        );
    }
    
    /**
     * It does nothing, at this stage of the implementation!
     * @param onwards
     * @param secondPass 
     */
    void finalizeAnalysisStep(boolean onwards, boolean secondPass) {
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
                    + ">");
        }
        sBuf.append("} time(/s)");
        sBuf.append(", alternating: {onwards = ");
        sBuf.append(this.betweenOnwards);
        sBuf.append(", backwards = ");
        sBuf.append(this.betweenBackwards);
        sBuf.append("} time(/s)\n");
        return "{" + sBuf.substring(2);
    }

    @Override
    public Object clone() {
        StatsCell clone = new StatsCell();
        clone.distances = new TreeMap<Integer, Integer>(this.distances);
        return clone;
    }
    
    
    public double howManyTimesItNeverAppearedBackwards() {
        if (this.distances.containsKey(NEVER_BACKWARDS))
            return this.distances.get(NEVER_BACKWARDS);
        return 0;
    }
    
    public double howManyTimesItNeverAppearedOnwards() {
        if (this.distances.containsKey(NEVER_ONWARDS))
            return this.distances.get(NEVER_ONWARDS);
        return 0;
    }
    
    public double howManyTimesItNeverAppearedAtAll() {
        if (this.distances.containsKey(NEVER_EVER))
            return this.distances.get(NEVER_EVER);
        return 0;
    }

	public void mergeAdditively(StatsCell other) {
		this.betweenBackwards += other.betweenBackwards;
		this.betweenOnwards += other.betweenOnwards;
		
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
	}

	public void mergeSubtractively(StatsCell other) {
		this.betweenBackwards -= other.betweenBackwards;
		this.betweenOnwards -= other.betweenOnwards;
		
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
	}
}