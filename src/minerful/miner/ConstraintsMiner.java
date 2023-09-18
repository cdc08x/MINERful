package minerful.miner;

import java.util.Set;

import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;

public interface ConstraintsMiner {
	ConstraintsBag discoverConstraints();

	ConstraintsBag discoverConstraints(
			ConstraintsBag constraintsBag);

	long howManyPossibleConstraints();

	long getComputedConstraintsAboveTresholds();

	boolean hasValuesAboveThresholds(Constraint c);

	boolean hasSufficientEvtCoverage(Constraint c);

	boolean hasSufficientEvtConfidence(Constraint c);

	boolean hasSufficientEvtSupport(Constraint c);

	void setCoverageThreshold(Double coverageThreshold);

	Double getCoverageThreshold();

	void setConfidenceThreshold(Double confidenceThreshold);

	Double getConfidenceThreshold();

	void setSupportThreshold(Double supportThreshold);

	Double getSupportThreshold();

	Set<TaskChar> getTasksToQueryFor();

	boolean hasSufficientTrcSupport(Constraint c);

	boolean hasSufficientTrcConfidence(Constraint c);

	boolean hasSufficientTrcCoverage(Constraint c);

	Double getTrcSupportThreshold();

	void setTrcSupportThreshold(Double trcSupportThreshold);

	Double getTrcConfidenceThreshold();

	void setTrcConfidenceThreshold(Double trcConfidenceThreshold);

	Double getTrcCoverageThreshold();

	void setTrcCoverageThreshold(Double trcCoverageThreshold);
}
