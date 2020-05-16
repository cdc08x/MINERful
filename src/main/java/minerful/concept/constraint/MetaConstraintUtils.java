package minerful.concept.constraint;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;
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
import minerful.concept.constraint.relation.MutualRelationConstraint;
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
	public static Collection<Class<? extends Constraint>> ALL_DISCOVERABLE_CONSTRAINT_TEMPLATES = getAllDiscoverableConstraintTemplates();
	public static Collection<Class<? extends Constraint>> ALL_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES = getRelationConstraintTemplates(ALL_DISCOVERABLE_CONSTRAINT_TEMPLATES);
	public static Map<String, Class<? extends Constraint>> ALL_DISCOVERABLE_CONSTRAINT_TEMPLATE_NAMES_MAP = getAllDiscoverableConstraintTemplateNamesMap();
	public static Collection<Class<? extends Constraint>> ALL_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES = getExistenceConstraintTemplates(ALL_DISCOVERABLE_CONSTRAINT_TEMPLATES);
	public static Collection<Class<? extends Constraint>> ALL_CONSTRAINT_TEMPLATES = getAllConstraintTemplates();
	public static Map<String, Class<? extends Constraint>> ALL_CONSTRAINT_TEMPLATE_NAMES_MAP = getAllConstraintTemplateNamesMap();
	public static Collection<Class<? extends Constraint>> ALL_RELATION_CONSTRAINT_TEMPLATES = getRelationConstraintTemplates(ALL_CONSTRAINT_TEMPLATES);
	public static Collection<Class<? extends Constraint>> ALL_EXISTENCE_CONSTRAINT_TEMPLATES = getExistenceConstraintTemplates(ALL_CONSTRAINT_TEMPLATES);
	public static int NUMBER_OF_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES = ALL_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES.size();
	public static int NUMBER_OF_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES = ALL_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES.size();
	public static int NUMBER_OF_RELATION_CONSTRAINT_TEMPLATES = ALL_RELATION_CONSTRAINT_TEMPLATES.size();
	public static int NUMBER_OF_EXISTENCE_CONSTRAINT_TEMPLATES = ALL_EXISTENCE_CONSTRAINT_TEMPLATES.size();
	public static int NUMBER_OF_CONSTRAINT_TEMPLATES = ALL_CONSTRAINT_TEMPLATES.size();

	public static Map<String, Class<? extends Constraint>> getAllConstraintTemplateNamesMap() {
		return getTemplateNamesMap(ALL_CONSTRAINT_TEMPLATES);
	}

	public static Map<String, Class<? extends Constraint>> getAllDiscoverableConstraintTemplateNamesMap() {
		return getTemplateNamesMap(ALL_DISCOVERABLE_CONSTRAINT_TEMPLATES);
	}

	private static Map<String, Class<? extends Constraint>> getTemplateNamesMap(Collection<Class<? extends Constraint>> templates) {
		Map<String, Class<? extends Constraint>> constraintTemplateNames =
				new HashMap<String, Class<? extends Constraint>>(templates.size(), (float)1.0);
		for (Class<? extends Constraint> cnsClass : templates) {
			constraintTemplateNames.put(getTemplateName(cnsClass), cnsClass);
		}
		return constraintTemplateNames;
	}

	public static String getTemplateName(Constraint constraint) {
		return getTemplateName(constraint.getClass());
	}

	public static String getTemplateName(Class<? extends Constraint> constraintClass) {
		return constraintClass.getCanonicalName().substring(constraintClass.getCanonicalName().lastIndexOf('.') + 1);
	}
	
	public static Collection<Constraint> createHierarchicalLinks(Collection<Constraint> constraints) {
		TreeSet<Constraint> treeConSet = new TreeSet<Constraint>(constraints);
		for (Constraint con : constraints) {
			Constraint constraintWhichThisShouldBeBasedUpon = con.suggestConstraintWhichThisShouldBeBasedUpon();
			if (		constraintWhichThisShouldBeBasedUpon != null
					&&	treeConSet.contains(constraintWhichThisShouldBeBasedUpon)
				) {
				con.setConstraintWhichThisIsBasedUpon(treeConSet.tailSet(constraintWhichThisShouldBeBasedUpon).first());
			}
			if (con.getSubFamily().equals(RelationConstraintSubFamily.COUPLING)) {
				MutualRelationConstraint coReCon = (MutualRelationConstraint) con;
				if (!coReCon.hasForwardConstraint() && treeConSet.contains(coReCon.getPossibleForwardConstraint())) {
					coReCon.setForwardConstraint((RelationConstraint) treeConSet.tailSet(coReCon.getPossibleForwardConstraint()).first());
				}
				if (!coReCon.hasBackwardConstraint() && constraints.contains(coReCon.getPossibleBackwardConstraint())) {
					coReCon.setBackwardConstraint((RelationConstraint) treeConSet.tailSet(coReCon.getPossibleBackwardConstraint()).first());
				}
			}
			if (con.getSubFamily().equals(RelationConstraintSubFamily.NEGATIVE)) {
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

	public static Collection<Class<? extends Constraint>> getAllDiscoverableConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(18);
		// Existence
		constraintTemplates.addAll(getAllDiscoverableExistenceConstraintTemplates());
		// Relation onwards
		constraintTemplates.addAll(getAllDiscoverableForwardRelationConstraintTemplates());
		// Relation backwards
		constraintTemplates.addAll(getAllDiscoverableBackwardRelationConstraintTemplates());
		// Mutual relation
		constraintTemplates.addAll(getAllDiscoverableMutualRelationConstraintTemplates());
		// Negation relation
		constraintTemplates.addAll(getAllDiscoverableNegativeRelationConstraintTemplates());

		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(18);
		// Existence
		constraintTemplates.addAll(getAllExistenceConstraintTemplates());
		// Relation onwards
		constraintTemplates.addAll(getAllForwardRelationConstraintTemplates());
		// Relation backwards
		constraintTemplates.addAll(getAllBackwardRelationConstraintTemplates());
		// Mutual relation
		constraintTemplates.addAll(getAllMutualRelationConstraintTemplates());
		// Negation relation
		constraintTemplates.addAll(getAllNegativeRelationConstraintTemplates());

		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllExistenceConstraintTemplates() {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return getAllDiscoverableExistenceConstraintTemplates();
	}
	public static Collection<Class<? extends Constraint>> getAllDiscoverableExistenceConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(4);
		// Existence
		constraintTemplates.add(End.class);
		constraintTemplates.add(Init.class);
		constraintTemplates.add(Participation.class);
		constraintTemplates.add(AtMostOne.class);
		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllForwardRelationConstraintTemplates() {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return getAllDiscoverableForwardRelationConstraintTemplates();
	}
	public static Collection<Class<? extends Constraint>> getAllDiscoverableForwardRelationConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(4);
		// Relation onwards
		constraintTemplates.add(RespondedExistence.class);
		constraintTemplates.add(Response.class);
		constraintTemplates.add(AlternateResponse.class);
		constraintTemplates.add(ChainResponse.class);
		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllBackwardRelationConstraintTemplates() {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return getAllDiscoverableBackwardRelationConstraintTemplates();
	}
	public static Collection<Class<? extends Constraint>> getAllDiscoverableBackwardRelationConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(3);
		// Relation backwards
		constraintTemplates.add(Precedence.class);
		constraintTemplates.add(AlternatePrecedence.class);
		constraintTemplates.add(ChainPrecedence.class);
		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllMutualRelationConstraintTemplates() {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return getAllDiscoverableMutualRelationConstraintTemplates();
	}
	public static Collection<Class<? extends Constraint>> getAllDiscoverableMutualRelationConstraintTemplates() {
		ArrayList<Class<? extends Constraint>> constraintTemplates = new ArrayList<Class<? extends Constraint>>(4);
		// Mutual relation
		constraintTemplates.add(CoExistence.class);
		constraintTemplates.add(Succession.class);
		constraintTemplates.add(AlternateSuccession.class);
		constraintTemplates.add(ChainSuccession.class);
		
		return constraintTemplates;
	}

	public static Collection<Class<? extends Constraint>> getAllNegativeRelationConstraintTemplates() {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return getAllDiscoverableNegativeRelationConstraintTemplates();
	}
	public static Collection<Class<? extends Constraint>> getAllDiscoverableNegativeRelationConstraintTemplates() {
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

	public static boolean isExistenceTemplate(Class<? extends Constraint> template) {
		return ExistenceConstraint.class.isAssignableFrom(template);
	}

	public static boolean isRelationTemplate(Class<? extends Constraint> template) {
		return RelationConstraint.class.isAssignableFrom(template);
	}

	public static Collection<Class<? extends Constraint>> getRelationConstraintTemplates(Collection<Class<? extends Constraint>> constraintTemplates) {
		Collection<Class<? extends Constraint>> relConTemplates = new ArrayList<Class<? extends Constraint>>();
		for (Class<? extends Constraint> cnsTemplate : constraintTemplates) {
			if (RelationConstraint.class.isAssignableFrom(cnsTemplate)) {
				relConTemplates.add(cnsTemplate);
			}
		}
		return relConTemplates;
	}
	
	public static Collection<Class<? extends Constraint>> getExistenceConstraintTemplates(Collection<Class<? extends Constraint>> constraintTemplates) {
		Collection<Class<? extends Constraint>> relConTemplates = new ArrayList<Class<? extends Constraint>>();

		for (Class<? extends Constraint> cnsTemplate : constraintTemplates) {
			if (ExistenceConstraint.class.isAssignableFrom(cnsTemplate)) {
				relConTemplates.add(cnsTemplate);
			}
		}
		return relConTemplates;
	}
	
	public static int howManyPossibleExistenceConstraints(int numOfTasksToQueryFor) {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return howManyDiscoverableExistenceConstraints(numOfTasksToQueryFor);
	}
	public static int howManyDiscoverableExistenceConstraints(int numOfTasksToQueryFor) {
		return NUMBER_OF_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES * numOfTasksToQueryFor;
	}
	
	public static int howManyPossibleRelationConstraints(int numOfTasksToQueryFor, int alphabetSize) {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return howManyDiscoverableRelationConstraints(numOfTasksToQueryFor, alphabetSize);
	}
	public static int howManyDiscoverableRelationConstraints(int numOfTasksToQueryFor, int alphabetSize) {
		return NUMBER_OF_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES * numOfTasksToQueryFor * (alphabetSize -1);
	}

	public static int howManyPossibleConstraints(int numOfTasksToQueryFor, int alphabetSize) {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return howManyDiscoverableConstraints(numOfTasksToQueryFor, alphabetSize);
	}
	public static int howManyDiscoverableConstraints(int numOfTasksToQueryFor, int alphabetSize) {
		return	howManyDiscoverableRelationConstraints(numOfTasksToQueryFor, alphabetSize) +
				howManyDiscoverableExistenceConstraints(numOfTasksToQueryFor);
	}
	
	public static int howManyPossibleConstraints(int alphabetSize) {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return howManyDiscoverableConstraints(alphabetSize);
	}
	public static int howManyDiscoverableConstraints(int alphabetSize) {
		return	howManyDiscoverableRelationConstraints(alphabetSize, alphabetSize) +
				howManyDiscoverableExistenceConstraints(alphabetSize);
	}

	public static Collection<Constraint> getAllPossibleRelationConstraints(TaskChar base, TaskChar implied) {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return getAllDiscoverableRelationConstraints(base, implied);
	}
	/* The second coolest method I ever coded! */
	public static Constraint makeConstraint(Class<? extends Constraint> template, TaskCharSet... params) {
		Constraint con = null;
		Constructor<? extends Constraint> constructor = null;
		try {
			if (isExistenceTemplate(template)) {
				constructor = template.getConstructor(TaskCharSet.class);
				con = constructor.newInstance(params[0]);
			} else if (isRelationTemplate(template)) {
				constructor = template.getConstructor(TaskCharSet.class, TaskCharSet.class);
				con = constructor.newInstance(params[0], params[1]);
			}
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return con;
	}
	/* The second coolest method I ever coded! */
	public static Constraint makeRelationConstraint(Class<? extends Constraint> template, TaskCharSet param1, TaskCharSet param2) {
		Constraint con = null;
		Constructor<? extends Constraint> constructor = null;
		try {
				constructor = template.getConstructor(TaskCharSet.class, TaskCharSet.class);
				con = constructor.newInstance(param1, param2);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return con;
	}
	/* The second coolest method I ever coded! */
	public static Constraint makeExistenceConstraint(Class<? extends Constraint> template, TaskCharSet param) {
		Constraint con = null;
		Constructor<? extends Constraint> constructor = null;
		try {
			constructor = template.getConstructor(TaskCharSet.class);
			con = constructor.newInstance(param);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return con;
	}
	/* The second-and-a-half coolest method I ever coded! */
	public static Collection<Constraint> getAllDiscoverableRelationConstraints(TaskChar base, TaskChar implied) {
		Collection<Constraint> relCons = new ArrayList<Constraint>();
		Constructor<? extends Constraint> tmpConstructor = null;

		try {
			for (Class<? extends Constraint> relationConstraintTypeClass : getAllDiscoverableForwardRelationConstraintTemplates()) {
				tmpConstructor = relationConstraintTypeClass.getConstructor(
						TaskChar.class, TaskChar.class, Double.TYPE);
				relCons.add(tmpConstructor.newInstance(base, implied, 0.0));
			}
			for (Class<? extends Constraint> relationConstraintTypeClass : getAllDiscoverableBackwardRelationConstraintTemplates()) {
				tmpConstructor = relationConstraintTypeClass.getConstructor(
						TaskChar.class, TaskChar.class, Double.TYPE);
				relCons.add(tmpConstructor.newInstance(implied, base, 0.0));
			}
			for (Class<? extends Constraint> relationConstraintTypeClass : getAllDiscoverableNegativeRelationConstraintTemplates()) {
				tmpConstructor = relationConstraintTypeClass.getConstructor(
						TaskChar.class, TaskChar.class, Double.TYPE);
				relCons.add(tmpConstructor.newInstance(base, implied, 0.0));
			}
			for (Class<? extends Constraint> relationConstraintTypeClass : getAllDiscoverableMutualRelationConstraintTemplates()) {
				tmpConstructor = relationConstraintTypeClass.getConstructor(
						TaskChar.class, TaskChar.class, Double.TYPE);
				relCons.add(tmpConstructor.newInstance(base, implied, 0.0));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			System.exit(1);
		}
		return relCons;
	}

	public static Collection<Constraint> getAllPossibleExistenceConstraints(TaskChar base) {
		// TODO Change it if new definitions of not yet discoverable templates are given
		return getAllDiscoverableExistenceConstraints(base);
	}
	/* The third coolest method I ever coded! */
	public static Collection<Constraint> getAllDiscoverableExistenceConstraints(TaskChar base) {
		Collection<Constraint> exiCons = new ArrayList<Constraint>();

		Collection<Class<? extends Constraint>> existenceConstraintTypeClasses = ALL_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES;
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