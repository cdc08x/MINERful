package minerful.io.encdec.declaremap;

import java.util.HashMap;

import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.ExactlyOne;
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
import minerful.utils.MessagePrinter;

public class DeclareMapToMinerFulTemplatesTranslator {

	public static HashMap<Class<? extends Constraint>, DeclareMapTemplate> MINERFUL_2_DECLARE_MAP =
			new HashMap<Class<? extends Constraint>, DeclareMapTemplate>(
					MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES + MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES,
					1.0F);
	public static HashMap<DeclareMapTemplate, Class<? extends Constraint>> DECLARE_2_MINERFUL_MAP =
			new HashMap<DeclareMapTemplate, Class<? extends Constraint>>(
					MetaConstraintUtils.NUMBER_OF_EXISTENCE_CONSTRAINT_TEMPLATES + MetaConstraintUtils.NUMBER_OF_RELATION_CONSTRAINT_TEMPLATES,
					1.0F);
	
//	public static HashMap<String, Class<? extends Constraint>> STRING_2_MINERFUL_MAP =

	static {
		MINERFUL_2_DECLARE_MAP.put(Init.class, DeclareMapTemplate.Init);
		MINERFUL_2_DECLARE_MAP.put(End.class, DeclareMapTemplate.Existence); // Declare does not cover the concept of "End"
		MINERFUL_2_DECLARE_MAP.put(Participation.class, DeclareMapTemplate.Existence);
		MINERFUL_2_DECLARE_MAP.put(AtMostOne.class, DeclareMapTemplate.Absence2);
		// Relation
		MINERFUL_2_DECLARE_MAP.put(RespondedExistence.class, DeclareMapTemplate.Responded_Existence);
		MINERFUL_2_DECLARE_MAP.put(Response.class, DeclareMapTemplate.Response);
		MINERFUL_2_DECLARE_MAP.put(Precedence.class, DeclareMapTemplate.Precedence);
		MINERFUL_2_DECLARE_MAP.put(AlternateResponse.class, DeclareMapTemplate.Alternate_Response);
		MINERFUL_2_DECLARE_MAP.put(AlternatePrecedence.class, DeclareMapTemplate.Alternate_Precedence);
		MINERFUL_2_DECLARE_MAP.put(ChainResponse.class, DeclareMapTemplate.Chain_Response);
		MINERFUL_2_DECLARE_MAP.put(ChainPrecedence.class, DeclareMapTemplate.Chain_Precedence);
		// Mutual relation
		MINERFUL_2_DECLARE_MAP.put(CoExistence.class, DeclareMapTemplate.CoExistence);
		MINERFUL_2_DECLARE_MAP.put(Succession.class, DeclareMapTemplate.Succession);
		MINERFUL_2_DECLARE_MAP.put(AlternateSuccession.class, DeclareMapTemplate.Alternate_Succession);
		MINERFUL_2_DECLARE_MAP.put(ChainSuccession.class, DeclareMapTemplate.Chain_Succession);
		// Negation relation
		MINERFUL_2_DECLARE_MAP.put(NotCoExistence.class, DeclareMapTemplate.Not_CoExistence);
		MINERFUL_2_DECLARE_MAP.put(NotChainSuccession.class, DeclareMapTemplate.Not_Chain_Succession);
		MINERFUL_2_DECLARE_MAP.put(NotSuccession.class, DeclareMapTemplate.Not_Succession);
		
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Init, Init.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Existence, Participation.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Absence2, AtMostOne.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Exactly1, ExactlyOne.class);
		// Relation
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Responded_Existence, RespondedExistence.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Response, Response.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Precedence, Precedence.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Alternate_Response, AlternateResponse.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Alternate_Precedence, AlternatePrecedence.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Chain_Response, ChainResponse.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Chain_Precedence, ChainPrecedence.class);
		// Mutual relation
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.CoExistence, CoExistence.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Succession, Succession.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Alternate_Succession, AlternateSuccession.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Chain_Succession, ChainSuccession.class);
		// Negation relation
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Not_CoExistence, NotCoExistence.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Not_Chain_Succession, NotChainSuccession.class);
		DECLARE_2_MINERFUL_MAP.put(DeclareMapTemplate.Not_Succession, NotSuccession.class);
	}

	public static DeclareMapTemplate translateTemplateName(Class<? extends Constraint> constraintClass) {
		if (MINERFUL_2_DECLARE_MAP.containsKey(constraintClass)) {
			return MINERFUL_2_DECLARE_MAP.get(constraintClass);
		}
		return null;
	}
	public static Class<? extends Constraint> translateTemplateName(DeclareMapTemplate declareMapTemplate) {
		if (DECLARE_2_MINERFUL_MAP.containsKey(declareMapTemplate)) {
			return DECLARE_2_MINERFUL_MAP.get(declareMapTemplate);
		} else {
			MessagePrinter.getInstance(DeclareMapEncoderDecoder.class).warn("Unmapped native Declare Map template: " + declareMapTemplate);
			return null;
		}
	}
}