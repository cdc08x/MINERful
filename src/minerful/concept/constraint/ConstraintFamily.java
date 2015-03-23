package minerful.concept.constraint;

public enum ConstraintFamily {
	// The natural order implemented by this method is the order in which the constants are declared.
	EXISTENCE,
	COUPLING,
	RELATION,
	NEGATIVE;
    
    public static enum ConstraintSubFamily {
    	NONE,
    	RESPONSE,
    	PRECEDENCE
    }
}