package minerful.concept.constraint.relation;

import minerful.concept.constraint.Constraint;

public interface NegativeRelationConstraintInterface {

	boolean isMoreReliableThanTheOpponent();

	RelationConstraint getOpponent();

	void setOpponent(RelationConstraint opponent);

	Constraint suggestOpponentConstraint();

	boolean hasOpponent();

}