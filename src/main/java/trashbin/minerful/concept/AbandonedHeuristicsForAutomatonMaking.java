package trashbin.minerful.concept;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.automaton.AutomatonFactory;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.index.LinearConstraintsIndexFactory;
import dk.brics.automaton.Automaton;

public class AbandonedHeuristicsForAutomatonMaking {
	public static Automaton buildAutomatonByBoundHeuristicAppliedTwiceInMultiThreading(ProcessModel model) {
		Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> map =
				LinearConstraintsIndexFactory.indexByImplyingAndImplied(model.bag);
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
			subAutomata.put(tCh, AutomatonFactory.fromRegularExpressions(regularExpressions, model.getTaskCharArchive().getIdentifiersAlphabet()));
		}
		
		for (TaskChar tCh : taskCharsSortedByNumberOfConnections) {
			if (processAutomaton == null) {
				processAutomaton = subAutomata.get(tCh);
			} else {
				processAutomaton = processAutomaton.intersection(subAutomata.get(tCh));
			}
		}
		
		return processAutomaton;
	}
	
	public static Automaton buildAutomatonByBoundAndDimensionalityHeuristicInMultiThreading(ProcessModel model) {
		Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> map =
				LinearConstraintsIndexFactory.indexByImplyingAndImplied(model.bag);
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
		return AutomatonFactory.fromRegularExpressionsByDimensionalityHeuristicInMultiThreading(indexedRegExps, model.getTaskCharArchive().getIdentifiersAlphabet());
	}

	public static Automaton buildAutomatonByStrictnessHeuristic(ProcessModel model) {
		SortedSet<Constraint> constraintsSortedByStrictness = LinearConstraintsIndexFactory.getAllConstraintsSortedByStrictness(model.bag);
		List<String> regularExpressions = new ArrayList<String>(constraintsSortedByStrictness.size());
		for (Constraint con : constraintsSortedByStrictness) {
			regularExpressions.add(con.getRegularExpression());
		}
		return AutomatonFactory.fromRegularExpressions(regularExpressions, model.getTaskCharArchive().getIdentifiersAlphabet());
	}
	
	public static ProcessModel generateNonEvaluatedBinaryModel(TaskCharArchive taskCharArchive) {
		ProcessModel proMod = null;
		
		Iterator<TaskChar>
			actIter = taskCharArchive.getTaskChars().iterator(),
			auxActIter = null;
		TaskChar
			auxActiParam1 = null,
			auxActiParam2 = null;
		Collection<Constraint>
			conSet = new TreeSet<Constraint>(),
			auxConSet = null;
		Collection<TaskChar> activitiesLeftToCombine = new TreeSet<TaskChar>(taskCharArchive.getTaskChars());

		while (actIter.hasNext()) {
			auxActiParam1 = actIter.next();
			
			auxConSet = MetaConstraintUtils.getAllDiscoverableExistenceConstraints(auxActiParam1);
			auxConSet = MetaConstraintUtils.createHierarchicalLinks(auxConSet);
			
			conSet.addAll(auxConSet);
			
			activitiesLeftToCombine.remove(auxActiParam1);
			auxActIter = activitiesLeftToCombine.iterator();

			auxConSet = new TreeSet<Constraint>();
			while (auxActIter.hasNext()) {
				auxActiParam2 = auxActIter.next();
				
				auxConSet = MetaConstraintUtils.getAllDiscoverableRelationConstraints(auxActiParam1, auxActiParam2);
				auxConSet.addAll(MetaConstraintUtils.getAllDiscoverableRelationConstraints(auxActiParam2, auxActiParam1));

				auxConSet = MetaConstraintUtils.createHierarchicalLinks(auxConSet);
				conSet.addAll(auxConSet);
			}
		}
		ConstraintsBag bag = new ConstraintsBag(taskCharArchive.getTaskChars(), conSet);
		proMod = new ProcessModel(taskCharArchive, bag);

		return proMod;
	}
	
	@Deprecated
	public static Automaton buildAutomatonByDimensionalityHeuristic(ProcessModel model) {
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
		for (TaskChar tChr : model.bag.getTaskChars()) {
			Collection<String> regExps = new ArrayList<String>();
			for (Constraint con : model.bag.getConstraintsOf(tChr)) {
				regExps.add(con.getRegularExpression());
			}
			regExpsMap.put(tChr.identifier, regExps);
		}
		
		return AutomatonFactory.fromRegularExpressionsByDimensionalityHeuristicInMultiThreading(regExpsMap, model.getTaskCharArchive().getIdentifiersAlphabet());
	}

}
