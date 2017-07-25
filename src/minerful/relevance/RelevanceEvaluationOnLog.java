package minerful.relevance;

public class RelevanceEvaluationOnLog {
	public static final String CSV_HEADER = "Satisfactions;VacuousSatisfactions;Violations";
	public int numberOfSatisfyingTraces = 0;
	public int numberOfVacuouslySatisfyingTraces = 0;
	public int numberOfViolatingTraces = 0;
	
	public void increment(TraceEvaluation eval) {
		switch (eval) {
		case SATISFACTION:
			this.numberOfSatisfyingTraces++;
			break;
		case VACUOUS_SATISFACTION:
		case NONE:
			this.numberOfVacuouslySatisfyingTraces++;
			break;
		case VIOLATION:
			this.numberOfViolatingTraces++;
			break;
		default:
			break;
		}
	}
	
	public int numberOfViolatingOrVacuouslySatisfyingTraces() {
		return this.numberOfVacuouslySatisfyingTraces + this.numberOfViolatingTraces;
	}
	
	public int numberOfVacuouslySatisfyingOrSatisfyingTraces() {
		return this.numberOfVacuouslySatisfyingTraces + this.numberOfSatisfyingTraces;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LogEvaluation [numberOfSatisfyingTraces=");
		builder.append(numberOfSatisfyingTraces);
		builder.append(", numberOfVacuouslySatisfyingTraces=");
		builder.append(numberOfVacuouslySatisfyingTraces);
		builder.append(", numberOfViolatingTraces=");
		builder.append(numberOfViolatingTraces);
		builder.append("]");
		return builder.toString();
	}
	
	public String printCSV() {
		return numberOfSatisfyingTraces + ";" + numberOfVacuouslySatisfyingTraces + ";" + numberOfViolatingTraces;
	}

	public double numberOfRelevantlySatisfyingTraces() {
		return numberOfSatisfyingTraces;
	}
}