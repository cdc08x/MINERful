package minerful.concept;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.automaton.AutomatonFactory;
import minerful.automaton.SubAutomaton;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.index.LinearConstraintsIndexFactory;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;

public class ProcessModel {
	private static Logger logger = Logger.getLogger(ProcessModel.class.getCanonicalName());
	public static String DEFAULT_NAME = "Discovered model";

	public TaskCharRelatedConstraintsBag bag;
	private Collection<Character> basicAlphabet;
	private String name;

	public ProcessModel(TaskCharRelatedConstraintsBag bag) {
		this(bag, DEFAULT_NAME);
	}

	public ProcessModel(TaskCharRelatedConstraintsBag bag, String name) {
		this.bag = bag;
		this.setupBasicAlphabet();
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	private void setupBasicAlphabet() {
		this.basicAlphabet = new ArrayList<Character>(bag.getTaskChars().size());
		for (TaskChar taskChar : bag.getTaskChars()) {
			this.basicAlphabet.add(taskChar.identifier);
		}
	}

	public Automaton buildAutomaton() {
		return buildAutomatonByBoundHeuristic();
	}

	public Automaton buildAlphabetAcceptingAutomaton() {
		return AutomatonFactory.fromRegularExpressions(new ArrayList<String>(0), basicAlphabet);
	}
	
	public Collection<SubAutomaton> buildSubAutomata() {
		return buildSubAutomata(AutomatonFactory.NO_LIMITS_IN_ACTIONS_FOR_SUBAUTOMATA);
	}
	
	public Collection<SubAutomaton> buildSubAutomata(int maxActions) {
		NavigableMap<Character, Collection<String>> regExpsMap = new TreeMap<Character, Collection<String>>();
		Collection<String> regExps = null;
		Collection<Constraint> cns = null;
//		Collection<TaskChar> involvedTaskChars = null;
//		Collection<Character> involvedTaskCharIds = null;
		String alphabetLimitingRegularExpression = AutomatonFactory.createRegExpLimitingTheAlphabet(basicAlphabet);
		
		for (TaskChar tChr : this.bag.getTaskChars()) {
//			involvedTaskChars = new TreeSet<TaskChar>();

			cns = this.bag.getConstraintsOf(tChr);
			regExps = new ArrayList<String>(cns.size());
			
			for (Constraint con : cns) {
				regExps.add(con.getRegularExpression());
//				involvedTaskChars.addAll(con.getInvolvedTaskChars());
			}
//			involvedTaskCharIds = new ArrayList<Character>(involvedTaskChars.size());
//			for (TaskChar involvedTaskChar : involvedTaskChars)
//				involvedTaskCharIds.add(involvedTaskChar.identifier);
			
			regExps.add(alphabetLimitingRegularExpression);
			
			regExpsMap.put(tChr.identifier, regExps);
		}
		
		if (maxActions > AutomatonFactory.NO_LIMITS_IN_ACTIONS_FOR_SUBAUTOMATA)
			return AutomatonFactory.subAutomataFromRegularExpressionsInMultiThreading(regExpsMap, basicAlphabet, maxActions);
		else
			return AutomatonFactory.subAutomataFromRegularExpressionsInMultiThreading(regExpsMap, basicAlphabet);
	}
	
	
	/*
	 * This turned out to be the best heuristic for computing the automaton!
	 */
	public Automaton buildAutomatonByBoundHeuristic() {
		Collection<String> regularExpressions = null;
		Collection<Constraint> constraints = LinearConstraintsIndexFactory.getAllConstraintsSortedByBoundsSupportFamilyConfidenceInterestFactorHierarchyLevel(this.bag);

		regularExpressions = new ArrayList<String>(constraints.size());
		for (Constraint con : constraints) {
			regularExpressions.add(con.getRegularExpression());
		}
		return AutomatonFactory.fromRegularExpressions(regularExpressions, basicAlphabet);
	}
	
	public Automaton buildAutomatonByBoundHeuristicAppliedTwiceInMultiThreading() {
		Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> map =
				LinearConstraintsIndexFactory.indexByImplyingAndImplied(bag);
		List<TaskChar> taskCharsSortedByNumberOfConnections =
				LinearConstraintsIndexFactory.getTaskCharsSortedByNumberOfConnections(
						LinearConstraintsIndexFactory.createMapOfConnections(map));
		Collection<Constraint> constraints = null;
		Collection<String> regularExpressions = null;
		AbstractMap<TaskChar, Automaton> subAutomata = new TreeMap<TaskChar, Automaton>();
		Map<TaskChar, NavigableSet<Constraint>>
			subMap = null,
			subMapReverse = null;
		Automaton processAutomaton = null;
		
		Set<TaskChar>
			taskChars = new TreeSet<TaskChar>(map.keySet()),
			taskCharsReverse = new TreeSet<TaskChar>(map.keySet());
		
		for (TaskChar tCh : taskChars) {
			subMap = map.get(tCh);
			constraints = new ArrayList<Constraint>();
			for (TaskChar tChRev : taskCharsReverse) {
				if (subMap.containsKey(tChRev) && subMap.get(tChRev) != null && subMap.get(tChRev).size() > 0) {
					constraints.addAll(subMap.get(tChRev));
					subMap.put(tChRev, null);
				}
				if (map.containsKey(tChRev)) {
					subMapReverse = map.get(tChRev);
					if (subMapReverse.containsKey(tCh) && subMapReverse.get(tCh) != null && subMapReverse.get(tCh).size() > 0) {
						constraints.addAll(subMapReverse.get(tCh));
						subMapReverse.put(tCh, null);
					}
				}
			}
			regularExpressions = new ArrayList<String>(constraints.size());
			for (Constraint con : constraints) {
				regularExpressions.add(con.getRegularExpression());
			}
			subAutomata.put(tCh, AutomatonFactory.fromRegularExpressions(regularExpressions, basicAlphabet));
		}
		
		for (TaskChar tCh : taskCharsSortedByNumberOfConnections) {
			if (processAutomaton == null) {
				processAutomaton = subAutomata.get(tCh);
			} else {
				processAutomaton = processAutomaton.intersection(subAutomata.get(tCh));
			}
			logger.trace("Automaton states: " + processAutomaton.getNumberOfStates() + "; automaton transitions: " + processAutomaton.getNumberOfTransitions());
		}
		
		return processAutomaton;
	}
	
	public Automaton buildAutomatonByBoundAndDimensionalityHeuristicInMultiThreading() {
		Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> map =
				LinearConstraintsIndexFactory.indexByImplyingAndImplied(bag);
		Collection<Constraint> constraints = null;
		Collection<String> regularExpressions = null;
		AbstractMap<Character, Collection<String>> indexedRegExps = new TreeMap<Character, Collection<String>>();
		Map<TaskChar, NavigableSet<Constraint>>
			subMap = null,
			subMapReverse = null;
		
		Set<TaskChar>
			taskChars = new TreeSet<TaskChar>(map.keySet()),
			taskCharsReverse = new TreeSet<TaskChar>(map.keySet());
		
		for (TaskChar tCh : taskChars) {
			subMap = map.get(tCh);
			constraints = new ArrayList<Constraint>();
			for (TaskChar tChRev : taskCharsReverse) {
				if (subMap.containsKey(tChRev) && subMap.get(tChRev) != null && subMap.get(tChRev).size() > 0) {
					constraints.addAll(subMap.get(tChRev));
					subMap.put(tChRev, null);
				}
				if (map.containsKey(tChRev)) {
					subMapReverse = map.get(tChRev);
					if (subMapReverse.containsKey(tCh) && subMapReverse.get(tCh) != null && subMapReverse.get(tCh).size() > 0) {
						constraints.addAll(subMapReverse.get(tCh));
						subMapReverse.put(tCh, null);
					}
				}
			}
			regularExpressions = new ArrayList<String>(constraints.size());
			for (Constraint con : constraints) {
				regularExpressions.add(con.getRegularExpression());
			}
			indexedRegExps.put(tCh.identifier, regularExpressions);
		}
		return AutomatonFactory.fromRegularExpressionsByDimensionalityHeuristicInMultiThreading(indexedRegExps, basicAlphabet);
	}

	public Automaton buildAutomatonByStrictnessHeuristic() {
		SortedSet<Constraint> constraintsSortedByStrictness = LinearConstraintsIndexFactory.getAllConstraintsSortedByStrictness(this.bag);
		List<String> regularExpressions = new ArrayList<String>(constraintsSortedByStrictness.size());
		for (Constraint con : constraintsSortedByStrictness) {
			regularExpressions.add(con.getRegularExpression());
		}
		return AutomatonFactory.fromRegularExpressions(regularExpressions, basicAlphabet);
	}
	
	public Automaton buildAutomatonByDimensionalityHeuristic() {
		TreeMap<Character, Collection<String>> regExpsMap = new TreeMap<Character, Collection<String>>();
		// FIXME This is just for testing purposes!!
/*
CharacterRelatedConstraintsBag impliedIndexedBag = ConstraintsIndexFactory.indexByImpliedTaskChar(bag);
for (Constraint con : bag.getConstraintsOf(new TaskChar('a'))) {
	if (con.hasReasonablePositiveSupport(threshold) && con.isOfInterest(interest))
		regExps.add(con.getRegularExpression());
}
for (Constraint con : impliedIndexedBag.getConstraintsOf(new TaskChar('a'))) {
	if (con.hasReasonablePositiveSupport(threshold) && con.isOfInterest(interest))
		regExps.add(con.getRegularExpression());
}

*/
		for (TaskChar tChr : bag.getTaskChars()) {
			Collection<String> regExps = new ArrayList<String>();
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				regExps.add(con.getRegularExpression());
			}
			regExpsMap.put(tChr.identifier, regExps);
		}
		
		return AutomatonFactory.fromRegularExpressionsByDimensionalityHeuristicInMultiThreading(regExpsMap, basicAlphabet);
	}
}