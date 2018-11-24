package minerful.checking.relevance.dao;

import minerful.concept.constraint.Constraint;

public class ModelFitnessEvaluation {
	public static final String CSV_POST_HEADER = ";Avg-fitness";
	public final ConstraintsFitnessEvaluationsMap evaloMap;
	public final String name;
	
	public ModelFitnessEvaluation(ConstraintsFitnessEvaluationsMap evaloMap, String name) {
		this.evaloMap = evaloMap;
		this.name = name;
	}
	
	public Double avgFitness() {
		Double avgFitness = 0.0;
		int denominator = 0;
		for (Constraint cns : evaloMap.evaluationsOnLog.keySet()) {
			avgFitness += cns.getFitness();
			denominator++;
		}
		return avgFitness / denominator;
	}
	
	public boolean isFullyFitting() {
		return avgFitness() >= 1.0;
	}

	public String printCSV() {
		String csv = this.evaloMap.printCSV();
		String appendedAvgFitness = ";" + avgFitness();
		csv = csv.replace("\n", appendedAvgFitness+"\n");
		csv = csv.replace(ConstraintFitnessEvaluation.CSV_HEADER+appendedAvgFitness, ConstraintFitnessEvaluation.CSV_HEADER + CSV_POST_HEADER);
		
		return csv;
	}
}
