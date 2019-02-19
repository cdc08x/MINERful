package minerful.io.encdec;

import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSetFactory;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;

public class TransferObjectToConstraintTranslator {
	private TaskCharSetFactory taskCharSetFactory;
	
	public TransferObjectToConstraintTranslator(TaskCharArchive taskCharArchive) {
		this.taskCharSetFactory = new TaskCharSetFactory(taskCharArchive);
	}
	
	public Constraint createConstraint(DeclareConstraintTransferObject conTO) {
		if (conTO.minerFulTemplate != null) {
			Constraint minerFulConstraint =
				MetaConstraintUtils.makeConstraint(
					conTO.minerFulTemplate,
					this.taskCharSetFactory.createSetsFromTaskStringsCollection(
						conTO.parameters
					)
				);
			if (conTO.support != null) {
				minerFulConstraint.setSupport(conTO.support);
			}
			if (conTO.confidence != null) {
				minerFulConstraint.setConfidence(conTO.confidence);
			}
			if (conTO.interestFactor != null) {
				minerFulConstraint.setInterestFactor(conTO.interestFactor);
			}
			return minerFulConstraint;
		}
		return null;
	}
}