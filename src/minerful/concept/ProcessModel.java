package minerful.concept;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.automaton.AutomatonFactory;
import minerful.automaton.SubAutomaton;
import minerful.automaton.utils.AutomatonUtils;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.xmlenc.ConstraintsBagAdapter;
import minerful.index.LinearConstraintsIndexFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;

@XmlRootElement(name="processModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessModel {
	@XmlTransient
	private static Logger logger = Logger.getLogger(ProcessModel.class.getCanonicalName());
	@XmlTransient
	public static String DEFAULT_NAME = "Discovered model";

	@XmlElement(name="declarative-model", required=true)
	@XmlJavaTypeAdapter(type=TreeSet.class, value=ConstraintsBagAdapter.class)
	public ConstraintsBag bag;
	@XmlAttribute
	private String name;
	@XmlElement
	private TaskCharArchive taskCharArchive;
	@XmlTransient
	public static final String MINERFUL_XMLNS = "https://github.com/cdc08x/MINERful/";

	protected ProcessModel() {}
	
	public ProcessModel(TaskCharArchive taskCharArchive, ConstraintsBag bag) {
		this(taskCharArchive, bag, DEFAULT_NAME);
	}

	public ProcessModel(TaskCharArchive taskCharArchive, ConstraintsBag bag, String name) {
		this.taskCharArchive = taskCharArchive;
		this.bag = bag;
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public Set<TaskChar> getProcessAlphabet() {
		return this.bag.getTaskChars();
	}

	public Automaton buildAutomaton() {
		return buildAutomatonByBondHeuristic();
	}

	public Automaton buildAlphabetAcceptingAutomaton() {
		return AutomatonFactory.fromRegularExpressions(new ArrayList<String>(0), this.taskCharArchive.getIdentifiersAlphabet());
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
		String alphabetLimitingRegularExpression = AutomatonUtils.createRegExpLimitingTheAlphabet(this.taskCharArchive.getIdentifiersAlphabet());
		
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
			return AutomatonFactory.subAutomataFromRegularExpressionsInMultiThreading(regExpsMap, this.taskCharArchive.getIdentifiersAlphabet(), maxActions);
		else
			return AutomatonFactory.subAutomataFromRegularExpressionsInMultiThreading(regExpsMap, this.taskCharArchive.getIdentifiersAlphabet());
	}

	/*
	 * This turned out to be the best heuristic for computing the automaton!
	 */
	public Automaton buildAutomatonByBondHeuristic() {
		Collection<String> regularExpressions = null;
		Collection<Constraint> constraints = LinearConstraintsIndexFactory.getAllConstraintsSortedByBoundsSupportFamilyConfidenceInterestFactorHierarchyLevel(this.bag);
		regularExpressions = new ArrayList<String>(constraints.size());
		for (Constraint con : constraints) {
			regularExpressions.add(con.getRegularExpression());
		}
		return AutomatonFactory.fromRegularExpressions(regularExpressions, this.taskCharArchive.getIdentifiersAlphabet());
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
			subAutomata.put(tCh, AutomatonFactory.fromRegularExpressions(regularExpressions, this.taskCharArchive.getIdentifiersAlphabet()));
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
		return AutomatonFactory.fromRegularExpressionsByDimensionalityHeuristicInMultiThreading(indexedRegExps, this.taskCharArchive.getIdentifiersAlphabet());
	}

	public Automaton buildAutomatonByStrictnessHeuristic() {
		SortedSet<Constraint> constraintsSortedByStrictness = LinearConstraintsIndexFactory.getAllConstraintsSortedByStrictness(this.bag);
		List<String> regularExpressions = new ArrayList<String>(constraintsSortedByStrictness.size());
		for (Constraint con : constraintsSortedByStrictness) {
			regularExpressions.add(con.getRegularExpression());
		}
		return AutomatonFactory.fromRegularExpressions(regularExpressions, this.taskCharArchive.getIdentifiersAlphabet());
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
			
			auxConSet = MetaConstraintUtils.getAllExistenceConstraints(auxActiParam1);
			auxConSet = MetaConstraintUtils.createHierarchicalLinks(auxConSet);
			
			conSet.addAll(auxConSet);
			
			activitiesLeftToCombine.remove(auxActiParam1);
			auxActIter = activitiesLeftToCombine.iterator();

			auxConSet = new TreeSet<Constraint>();
			while (auxActIter.hasNext()) {
				auxActiParam2 = auxActIter.next();
				
				auxConSet = MetaConstraintUtils.getAllRelationConstraints(auxActiParam1, auxActiParam2);
				auxConSet.addAll(MetaConstraintUtils.getAllRelationConstraints(auxActiParam2, auxActiParam1));

				auxConSet = MetaConstraintUtils.createHierarchicalLinks(auxConSet);
				conSet.addAll(auxConSet);
			}
		}
		ConstraintsBag bag = new ConstraintsBag(taskCharArchive.getTaskChars(), conSet);
		proMod = new ProcessModel(taskCharArchive, bag);

		return proMod;
	}
	
	@Deprecated
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
		
		return AutomatonFactory.fromRegularExpressionsByDimensionalityHeuristicInMultiThreading(regExpsMap, this.taskCharArchive.getIdentifiersAlphabet());
	}

	public TaskCharArchive getTaskCharArchive() {
		return this.taskCharArchive;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProcessModel [bag=");
		builder.append(bag);
		builder.append(", name=");
		builder.append(name);
		builder.append(", taskCharArchive=");
		builder.append(taskCharArchive);
		builder.append("]");
		return builder.toString();
	}

	public SortedSet<Constraint> getAllConstraints() {
		return LinearConstraintsIndexFactory.getAllConstraints(bag);
	}
}