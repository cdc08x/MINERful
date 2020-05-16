package minerful.automaton;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class CallableAutomataMaker implements Callable<Automaton> {
	private static Logger logger = Logger.getLogger(CallableAutomataMaker.class.getCanonicalName());
	
	public Collection<String> regularExpressions;

	public CallableAutomataMaker(Collection<String> regularExpressions) {
		this.regularExpressions = regularExpressions;
	}

	@Override
	public Automaton call() throws Exception {
		return makeAutomaton();
	}

	public Automaton makeAutomaton() {
		Automaton processAutomaton = null, nuConstraintAutomaton = null;
		String nuRegExp = null;

//		logger.trace("Preparing the automaton...");

		if (regularExpressions.size() > 0) {
			Iterator<String> regExpsIterator = regularExpressions.iterator();

//			int i = 1;
			while (regExpsIterator.hasNext()) {
				nuRegExp = regExpsIterator.next();
//				logger.trace("Intersecting the automaton with the accepting for: "
//						+ nuRegExp + " (" + i + " / " + this.regularExpressions.size() + ")");
//				i++;

				nuConstraintAutomaton = new RegExp(nuRegExp).toAutomaton();
				if (processAutomaton != null) {
					processAutomaton = processAutomaton
							.intersection(nuConstraintAutomaton);
				} else {
					processAutomaton = nuConstraintAutomaton;
				}
				processAutomaton.minimize();
//				logger.trace("Automaton states: "
//						+ processAutomaton.getNumberOfStates()
//						+ "; automaton transitions: "
//						+ processAutomaton.getNumberOfTransitions());
			}
		}
		return processAutomaton;
	}
}