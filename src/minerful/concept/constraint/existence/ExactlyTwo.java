package minerful.concept.constraint.existence;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

@XmlRootElement
public class ExactlyTwo extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){2,2}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G( ( F(%1$s & X(F(%1$s))) | O(%1$s & Y(O(%1$s))) ) & ( %1$s -> X(G( %1$s -> X(G(!%1$s)) )) ) )";
    		// G( ( F(a & X(F(a))) | O(a & Y(O(a))) ) & ( a -> X(G( a -> X(G(!a)) )) ) )
    }

	protected ExactlyTwo() {
    	super();
    }

	public ExactlyTwo(TaskChar param1, double support) {
		super(param1, support);
	}
	public ExactlyTwo(TaskChar param1) {
		super(param1);
	}
	public ExactlyTwo(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public ExactlyTwo(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AtLeastOne(getBase());
	}
	
	@Override
	public Constraint[] suggestImpliedConstraints() {
		Constraint[] impliCons = null;
		Constraint[] inheritedImpliCons = super.suggestImpliedConstraints();
		int i = 0;

		if (inheritedImpliCons != null) {
			impliCons = new Constraint[inheritedImpliCons.length + 1];
			for (Constraint impliCon : inheritedImpliCons) {
				impliCons[i++] = impliCon;
			}
		} else {
			impliCons = new Constraint[1];
		}
		impliCons[i++] = new AtMostOne(getBase());
		
		return impliCons;
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.NUMEROSITY;
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new ExactlyTwo(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new ExactlyTwo(taskCharSets[0]);
	}
}