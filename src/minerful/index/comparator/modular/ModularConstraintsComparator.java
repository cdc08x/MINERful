package minerful.index.comparator.modular;

import java.util.Comparator;

import org.apache.log4j.Logger;

import minerful.concept.constraint.Constraint;

public abstract class ModularConstraintsComparator implements Comparator<Constraint> {
	protected static Logger logger = Logger.getLogger(ModularConstraintsComparator.class);
	
	private ModularConstraintsComparator secondLevelComparator;

	public ModularConstraintsComparator(ModularConstraintsComparator secondLevelComparator) {
		this.secondLevelComparator = secondLevelComparator;
	}

	public ModularConstraintsComparator() {
		this.secondLevelComparator = null;
	}

	/*
	 * Does basically nothing but invoking the second-level comparator, if any
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Constraint o1, Constraint o2) {
		if (this.secondLevelComparator != null)
			return this.secondLevelComparator.compare(o1, o2);
		return o1.compareTo(o2);
	}
}