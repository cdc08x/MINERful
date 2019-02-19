package minerful.checking.relevance.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import minerful.concept.constraint.Constraint;

public class ConstraintsFitnessEvaluationsMap {
	public static final String CSV_PRE_HEADER = "Template;Constraint;Fitness;";

	public Map<Constraint, ConstraintFitnessEvaluation> evaluationsOnLog;
	
	private int fittingTracesCount = 0;
	private int nonFittingTracesCount = 0;
	
	public ConstraintsFitnessEvaluationsMap(List<Constraint> checkedConstraints) {
		this.evaluationsOnLog = new HashMap<Constraint, ConstraintFitnessEvaluation>(checkedConstraints.size(), (float)1.0);
		for (Constraint chkCns : checkedConstraints) {
			this.evaluationsOnLog.put(chkCns, new ConstraintFitnessEvaluation());
		}
	}

	public int getFittingTracesCount() {
		return fittingTracesCount;
	}

	public int getNonFittingTracesCount() {
		return nonFittingTracesCount;
	}

	public ConstraintFitnessEvaluation increment(Constraint constraintUnderAnalysis, TraceEvaluation traceEvaluation) {
		ConstraintFitnessEvaluation eval = this.evaluationsOnLog.get(constraintUnderAnalysis);
		eval.increment(traceEvaluation);
		return eval; 
	}

	public ConstraintFitnessEvaluation remove(Constraint constraintUnderAnalysis) {
		return this.evaluationsOnLog.remove(constraintUnderAnalysis);
	}
	
	public int incrementFittingTracesCount() {
		fittingTracesCount++;
		return this.getFittingTracesCount();
	}
	
	public int incrementNonFittingTracesCount() {
		nonFittingTracesCount++;
		return this.getNonFittingTracesCount();
	}

	public String printCSV() {
		StringBuilder sBuil = new StringBuilder();
		
		sBuil.append(ConstraintsFitnessEvaluationsMap.CSV_PRE_HEADER);
		sBuil.append(ConstraintFitnessEvaluation.CSV_HEADER);
		sBuil.append("\n");
		
		TreeSet<Constraint> constraints = new TreeSet<Constraint>(evaluationsOnLog.keySet());
		
		for (Constraint con : constraints) {
			sBuil.append(con.getTemplateName());
			sBuil.append(';');
			sBuil.append(con);
			sBuil.append(';');
			sBuil.append(con.getFitness());
			sBuil.append(';');
			sBuil.append(this.evaluationsOnLog.get(con).printCSV());
			sBuil.append('\n');
		}
		
		return sBuil.toString();
	}
}
