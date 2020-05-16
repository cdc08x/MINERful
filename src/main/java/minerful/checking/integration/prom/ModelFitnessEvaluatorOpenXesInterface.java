package minerful.checking.integration.prom;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import minerful.checking.ProcessSpecificationFitnessEvaluator;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.XesLogParser;
import minerful.logparser.XesTraceParser;

public class ModelFitnessEvaluatorOpenXesInterface {
	public ProcessSpecificationFitnessEvaluator evaluator;
	private XesLogParser logParser;
	
	public ModelFitnessEvaluatorOpenXesInterface(XLog log, ClassificationType eventClassType, ProcessModel specification) {
		this.logParser = new XesLogParser(log, eventClassType);
		this.evaluator = new ProcessSpecificationFitnessEvaluator(logParser.getEventEncoderDecoder(), specification);
	}
	
	public ModelFitnessEvaluation evaluateOnLog() {
		return this.evaluator.evaluateOnLog(logParser);
	}

	public ModelFitnessEvaluation evaluateOnLog(Double fitnessThreshold) {
		return this.evaluator.evaluateOnLog(logParser, fitnessThreshold);
	}

	public ModelFitnessEvaluation evaluateOnTrace(XTrace trace) {
		XesTraceParser xesTraceParser = new XesTraceParser(trace, this.logParser);
		return this.evaluator.evaluateOnTrace(xesTraceParser);
	}
}