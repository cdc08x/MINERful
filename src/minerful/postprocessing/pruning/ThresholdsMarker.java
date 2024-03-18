package minerful.postprocessing.pruning;

import org.apache.log4j.Logger;

import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintMeasuresManager;
import minerful.concept.constraint.ConstraintsBag;

public class ThresholdsMarker {
	public static final String THRESHOLDS_CHECK_CODE = "'Th-check'";

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

	public ConstraintsBag markConstraintsBelowThresholds(double evtSupportTh, double evtConfidenceTh, double evtCoverageTh) {
		ConstraintMeasuresManager conMes = null;

        for (TaskChar key : constraintsBag.getTaskChars()) {
            for (Constraint con : constraintsBag.getConstraintsOf(key)) {
            	conMes = con.getEventBasedMeasures();
            	conMes.setBelowSupportThreshold(!conMes.hasSufficientSupport(evtSupportTh));
            	conMes.setBelowConfidenceThreshold(!conMes.hasSufficientConfidence(evtConfidenceTh));
            	conMes.setBelowCoverageThreshold(!conMes.hasSufficientCoverage(evtCoverageTh));
            	if (conMes.isBelowSupportThreshold() || conMes.isBelowConfidenceThreshold() || conMes.isBelowCoverageThreshold()) {
            		this.numberOfMarkedConstraints++;
            	}
            }
        }

        return constraintsBag;
    }

	public ConstraintsBag markConstraintsBelowThresholds(
			double evtSupportTh, double evtConfidenceTh, double evtCoverageTh,
			double trcSupportTh, double trcConfidenceTh, double trcCoverageTh) {
		ConstraintMeasuresManager[] conMess = new ConstraintMeasuresManager[2];
		boolean markedAsBelowThreshold = false;
        for (TaskChar key : constraintsBag.getTaskChars()) {
            for (Constraint con : constraintsBag.getConstraintsOf(key)) {
            	conMess[0] = con.getEventBasedMeasures();
            	conMess[1] = con.getTraceBasedMeasures();
            	for (ConstraintMeasuresManager conMes: conMess) {
					if (conMes == conMess[0]){
	            		conMes.setBelowSupportThreshold(!conMes.hasSufficientSupport(evtSupportTh));
	            		conMes.setBelowConfidenceThreshold(!conMes.hasSufficientConfidence(evtConfidenceTh));
	            		conMes.setBelowCoverageThreshold(!conMes.hasSufficientCoverage(evtCoverageTh));
					}
					if(conMes == conMess[1]){
						conMes.setBelowSupportThreshold(!conMes.hasSufficientSupport(trcSupportTh));
	            		conMes.setBelowConfidenceThreshold(!conMes.hasSufficientConfidence(trcConfidenceTh));
	            		conMes.setBelowCoverageThreshold(!conMes.hasSufficientCoverage(trcCoverageTh));
					}
	            	if (conMes.isBelowSupportThreshold() || conMes.isBelowConfidenceThreshold() || conMes.isBelowCoverageThreshold()) {
	            		markedAsBelowThreshold = true;
	            	}
            	}
	            if (markedAsBelowThreshold) {
            		this.numberOfMarkedConstraints++;
	            }
	            markedAsBelowThreshold = false;
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