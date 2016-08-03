package minerful.io.encdec;

import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSetFactory;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotCoExistence;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;

public class TransferObjectToConstraintTranslator {
	private TaskCharArchive taskCharArchive;
	private TaskCharSetFactory taskCharSetFactory;
	
	public TransferObjectToConstraintTranslator(TaskCharArchive taskCharArchive) {
		this.taskCharArchive = taskCharArchive;
		this.taskCharSetFactory = new TaskCharSetFactory(taskCharArchive);
	}
	
	public Constraint createConstraint(DeclareConstraintTransferObject conTO) {
		Constraint minerFulConstraint =
			MetaConstraintUtils.makeConstraint(
				conTO.minerfulTemplate,
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
}