package minerful.index.comparator.modular.modules;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.index.comparator.modular.ModularConstraintsComparator;

public class ModActivationTargetBondsBasedComparator extends ModularConstraintsComparator {
	private static class ActivationTargetBondsBasedComparator implements Comparator<TaskChar> {
		private Map<TaskChar, Set<TaskChar>> relatedActivitiesPerActivity;
		
		public ActivationTargetBondsBasedComparator(
				SortedMap<TaskChar, Set<TaskChar>> relatedActivitiesPerActivity) {
			this.relatedActivitiesPerActivity = relatedActivitiesPerActivity;
		}

		@Override
		public int compare(TaskChar o1, TaskChar o2) {
			int result = o1.compareTo(o2);
			if (result != 0) {
				int triggerComparison = 0;
				if (relatedActivitiesPerActivity.containsKey(o1) && relatedActivitiesPerActivity.containsKey(o2)) {
					triggerComparison = new Integer(relatedActivitiesPerActivity.get(o1).size()).compareTo(relatedActivitiesPerActivity.get(o2).size());
					triggerComparison *= -1;
				}
				if (triggerComparison != 0) {
					result = triggerComparison;
				}
			}
			return result;
		}
	}
	
	private SortedMap<TaskChar, Set<TaskChar>> relatedActivitiesPerActivity;
	private HashMap<TaskChar, Integer> activationTargetBondsIndex;
//	private Map<TaskChar, Integer> activityIndexByChainedTargeting;

	public ModActivationTargetBondsBasedComparator(Collection<Constraint> constraints) {
		super();
		this.computeOrderingFunction(constraints);
	}

	public ModActivationTargetBondsBasedComparator(ModularConstraintsComparator secondLevelComparator, Collection<Constraint> constraints) {
		super(secondLevelComparator);
		this.computeOrderingFunction(constraints);
	}

	public void computeOrderingFunction(Collection<Constraint> constraints) {
		this.countRelatedActivitiesPerActivity(constraints);
		this.createIndex();
	}

