package minerful.concept.constraint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NegatedRelationConstraint;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.concept.constraint.xmlenc.ConstraintsBagMapAdapter;
import minerful.io.ConstraintsPrinter;

import org.apache.log4j.Logger;

@XmlRootElement(name="processModelConstraints")
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskCharRelatedConstraintsBag implements Cloneable {
	@XmlTransient
	private static Logger logger = Logger.getLogger(TaskCharRelatedConstraintsBag.class.getCanonicalName());

	@XmlJavaTypeAdapter(value=ConstraintsBagMapAdapter.class)
    private Map<TaskChar, Set<Constraint>> bag;
	
	@XmlTransient
	private SortedSet<TaskChar> taskChars = new TreeSet<TaskChar>();
 
	private TaskCharRelatedConstraintsBag() {}
	
    public TaskCharRelatedConstraintsBag(Set<TaskChar> taskChars) {
    	this();
        this.bag = new HashMap<TaskChar, Set<Constraint>>(taskChars.size(), (float) 1.0);
        this.setAlphabet(taskChars);
    }
    
    public boolean add(TaskChar tCh, Constraint c) {
    	if (!this.bag.containsKey(tCh)) {
            this.bag.put(tCh, new TreeSet<Constraint>());
            this.taskChars.add(tCh);
        }
    	return this.bag.get(tCh).add(c);
    }

    public boolean add(TaskCharSet taskCharSet, Constraint c) {
    	boolean added = true;
    	for (TaskChar tCh : taskCharSet.getTaskChars()) {
    		added = added && this.add(tCh, c);
    	}
    	return added;
    }

    public boolean remove(TaskChar character, Constraint c) {
        if (!this.bag.containsKey(character)) {
            return false;
        }
        return this.bag.get(character).remove(c);
    }

	public void replaceConstraints(TaskChar taskChar, Collection<? extends Constraint> cs) {
		this.bag.put(taskChar, new TreeSet<Constraint>());
	}

    public boolean addAll(TaskChar character, Collection<? extends Constraint> cs) {
        if (!this.bag.containsKey(character)) {
            this.bag.put(character, new TreeSet<Constraint>());
            this.taskChars.add(character);
        }
        return this.bag.get(character).addAll(cs);
    }

    public Set<TaskChar> getTaskChars() {
        return this.taskChars;
    }

    public Set<Constraint> getConstraintsOf(TaskChar character) {
        return this.bag.get(character);
    }

    @Override
    public String toString() {
        return "CharacterRelatedConstraintsBag{" + new ConstraintsPrinter(this).printBag() + '}';
    }

	@Override
    public Object clone() {
        TaskCharRelatedConstraintsBag clone = new TaskCharRelatedConstraintsBag(this.taskChars);
        for (TaskChar chr : this.taskChars) {
            for (Constraint c: this.bag.get(chr)) {
                clone.add(chr, c);
            }
        }
        return clone;
    }

    public TaskCharRelatedConstraintsBag createRedundantCopy(Collection<TaskChar> wholeAlphabet) {
        MetaConstraintUtils conUtils = new MetaConstraintUtils();
    	TaskCharRelatedConstraintsBag nuBag =
                (TaskCharRelatedConstraintsBag) this.clone();
        
        Collection<TaskChar> bases = wholeAlphabet;
        Collection<TaskChar> implieds = wholeAlphabet;
        
        for (TaskChar base: bases) {
        	nuBag.addAll(base, conUtils.getAllExistenceConstraints(base));
        	for (TaskChar implied: implieds) {
        		if (!base.equals(implied))
        			nuBag.addAll(base, conUtils.getAllRelationConstraints(base, implied));
        	}
        }
        
        return nuBag;
    }

    public TaskCharRelatedConstraintsBag createHierarchyUnredundantCopy() {
        TaskCharRelatedConstraintsBag nuBag =
                (TaskCharRelatedConstraintsBag) this.clone();

        // exploit the ordering
        RelationConstraint currCon = null;
        CoExistence coExiCon = null;
        NegatedRelationConstraint noRelCon = null;

        for (TaskChar key : this.taskChars) {
            for (Constraint c : this.bag.get(key)) {
                if (c instanceof RelationConstraint) {
                    currCon = (RelationConstraint) c;

                    if (currCon.hasConstraintToBeBasedUpon()) {
                        if (currCon.isMoreReliableThanGeneric()) {
                        	logger.trace("Removing the genealogy of " +
                        			currCon.getConstraintWhichThisIsBasedUpon() +
                        			" because " +
                        			currCon +
                        			" is subsuming and more reliable");
                            nuBag = destroyGenealogy(currCon.getConstraintWhichThisIsBasedUpon(), key, nuBag);
                        } else {
                        	logger.trace("Removing " +
                        			currCon +
                        			" because " +
                        			currCon.getConstraintWhichThisIsBasedUpon() +
                        			" is more reliable and this is based upon it");
                            nuBag.remove(key, currCon);
                        }
                    }
                    if (currCon.getFamily() == CoExistence.CO_FAMILY_ID) {
                        coExiCon = (CoExistence) currCon;
                        if (coExiCon.hasImplyingConstraints()) {
                            if (coExiCon.isMoreReliableThanTheImplyingConstraints()) {
                            	logger.trace("Removing the genealogy of " +
                            			coExiCon.getForwardConstraint() +
                            			", which is the forward, and the genealogy of " +
                            			coExiCon.getBackwardConstraint() +
                            			", which is the backward, because " +
                            			coExiCon +
                            			" is the Mutual Relation referring to them and more reliable");
// BUGFIX: these two lines worked horribly, if, say, you have ChainSuccession(A, B), ChainResponse(A, B) and ChainPrecedence(A, B) sharing the same support, equal to 0.
//                                nuBag = destroyGenealogy(coExiCon.getForwardConstraint(), key, nuBag);
//                                nuBag = destroyGenealogy(coExiCon.getBackwardConstraint(), key, nuBag);
                            	nuBag.remove(key, coExiCon.getForwardConstraint());
                            	nuBag.remove(key, coExiCon.getBackwardConstraint());
//                                nuBag.remove(key, coExiCon.getForwardConstraint());
//                                nuBag.remove(key, coExiCon.getBackwardConstraint());
                            } else {
//                                nuBag.remove(key, coExiCon);
                            }
                        }
                    }
                    if (currCon.getFamily() == NegatedRelationConstraint.NOT_FAMILY_ID) {
                        noRelCon = (NegatedRelationConstraint) currCon;
                        if (noRelCon.hasOpponent()) {
                            if (noRelCon.isMoreReliableThanTheOpponent()) {
                            	logger.trace("Removing " +
                            			noRelCon.getOpponent() +
                            			" because it is the opponent of " +
                            			noRelCon +
                            			" but less reliable");
                                nuBag.remove(key, noRelCon.getOpponent());
                            } else {
                            	logger.trace("Removing " +
                            			noRelCon +
                            			" because it is the opponent of " +
                            			noRelCon.getOpponent() +
                            			" but less reliable");
                                nuBag.remove(key, noRelCon);
                            }
                        }

                    }
                }
            }
        }
        return nuBag;
    }

    private TaskCharRelatedConstraintsBag destroyGenealogy(
            RelationConstraint lastSon,
            TaskChar key,
            TaskCharRelatedConstraintsBag genealogyTree) {
        RelationConstraint genealogyDestroyer = lastSon;
        int destructionGeneratorsFamily = lastSon.getSubFamily();
        while (genealogyDestroyer != null) {
        	// The ancestor of *Precedence(a, b) is RespondedExistence(b, a), thus under a different indexing character!
        	// TODO: solve this issue, because "binary" Precedence and branched Precedences do not work the same in this regard!
        	if (destructionGeneratorsFamily == Precedence.PRECEDENCE_SUB_FAMILY_ID && !genealogyDestroyer.isBranched() && genealogyDestroyer.getFamily() != Precedence.PRECEDENCE_SUB_FAMILY_ID) {
        		key = genealogyDestroyer.base.getFirstTaskChar();
        	}
            genealogyTree.remove(key, genealogyDestroyer);
            genealogyDestroyer = genealogyDestroyer.getConstraintWhichThisIsBasedUpon();
        }

        return genealogyTree;
    }
    
    public TaskCharRelatedConstraintsBag createCopyPrunedByThresholdConfidenceAndInterest(double threshold, double confidence, double interest) {
        TaskCharRelatedConstraintsBag nuBag =
                (TaskCharRelatedConstraintsBag) this.clone();
        for (TaskChar key : this.taskChars) {
            for (Constraint con : this.bag.get(key)) {
            	if (!(con.hasSufficientSupport(threshold) && con.isConfident(confidence) && con.isOfInterest(interest))) {
					nuBag.getConstraintsOf(key).remove(con);
				}
            }
        }
        
        return nuBag;
    }

	public Long howManyConstraints() {
		long i = 0L;
        for (TaskChar key : this.taskChars) {
        	i += this.bag.get(key).size();
        }
		return i;
	}

	public Long howManyExistenceConstraints() {
		long i = 0L;
        for (TaskChar key : this.taskChars)
        	for (Constraint c : this.getConstraintsOf(key))
        		if (MetaConstraintUtils.isExistenceConstraint(c))
        			i++;
		return i;
	}

	public void setAlphabet(Set<TaskChar> alphabet) {
		for (TaskChar taskChr : alphabet) {
			if (!this.bag.containsKey(taskChr)) {
				this.bag.put(taskChr, new TreeSet<Constraint>());
				this.taskChars.add(taskChr);
			}
		}
    }
}