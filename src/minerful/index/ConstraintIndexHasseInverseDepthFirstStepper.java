package minerful.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;

import minerful.concept.TaskCharSet;

public class ConstraintIndexHasseInverseDepthFirstStepper extends ConstraintIndexHasseManager {
	private Stack<ListIterator<ConstraintIndexHasseNode>> stackOfIterators;

	public ConstraintIndexHasseInverseDepthFirstStepper(ConstraintIndexHasseDiagram hasseDiagram) {
		super(hasseDiagram);
		this.stackOfIterators = new Stack<ListIterator<ConstraintIndexHasseNode>>();
		this.resetCurrentPointers();
		this.preSetUpStackOfIterators(this.hasseDiagram.root);
		this.setUpStackOfIterators();
	}
	
	private void resetCurrentPointers() {
		this.currentNode = this.hasseDiagram.root;
		this.currentTaskCharSet = this.currentNode.indexedTaskCharSet;
	}
	
	private void setUpStackOfIterators() {
		if (this.stackOfIterators.size() > 0)
			this.updateCurrentPointers();
	}

	private void preSetUpStackOfIterators(ConstraintIndexHasseNode ancestor) {
		while (ancestor.children.size() > 0) {
			this.stackOfIterators.push(new ArrayList<ConstraintIndexHasseNode>(ancestor.children.values()).listIterator());
			ancestor = ancestor.children.get(ancestor.children.firstKey());
		}
	}
	
	private void updateCurrentPointers() {
		this.currentNode = this.stackOfIterators.peek().next();
		this.currentTaskCharSet = this.currentNode.indexedTaskCharSet;
	}

	public ConstraintIndexHasseNode getCurrentNode() {
		return currentNode;
	}

	public TaskCharSet getCurrentTaskCharSet() {
		return currentTaskCharSet;
	}

	public boolean moveOneStepAhead() {
		if (!this.stackOfIterators.isEmpty()) {
			if (!this.stackOfIterators.peek().hasNext()) {
				this.stackOfIterators.pop();
				if (this.stackOfIterators.isEmpty()) {
					this.resetCurrentPointers();
					return false;
				} else {
					this.updateCurrentPointers();
					return true;
				}
			} else {
				if (this.stackOfIterators.peek().hasPrevious()) { // is it a following sibling? Before visiting it, try and see whether there is a hierarchy below!
					ListIterator<ConstraintIndexHasseNode> explorator = this.stackOfIterators.peek();
					ConstraintIndexHasseNode newRoot = explorator.next();
					this.preSetUpStackOfIterators(newRoot);
					explorator.previous();
					this.updateCurrentPointers();
					return true;
				} else {
					this.updateCurrentPointers();
					return true;
				}
			}
			
		} else {
			this.resetCurrentPointers();
			return false;
		}
	}
	
	public boolean isThereAnyNodeLeftToAnalyse() {
		return !(this.currentNode == this.hasseDiagram.root);
	}
}