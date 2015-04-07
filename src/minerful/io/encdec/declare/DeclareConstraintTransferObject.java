package minerful.io.encdec.declare;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.relation.RelationConstraint;

public class DeclareConstraintTransferObject {
	public final DeclareTemplate template;
	public final List<Set<String>> parameters;
	
	public DeclareConstraintTransferObject(Constraint con) {
		DeclareTemplateTranslator declaTemplaTranslo = new DeclareTemplateTranslator();
		this.template = declaTemplaTranslo.translateTemplateName(con.getClass());
		
		Set<String>
			firstParamSet = new TreeSet<String>(),
			secondParamSet = new TreeSet<String>();
		
		for (TaskChar tChars : con.base.getTaskChars()) {
			firstParamSet.add(tChars.taskClass.getName());
		}
		
		if (con instanceof RelationConstraint) {
			RelationConstraint relaCon = (RelationConstraint) con;
			for (TaskChar tChars : relaCon.implied.getTaskChars()) {
				secondParamSet.add(tChars.taskClass.getName());
			}
		}

		this.parameters = new ArrayList<Set<String>>();
		this.parameters.add(firstParamSet);
		if (!secondParamSet.isEmpty())
			this.parameters.add(secondParamSet);
	}
}