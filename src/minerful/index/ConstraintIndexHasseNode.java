package minerful.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;

public class ConstraintIndexHasseNode implements Comparable<ConstraintIndexHasseNode> {
	public HashMap<Class<? extends Constraint>, Constraint> constraints;
	public final TaskCharSet indexedTaskCharSet;
	public final ConstraintIndexHasseNode parent;
	public final Collection<ConstraintIndexHasseNode> uncles;
	public final SortedMap<TaskChar, ConstraintIndexHasseNode> children;
	public final UUID identifier;

	public ConstraintIndexHasseNode(
			ConstraintIndexHasseNode parent,
			TaskCharSet indexedTaskCharSet,
			SortedMap<TaskChar, ConstraintIndexHasseNode> children) {
		this.identifier = UUID.randomUUID();
		this.constraints = new HashMap<Class<? extends Constraint>, Constraint>(
				MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES,
				(float) 1.0);
		this.uncles = new ArrayList<ConstraintIndexHasseNode>();
		this.parent = parent;
		this.children = children;
		this.indexedTaskCharSet = indexedTaskCharSet;
	}
	
	@Override
	public String toString() {
		StringBuilder sBuil = new StringBuilder();
		sBuil.append("\n");
		sBuil.append("[ID=");
		sBuil.append(this.identifier);
		sBuil.append("]\n");
		
		for (Class<? extends Constraint> key : this.constraints.keySet()) {
			sBuil.append(this.constraints.get(key));
		}
		
		for (TaskChar traversingKey : this.children.keySet()) {
			sBuil.append("\n");
			sBuil.append(traversingKey);
			sBuil.append(this.children.get(traversingKey).toString());
		}
		return sBuil.toString();
	}
	
	public Collection<ConstraintIndexHasseNode> getParentAndUncles() {
		Collection<ConstraintIndexHasseNode> relatives = new ArrayList<ConstraintIndexHasseNode>(
				this.uncles.size() +
				(parent == null ? 0 : 1)
		);
		if (parent != null)
			relatives.add(parent);
		if (uncles != null && uncles.size() > 0)
			relatives.addAll(uncles);
		return relatives;
	}
	
	public String toPrefixedPathString() {
		return this.toPrefixedPathString("");
	}

	public String toPrefixedPathString(String prefix) {
		StringBuilder sBuil = new StringBuilder();
		sBuil.append("\n");
		for (Constraint constraint : this.constraints.values()) {
			sBuil.append("Constraints: ");
//			if (!constraint.redundant) {
				sBuil.append(constraint);
				sBuil.append(" => ");
				sBuil.append(constraint.getSupport());
				if (constraint.isRedundant())
					sBuil.append(" (redundant)");
				sBuil.append("\n");
//			}
		}
		
		String prefixForRecursion = "";
		for (TaskChar traversingKey : this.children.keySet()) {
			prefixForRecursion = prefix + '.' + traversingKey;
			
			sBuil.append("\n");
			sBuil.append(prefixForRecursion);
			sBuil.append(this.children.get(traversingKey).toPrefixedPathString(prefixForRecursion));
		}
		return sBuil.toString();
	}

	ConstraintIndexHasseNode(ConstraintIndexHasseNode parent, TaskCharSet taskCharSet) {
		this(parent, taskCharSet, new TreeMap<TaskChar, ConstraintIndexHasseNode>());
	}
	
	public void addConstraint(Constraint c) {
		this.constraints.put(c.getClass(), c);
	}

	@Override
	public int compareTo(ConstraintIndexHasseNode o) {
		return this.identifier.compareTo(o.identifier);
	}
}