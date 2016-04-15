package minerful.relevance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import minerful.automaton.concept.relevance.ActivationStatusWildcardAwareState;
import minerful.automaton.concept.relevance.RelevanceAwareTransition;
import minerful.automaton.concept.relevance.TransitionRelevance;
import minerful.concept.AbstractTaskClass;

public class RelevanceAutomatonWalker {
	private Map<AbstractTaskClass, Character> alteredInverseLogTranslationMap;
	private Map<Character, AbstractTaskClass> alteredLogTranslationMap;
	private ActivationStatusWildcardAwareState currentStateInTheWalk;
	private ActivationStatusWildcardAwareState initialState;
	private TraceEvaluation traceEvaluation;
	private boolean relevantTransitionTraversed;
	private boolean noNeedToCheckFurther;

	public RelevanceAutomatonWalker(List<Character> vector, SortedSet<Character> alphabetWithoutWildcard, Map<Character, AbstractTaskClass> logTranslationMap, ActivationStatusWildcardAwareState initialState) {
		this.alteredInverseLogTranslationMap = new HashMap<AbstractTaskClass, Character>(vector.size(), (float)1.0);
		Character[]
				alphabetWithoutWildcardArray = alphabetWithoutWildcard.toArray(new Character[alphabetWithoutWildcard.size()]),
				recodifiedAlphabet = vector.toArray(new Character[alphabetWithoutWildcard.size()]);
		
		for (int i = 0; i < recodifiedAlphabet.length; i++) {
			alteredInverseLogTranslationMap.put(logTranslationMap.get(recodifiedAlphabet[i]), new Character(alphabetWithoutWildcardArray[i]));
		}
		
		this.initialState = initialState;
		this.setUpAlteredLogTranslationMap();
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

	private void setUpAlteredLogTranslationMap() {
		alteredLogTranslationMap = new TreeMap<Character, AbstractTaskClass>();
		for (AbstractTaskClass cls : alteredInverseLogTranslationMap.keySet()) {
			alteredLogTranslationMap.put(codify(cls), cls);
		}
	}

	public TraceEvaluation step(AbstractTaskClass tasClass) {
		if (!noNeedToCheckFurther) {
			ActivationStatusWildcardAwareState necState = null;
			RelevanceAwareTransition relAwaTrans = null;
			Character arg0 = null;
			if (alteredInverseLogTranslationMap.containsKey(tasClass)) {
				arg0 = codify(tasClass);
				relAwaTrans = this.currentStateInTheWalk.getTransition(arg0);
				necState = ((ActivationStatusWildcardAwareState) this.currentStateInTheWalk.step(arg0));
			} else {
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

	public AbstractTaskClass decode(Character identifier) {
		return this.alteredLogTranslationMap.get(identifier);
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