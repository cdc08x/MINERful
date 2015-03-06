package minerful.concept.constraint;

public enum ConstraintFamily {
	// The natural order implemented by this method is the order in which the constants are declared.
	EXISTENCE_CONSTRAINT_FAMILY_ID,
	RELATION_CONSTRAINT_FAMILY_ID,
	RESPONDED_EXISTENCE_FAMILY_ID,
	CO_FAMILY_ID,
	NEGATIVE_RELATION_FAMILY_ID;
    
    public static enum ConstraintSubFamily {
    	NO_SUB_FAMILY_ID,
    	
    	RESPONSE_SUB_FAMILY_ID,
    	PRECEDENCE_SUB_FAMILY_ID
    	
    }
}