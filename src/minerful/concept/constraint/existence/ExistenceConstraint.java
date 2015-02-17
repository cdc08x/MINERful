package minerful.concept.constraint.existence;

import java.util.Collection;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public abstract class ExistenceConstraint extends Constraint {

    public ExistenceConstraint(TaskChar base, double support) {
        super(base, support);
    }
    public ExistenceConstraint(TaskChar base) {
        super(base);
    }
    public ExistenceConstraint(TaskCharSet base, double support) {
		super(base, support);
	}
	public ExistenceConstraint(TaskCharSet base) {
		super(base);
	}

	public static String toExistenceQuantifiersString(Participation least, Uniqueness atMost) {
        String min = "0",
                max = "*";
        if (least != null) {
            min = "1";
        }
        if (atMost != null) {
            max = "*";
        }
        return "[ " + min + " ... " + max + " ]";
    }

    @Override
    public int compareTo(Constraint t) {
        int result = super.compareTo(t);
        if (result == 0) {
            return this.getClass().getCanonicalName().compareTo(
                    t.getClass().getCanonicalName());
        }
        return result;
    }
    

    @Override
    public String toString() {
        return super.toString()
                + "(" + base + ")";
    }

	@Override
	public TaskCharSet getImplied() {
		return null;
	}
	

	@Override
	public Collection<TaskChar> getInvolvedTaskChars() {
		return this.base.getTaskCharsCollection();
	}
}