package minerful.index.comparator.allinone;

import minerful.concept.constraint.Constraint;

public class TemplateAndParametersBasedComparator extends TemplateBasedComparator {
	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = super.compare(o1, o2);
		if (result == 0) {
			result = o1.compareTo(o2);
		}
		return result;
	}
}