package minerful.io.encdec.declaremap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.relation.RelationConstraint;

import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.Parameter;

public class DeclareMapConstraintTransferObject {
	public final DeclareMapTemplate template;
	public final List<Set<String>> parameters;
	public final Double support;
	public final Double confidence;
	public final Double interestFactor;
	
	public static final String
		INTEREST_FACTOR_LABEL = "IF",
		CONFIDENCE_LABEL = "confidence",
		SUPPORT_LABEL = "support";
	public static final String
		INTEREST_FACTOR_EXTRACTION_REG_EXP =
			".*" + INTEREST_FACTOR_LABEL + ";([0-9\\.]+).*",
		CONFIDENCE_EXTRACTION_REG_EXP =
			".*" + CONFIDENCE_LABEL + ";([0-9\\.]+).*",
		SUPPORT_EXTRACTION_REG_EXP =
			".*" + SUPPORT_LABEL + ";([0-9\\.]+).*";
	public static final Pattern
		SUPPORT_PATTERN = Pattern.compile(SUPPORT_EXTRACTION_REG_EXP),
		CONFIDENCE_PATTERN = Pattern.compile(CONFIDENCE_EXTRACTION_REG_EXP),
		INTEREST_FACTOR_PATTERN = Pattern.compile(INTEREST_FACTOR_EXTRACTION_REG_EXP);

	public DeclareMapConstraintTransferObject(Constraint con) {
		DeclareMapTemplateTranslator declaTemplaTranslo = new DeclareMapTemplateTranslator();
		this.template = declaTemplaTranslo.translateTemplateName(con.getClass());
		this.parameters = new ArrayList<Set<String>>();
		
		Set<String>
			firstParamSet = new TreeSet<String>(),
			secondParamSet = new TreeSet<String>();
		
		for (TaskChar tChars : con.getBase().getTaskCharsArray()) {
			firstParamSet.add(tChars.taskClass.getName());
		}
		
		if (con instanceof RelationConstraint) {
			RelationConstraint relaCon = (RelationConstraint) con;
			for (TaskChar tChars : relaCon.getImplied().getTaskCharsArray()) {
				secondParamSet.add(tChars.taskClass.getName());
			}
		}

		this.parameters.add(firstParamSet);
		if (!secondParamSet.isEmpty()) {
			this.parameters.add(secondParamSet);
		}
		
		this.support = con.getSupport();
		this.confidence = con.getConfidence();
		this.interestFactor = con.getInterestFactor();
	}
	
	public DeclareMapConstraintTransferObject(ConstraintDefinition declareMapConstraint) {
		this.template = DeclareMapTemplate.fromName(declareMapConstraint.getName());
		this.parameters = new ArrayList<Set<String>>();
		
		Set<String>
			firstParamSet = new TreeSet<String>(),
			secondParamSet = new TreeSet<String>();

		Collection<Parameter> params = declareMapConstraint.getParameters();
		Parameter p = null;
		for (int paramNum = 1; paramNum <= params.size() && paramNum <= 2; paramNum++) {
			for (ActivityDefinition ad : declareMapConstraint.getBranches(p)) {
				switch (paramNum) {
				case 1:
					firstParamSet.add(ad.getName());
					break;
				case 2:
					secondParamSet.add(ad.getName());
					break;
				default:
					break;
				}
			}
		}
		
		this.parameters.add(firstParamSet);
		if (!secondParamSet.isEmpty()) {
			this.parameters.add(secondParamSet);
		}
		Matcher
			supMatcher = SUPPORT_PATTERN.matcher(declareMapConstraint.getText().trim()),
			confiMatcher = CONFIDENCE_PATTERN.matcher(declareMapConstraint.getText().trim()),
			inteFaMatcher = INTEREST_FACTOR_PATTERN.matcher(declareMapConstraint.getText().trim());

		this.support = (supMatcher.matches() && supMatcher.groupCount() > 0 ? Double.valueOf(supMatcher.group(1)) : Constraint.DEFAULT_SUPPORT);
		this.confidence = (confiMatcher.matches() && confiMatcher.groupCount() > 0 ? Double.valueOf(confiMatcher.group(1)) : Constraint.DEFAULT_CONFIDENCE);
		this.interestFactor = (inteFaMatcher.matches() && inteFaMatcher.groupCount() > 0 ? Double.valueOf(inteFaMatcher.group(1)): Constraint.DEFAULT_INTEREST_FACTOR);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeclareMapConstraintTransferObject [template=");
		builder.append(template);
		builder.append(", parameters=");
		builder.append(parameters);
		builder.append(", support=");
		builder.append(support);
		builder.append(", confidence=");
		builder.append(confidence);
		builder.append(", interestFactor=");
		builder.append(interestFactor);
		builder.append("]");
		return builder.toString();
	}
}