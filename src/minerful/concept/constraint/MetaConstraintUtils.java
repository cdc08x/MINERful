package minerful.concept.constraint;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.TaskChar;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.ExistenceConstraint;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.CouplingRelationConstraint;
import minerful.concept.constraint.relation.NegativeRelationConstraint;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotCoExistence;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;

import org.reflections.Reflections;

public class MetaConstraintUtils {
	public static Collection<Class<? extends Constraint>> ALL_POSSIBLE_CONSTRAINT_TEMPLATES = getAllPossibleConstraintTemplates();
	public static Collection<Class<? extends Constraint>> ALL_POSSIBLE_RELATION_CONSTRAINT_TEMPLATES = getAllPossibleRelationConstraintTemplates(ALL_POSSIBLE_CONSTRAINT_TEMPLATES);
	public static Collection<Class<? extends Constraint>> ALL_POSSIBLE_EXISTENCE_CONSTRAINT_TEMPLATES = getAllPossibleExistenceConstraintTemplates();
	public static int NUMBER_OF_POSSIBLE_RELATION_CONSTRAINT_TEMPLATES = ALL_POSSIBLE_RELATION_CONSTRAINT_TEMPLATES.size();
	public static int NUMBER_OF_POSSIBLE_EXISTENCE_CONSTRAINT_TEMPLATES = ALL_POSSIBLE_EXISTENCE_CONSTRAINT_TEMPLATES.size();

	public static Set<Constraint> createHierarchicalLinks(Set<Constraint> constraints) {
		TreeSet<Constraint> treeConSet = new TreeSet<Constraint>(constraints);
		for (Constraint con : constraints) {
			Constraint constraintWhichThisShouldBeBasedUpon = con.getConstraintWhichThisShouldBeBasedUpon();
			if (		constraintWhichThisShouldBeBasedUpon != null
					&&	treeConSet.contains(constraintWhichThisShouldBeBasedUpon)
				) {
				con.setConstraintWhichThisIsBasedUpon(constraintWhichThisShouldBeBasedUpon);
			}
			if (con.getFamily().equals(ConstraintFamily.COUPLING)) {
				CouplingRelationConstraint coReCon = (CouplingRelationConstraint) con;
				if (!coReCon.hasForwardConstraint() && treeConSet.contains(coReCon.getSupposedForwardConstraint())) {
					coReCon.setForwardConstraint((RelationConstraint) treeConSet.tailSet(coReCon.getSupposedForwardConstraint()).first());
				}
				if (!coReCon.hasBackwardConstraint() && constraints.contains(coReCon.getSupposedBackwardConstraint())) {
					coReCon.setBackwardConstraint((RelationConstraint) treeConSet.tailSet(coReCon.getSupposedBackwardConstraint()).first());
				}
			}
			if (con.getFamily().equals(ConstraintFamily.NEGATIVE)) {
				NegativeRelationConstraint negaCon = (NegativeRelationConstraint) con;
				if (!negaCon.hasOpponent() && treeConSet.contains(negaCon.getSupposedOpponentConstraint())) {
					negaCon.setOpponent((RelationConstraint) treeConSet.tailSet(negaCon.getSupposedOpponentConstraint()).first());
				}
			}
		}
		return constraints;
	}
	
	/**
	 *  The coolest method I have probably coded, ever!
	 */
	public Collection<Class<? extends Constraint>> getAllPossibleConstraintTemplatesStylish() {
		Reflections reflections = new Reflections(this.getClass().getPackage().getName());
		
		Set<Class<? extends Constraint>> constraintSubClasses = reflections.getSubTypesOf(Constraint.class);
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(constraintSubClasses.size());
		
		for (Class<? extends Constraint> constraintSubClass : constraintSubClasses) {
			if (!Modifier.isAbstract(constraintSubClass.getModifiers())) {
				constraintTemplates.add(constraintSubClass);
			}
		}
		
		constraintTemplates.trimToSize();
		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllPossibleConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(18);
		// Existence
		constraintTemplates.addAll(getAllPossibleExistenceConstraintTemplates());
		// Relation onwards
		constraintTemplates.addAll(getAllPossibleOnwardsRelationConstraintTemplates());
		// Relation backwards
		constraintTemplates.addAll(getAllPossibleBackwardsRelationConstraintTemplates());
		// Mutual relation
		constraintTemplates.addAll(getAllPossibleMutualRelationConstraintTemplates());
		// Negation relation
		constraintTemplates.addAll(getAllPossibleNegativeRelationConstraintTemplates());

		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllPossibleExistenceConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(4);
		// Existence
		constraintTemplates.add(End.class);
		constraintTemplates.add(Init.class);
		constraintTemplates.add(Participation.class);
		constraintTemplates.add(AtMostOne.class);
		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllPossibleOnwardsRelationConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(4);
		// Relation onwards
		constraintTemplates.add(RespondedExistence.class);
		constraintTemplates.add(Response.class);
		constraintTemplates.add(AlternateResponse.class);
		constraintTemplates.add(ChainResponse.class);
		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllPossibleBackwardsRelationConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(3);
		// Relation backwards
		constraintTemplates.add(Precedence.class);
		constraintTemplates.add(AlternatePrecedence.class);
		constraintTemplates.add(ChainPrecedence.class);
		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllPossibleMutualRelationConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(4);
		// Mutual relation
		constraintTemplates.add(CoExistence.class);
		constraintTemplates.add(Succession.class);
		constraintTemplates.add(AlternateSuccession.class);
		constraintTemplates.add(ChainSuccession.class);
		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllPossibleNegativeRelationConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(3);
		// Negation relation
		constraintTemplates.add(NotCoExistence.class);
		constraintTemplates.add(NotChainSuccession.class);
		constraintTemplates.add(NotSuccession.class);
		
		return constraintTemplates;
	}
	
