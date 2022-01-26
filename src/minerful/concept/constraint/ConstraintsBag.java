package minerful.concept.constraint;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.io.encdec.xml.ConstraintsBagAdapter;

/**
 * The class managing the set of constraints of a declarative process model.
 * 
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
@XmlRootElement
@XmlJavaTypeAdapter(ConstraintsBagAdapter.class)
//@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintsBag implements Cloneable, PropertyChangeListener {
	// @XmlTransient
	private static Logger logger = Logger.getLogger(ConstraintsBag.class.getCanonicalName());

	@XmlTransient
	private PropertyChangeSupport pcs;

	@XmlElementRef
	private Map<TaskChar, TreeSet<Constraint>> bag;

	// @XmlTransient
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
		this.pcs = new PropertyChangeSupport(this);
	}

	/**
	 * Adds a constraint.
	 * 
	 * @param constraint {@link Constraint} that should be added
	 * @return true if successfully added, otherwise false
	 */
	public boolean add(Constraint constraint) {
		return this.add(constraint.base, constraint);
	}

	/**
	 * Adds a constraint for the provided task character. If the task character does not exist,
	 * a new entry in the constraints bag is created.
	 * 
	 * @param taskChar   {@link TaskChar} for which the constraint should be added
	 * @param constraint {@link Constraint} that should be added for a task
	 *                   character
	 * @return true if successfully added, otherwise false
	 */
	public boolean add(TaskChar taskChar, Constraint constraint) {
		if (!this.bag.containsKey(taskChar)) {
			this.bag.put(taskChar, new TreeSet<>());
			this.taskChars.add(taskChar);
		}
		if (this.bag.get(taskChar).add(constraint)) {
			constraint.addPropertyChangeListener(this);
			return true;
		}
		return false;
	}

	/**
	 * Adds a task character to the constraints bag and the set of used task
	 * characters.
	 * 
	 * @param taskChar task character which should be added to the constraints bag
	 * @return true if added, otherwise false
	 */
	public boolean add(TaskChar taskChar) {
		if (!this.bag.containsKey(taskChar)) {
			this.bag.put(taskChar, new TreeSet<>());
			this.taskChars.add(taskChar);
			return true;
		}
		return false;
	}

	/**
	 * Adds task characters of task characters set with the provided constraint to
	 * the constraints bag.
	 * 
	 * @param taskCharSet Set of task characters added to the constraints bag
	 * @param constraint  constraints added for the task character
	 * @return true if constraint was added, otherwise false
	 */
	public boolean add(TaskCharSet taskCharSet, Constraint constraint) {
		boolean added = false;
		for (TaskChar tCh : taskCharSet.getTaskCharsArray()) {
			added = added || this.add(tCh, constraint);
		}
		return added;
	}

	/**
	 * Adds a collection of constraints for a task character.
	 * 
	 * @param taskChar             task character for which constraints should be
	 *                             added
	 * @param constraintCollection collection of constraints
	 * @return true if value was added, otherwise false
	 */
	public boolean addAll(TaskChar taskChar, Collection<? extends Constraint> constraintCollection) {
		this.add(taskChar);
		Set<Constraint> existingConSet = this.bag.get(taskChar);
		for (Constraint c : constraintCollection) {
			if (!existingConSet.contains(c)) {
				c.addPropertyChangeListener(this);
			}
		}
		return this.bag.get(taskChar).addAll(constraintCollection);
	}

	/**
	 * Removes a constraint for a specific task character.
	 * 
	 * @param taskChar   task character for which a constraint should be removed
	 * @param constraint constraint that should be removed
	 * @return true if constraint was removed, otherwise false
	 */
	public boolean remove(TaskChar taskChar, Constraint constraint) {
		if (!this.bag.containsKey(taskChar)) {
			return false;
		}
		if (this.bag.get(taskChar) == null) {
			return false;
		}
		if (this.bag.get(taskChar).remove(constraint)) {
			constraint.removePropertyChangeListener(this);
			return true;
		}
		return false;
	}

	/**
	 * Removes a constraint from every corresponding task character.
	 * 
	 * @param constraint constraint which should be removed
	 * @return true if at least one constraint was removed, otherwise false
	 */
	public boolean remove(Constraint constraint) {
		boolean removed = false;
		for (TaskChar tCh : constraint.base.getTaskCharsArray()) {
			if (this.bag.get(tCh) != null && this.bag.get(tCh).remove(constraint)) {
				removed = removed || Boolean.TRUE;
			}
		}
		return removed;
	}

	/**
	 * Replaces a constraint of a specific task character.
	 * 
	 * @param taskChar   corresponding task character
	 * @param constraint constraint that should be replaced
	 */
	public void replace(TaskChar taskChar, Constraint constraint) {
		this.remove(taskChar, constraint);
		this.add(taskChar, constraint);
	}

	/**
	 * Erases all constraints for a given task character.
	 * 
	 * @param taskChar corresponding task character
	 * @return number of removed constraints
	 */
	public int eraseConstraintsOf(TaskChar taskChar) {
		int constraintsRemoved = 0;
		if (this.bag.containsKey(taskChar)) {
			for (Constraint c : this.getConstraintsOf(taskChar)) {
				c.removePropertyChangeListener(this);
				constraintsRemoved++;
			}
			this.bag.put(taskChar, new TreeSet<Constraint>());
		}
		return constraintsRemoved;
	}
	
	/**
	 * Removes all constraints.
	 * 
	 * @return The number of erased constraints
	 */
	public int wipeOutConstraints() {
		int erasedConstraints = 0;
		for (TaskChar tChar : this.taskChars) {
			erasedConstraints += eraseConstraintsOf(tChar);
		}
		return erasedConstraints;
	}

	/**
	 * Get all task characters of the constraints bag
	 * 
	 * @return Set of task characters
	 */
	public Set<TaskChar> getTaskChars() {
		return this.taskChars;
	}

	/**
	 * Get all constraints for a specific task character
	 * 
	 * @param taskChar
	 * @return Set of constraints
	 */
	public Set<Constraint> getConstraintsOf(TaskChar taskChar) {
		if (this.bag.get(taskChar) == null)
			return new TreeSet<>();
		return this.bag.get(taskChar);
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
			for (Constraint c : this.getConstraintsOf(chr)) {
				clone.add(chr, c);
			}
		}
		return clone;
	}

	public ConstraintsBag createRedundantCopy(Collection<TaskChar> wholeAlphabet) {
		ConstraintsBag nuBag = (ConstraintsBag) this.clone();

		Collection<TaskChar> bases = wholeAlphabet;
		Collection<TaskChar> implieds = wholeAlphabet;

		for (TaskChar base : bases) {
			nuBag.addAll(base, MetaConstraintUtils.getAllDiscoverableExistenceConstraints(base));
			for (TaskChar implied : implieds) {
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
		ConstraintsBag nuBag = (ConstraintsBag) this.clone();
		for (TaskChar key : this.taskChars) {
			for (Constraint con : this.getConstraintsOf(key)) {
				if (con.hasSufficientSupport(supportThreshold)) {
					nuBag.remove(key, con);
				}
			}
		}

		return nuBag;
	}

	/**
	 * Returns the number of all constraints.
	 * 
	 * @return number of all constraints
	 */
	public int howManyConstraints() {
		// TODO: use util class for this
		int numberOfConstraints = 0;
		for (TaskChar tCh : this.getTaskChars()) {
			numberOfConstraints += (this.bag.get(tCh) == null ? 0 : this.bag.get(tCh).size());
		}
		return numberOfConstraints;
	}

	/**
	 * Returns the number of all unmarked constraints.
	 * 
	 * @return number of all unmarked constraints
	 */
	public int howManyUnmarkedConstraints() {
		// TODO: use util class for this
		int i = 0;
		for (TaskChar key : this.getTaskChars())
			for (Constraint c : this.getConstraintsOf(key))
				if (!c.isMarkedForExclusion())
					i++;
		return i;
	}

	/**
	 * Returns the number of existence constraints.
	 * 
	 * @return
	 */
	public Long howManyExistenceConstraints() {
		// TODO: use util class for this
		long i = 0L;
		for (TaskChar key : this.taskChars)
			for (Constraint c : this.getConstraintsOf(key))
				if (MetaConstraintUtils.isExistenceConstraint(c))
					i++;
		return i;
	}

	/**
	 * Set the alphabet/task characters for the constraints bag.
	 * 
	 * @param alphabet collection of task characters
	 */
	public void setAlphabet(Collection<TaskChar> alphabet) {
		for (TaskChar taskChr : alphabet) {
			if (!this.bag.containsKey(taskChr)) {
				this.bag.put(taskChr, new TreeSet<Constraint>());
				this.taskChars.add(taskChr);
			}
		}
	}

	/**
	 * Checks if a task character is in the alphabet of the constraint bag.
	 * 
	 * @param taskChar taskCharacter which should be checked for
	 * @return true if task character is part of the alphabet, otherwise false
	 */
	public boolean contains(TaskChar taskChar) {
		return this.taskChars.contains(taskChar);
	}

	/**
	 * Merges the task characters of another constraints bag with this constraints
	 * bag. The constraints of an existing task character are replaced with the
	 * constraints of the other constraints bag.
	 * 
	 * @param otherConstraintsBag constraints bag which should be merged
	 */
	public void merge(ConstraintsBag otherConstraintsBag) {
		// TODO: use util class for this
		for (TaskChar taskChar : otherConstraintsBag.taskChars) {
			this.shallowReplace(taskChar, otherConstraintsBag.bag.get(taskChar));
		}
	}

	/**
	 * Merges the task characters of another constraints bag with this constraints
	 * bag. Unlike merge, it does not replace the constraints of an already existing
	 * task character.
	 * 
	 * @param otherConstraintsBag constraints bag which should be merged
	 */
	public void shallowMerge(ConstraintsBag otherConstraintsBag) {
		// TODO: use util class for this
		for (TaskChar taskChar : otherConstraintsBag.taskChars) {
			if (this.contains(taskChar)) {
				this.addAll(taskChar, otherConstraintsBag.getConstraintsOf(taskChar));
			} else {
				this.shallowReplace(taskChar, otherConstraintsBag.bag.get(taskChar));
			}
		}
	}

	public void shallowReplace(TaskChar taskChar, TreeSet<Constraint> cs) {
		this.bag.put(taskChar, cs);
	}

	/**
	 * Remove all marked constraints from the constraints bag.
	 * 
	 * @return number of removed constraints
	 */
	public int removeMarkedConstraints() {
		// TODO: use util class for this
		Constraint auxCon = null;
		int markedConstraintsRemoved = 0;
		for (TaskChar taskChar : this.taskChars) {
			if (this.bag.get(taskChar) != null) {
				Iterator<Constraint> constraIter = this.bag.get(taskChar).iterator();
				while (constraIter.hasNext()) {
					auxCon = constraIter.next();
					if (auxCon.isMarkedForExclusionOrForbidden()) {
						constraIter.remove();
						markedConstraintsRemoved++;
					}
				}
			}
		}
		return markedConstraintsRemoved;
	}

	/**
	 * Creates a new constraints bag which contains all constraints for the provided
	 * task characters of this constraints bag.
	 * 
	 * @param indexingTaskCharGroup task characters that should be part of the new
	 *                              constraints bag
	 * @return new constraints bag
	 */
	public ConstraintsBag slice(Set<TaskChar> indexingTaskCharGroup) {
		// TODO: use util class for this
		ConstraintsBag slicedBag = new ConstraintsBag(indexingTaskCharGroup);

		for (TaskChar indexingTaskChar : indexingTaskCharGroup) {
			slicedBag.bag.put(indexingTaskChar, this.bag.get(indexingTaskChar));
		}

		return slicedBag;
	}

	/**
	 * Returns all constraints of the constraints bag.
	 * 
	 * @return set of constraints
	 */
	public Set<Constraint> getAllConstraints() {
		Set<Constraint> constraints = new TreeSet<Constraint>(); 
		for (TaskChar tCh : this.getTaskChars()) { constraints.addAll(this.getConstraintsOf(tCh)); }
		return constraints;
	}

	/**
	 * Returns only fully supported constraints of the constraints bag.
	 * 
	 * @return collection of constraints
	 */
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

	/**
	 * Returns only fully supported constraints of the constraints bag in a new
	 * constraints bag.
	 * 
	 * @return new constraints bag with all fully supported constraints
	 */
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
