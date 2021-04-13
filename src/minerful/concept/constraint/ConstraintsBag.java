package minerful.concept.constraint;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.io.encdec.xml.ConstraintsBagAdapter;

import org.apache.log4j.Logger;

/**
 * The class managing the set of constraints of a declarative process model.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
@XmlRootElement
@XmlJavaTypeAdapter(ConstraintsBagAdapter.class)
//@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintsBag extends Observable implements Cloneable, Observer {
//	@XmlTransient
	private static Logger logger = Logger.getLogger(ConstraintsBag.class.getCanonicalName());
	
	@XmlElementRef
    private Map<TaskChar, TreeSet<Constraint>> bag;
	
//	@XmlTransient
	private Set<TaskChar> taskChars = new TreeSet<TaskChar>();
	
	public ConstraintsBag() {
		this(new TreeSet<TaskChar>());
	}
	
    public ConstraintsBag(Collection<TaskChar> taskChars) {
    	this.initBag();
        this.setAlphabet(taskChars);
    }

    public ConstraintsBag(Set<TaskChar> taskChars, Collection<Constraint> constraints) {
    	this.initBag();
        this.setAlphabet(taskChars);
        for (Constraint con : constraints) {
        	this.add(con.base, con);
        }
    }
    
	private void initBag() {
		this.bag = new TreeMap<TaskChar, TreeSet<Constraint>>();
	}
	
	public boolean add(Constraint c) {
		if (this.add(c.base, c)) {
			return true;
		}
		return false;
	}

    public boolean add(TaskChar tCh, Constraint c) {
    	if (!this.bag.containsKey(tCh)) {
            this.bag.put(tCh, new TreeSet<Constraint>());
            this.taskChars.add(tCh);
        }
    	if (this.bag.get(tCh).add(c)) {
    		c.addObserver(this);
    		return true;
    	}
    	return false;
    }

    public boolean add(TaskCharSet taskCharSet, Constraint c) {
    	boolean added = false;
    	for (TaskChar tCh : taskCharSet.getTaskCharsArray()) {
    		added = added || this.add(tCh, c);
    	}
    	return added;
    }

    public boolean remove(TaskChar character, Constraint c) {
        if (!this.bag.containsKey(character)) {
            return false;
        }
        if (this.bag.get(character) == null) {
        	return false;
        }
        if (this.bag.get(character).remove(c)) {
        	c.deleteObserver(this);
        	return true;
        }
        return false;
    }

    public boolean remove(Constraint c) {
    	boolean removed = false;
    	for(TaskChar tCh : c.base.getTaskCharsArray()) {
    		if (this.bag.get(tCh) != null) {
		        if (this.bag.get(tCh).remove(c)) {
		        	removed = removed || true;
		        }
    		}
    	}
        return removed;
    }

	public void replace(TaskChar tCh, Constraint constraint) {
        this.remove(tCh, constraint);
        this.add(tCh, constraint);
        
	}

	public int eraseConstraintsOf(TaskChar taskChar) {
		int constraintsRemoved = 0;
		if (this.bag.containsKey(taskChar)) {
			for (Constraint c : this.getConstraintsOf(taskChar)) {
				c.deleteObserver(this);
				constraintsRemoved++;
			}
			this.bag.put(taskChar, new TreeSet<Constraint>());
		}
		return constraintsRemoved;
	}
	
	/**
	 * Removes all constraints.
	 * @return The number of erased constraints
	 */
	public int wipeOutConstraints() {
		int erasedConstraints = 0;
		for (TaskChar tChar : this.taskChars) {
			erasedConstraints += eraseConstraintsOf(tChar);
		}
		return erasedConstraints;
	}

	public boolean add(TaskChar tCh) {
        if (!this.bag.containsKey(tCh)) {
            this.bag.put(tCh, new TreeSet<Constraint>());
            this.taskChars.add(tCh);
            return true;
        }
        return false;
	}

    public boolean addAll(TaskChar tCh, Collection<? extends Constraint> cs) {
    	this.add(tCh);
    	Set<Constraint> existingConSet = this.bag.get(tCh);
    	for (Constraint c : cs) {
    		if (!existingConSet.contains(c)) {
    			c.addObserver(this);
    		}
    	}
        return this.bag.get(tCh).addAll(cs);
    }

    public Set<TaskChar> getTaskChars() {
        return this.taskChars;
    }

    public Set<Constraint> getConstraintsOf(TaskChar character) {
    	if (this.bag.get(character) == null)
    		return new TreeSet<Constraint>();
        return this.bag.get(character);
    }

    public Constraint get(TaskChar character, Constraint searched) {
    	if (!this.bag.containsKey(character)) {
    		return null;
    	}
    	TreeSet<Constraint> cnsOf = this.bag.get(character);
    	if (cnsOf == null)
    		return null;
    	if (!cnsOf.contains(searched)) {
    		return null;
    	}
    	NavigableSet<Constraint> srCnss = cnsOf.headSet(searched, true);
    	if (srCnss.isEmpty()) {
    		return null;
    	}
   		return srCnss.last();
    }
    
    public Constraint getOrAdd(TaskChar character, Constraint searched) {
    	Constraint con = this.get(character, searched);
		if (con == null) {
			this.add(character, searched);
			con = this.get(character, searched);
		}
		return con;
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
            for (Constraint c: this.getConstraintsOf(chr)) {
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
        	nuBag.addAll(base, MetaConstraintUtils.getAllDiscoverableExistenceConstraints(base));
        	for (TaskChar implied: implieds) {
        		if (!base.equals(implied))
        			nuBag.addAll(base, MetaConstraintUtils.getAllDiscoverableRelationConstraints(base, implied));
        	}
        }
        
        return nuBag;
    }
    
    public ConstraintsBag createEmptyIndexedCopy() {
    	return new ConstraintsBag(getTaskChars());
    }

    public ConstraintsBag createComplementOfCopyPrunedByThreshold(double supportThreshold) {
        ConstraintsBag nuBag =
                (ConstraintsBag) this.clone();
        for (TaskChar key : this.taskChars) {
            for (Constraint con : this.getConstraintsOf(key)) {
            	if (con.hasSufficientSupport(supportThreshold)) {
					nuBag.remove(key, con);
				}
            }
        }
        
        return nuBag;
    }

	public int howManyConstraints() {
		int numberOfConstraints = 0;
		for (TaskChar tCh : this.getTaskChars()) {
			numberOfConstraints += (this.bag.get(tCh) == null ? 0 : this.bag.get(tCh).size());
		}
		return numberOfConstraints;
	}

	public int howManyUnmarkedConstraints() {
		int i = 0;
		for (TaskChar key : this.getTaskChars())
        	for (Constraint c : this.getConstraintsOf(key))
        		if (!c.isMarkedForExclusion())
        			i++;
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

	public void setAlphabet(Collection<TaskChar> alphabet) {
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
			this.shallowReplace(tCh, other.bag.get(tCh));
		}
	}

	public void shallowMerge(ConstraintsBag other) {
		for (TaskChar tCh : other.taskChars) {
			if (this.contains(tCh)) {
				this.addAll(tCh, other.getConstraintsOf(tCh));
			} else {
				this.shallowReplace(tCh, other.bag.get(tCh));
			}
		}
	}

	public void shallowReplace(TaskChar taskChar, TreeSet<Constraint> cs) {
		this.bag.put(taskChar, cs);
	}

	public int removeMarkedConstraints() {
		Constraint auxCon = null;
		int markedConstraintsRemoved = 0;
		for (TaskChar tChar : this.taskChars) {
			if (this.bag.get(tChar) != null) {
				Iterator<Constraint> constraIter = this.bag.get(tChar).iterator();
				while (constraIter.hasNext()) {
					auxCon = constraIter.next();
					if (auxCon.isMarkedForExclusion()) {
						constraIter.remove();
						markedConstraintsRemoved++;
					}
				}
			}
		}
		return markedConstraintsRemoved;
	}
	
	public ConstraintsBag slice(Set<TaskChar> indexingTaskCharGroup) {
		ConstraintsBag slicedBag = new ConstraintsBag(indexingTaskCharGroup);
		
		for (TaskChar indexingTaskChar : indexingTaskCharGroup) {
			slicedBag.bag.put(indexingTaskChar, this.bag.get(indexingTaskChar));
		}
		
		return slicedBag;
	}

	public Set<Constraint> getAllConstraints() {
		Set<Constraint> constraints = new TreeSet<Constraint>(); 
		for (TaskChar tCh : this.getTaskChars()) { constraints.addAll(this.getConstraintsOf(tCh)); }
		return constraints;
	}
	
	public Collection<Constraint> getOnlyFullySupportedConstraints() {
		Collection<Constraint> constraints = new TreeSet<Constraint>(); 
		for (TaskChar tCh : this.getTaskChars()) { 
			for (Constraint cns : this.getConstraintsOf(tCh)) {
				if (cns.hasMaximumSupport()) {
					constraints.add(cns);
				}
			}
		}
		return constraints;
	}
	
	public ConstraintsBag getOnlyFullySupportedConstraintsInNewBag() {
		ConstraintsBag clone = (ConstraintsBag) this.clone();
		for (TaskChar tCh : clone.getTaskChars()) { 
			for (Constraint cns : this.getConstraintsOf(tCh)) {
				if (!cns.hasMaximumSupport()) {
					clone.remove(tCh, cns);
				}
			}
		}
		return clone;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (Constraint.class.isAssignableFrom(o.getClass())) {
			this.setChanged();
			this.notifyObservers(arg);
			this.clearChanged();
		}
	}
}