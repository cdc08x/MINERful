package minerful.concept;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;
import minerful.automaton.AutomatonFactory;
import minerful.automaton.SubAutomaton;
import minerful.automaton.utils.AutomatonUtils;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.index.LinearConstraintsIndexFactory;


public class ProcessSpecification implements PropertyChangeListener {
	private static Logger logger = Logger.getLogger(ProcessSpecification.class.getCanonicalName());
	
	private PropertyChangeSupport pcs;
	
	public static String DEFAULT_NAME = "Discovered specification";

	public ConstraintsBag bag;
	private String name;
	private TaskCharArchive taskCharArchive;
	public static final String MINERFUL_XMLNS = "https://github.com/cdc08x/MINERful/";

	protected ProcessSpecification() {}

	public ProcessSpecification(ConstraintsBag bag) {
		this(new TaskCharArchive(bag.getTaskChars()), bag, DEFAULT_NAME);
	}

	public ProcessSpecification(ConstraintsBag bag, String name) {
		this(new TaskCharArchive(bag.getTaskChars()), bag, name);
	}
	
	public ProcessSpecification(TaskCharArchive taskCharArchive, ConstraintsBag bag) {
		this(taskCharArchive, bag, DEFAULT_NAME);
	}

	public ProcessSpecification(TaskCharArchive taskCharArchive, ConstraintsBag bag, String name) {
		this.taskCharArchive = taskCharArchive;
		this.bag = bag;
		this.name = name;
		this.pcs = new PropertyChangeSupport(this);
		this.bag.addPropertyChangeListener(this);
	}

	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Set<TaskChar> getProcessAlphabet() {
		return this.bag.getTaskChars();
	}

	public Automaton buildAutomaton() {
		return buildAutomatonByBondHeuristic();
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	public Automaton buildAutomaton(String nc) {
		return buildAutomatonByBondHeuristic(nc);
	}
	public Automaton buildNegativeAutomaton() {
		return buildNegativeAutomatonByBondHeuristic();
	}
	///////////////////////////////////////////////////////////////////////////////////////////////
	

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
		String alphabetLimitingRegularExpression = AutomatonUtils.createRegExpLimitingTheAlphabet(this.taskCharArchive.getIdentifiersAlphabet());
		
		for (TaskChar tChr : this.bag.getTaskChars()) {

			cns = this.bag.getConstraintsOf(tChr);
			regExps = new ArrayList<String>(cns.size());
			
			for (Constraint con : cns) {
				if (!con.isMarkedForExclusion()) {
					regExps.add(con.getRegularExpression());
				}
			}
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
	protected Automaton buildAutomatonByBondHeuristic() {
		Collection<String> regularExpressions = null;
		Collection<Constraint> constraints = LinearConstraintsIndexFactory.getAllUnmarkedConstraintsSortedByBondsSupportFamilyConfidenceCoverageHierarchyLevel(this.bag);
		regularExpressions = new ArrayList<String>(constraints.size());
		for (Constraint con : constraints) {
			regularExpressions.add(con.getRegularExpression());
		}
		return AutomatonFactory.fromRegularExpressions(regularExpressions, this.taskCharArchive.getIdentifiersAlphabet());
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	protected Automaton buildAutomatonByBondHeuristic(String nc) {
		Collection<String> regularExpressions = null;
		Collection<Constraint> constraints = LinearConstraintsIndexFactory.getAllUnmarkedConstraintsSortedByBondsSupportFamilyConfidenceCoverageHierarchyLevel(this.bag);
		regularExpressions = new ArrayList<String>(constraints.size());
		String[] ncobj = Arrays.stream(nc.split(",")).map(String::trim).toArray(String[]::new);
		List<String> ncList = Arrays.asList(ncobj);
		for (Constraint con : constraints) {
			if (ncList.contains(con.toString())){
				regularExpressions.add(con.getNegativeRegularExpression());
			}
			else {
				regularExpressions.add(con.getRegularExpression());
			}
		}
		return AutomatonFactory.fromRegularExpressions(regularExpressions, this.taskCharArchive.getIdentifiersAlphabet());
	}

	protected Automaton buildNegativeAutomatonByBondHeuristic() {
		Collection<String> regularExpressions = null;
		Collection<Constraint> constraints = LinearConstraintsIndexFactory.getAllUnmarkedConstraintsSortedByBondsSupportFamilyConfidenceCoverageHierarchyLevel(this.bag);
		regularExpressions = new ArrayList<String>(constraints.size());
		for (Constraint con : constraints) {
			regularExpressions.add(con.getNegativeRegularExpression());
		}
		return AutomatonFactory.fromRegularExpressions(regularExpressions, this.taskCharArchive.getIdentifiersAlphabet());
	}

	///////////////////////////////////////////////////////////////////////////////////////////////

	
	public TaskCharArchive getTaskCharArchive() {
		return this.taskCharArchive;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProcessSpecification [bag=");
		builder.append(bag);
		builder.append(", name=");
		builder.append(name);
		builder.append(", taskCharArchive=");
		builder.append(taskCharArchive);
		builder.append("]");
		return builder.toString();
	}

	public static ProcessSpecification generateNonEvaluatedDiscoverableSpecification(TaskCharArchive taskCharArchive) {
		ProcessSpecification proSpec = null;
		
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
		proSpec = new ProcessSpecification(taskCharArchive, bag);

		return proSpec;
	}

	public SortedSet<Constraint> getAllConstraints() {
		return LinearConstraintsIndexFactory.getAllConstraints(bag);
	}

	public SortedSet<Constraint> getAllUnmarkedConstraints() {
		return LinearConstraintsIndexFactory.getAllUnmarkedConstraints(bag);
	}

	public int howManyConstraints() {
		return bag.howManyConstraints();
	}

	public int howManyUnmarkedConstraints() {
		return bag.howManyUnmarkedConstraints();
	}

	public int howManyTasks() {
		return this.taskCharArchive.size();
	}

	public Set<TaskChar> getTasks() {
		return this.taskCharArchive.getTaskChars();
	}

	public void resetMarks() {
		for (TaskChar tCh : this.bag.getTaskChars()) {
			for (Constraint con : this.bag.getConstraintsOf(tCh)) {
				con.resetMarks();
			}
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		pcs.firePropertyChange(evt);
	}
	
}
