package minerful.concept.constraint;

public enum ConstraintFamily {
	// The natural order implemented by this method is the order in which the constants are declared.
	EXISTENCE,
	RELATION;
	
	public static interface ConstraintSubFamily {
		
	}
	
    public static enum RelationConstraintSubFamily implements ConstraintSubFamily {
    	NONE,
    	SINGLE_ACTIVATION,
    	COUPLING,
    	NEGATIVE
    }

    public static enum ExistenceConstraintSubFamily implements ConstraintSubFamily {
    	NONE,
    	NUMEROSITY,
    	POSITION
    }

    public static enum ConstraintImplicationVerse {
    	BOTH,
    	FORWARD,
    	BACKWARD
    }
}