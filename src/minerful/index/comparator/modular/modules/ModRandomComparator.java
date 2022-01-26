package minerful.index.comparator.modular.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.ModularConstraintsComparator;

import org.apache.commons.lang3.StringUtils;

public class ModRandomComparator extends ModularConstraintsComparator {
	private HashMap<Constraint, Integer> randomIndex;
//	private Map<TaskChar, Integer> activityIndexByChainedTargeting;

	public ModRandomComparator(Collection<Constraint> constraints) {
		super();
		this.computeOrderingFunction(constraints);
	}

	public ModRandomComparator(ModularConstraintsComparator secondLevelComparator, Collection<Constraint> constraints) {
		super(secondLevelComparator);
		this.computeOrderingFunction(constraints);
	}

	public void computeOrderingFunction(Collection<Constraint> constraints) {
		this.createIndex(constraints);
	}

	private void createIndex(Collection<Constraint> constraints) {
		ArrayList<Constraint> shuffledConstraints = new ArrayList<Constraint>(constraints);
		Collections.shuffle(shuffledConstraints);
		this.randomIndex = new HashMap<Constraint, Integer>(shuffledConstraints.size(), (float)1.0);
		Integer i = 0;
		for (Constraint cns : shuffledConstraints) {
			this.randomIndex.put(cns, i++);
		}
		logger.trace("Sorted constraints: " + StringUtils.join(shuffledConstraints, ", "));
	}
	
	private Integer computeIndex(Constraint con) {
		return randomIndex.get(con);
	}

	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = this.computeIndex(o1).compareTo(this.computeIndex(o2));
//System.err.println("Lurido merdone: merdonazzo: " + o1 + " against " + o2 + " fa " + result);
		if (result == 0)
			return super.compare(o1, o2);
		
		return result;
	}
}