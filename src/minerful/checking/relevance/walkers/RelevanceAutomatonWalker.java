package minerful.checking.relevance.walkers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import minerful.automaton.concept.relevance.ActivationStatusWildcardAwareState;
import minerful.automaton.concept.relevance.RelevanceAwareTransition;
import minerful.automaton.concept.relevance.TransitionRelevance;
import minerful.checking.relevance.dao.TraceEvaluation;
import minerful.concept.AbstractTaskClass;
import minerful.logparser.LogTraceParser;
import minerful.utils.MessagePrinter;

public class RelevanceAutomatonWalker {
	public static MessagePrinter logger = MessagePrinter.getInstance(RelevanceAutomatonWalker.class);
	
	private Map<AbstractTaskClass, Character> alteredInverseLogTranslationMap;
	private ActivationStatusWildcardAwareState currentStateInTheWalk;
	private ActivationStatusWildcardAwareState initialState;
	private TraceEvaluation traceEvaluation;
	private boolean relevantTransitionTraversed;
	private boolean noNeedToCheckFurther;
	public String name;

	public RelevanceAutomatonWalker(String name, List<Collection<Character>> actualCharParams, List<Character> symbolicCharParams, Map<Character, AbstractTaskClass> logTranslationMap, ActivationStatusWildcardAwareState initialState) {
		this.name = name + '(';
		int j = 0;
		for (Collection<Character> actualCharParam : actualCharParams) {
			if (j++ > 0) {
				this.name += ", ";
			}
			if (actualCharParam.size() > 1) {
				this.name += '{';
			}
			for (int i = 0; i < actualCharParam.size(); i++) {
				Iterator<Character> charIter = actualCharParam.iterator();
				if (i > 0) {
					this.name += ", ";
				}
				this.name += logTranslationMap.get(charIter.next());
			}
			if (actualCharParam.size() > 1) {
				this.name += '}';
			}
		}
		this.name = this.name + ')';
		this.alteredInverseLogTranslationMap = new HashMap<AbstractTaskClass, Character>(actualCharParams.size(), (float)1.0);

		Iterator<Character> charIter = null;
		for (int i = 0; i < symbolicCharParams.size(); i++) {
			try {
				charIter = actualCharParams.get(i).iterator();
			} catch (Exception e) {
				logger.error("Issue: symbolicCharParams " + symbolicCharParams.toString());
				logger.error("Issue: actualCharParams " + actualCharParams.toString());
				throw e;
			}
			while (charIter.hasNext()) {
				alteredInverseLogTranslationMap.put(logTranslationMap.get(charIter.next()),
						symbolicCharParams.get(i));
			}
		}

		this.initialState = initialState;
		logger.debug("DEBUG PRINTOUT: symbolicCharParams " + symbolicCharParams.toString());
		logger.debug("DEBUG PRINTOUT: actualCharParams " + actualCharParams.toString());
		logger.debug("DEBUG PRINTOUT: alteredInverseLogTranslationMap " + alteredInverseLogTranslationMap.toString());
		reset();
	}

	public void reset() {
		this.traceEvaluation = TraceEvaluation.NONE;
		this.currentStateInTheWalk = this.initialState;
		this.relevantTransitionTraversed = false;
		this.noNeedToCheckFurther = false;
	}

	public Map<AbstractTaskClass, Character> getAlteredInverseLogTranslationMap() {
		return alteredInverseLogTranslationMap;
	}	

	public void run(LogTraceParser traceParser) {
		this.reset();

		AbstractTaskClass tasCla = null;
		while (!traceParser.isParsingOver()) {
			tasCla = traceParser.parseSubsequent().getEvent().getTaskClass();
			this.step(tasCla);
		}
	}

	public TraceEvaluation step(AbstractTaskClass taskClass) {
		if (!noNeedToCheckFurther) {
			ActivationStatusWildcardAwareState necState = null;
			RelevanceAwareTransition relAwaTrans = null;
			Character stepChar = null;
			logger.debug(this.name.toString() + " reads " + taskClass);
			if (alteredInverseLogTranslationMap.containsKey(taskClass)) {
				stepChar = codify(taskClass);
				relAwaTrans = this.currentStateInTheWalk.getTransition(stepChar);
				necState = ((ActivationStatusWildcardAwareState) this.currentStateInTheWalk.step(stepChar));
				logger.debug("currentStateInTheWalk.getTransitions(): " + this.currentStateInTheWalk.getTransitions());
				logger.debug(taskClass + " => " + stepChar + " is involved in this constraint (" + this.name + ") and leads to " + (necState!=null?necState.getStatus():null));
			} else {
				logger.debug(taskClass + " is not involved in this constraint (" + this.name + ")");
				relAwaTrans = this.currentStateInTheWalk.getWildTransition();
				necState = (ActivationStatusWildcardAwareState) this.currentStateInTheWalk.stepWild();
			}
			
			if (necState != null) {
				this.currentStateInTheWalk = necState;
				this.evaluateTrace(relAwaTrans);
			} else {
				this.noNeedToCheckFurther = true;
				this.traceEvaluation = TraceEvaluation.VIOLATION;
			}
		}
		return this.traceEvaluation;
	}

	public Character codify(AbstractTaskClass taskClass) {
		return this.alteredInverseLogTranslationMap.get(taskClass);
	}
	
	private void evaluateTrace(RelevanceAwareTransition relAwaTrans) {
		this.relevantTransitionTraversed =
				this.relevantTransitionTraversed
				||
				relAwaTrans.getRelevance().equals(TransitionRelevance.RELEVANT);
		
		switch (this.currentStateInTheWalk.getStatus()) {
		case SAT_PERM:
			noNeedToCheckFurther = true;
			if (relevantTransitionTraversed) {
				this.traceEvaluation = TraceEvaluation.SATISFACTION;
			} else {
				this.traceEvaluation = TraceEvaluation.VACUOUS_SATISFACTION;
			}
			break;
		case SAT_TEMP:
			if (relevantTransitionTraversed) {
				this.traceEvaluation = TraceEvaluation.SATISFACTION;
			} else {
				this.traceEvaluation = TraceEvaluation.VACUOUS_SATISFACTION;
			}
			break;
		case VIO_TEMP:
			this.traceEvaluation = TraceEvaluation.VIOLATION;
			break;
		case VIO_PERM:
			noNeedToCheckFurther = true;
			this.traceEvaluation = TraceEvaluation.VIOLATION;
			break;
		}
	}
	
	public boolean isRelevantTransitionTraversed() {
		return relevantTransitionTraversed;
	}

	public ActivationStatusWildcardAwareState getCurrentStateInTheWalk() {
		return currentStateInTheWalk;
	}

	public ActivationStatusWildcardAwareState getInitialState() {
		return initialState;
	}

	public TraceEvaluation getTraceEvaluation() {
		return traceEvaluation;
	}
}