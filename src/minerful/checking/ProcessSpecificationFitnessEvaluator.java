package minerful.checking;

import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.logparser.XesLogParser;

public class ProcessSpecificationFitnessEvaluator extends ConstraintsFitnessEvaluator {
	private ProcessModel processSpecification;

	/**
	 * Constructor of this class.
	 * @param taskCharEncoderDecoder Encoder of tasks stemming from the event log
	 * @param constraints Constraints to be evaluated
	 */
	public ProcessSpecificationFitnessEvaluator(TaskCharEncoderDecoder taskCharEncoderDecoder, ProcessModel specification) {
		super(taskCharEncoderDecoder, specification.getAllUnmarkedConstraints().toArray(new Constraint[0]));
		this.processSpecification = specification;
	}
	
	public ModelFitnessEvaluation evaluateOnLog(LogParser logParser) {
		return new ModelFitnessEvaluation(super.runOnLog(logParser), processSpecification.getName());
	}

	public ModelFitnessEvaluation evaluateOnLog(XesLogParser logParser, Double fitnessThreshold) {
		return new ModelFitnessEvaluation(super.runOnLog(logParser, fitnessThreshold), processSpecification.getName());
	}
	
	public ModelFitnessEvaluation evaluateOnTrace(LogTraceParser loTraParser) {
		return new ModelFitnessEvaluation(super.runOnTrace(loTraParser), processSpecification.getName());
	}

	public ProcessModel getSpecification() {
		return this.processSpecification;
	}
}