	private void createIndex() {
		ActivationTargetBondsBasedComparator tasksComparator = new ActivationTargetBondsBasedComparator(relatedActivitiesPerActivity);
		// Sort the taskChars by the number of links to other activities through activated constraints
		SortedSet<TaskChar> tasksSortedByTargetedConstraintRelDegree = new TreeSet<TaskChar>(tasksComparator);
		tasksSortedByTargetedConstraintRelDegree.addAll(relatedActivitiesPerActivity.keySet());
		// Build an indexing hash-map for all tasks <task, index>
		this.activationTargetBondsIndex = new HashMap<TaskChar, Integer>(tasksSortedByTargetedConstraintRelDegree.size(), (float) 1.0);
		int i = 0;
		for (TaskChar tCh : tasksSortedByTargetedConstraintRelDegree) {
			this.activationTargetBondsIndex.put(tCh, new Integer(i++));
		}
//		System.err.println("Lurido merdone: " + this.activationTargetBondsIndex);
/*
		// Start creating the final indexing
		this.activityIndexByChainedTargeting = new HashMap<TaskChar, Integer>(tasksSortedByTargetedConstraintRelDegree.size(), (float) 1.0);
		// The first task in the list is the one with the highest number of other activities bound by triggered constraints
		int j = 0;
		TaskChar tCh = null;
		
		while (!tasksSortedByTargetedConstraintRelDegree.isEmpty()) {
			tCh = tasksSortedByTargetedConstraintRelDegree.first();
			this.activityIndexByChainedTargeting.put(tCh, j++);
			// Let us remove the already considered task from the bag
			tasksSortedByTargetedConstraintRelDegree.remove(tCh);
			// The second task in the ordering should be taken from the set of activities with which the first element relates to
			SortedSet<TaskChar> auxRelatedTaskCharsSortedByTriCReDeg = null;
			// We order the triggered-constraint-related activities by their triggered-constraint-relationship-degree index
			auxRelatedTaskCharsSortedByTriCReDeg = new TreeSet<TaskChar>(tasksComparator);
			auxRelatedTaskCharsSortedByTriCReDeg.addAll(this.relatedActivitiesPerActivity.get(tCh));
			
			Iterator<TaskChar> relatedSortedTaskCharsIterator = auxRelatedTaskCharsSortedByTriCReDeg.iterator();
			
			// Now, let us pick the first from this set... 
			while (relatedSortedTaskCharsIterator.hasNext()) {
				tCh = relatedSortedTaskCharsIterator.next();
				// If it is not already ranked...
				if (!this.activityIndexByChainedTargeting.containsKey(tCh)) {
					
				}
			}
			
			// ... add it to the index list ...
			this.activityIndexByChainedTargeting.put(tCh, j++);
			// ... remove it from the aux bag of task chars
			tasksSortedByTargetedConstraintRelDegree.remove(tCh);
			// ... and proceed with the depth-first search
			auxRelatedTaskCharsSortedByTriCReDeg = new TreeSet<TaskChar>(tasksComparator);
			auxRelatedTaskCharsSortedByTriCReDeg.addAll(this.relatedActivitiesPerActivity.get(tCh));
		}
*/
	}
	private void countRelatedActivitiesPerActivity(Collection<Constraint> constraints) {
		Set<TaskChar> relatedActivities = null;
		RelationConstraint relaCon = null;
		
		SortedMap<TaskChar, Set<TaskChar>> auxRelatedActivitiesPerActivity = new TreeMap<TaskChar, Set<TaskChar>>();

		for (Constraint con : constraints) {
			for (TaskChar base : con.getBase().getTaskCharsArray()) {
				if (!auxRelatedActivitiesPerActivity.keySet().contains(base)) {
					auxRelatedActivitiesPerActivity.put(base, new TreeSet<TaskChar>());
				}
				relatedActivities = auxRelatedActivitiesPerActivity.get(base);
				if (con.getImplied() != null) {
					for (TaskChar implied : con.getImplied().getTaskCharsArray()) {
						relatedActivities.add(implied);
					}
				} // in the case of existence constraints, add the base itself!
				else {
					relatedActivities.add(base);
				}
			} // in the case of mutual relation constraints, also the second parameter is an activation!
			if (con.getFamily() == ConstraintFamily.RELATION) {
				relaCon = (RelationConstraint) con;
				if (relaCon.getImplicationVerse() == ConstraintImplicationVerse.BOTH) {
					for (TaskChar revBase : con.getImplied().getTaskCharsArray()) {
						if (!auxRelatedActivitiesPerActivity.keySet().contains(revBase)) {
							auxRelatedActivitiesPerActivity.put(revBase, new TreeSet<TaskChar>());
						}
						relatedActivities = auxRelatedActivitiesPerActivity.get(revBase);
						for (TaskChar revImplied : con.getBase().getTaskCharsArray()) {
							relatedActivities.add(revImplied);
						}
					}
				}
			}
		}
//		System.err.println("Lurido merdone: " + auxRelatedActivitiesPerActivity);
		this.relatedActivitiesPerActivity = auxRelatedActivitiesPerActivity;
	}
	
	private Integer computeIndex(Constraint con) {
		Integer
			index = Integer.MAX_VALUE,
			comparison = Integer.MAX_VALUE;
		
		for (TaskChar tCh : con.getBase().getTaskCharsArray()) {
			comparison = this.activationTargetBondsIndex.get(tCh);
			index = (index < comparison ? index : comparison);
		}
//System.err.println("Lurido merdone: merdonazzo: per " + con + " index est " + index);
		
		if (con.getFamily() == ConstraintFamily.RELATION 
				&& ((RelationConstraint) con).getImplicationVerse() == ConstraintImplicationVerse.BOTH) {
//System.err.println("Lurido merdone: merdonazzo: ah, ma quisht est un bottimplichescionvers!");
			for (TaskChar tCh : con.getImplied().getTaskCharsArray()) {
				comparison = this.activationTargetBondsIndex.get(tCh);
				index = (index < comparison ? index : comparison);
				}
//System.err.println("Lurido merdone: merdonazzo: allora per " + con + " mo index fa " + index);
		}		
		return index;
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