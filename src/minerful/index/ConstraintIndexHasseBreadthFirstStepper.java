package minerful.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class ConstraintIndexHasseBreadthFirstStepper extends ConstraintIndexHasseManager {
	public final NavDirection navDirection;
	private Collection<ConstraintIndexHasseNode> nodesUnderAnalysis;
	private boolean evenStep = false;
	
	public ConstraintIndexHasseBreadthFirstStepper(ConstraintIndexHasseDiagram hasseDiagram, NavDirection navDirection) {
		super(hasseDiagram);
		this.navDirection = navDirection;
		
		switch (this.navDirection) {
		case DOWN:
			this.nodesUnderAnalysis = new ArrayList<ConstraintIndexHasseNode>(this.hasseDiagram.root.children.size());
			for (Entry<TaskChar, ConstraintIndexHasseNode> entry : this.hasseDiagram.root.children.entrySet())
				this.nodesUnderAnalysis.add(entry.getValue());
			break;
		case UP:
			this.nodesUnderAnalysis = hasseDiagram.getSinkNodes();
			break;
		}
	}
	
	public Collection<TaskCharSet> getCurrentTaskCharSetsInBreadthFirstVisit() {
		TreeSet<TaskCharSet> tChSets = new TreeSet<TaskCharSet>();
		
		for (ConstraintIndexHasseNode node : this.nodesUnderAnalysis) {
			tChSets.add(node.indexedTaskCharSet);
		}
		
		return (!evenStep ? tChSets : tChSets.descendingSet());
	}
	
	public Collection<ConstraintIndexHasseNode> getCurrentNodesInBreadthFirstVisit() {
		return this.nodesUnderAnalysis;
	}
	
	public boolean moveOneStepAhead() {
		TreeSet<ConstraintIndexHasseNode> nextNodesUnderAnalysis = new TreeSet<ConstraintIndexHasseNode>();
		switch (this.navDirection) {
		case DOWN:
			for (ConstraintIndexHasseNode node : this.nodesUnderAnalysis) {
				nextNodesUnderAnalysis.addAll(node.children.values());
			}
			break;
		case UP:
			for (ConstraintIndexHasseNode node : this.nodesUnderAnalysis) {
				nextNodesUnderAnalysis.addAll(node.getParentAndUncles());	// duplicates will be automatically removed!
			}
			break;
		}
		
		if (nextNodesUnderAnalysis.size() < 1)
			return false;
		
		this.nodesUnderAnalysis = nextNodesUnderAnalysis;
		
		evenStep = !evenStep;
		
		return true;
	}

}