package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

public class Exactly1 extends AtLeast1 { // Multiple inheritance is not allowed in Java, but there should be AtMostOne too, here
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){1,1}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
//    	return "G((F(%1$s) | O(%1$s)) & (%1$s -> X(G(!%1$s))))"; // G((F(a) | O(a)) & (a -> X(G(!a))))
    	return "!%1$s U (%1$s & X(G(!%1$s)))"; // !a U (a & X(G(!a)))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
		return "([^%1$s]*([%1$s][^%1$s]*){2,}[^%1$s]*)|[^%1$s]*";
	}
	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
//    	return "G((F(%1$s) | O(%1$s)) & (%1$s -> X(G(!%1$s))))"; // G((F(a) | O(a)) & (a -> X(G(!a))))
		return "%1$s U (!%1$s & X(G(%1$s)))"; // !a U (a & X(G(!a)))
	}

	protected Exactly1() {
    	super();
    }

	public Exactly1(TaskChar param1) {
		super(param1);
	}
	public Exactly1(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AtLeast1(getBase());
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
		impliCons[i++] = new AtMost1(getBase());
		
		return impliCons;
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.NUMEROSITY;
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new Exactly1(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new Exactly1(taskCharSets[0]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new Exactly1(TaskChar.SYMBOLIC_TASKCHARS[0]);
	}
}