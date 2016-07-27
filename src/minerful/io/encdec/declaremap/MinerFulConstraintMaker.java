package minerful.io.encdec.declaremap;

import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSetFactory;
import minerful.concept.constraint.Constraint;
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

public class MinerFulConstraintMaker {
	private TaskCharArchive taskCharArchive;
	private TaskCharSetFactory taskCharSetFactory;
	
	public MinerFulConstraintMaker(TaskCharArchive taskCharArchive) {
		this.taskCharArchive = taskCharArchive;
		this.taskCharSetFactory = new TaskCharSetFactory(taskCharArchive);
	}

	public Constraint createConstraintOutOfTransferObject(DeclareMapConstraintTransferObject deMapCTO) {
		Constraint minerFulConstraint = null;
		
		switch(deMapCTO.template) {
		case Responded_Existence:
			minerFulConstraint = new RespondedExistence(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Response:
			minerFulConstraint = new Response(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Alternate_Response:
			minerFulConstraint = new AlternateResponse(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Chain_Response:
			minerFulConstraint = new ChainResponse(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Precedence:
			minerFulConstraint = new Precedence(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Alternate_Precedence:
			minerFulConstraint = new AlternatePrecedence(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Chain_Precedence:
			minerFulConstraint = new ChainPrecedence(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case CoExistence:
			minerFulConstraint = new CoExistence(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Succession:
			minerFulConstraint = new Succession(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Alternate_Succession:
			minerFulConstraint = new AlternateSuccession(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Chain_Succession:
			minerFulConstraint = new ChainSuccession(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Not_Chain_Succession:
			minerFulConstraint = new NotChainSuccession(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Not_Succession:
			minerFulConstraint = new NotSuccession(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Not_CoExistence:
			minerFulConstraint = new NotCoExistence(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0)),
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(1))
			);
			break;
		case Absence2:
			minerFulConstraint = new AtMostOne(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0))
			);
			break;
		case Existence:
			minerFulConstraint = new Participation(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0))
			);
			break;
		case Init:
			minerFulConstraint = new Init(
					this.taskCharSetFactory.createSetFromTaskStrings(deMapCTO.parameters.get(0))
			);
			break;
		default:
			return null;
		}
		
		minerFulConstraint.setSupport(deMapCTO.support);
		minerFulConstraint.setConfidence(deMapCTO.confidence);
		minerFulConstraint.setInterestFactor(deMapCTO.interestFactor);
		
		return minerFulConstraint;
	}
}