package minerful.automaton;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class RunnableAutoMaker implements Runnable {
	public Automaton automaton;
	private static Logger logger = Logger.getLogger(AutomatonFactory.class.getCanonicalName());
	
	public Collection<String> regularExpressions;
	

	public RunnableAutoMaker(Collection<String> regularExpressions) {
		this.automaton = null;
		this.regularExpressions = regularExpressions;
	}

	@Override
	public void run() {
        // Turn strings into Regular Expressions and attach them
        Automaton
        	nuConstraintAutomaton = null;
        String
        	nuRegExp = null;

        logger.trace("Preparing the automaton...");
        
        if (regularExpressions.size() > 0) {

        	Iterator<String> regExpsIterator = regularExpressions.iterator();
        	if (regExpsIterator.hasNext()) {
// DEBUGGING!
//        	processAutomaton = processAutomaton.intersection(new RegExp(".{1," + alphabet.size() * 2 + "}").toAutomaton());
//        	processAutomaton = processAutomaton.intersection(new RegExp(".{1," + ((int)(alphabet.size() * 1.25)) + "}").toAutomaton());
//        	processAutomaton = processAutomaton.intersection(new RegExp(".{1,24}").toAutomaton());
// E.O. DEBUGGING
        		
        		while (regExpsIterator.hasNext()) {
        			nuRegExp = regExpsIterator.next();
        			
//        			logger.trace("Intersecting the automaton with the accepting for: " + nuRegExp);
        			
        			nuConstraintAutomaton = new RegExp(nuRegExp).toAutomaton();
        			if (this.automaton == null) {
        				this.automaton = nuConstraintAutomaton;
        			} else {
        				this.automaton = this.automaton.intersection(nuConstraintAutomaton);
        				this.automaton.minimize();
        			}
        			
        		}
        		logger.trace("Automaton states: " + this.automaton.getNumberOfStates() + "; automaton transitions: " + this.automaton.getNumberOfTransitions());
        	}
// DEBUGGING!
//        	processAutomaton = processAutomaton.intersection(new RegExp("[^a]?[^a]a[^a][^a]?").toAutomaton());
// E.O. DEBUGGING
        }
	}
}