package minerful.postprocessing.pruning;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;

public class ThresholdsMarker {
	public static final String THRESHOLDS_CHECK_CODE = "'Th-check'";

	@XmlTransient
	private static Logger logger = Logger.getLogger(ThresholdsMarker.class.getCanonicalName());

	private int numberOfMarkedConstraints = 0;
	private boolean checking = false;

	public int getNumberOfMarkedConstraints() {
		return numberOfMarkedConstraints;
	}

	public boolean isChecking() {
		return checking;
	}

	private ConstraintsBag constraintsBag = null;
	
	public ThresholdsMarker(ConstraintsBag constraintsBag) {
		this.constraintsBag = constraintsBag;
	}

	public ConstraintsBag markConstraintsBelowSupportThreshold(double supportThreshold) {
	    return markConstraintsBelowThresholds(supportThreshold, Constraint.DEFAULT_CONFIDENCE, Constraint.DEFAULT_INTEREST_FACTOR);
	}

	public ConstraintsBag markConstraintsBelowThresholds(double supportThreshold, double confidence, double interest) {
        for (TaskChar key : constraintsBag.getTaskChars()) {
            for (Constraint con : constraintsBag.getConstraintsOf(key)) {
            	con.setBelowSupportThreshold(!con.hasSufficientSupport(supportThreshold));
            	con.setBelowConfidenceThreshold(!con.hasSufficientConfidence(confidence));
            	con.setBelowInterestFactorThreshold(!con.hasSufficientInterestFactor(interest));
            	if (con.isBelowSupportThreshold() || con.isBelowConfidenceThreshold() || con.isBelowInterestFactorThreshold()) {
            		this.numberOfMarkedConstraints++;
            	}
            }
        }

        return constraintsBag;
    }

	public void printComputationStats(long before, long after) {
		if (this.isChecking()) {
			throw new IllegalStateException("Subsumption-hierarchy-based check in progress");
		}
		
        StringBuffer
    	csvSummaryBuffer = new StringBuffer(),
    	csvSummaryLegendBuffer = new StringBuffer(),
    	csvSummaryComprehensiveBuffer = new StringBuffer();

        csvSummaryBuffer.append(ThresholdsMarker.THRESHOLDS_CHECK_CODE);
        csvSummaryLegendBuffer.append("'Operation code'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.constraintsBag.howManyConstraints());
        csvSummaryLegendBuffer.append("'Input constraints'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.getNumberOfMarkedConstraints());
        csvSummaryLegendBuffer.append("'Marked constraints'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(after - before);
        csvSummaryLegendBuffer.append("'Time'");
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append(";");

        csvSummaryComprehensiveBuffer.append("\n\nThresholds-based pruning: \n");
        csvSummaryComprehensiveBuffer.append(csvSummaryLegendBuffer.toString());
        csvSummaryComprehensiveBuffer.append("\n");
        csvSummaryComprehensiveBuffer.append(csvSummaryBuffer.toString());

        logger.info(csvSummaryComprehensiveBuffer.toString());
	}
}