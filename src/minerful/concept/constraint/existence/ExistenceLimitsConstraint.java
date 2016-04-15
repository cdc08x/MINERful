/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

/**
 * Substituted by {@link Participation}
 * @author cdc
 */
@Deprecated
public abstract class ExistenceLimitsConstraint extends Constraint {
    public static final int NO_MAX_EXISTENCE_CONSTRAINT = Integer.MAX_VALUE;
    public static final int NO_MIN_EXISTENCE_CONSTRAINT = 0;
    private int minimum;
    private int maximum;

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public void setMinimum(TaskChar param1, int minimum) {
        this.minimum = minimum;
    }

    public ExistenceLimitsConstraint(TaskChar param1, int minimum, int maximum) {
        super(param1);
        this.minimum = minimum;
        this.maximum = maximum;
    }
    
    @Override
    public String toString() {
        return  super.toString()
                + "{ [" 
                + (
                    this.minimum == NO_MIN_EXISTENCE_CONSTRAINT
                    ?   "0" :
                    String.valueOf(this.minimum)
                )
                + "…" 
                + (
                    this.maximum == NO_MAX_EXISTENCE_CONSTRAINT
                    ?   "*" :
                    String.valueOf(this.maximum)
                ) 
                + "] }";
    }

    @Override
    public int compareTo(Constraint t) {
        int result = this.base.compareTo(t.getBase());
        if (result == 0) {
            if (!this.getClass().getCanonicalName().equals(t.getClass().getCanonicalName())) {
                return 1;
            }
        }
        return result;
    }

	@Override
	public TaskCharSet getImplied() {
		return null;
	}
}