package minerful.index.comparator.modular;

import java.util.Collection;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.modules.ModActivationTargetBondsBasedComparator;
import minerful.index.comparator.modular.modules.ModConfidenceBasedComparator;
import minerful.index.comparator.modular.modules.ModDefaultComparator;
import minerful.index.comparator.modular.modules.ModFamilyBasedComparator;
import minerful.index.comparator.modular.modules.ModHierarchyBasedComparator;
import minerful.index.comparator.modular.modules.ModInterestFactorBasedComparator;
import minerful.index.comparator.modular.modules.ModRandomComparator;
import minerful.index.comparator.modular.modules.ModSupportBasedComparator;

public class ModularConstraintsComparatorFactory {
	private Collection<Constraint> constraints;

	public ModularConstraintsComparatorFactory(Collection<Constraint> constraints) {
		this.constraints = constraints;
	}
	
	public ModularConstraintsComparator createModularComparator(ConstraintSortingPolicy... types) {
		return createModularComparator(true, types);
	}
	
	public ModularConstraintsComparator createModularComparator(boolean lastLevelComparatorIsRandom, ConstraintSortingPolicy... types) {
		// Starting from the last one (i.e., the one which discriminates at the finest level of granularity
		int i = types.length - 1;
		ModularConstraintsComparator
			subCompa = (
					lastLevelComparatorIsRandom
					? new ModRandomComparator(this.constraints)
					:	(	types.length > 1
							? createModularComparator(types[i--])
							: new ModDefaultComparator()
						)
					),
			compa = subCompa;
		for (; i >= 0; i--) {
			compa = createModularComparator(subCompa, types[i]);
			subCompa = compa;
		}
				
		return compa;
	}

	private ModularConstraintsComparator createModularComparator(ConstraintSortingPolicy type) {
		return createModularComparator(null, type);
	}

	private ModularConstraintsComparator createModularComparator(ModularConstraintsComparator nextLevelComparator, ConstraintSortingPolicy type) {
		ModularConstraintsComparator
			compa = null,
			subCompa = null;
		
		switch(type) {
		case ACTIVATIONTARGETBONDS:
			compa = new ModActivationTargetBondsBasedComparator(nextLevelComparator, this.constraints);
			break;
		case FAMILYHIERARCHY:
			subCompa = new ModHierarchyBasedComparator(nextLevelComparator);
			compa = new ModFamilyBasedComparator(subCompa);
			break;
		case SUPPORTCONFIDENCEINTERESTFACTOR:
			subCompa = new ModInterestFactorBasedComparator(nextLevelComparator);
			compa = new ModConfidenceBasedComparator(subCompa);
			subCompa = compa;
			compa = new ModSupportBasedComparator(subCompa);
			break;
		case RANDOM:
			compa = new ModRandomComparator(nextLevelComparator, constraints);
			break;
		case DEFAULT:
			compa = new ModDefaultComparator(nextLevelComparator);
			break;
		default:
			throw new UnsupportedOperationException("Modular comparator " + type + " is not yet implemented.");
		}
		
		return compa;
	}
}