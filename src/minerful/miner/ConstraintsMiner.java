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

	boolean hasSufficientInterestFactor(Constraint c);

	boolean hasSufficientConfidence(Constraint c);

	boolean hasSufficientSupport(Constraint c);

	void setInterestFactorThreshold(Double interestFactorThreshold);

	Double getInterestFactorThreshold();

	void setConfidenceThreshold(Double confidenceThreshold);

	Double getConfidenceThreshold();

	void setSupportThreshold(Double supportThreshold);

	Double getSupportThreshold();

	Set<TaskChar> getTasksToQueryFor();
}
