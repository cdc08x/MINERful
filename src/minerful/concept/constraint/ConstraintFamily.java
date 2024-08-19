package minerful.concept.constraint;

public enum ConstraintFamily implements Comparable<ConstraintFamily> {
	// The natural order implemented by this method is the order in which the constants are declared.
	EXISTENCE,
	RELATION;
	
	public static interface ConstraintSubFamily {
	}
	
    public enum RelationConstraintSubFamily implements ConstraintSubFamily {
    	POSITIVE,
    	POSITIVE_MUTUAL,
    	NEGATIVE,
    	NEGATIVE_MUTUAL,
    	NONE,
    }

    public enum ExistenceConstraintSubFamily implements ConstraintSubFamily {
    	POSITION,
    	NUMEROSITY,
    	NONE,
    }

    public enum ConstraintImplicationVerse {
    	BOTH,
    	FORWARD,
    	BACKWARD
    }
}
