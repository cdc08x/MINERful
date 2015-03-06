package minerful.io.encdec.declare;

import java.util.HashMap;

import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.End;
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

public class DeclareTemplateTranslator {

	public static HashMap<Class<? extends Constraint>, DeclareTemplate> NAME_CONVERTER =
			new HashMap<Class<? extends Constraint>, DeclareTemplate>(
					MetaConstraintUtils.NUMBER_OF_POSSIBLE_EXISTENCE_CONSTRAINT_TEMPLATES + MetaConstraintUtils.NUMBER_OF_POSSIBLE_RELATION_CONSTRAINT_TEMPLATES,
					1.0F);

	static {
		NAME_CONVERTER.put(Init.class, DeclareTemplate.Init);
//		NAME_CONVERTER.put(End.class, "end");
		NAME_CONVERTER.put(End.class, DeclareTemplate.Existence); // Declare does not cover the concept of "End"
		NAME_CONVERTER.put(Participation.class, DeclareTemplate.Existence);
		NAME_CONVERTER.put(AtMostOne.class, DeclareTemplate.Absence2);
		// Relation
		NAME_CONVERTER.put(RespondedExistence.class, DeclareTemplate.Responded_Existence);
		NAME_CONVERTER.put(Response.class, DeclareTemplate.Response);
		NAME_CONVERTER.put(Precedence.class, DeclareTemplate.Precedence);
		NAME_CONVERTER.put(AlternateResponse.class, DeclareTemplate.Alternate_Response);
		NAME_CONVERTER.put(AlternatePrecedence.class, DeclareTemplate.Alternate_Precedence);
		NAME_CONVERTER.put(ChainResponse.class, DeclareTemplate.Chain_Response);
		NAME_CONVERTER.put(ChainPrecedence.class, DeclareTemplate.Chain_Precedence);
		// Mutual relation
		NAME_CONVERTER.put(CoExistence.class, DeclareTemplate.CoExistence);
		NAME_CONVERTER.put(Succession.class, DeclareTemplate.Succession);
		NAME_CONVERTER.put(AlternateSuccession.class, DeclareTemplate.Alternate_Succession);
		NAME_CONVERTER.put(ChainSuccession.class, DeclareTemplate.Chain_Succession);
		// Negation relation
		NAME_CONVERTER.put(NotCoExistence.class, DeclareTemplate.Not_CoExistence);
		NAME_CONVERTER.put(NotChainSuccession.class, DeclareTemplate.Not_Chain_Succession);
		NAME_CONVERTER.put(NotSuccession.class, DeclareTemplate.Not_Succession);
	}

	public DeclareTemplate translateTemplateName(Class<? extends Constraint> constraintClass) {
		if (NAME_CONVERTER.containsKey(constraintClass)) {
			return NAME_CONVERTER.get(constraintClass);
		}
		return null;
	}
}