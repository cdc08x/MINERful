package minerful.concept.constraint;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;
import minerful.concept.constraint.relation.CouplingRelationConstraint;
import minerful.concept.constraint.relation.NegativeRelationConstraint;
import minerful.concept.constraint.xmlenc.ConstraintsBagMapAdapter;

import org.apache.log4j.Logger;

@XmlRootElement(name="processModelConstraints")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintsBag implements Cloneable {
	@XmlTransient
	private static Logger logger = Logger.getLogger(ConstraintsBag.class.getCanonicalName());

	@XmlJavaTypeAdapter(value=ConstraintsBagMapAdapter.class)
    private Map<TaskChar, TreeSet<Constraint>> bag;
	
	@XmlTransient
	private SortedSet<TaskChar> taskChars = new TreeSet<TaskChar>();
 
	public ConstraintsBag() {
		this(new TreeSet<TaskChar>());
	}
	
    public ConstraintsBag(Set<TaskChar> taskChars) {
        this.bag = new TreeMap<TaskChar, TreeSet<Constraint>>();
        this.setAlphabet(taskChars);
    }
	
    public ConstraintsBag(Set<TaskChar> taskChars, Collection<Constraint> constraints) {
        this.bag = new TreeMap<TaskChar, TreeSet<Constraint>>();
        this.setAlphabet(taskChars);
        for (Constraint con : constraints) {
        	this.add(con.base, con);
        }
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

    public boolean remove(Constraint c) {
        return this.bag.get(c.base).remove(c);
    }

	public void replace(TaskChar tCh, Constraint uniqueness) {
        if (!this.bag.containsKey(tCh)) {
            this.bag.put(tCh, new TreeSet<Constraint>());
            this.taskChars.add(tCh);
        }
        this.bag.get(tCh).remove(uniqueness);
        this.bag.get(tCh).add(uniqueness);
	}

	public void replaceConstraints(TaskChar taskChar, Collection<? extends Constraint> cs) {
		this.bag.put(taskChar, new TreeSet<Constraint>());
	}
	
	public void add(TaskChar tCh) {
        if (!this.bag.containsKey(tCh)) {
            this.bag.put(tCh, new TreeSet<Constraint>());
            this.taskChars.add(tCh);
        }
	}

    public boolean addAll(TaskChar tCh, Collection<? extends Constraint> cs) {
        this.add(tCh);
        return this.bag.get(tCh).addAll(cs);
    }

    public Set<TaskChar> getTaskChars() {
        return this.taskChars;
    }

    public Set<Constraint> getConstraintsOf(TaskChar character) {
        return this.bag.get(character);
    }

    public Constraint get(TaskChar character, Constraint searched) {
   		return this.bag.get(character).headSet(searched, true).last();
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConstraintsBag [bag=");
		builder.append(bag);
		builder.append(", taskChars=");
		builder.append(taskChars);
		builder.append("]");
		return builder.toString();
	}

	@Override
    public Object clone() {
        ConstraintsBag clone = new ConstraintsBag(this.taskChars);
        for (TaskChar chr : this.taskChars) {
            for (Constraint c: this.bag.get(chr)) {
                clone.add(chr, c);
            }
        }
        return clone;
    }

    public ConstraintsBag createRedundantCopy(Collection<TaskChar> wholeAlphabet) {
    	ConstraintsBag nuBag =
                (ConstraintsBag) this.clone();
        
        Collection<TaskChar> bases = wholeAlphabet;
        Collection<TaskChar> implieds = wholeAlphabet;
        
        for (TaskChar base: bases) {
        	nuBag.addAll(base, MetaConstraintUtils.getAllExistenceConstraints(base));
        	for (TaskChar implied: implieds) {
        		if (!base.equals(implied))
        			nuBag.addAll(base, MetaConstraintUtils.getAllRelationConstraints(base, implied));
        	}
        }
        
        return nuBag;
    }
    
    public ConstraintsBag createEmptyIndexedCopy() {
    	return new ConstraintsBag(getTaskChars());
    }

    public ConstraintsBag markSubsumptionRedundantConstraints() {
    	return this.markSubsumptionRedundantConstraints(this.taskChars);
    }

    public ConstraintsBag markSubsumptionRedundantConstraints(Collection<TaskChar> targetTaskChars) {
        ConstraintsBag constraintsBag =
//                (TaskCharRelatedConstraintsBag) this.clone();
        		this;
    	
        // exploit the ordering
        CouplingRelationConstraint coExiCon = null;
        NegativeRelationConstraint noRelCon = null;

        for (TaskChar key : targetTaskChars) {
            for (Constraint currCon : this.bag.get(key)) {
            	if (!currCon.isRedundant()) {
	                if (currCon.hasConstraintToBaseUpon()) {
	                    if (currCon.isMoreReliableThanGeneric()) {
	                    	logger.trace("Removing the genealogy of " +
	                    			currCon.getConstraintWhichThisIsBasedUpon() +
	                    			" because " +
	                    			currCon +
	                    			" is subsuming and more reliable");
	                        destroyGenealogy(currCon.getConstraintWhichThisIsBasedUpon(), currCon, key, constraintsBag);
	                    } else {
	                    	logger.trace("Removing " +
	                    			currCon +
	                    			" because " +
	                    			currCon.getConstraintWhichThisIsBasedUpon() +
	                    			" is more reliable and this is based upon it");
//	                        constraintsBag.remove(key, currCon);
	                        currCon.redundant = true;
	                    }
	                }
	                if (currCon.getSubFamily() == RelationConstraintSubFamily.COUPLING) {
	                    coExiCon = (CouplingRelationConstraint) currCon;
	                    if (coExiCon.hasImplyingConstraints()) {
	                        if (coExiCon.isMoreReliableThanTheImplyingConstraints()) {
	                        	logger.trace("Removing " +
	                        			coExiCon.getForwardConstraint() +
	                        			", which is the forward, and " +
	                        			coExiCon.getBackwardConstraint() +
	                        			", which is the backward, because " +
	                        			coExiCon +
	                        			" is the Mutual Relation referring to them and more reliable");
	                        	// constraintsBag.remove(key, coExiCon.getForwardConstraint());
	                        	coExiCon.getForwardConstraint().redundant = true;
	                        	// constraintsBag.remove(key, coExiCon.getBackwardConstraint());
	                        	coExiCon.getForwardConstraint().redundant = true;
//	                        } else if (coExiCon.isMoreReliableThanAnyOfImplyingConstraints()){
//	                        	// Remove the weaker, if any
//	                        	if (coExiCon.isMoreReliableThanForwardConstraint()) {
//	                        		nuBag.remove(key, coExiCon.getForwardConstraint());
//	                        	} else {
//	                        		nuBag.remove(key, coExiCon.getBackwardConstraint());
//	                        	}
	                        } else {
//	                        	constraintsBag.remove(key, coExiCon);
	                        	coExiCon.redundant = true;
	                        }
	                    }
	                }
	                if (currCon.getSubFamily() == RelationConstraintSubFamily.NEGATIVE) {
	                    noRelCon = (NegativeRelationConstraint) currCon;
	                    if (noRelCon.hasOpponent()) {
	                        if (noRelCon.isMoreReliableThanTheOpponent()) {
	                        	logger.trace("Removing " +
	                        			noRelCon.getOpponent() +
	                        			" because it is the opponent of " +
	                        			noRelCon +
	                        			" but less reliable");
//	                            constraintsBag.remove(key, noRelCon.getOpponent());
	                            noRelCon.getOpponent().redundant = true;
	                        } else {
	                        	logger.trace("Removing " +
	                        			noRelCon +
	                        			" because it is the opponent of " +
	                        			noRelCon.getOpponent() +
	                        			" but less reliable");
//	                            constraintsBag.remove(key, noRelCon);
	                            noRelCon.redundant = true;
	                        }
	                    }
	
	                }
            	}
            }
        }
        return constraintsBag;
    }

    private ConstraintsBag destroyGenealogy(
            Constraint lastSon,
            Constraint lastSurvivor,
            TaskChar key,
            ConstraintsBag genealogyTree) {
        Constraint genealogyDestroyer = lastSon;
//        ConstraintImplicationVerse destructionGeneratorsFamily = lastSurvivor.getSubFamily();
        while (genealogyDestroyer != null) {
        	// The ancestor of *Precedence(a, b) is RespondedExistence(b, a), thus under a different indexing character!
        	// TODO: solve this issue, because "binary" Precedence and branched Precedences do not work the same in this regard!
//        	if (	destructionGeneratorsFamily.equals(ConstraintImplicationVerse.BACKWARD)
//        		&&	!genealogyDestroyer.isBranched()
//        		&&	!genealogyDestroyer.getSubFamily().equals(ConstraintImplicationVerse.BACKWARD)
//        	) {
        		key = genealogyDestroyer.base.getFirstTaskChar();
//        	}
//            genealogyTree.remove(key, genealogyDestroyer);
            genealogyDestroyer.redundant = true;
            genealogyDestroyer = genealogyDestroyer.getConstraintWhichThisIsBasedUpon();
        }

        return genealogyTree;
    }
    
    public ConstraintsBag markConstraintsBelowThresholds(double supportThreshold, double confidence, double interest) {
        ConstraintsBag nuBag =
//                (TaskCharRelatedConstraintsBag) this.clone();
        		this;
        for (TaskChar key : this.taskChars) {
            for (Constraint con : this.bag.get(key)) {
            	con.belowSupportThreshold = !con.hasSufficientSupport(supportThreshold);
            	con.belowConfidenceThreshold = !con.isConfident(confidence);
            	con.belowInterestFactorThreshold = !con.isOfInterest(interest);
//            	if (!(con.hasSufficientSupport(supportThreshold) && con.isConfident(confidence) && con.isOfInterest(interest))) {
//					nuBag.getConstraintsOf(key).remove(con);
//				}
            }
        }
        
        return nuBag;
    }

    public ConstraintsBag markConstraintsBelowSupportThreshold(double supportThreshold) {
        return markConstraintsBelowThresholds(supportThreshold, Constraint.DEFAULT_CONFIDENCE, Constraint.DEFAULT_INTEREST_FACTOR);
    }

    public ConstraintsBag createComplementOfCopyPrunedByThreshold(double supportThreshold) {
        ConstraintsBag nuBag =
                (ConstraintsBag) this.clone();
        for (TaskChar key : this.taskChars) {
            for (Constraint con : this.bag.get(key)) {
            	if (con.hasSufficientSupport(supportThreshold)) {
					nuBag.getConstraintsOf(key).remove(con);
				}
            }
        }
        
        return nuBag;
    }

	public int howManyConstraints() {
		int i = 0;
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

	public boolean contains(TaskChar tCh) {
		return this.taskChars.contains(tCh);
	}

	public void merge(ConstraintsBag other) {
		for (TaskChar tCh : other.taskChars) {
			this.addAll(tCh, other.getConstraintsOf(tCh));
		}
	}

	public void removeMarkedConstraints() {
		Constraint auxCon = null;
		for (TaskChar tChar : this.taskChars) {
			Iterator<Constraint> constraIter = this.getConstraintsOf(tChar).iterator();
			while (constraIter.hasNext()) {
				auxCon = constraIter.next();
//System.out.println("Lurido merdone, ora controllo: " + auxCon + " che est subsunto = " + auxCon.isRedundant());
				if (auxCon.isMarkedForExclusion()) {
//System.out.println("Lurido merdone, ora tolgo: " + auxCon);
					constraIter.remove();
				}
			}
		}
	}
}