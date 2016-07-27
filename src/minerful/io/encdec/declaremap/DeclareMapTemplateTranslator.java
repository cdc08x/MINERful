package minerful.io.encdec.declaremap;

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

public class DeclareMapTemplateTranslator {

	public static HashMap<Class<? extends Constraint>, DeclareMapTemplate> NAME_CONVERTER =
			new HashMap<Class<? extends Constraint>, DeclareMapTemplate>(
					MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES + MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES,
					1.0F);
	public static HashMap<Class<? extends Constraint>, DeclareMapTemplate> INVERSE_NAME_CONVERTER =
			new HashMap<Class<? extends Constraint>, DeclareMapTemplate>(
					MetaConstraintUtils.NUMBER_OF_EXISTENCE_CONSTRAINT_TEMPLATES + MetaConstraintUtils.NUMBER_OF_RELATION_CONSTRAINT_TEMPLATES,
					1.0F);

	static {
		NAME_CONVERTER.put(Init.class, DeclareMapTemplate.Init);
//		NAME_CONVERTER.put(End.class, "end");
		NAME_CONVERTER.put(End.class, DeclareMapTemplate.Existence); // Declare does not cover the concept of "End"
		NAME_CONVERTER.put(Participation.class, DeclareMapTemplate.Existence);
		NAME_CONVERTER.put(AtMostOne.class, DeclareMapTemplate.Absence2);
		// Relation
		NAME_CONVERTER.put(RespondedExistence.class, DeclareMapTemplate.Responded_Existence);
		NAME_CONVERTER.put(Response.class, DeclareMapTemplate.Response);
		NAME_CONVERTER.put(Precedence.class, DeclareMapTemplate.Precedence);
		NAME_CONVERTER.put(AlternateResponse.class, DeclareMapTemplate.Alternate_Response);
		NAME_CONVERTER.put(AlternatePrecedence.class, DeclareMapTemplate.Alternate_Precedence);
		NAME_CONVERTER.put(ChainResponse.class, DeclareMapTemplate.Chain_Response);
		NAME_CONVERTER.put(ChainPrecedence.class, DeclareMapTemplate.Chain_Precedence);
		// Mutual relation
		NAME_CONVERTER.put(CoExistence.class, DeclareMapTemplate.CoExistence);
		NAME_CONVERTER.put(Succession.class, DeclareMapTemplate.Succession);
		NAME_CONVERTER.put(AlternateSuccession.class, DeclareMapTemplate.Alternate_Succession);
		NAME_CONVERTER.put(ChainSuccession.class, DeclareMapTemplate.Chain_Succession);
		// Negation relation
		NAME_CONVERTER.put(NotCoExistence.class, DeclareMapTemplate.Not_CoExistence);
		NAME_CONVERTER.put(NotChainSuccession.class, DeclareMapTemplate.Not_Chain_Succession);
		NAME_CONVERTER.put(NotSuccession.class, DeclareMapTemplate.Not_Succession);
	}

	public DeclareMapTemplate translateTemplateName(Class<? extends Constraint> constraintClass) {
		if (NAME_CONVERTER.containsKey(constraintClass)) {
			return NAME_CONVERTER.get(constraintClass);
		}
		return null;
	}
}