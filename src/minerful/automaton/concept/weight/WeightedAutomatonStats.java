package minerful.automaton.concept.weight;

import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class WeightedAutomatonStats {
	public static int AMOUNT_OF_QUANTILES = 4;
	public static int MAX_PERCENTAGE = 100;

	private WeightedAutomaton automaton;
	private DescriptiveStatistics
		stateStats,
		transiStats,
		stateIllegalityStats,
		transIllegalityStats;
	private double
		stateQuantileBoundaries[] = new double[AMOUNT_OF_QUANTILES - 1],
		transQuantileBoundaries[] = new double[AMOUNT_OF_QUANTILES - 1],
		stateIllegalityQuantileBoundaries[] = new double[AMOUNT_OF_QUANTILES - 1],
		transIllegalityQuantileBoundaries[] = new double[AMOUNT_OF_QUANTILES - 1]; 
	
	public WeightedAutomatonStats(WeightedAutomaton automaton) {
		this.automaton = automaton;
		this.buildStats();
		this.buildIllegalityStats();
	}

	public void augmentWeightedAutomatonWithQuantiles(boolean doRemoveNeverTraversedTransitions) {
		WeightedState auxWState = null;
		WeightedTransition auxWTrans = null;
		ArrayList<Transition> neverTraversedTransitions = null;
		for (State state : this.automaton.getStates()) {
			if (doRemoveNeverTraversedTransitions) {
				neverTraversedTransitions = new ArrayList<Transition>();
			}
			
			auxWState = (WeightedState) state;
			auxWState.setWeightQuantile(this.calculateStateQuantile(auxWState.getWeight()));
			for (Transition trans : auxWState.getTransitions()) {
				auxWTrans = (WeightedTransition) trans;
				if (doRemoveNeverTraversedTransitions && auxWTrans.getWeight() == 0) {
					neverTraversedTransitions.add(auxWTrans);
				} else {
					auxWTrans.setWeightQuantile(this.calculateTransQuantile(auxWTrans.getWeight()));
				}
			}
			
			if (doRemoveNeverTraversedTransitions) {
				for (Transition inuTran : neverTraversedTransitions) {
					auxWState.getTransitions().remove(inuTran);
				}
			}
		}
	}

	public void augmentWeightedAutomatonWithIllegalityQuantiles() {
		WeightedState auxWState = null;
		WeightedTransition auxWTrans = null;
		for (State state : this.automaton.getStates()) {
			auxWState = (WeightedState) state;
			auxWState.setNonConformityWeightQuantile(this.calculateStateIllegalityQuantile(auxWState.getNonConformityWeight()));
			for (Transition trans : auxWState.getTransitions()) {
				auxWTrans = (WeightedTransition) trans;
				auxWTrans.setNonConformityWeightQuantile(this.calculateTransIllegalityQuantile(auxWTrans.getNonConformityWeight()));
			}
		}
	}
	
	public int calculateStateQuantile(int value) {
		return this.calculateXtile(value, stateQuantileBoundaries);
	}
	public int calculateTransQuantile(int value) {
		return this.calculateXtile(value, transQuantileBoundaries);
	}
	
	public int calculateStateIllegalityQuantile(int value) {
		return this.calculateXtile(value, stateIllegalityQuantileBoundaries);
	}
	public int calculateTransIllegalityQuantile(int value) {
		return this.calculateXtile(value, transIllegalityQuantileBoundaries);
	}
	
	public int calculateXtile(int value, double[] xTileBoundaries) {
		int trials = 1;
		int xBoundaryIndex = ( AMOUNT_OF_QUANTILES / (int) Math.pow(2, trials) ) - 1;
		double auxBoundary = xTileBoundaries[xBoundaryIndex];
		boolean onTheLeft = false;
		
		while ( trials <= Math.log(AMOUNT_OF_QUANTILES)/Math.log(2) ) {

			if (value < auxBoundary) {
				xBoundaryIndex -= AMOUNT_OF_QUANTILES / (int) Math.pow(2, ++trials);
				onTheLeft = true;
			} else {
				xBoundaryIndex += AMOUNT_OF_QUANTILES / (int) Math.pow(2, ++trials);
				onTheLeft = false;
			}

			auxBoundary = xTileBoundaries[xBoundaryIndex];
		}
		
		return xBoundaryIndex + (onTheLeft ? 0 : 1);
	}
	
	private void buildStats() {
		this.stateStats = new DescriptiveStatistics();
		this.transiStats = new DescriptiveStatistics();

		for (State state : this.automaton.getStates()) {
			this.stateStats.addValue(((WeightedState) state).getWeight());
			for (Transition trans: state.getTransitions()) {
				/*
				 * In BPI 2012, it happened that most of weights (vast majority,
				 * amounting to more than 82.5%) were equal to 0. This entailed
				 * the presence of 6 quantile-boundaries equal to 0.0 and only
				 * one having a higher value. However, this made it impossible
				 * to distinguish between, e.g., transitions which were
				 * traversed 8100 times from transitions traversed twice.
				 * Therefore, we decide to remove from the quantile-boundary-computation all those values that amount to 0.
				 * Motivation is, we do not care about never-enacted behaviour!
				 * This prevents the imbalance.
				 */
				if (((WeightedTransition) trans).getWeight() > 0)
					this.transiStats.addValue(((WeightedTransition) trans).getWeight());
			}
		}
		
		
		for (int q = 0; q < AMOUNT_OF_QUANTILES - 1 ; q++) { // say we want quartiles. Then AMOUNT_OF_QUANTILES = 4. We want boundary values for 25, 50 and 75 => q values have to be 0, 1, 2 because the percentile is calculated as MAX_PERCENTAGE / AMOUNT_OF_QUANTILES * (q + 1) -- see the +1 there? Good! 
			stateQuantileBoundaries[q] = stateStats.getPercentile((double) MAX_PERCENTAGE / AMOUNT_OF_QUANTILES * (q + 1) );
		}

		for (int q = 0; q < AMOUNT_OF_QUANTILES - 1 ; q++) { // say we want quartiles. Then AMOUNT_OF_QUANTILES = 4. We want boundary values for 25, 50 and 75 => q values have to be 0, 1, 2 because the percentile is calculated as MAX_PERCENTAGE / AMOUNT_OF_QUANTILES * (q + 1) -- see the +1 there? Good! 
			transQuantileBoundaries[q] = transiStats.getPercentile((double) MAX_PERCENTAGE / AMOUNT_OF_QUANTILES * (q + 1) );
		}
	}
	
	private void buildIllegalityStats() {
		this.stateIllegalityStats = new DescriptiveStatistics();
		this.transIllegalityStats = new DescriptiveStatistics();
		
		for (State state : this.automaton.getStates()) {
			this.stateIllegalityStats.addValue(((WeightedState) state).getWeight());
			for (Transition trans: state.getTransitions()) {
				if (((WeightedTransition) trans).getWeight() > 0)
					this.transIllegalityStats.addValue(((WeightedTransition) trans).getWeight());
			}
		}
		
		
		for (int q = 0; q < AMOUNT_OF_QUANTILES - 1 ; q++) { // say we want quartiles. Then AMOUNT_OF_QUANTILES = 4. We want boundary values for 25, 50 and 75 => q values have to be 0, 1, 2 because the percentile is calculated as MAX_PERCENTAGE / AMOUNT_OF_QUANTILES * (q + 1) -- see the +1 there? Good! 
			stateIllegalityQuantileBoundaries[q] = stateIllegalityStats.getPercentile((double) MAX_PERCENTAGE / AMOUNT_OF_QUANTILES * (q + 1) );
		}

		for (int q = 0; q < AMOUNT_OF_QUANTILES - 1 ; q++) { // say we want quartiles. Then AMOUNT_OF_QUANTILES = 4. We want boundary values for 25, 50 and 75 => q values have to be 0, 1, 2 because the percentile is calculated as MAX_PERCENTAGE / AMOUNT_OF_QUANTILES * (q + 1) -- see the +1 there? Good! 
			transIllegalityQuantileBoundaries[q] = transIllegalityStats.getPercentile((double) MAX_PERCENTAGE / AMOUNT_OF_QUANTILES * (q + 1) );
		}
	}
}