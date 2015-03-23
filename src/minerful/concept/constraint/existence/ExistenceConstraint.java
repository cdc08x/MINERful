package minerful.concept.constraint.existence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlTransient;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;

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

	public static String toExistenceQuantifiersString(Participation least, AtMostOne atMost) {
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
    
    @Override
    public ConstraintFamily getFamily() {
        return ConstraintFamily.EXISTENCE;
    }
    @Override
    public ConstraintSubFamily getSubFamily() {
        return ConstraintSubFamily.NONE;
    }
}