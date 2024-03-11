package minerful.checking.integration.prom;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import minerful.checking.ProcessSpecificationFitnessEvaluator;
import minerful.checking.relevance.dao.SpecificationFitnessEvaluation;
import minerful.concept.ProcessSpecification;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.XesLogParser;
import minerful.logparser.XesTraceParser;

public class SpecificationFitnessEvaluatorOpenXesInterface {
	public ProcessSpecificationFitnessEvaluator evaluator;
	private XesLogParser logParser;
	
	public SpecificationFitnessEvaluatorOpenXesInterface(XLog log, ClassificationType eventClassType, ProcessSpecification specification) {
		this.logParser = new XesLogParser(log, eventClassType);
		this.evaluator = new ProcessSpecificationFitnessEvaluator(logParser.getEventEncoderDecoder(), specification);
	}
	
	public SpecificationFitnessEvaluation evaluateOnLog() {
		return this.evaluator.evaluateOnLog(logParser);
	}

	public SpecificationFitnessEvaluation evaluateOnLog(Double fitnessThreshold) {
		return this.evaluator.evaluateOnLog(logParser, fitnessThreshold);
	}

	public SpecificationFitnessEvaluation evaluateOnTrace(XTrace trace) {
		XesTraceParser xesTraceParser = new XesTraceParser(trace, this.logParser);
		return this.evaluator.evaluateOnTrace(xesTraceParser);
	}
}