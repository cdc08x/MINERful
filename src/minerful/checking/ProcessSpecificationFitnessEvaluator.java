package minerful.checking;

import minerful.checking.relevance.dao.SpecificationFitnessEvaluation;
import minerful.concept.ProcessSpecification;
import minerful.concept.constraint.Constraint;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.logparser.XesLogParser;

public class ProcessSpecificationFitnessEvaluator extends ConstraintsFitnessEvaluator {
	private ProcessSpecification processSpecification;

	/**
	 * Constructor of this class.
	 * @param taskCharEncoderDecoder Encoder of tasks stemming from the event log
	 * @param specification Specification to be evaluated
	 */
	public ProcessSpecificationFitnessEvaluator(TaskCharEncoderDecoder taskCharEncoderDecoder, ProcessSpecification specification) {
		super(taskCharEncoderDecoder, specification.getAllUnmarkedConstraints().toArray(new Constraint[0]));
		this.processSpecification = specification;
	}
	
	public SpecificationFitnessEvaluation evaluateOnLog(LogParser logParser) {
		return new SpecificationFitnessEvaluation(super.runOnLog(logParser), processSpecification.getName());
	}

	public SpecificationFitnessEvaluation evaluateOnLog(XesLogParser logParser, Double fitnessThreshold) {
		return new SpecificationFitnessEvaluation(super.runOnLog(logParser, fitnessThreshold), processSpecification.getName());
	}
	
	public SpecificationFitnessEvaluation evaluateOnTrace(LogTraceParser loTraParser) {
		return new SpecificationFitnessEvaluation(super.runOnTrace(loTraParser), processSpecification.getName());
	}

	public ProcessSpecification getSpecification() {
		return this.processSpecification;
	}
}