	public static boolean isExistenceConstraint(Constraint c) {
		return c instanceof ExistenceConstraint;
	}
	
	public static boolean isRelationConstraint(Constraint c) {
		return c instanceof RelationConstraint;
	}
	
	public static Collection<Class<? extends Constraint>> getAllPossibleRelationConstraintTemplates(Collection<Class<? extends Constraint>> allPossibleConstraintTemplates) {
		Collection<Class<? extends Constraint>> relConTemplates = new ArrayList<Class<? extends Constraint>>();
		for (Class<? extends Constraint> cnsTemplate : allPossibleConstraintTemplates) {
			if (RelationConstraint.class.isAssignableFrom(cnsTemplate)) {
				relConTemplates.add(cnsTemplate);
			}
		}
		return relConTemplates;
	}
	
	public static Collection<Class<? extends Constraint>> getAllPossibleExistenceConstraintTemplates(Collection<Class<? extends Constraint>> allPossibleConstraintTemplates) {
		Collection<Class<? extends Constraint>> relConTemplates = new ArrayList<Class<? extends Constraint>>();

		for (Class<? extends Constraint> cnsTemplate : allPossibleConstraintTemplates) {
			if (ExistenceConstraint.class.isAssignableFrom(cnsTemplate)) {
				relConTemplates.add(cnsTemplate);
			}
		}
		return relConTemplates;
	}
	
	public static int howManyPossibleExistenceConstraints(int alphabetSize) {
		return NUMBER_OF_POSSIBLE_EXISTENCE_CONSTRAINT_TEMPLATES * alphabetSize;
	}
	
	public static int howManyPossibleRelationConstraints(int alphabetSize) {
		return NUMBER_OF_POSSIBLE_RELATION_CONSTRAINT_TEMPLATES * alphabetSize * (alphabetSize -1);
	}

	public static int howManyPossibleConstraints(int alphabetSize) {
		return	howManyPossibleRelationConstraints(alphabetSize) +
				howManyPossibleExistenceConstraints(alphabetSize);
	}

	/* The second coolest method I ever coded! */
	public Collection<? extends Constraint> getAllRelationConstraints(TaskChar implying, TaskChar implied) {
		Collection<Constraint> relCons = new ArrayList<Constraint>();

		Collection<Class<? extends Constraint>> relationConstraintTemplates = ALL_POSSIBLE_RELATION_CONSTRAINT_TEMPLATES;
		Constructor<? extends Constraint> tmpConstructor = null;

		for (Class<? extends Constraint> relationConstraintTypeClass : relationConstraintTemplates) {
			try {
				tmpConstructor = relationConstraintTypeClass.getConstructor(
						TaskChar.class, TaskChar.class, Double.TYPE);
				relCons.add(tmpConstructor.newInstance(implying, implied, 0.0));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				System.exit(1);
			}
		}
		return relCons;
	}
	
	/* The third coolest method I ever coded! */
	public Collection<Constraint> getAllExistenceConstraints(TaskChar base) {
		Collection<Constraint> exiCons = new ArrayList<Constraint>();

		Collection<Class<? extends Constraint>> existenceConstraintTypeClasses = ALL_POSSIBLE_EXISTENCE_CONSTRAINT_TEMPLATES;
		Constructor<? extends Constraint> tmpConstructor = null;
		
		for (Class<? extends Constraint> existenceConstraintTypeClass : existenceConstraintTypeClasses) {
			if (!Modifier.isAbstract(existenceConstraintTypeClass.getModifiers())) {
				try {
					tmpConstructor = existenceConstraintTypeClass
							.getConstructor(TaskChar.class, Double.TYPE);
					exiCons.add(
							tmpConstructor
							.newInstance(base, 0.0));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					System.exit(1);
				}
			}
		}
		return exiCons;
	}
}