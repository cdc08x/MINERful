package minerful.concept.constraint.existence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

@XmlType
public abstract class ExistenceConstraint extends Constraint {
    protected ExistenceConstraint() {
    	super();
    }
	
	public ExistenceConstraint(TaskChar param1, double support) {
        super(param1, support);
    }
    public ExistenceConstraint(TaskChar param1) {
        super(param1);
    }
    public ExistenceConstraint(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public ExistenceConstraint(TaskCharSet param1) {
		super(param1);
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
        return super.toString();
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
    public ExistenceConstraintSubFamily getSubFamily() {
        return ExistenceConstraintSubFamily.NONE;
    }
}