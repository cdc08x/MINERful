package minerful.checking.relevance.dao;

public class ConstraintFitnessEvaluation {
	public static final String CSV_HEADER = "FullSatisfactions;VacuousSatisfactions;Violations";
	public int numberOfFullySatisfyingTraces = 0;
	public int numberOfVacuouslySatisfyingTraces = 0;
	public int numberOfViolatingTraces = 0;
	
	public void increment(TraceEvaluation eval) {
		switch (eval) {
		case SATISFACTION:
			this.numberOfFullySatisfyingTraces++;
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
	
	public int numberOfNonViolatingTraces() {
		return this.numberOfVacuouslySatisfyingTraces + this.numberOfFullySatisfyingTraces;
	}

	public double numberOfFullySatisfyingTraces() {
		return numberOfFullySatisfyingTraces;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LogEvaluation [numberOfFullySatisfyingTraces=");
		builder.append(numberOfFullySatisfyingTraces);
		builder.append(", numberOfVacuouslySatisfyingTraces=");
		builder.append(numberOfVacuouslySatisfyingTraces);
		builder.append(", numberOfViolatingTraces=");
		builder.append(numberOfViolatingTraces);
		builder.append("]");
		return builder.toString();
	}
	
	public String printCSV() {
		return numberOfFullySatisfyingTraces + ";" + numberOfVacuouslySatisfyingTraces + ";" + numberOfViolatingTraces;
	}
}