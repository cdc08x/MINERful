package minerful.io.encdec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.Parameter;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapTemplate;
import minerful.io.encdec.declaremap.DeclareMapToMinerFulTemplatesTranslator;
import minerful.io.encdec.pojo.ConstraintPojo;

public class DeclareConstraintTransferObject implements Comparable<DeclareConstraintTransferObject> {
	public final DeclareMapTemplate declareMapTemplate;
	public final Class<? extends Constraint> minerFulTemplate;
	public final List<Set<String>> parameters;
	public final Double support;
	public final Double confidence;
	public final Double interestFactor;
	
	public DeclareConstraintTransferObject(Constraint con) {
		this.minerFulTemplate = con.getClass();
		this.declareMapTemplate = DeclareMapToMinerFulTemplatesTranslator.translateTemplateName(this.minerFulTemplate);
		this.parameters = new ArrayList<Set<String>>();
		
		List<TaskCharSet> params = con.getParameters();
		
		Iterator<TaskCharSet> taskChaIterator = params.iterator();
		Set<String> auxParamSet = null;
		while (taskChaIterator.hasNext()) {
			auxParamSet = new TreeSet<String>();
			for (TaskChar tChars : taskChaIterator.next().getTaskCharsArray()) {
				auxParamSet.add(tChars.taskClass.getName());
			}
			this.parameters.add(auxParamSet);
		}
		
		this.support = con.getSupport();
		this.confidence = con.getConfidence();
		this.interestFactor = con.getInterestFactor();
	}
	
	public DeclareConstraintTransferObject(ConstraintDefinition declareMapConstraint) {
		this.declareMapTemplate = DeclareMapTemplate.fromName(declareMapConstraint.getName());
		this.minerFulTemplate = DeclareMapToMinerFulTemplatesTranslator.translateTemplateName(this.declareMapTemplate);
		this.parameters = new ArrayList<Set<String>>();
		
		Collection<Parameter> params = declareMapConstraint.getParameters();
		Set<String> auxParamSet = null;
		for(Parameter p : declareMapConstraint.getParameters()){
			auxParamSet = new TreeSet<String>();
			for (ActivityDefinition ad : declareMapConstraint.getBranches(p)) {
				auxParamSet.add(ad.getName());
			}
			this.parameters.add(auxParamSet);
		}
		
		Matcher
			supMatcher = DeclareMapEncoderDecoder.SUPPORT_PATTERN.matcher(declareMapConstraint.getText().trim()),
			confiMatcher = DeclareMapEncoderDecoder.CONFIDENCE_PATTERN.matcher(declareMapConstraint.getText().trim()),
			inteFaMatcher = DeclareMapEncoderDecoder.INTEREST_FACTOR_PATTERN.matcher(declareMapConstraint.getText().trim());

		this.support = (supMatcher.matches() && supMatcher.groupCount() > 0 ? Double.valueOf(supMatcher.group(1)) : Constraint.DEFAULT_SUPPORT);
		this.confidence = (confiMatcher.matches() && confiMatcher.groupCount() > 0 ? Double.valueOf(confiMatcher.group(1)) : Constraint.DEFAULT_CONFIDENCE);
		this.interestFactor = (inteFaMatcher.matches() && inteFaMatcher.groupCount() > 0 ? Double.valueOf(inteFaMatcher.group(1)): Constraint.DEFAULT_INTEREST_FACTOR);
	}
	
	public DeclareConstraintTransferObject(ConstraintPojo pojo) {
		/* Search within all possible MINERFul templates */
		Class<? extends Constraint> givenMinerFulTemplate = StringToLowerCaseAlphanumToTemplateTranslator.translateTemplateName(pojo.template);
		if (givenMinerFulTemplate != null) {
			this.minerFulTemplate = givenMinerFulTemplate;
			this.declareMapTemplate = DeclareMapToMinerFulTemplatesTranslator.translateTemplateName(this.minerFulTemplate);
		} else {
			/* Search within all possible DeclareMap templates */
			this.declareMapTemplate = DeclareMapTemplate.fromName(pojo.template);
			if (this.declareMapTemplate != null) {
				this.minerFulTemplate = DeclareMapToMinerFulTemplatesTranslator.translateTemplateName(this.declareMapTemplate);
			} else {
				throw new IllegalArgumentException("Requested Declare template " + pojo.template + " does not exist.");
			}
		}

		this.parameters = pojo.parameters;
		this.support = pojo.support;
		this.confidence = pojo.confidence;
		this.interestFactor = pojo.interestFactor;
	}
	
	public ConstraintPojo toPojo() {
		ConstraintPojo pojo = new ConstraintPojo();
		
		pojo.template = MetaConstraintUtils.getTemplateName(this.minerFulTemplate);
		pojo.parameters = this.parameters;
		
		pojo.support = this.support;
		pojo.confidence = this.confidence;
		pojo.interestFactor = this.interestFactor;
		
		return pojo;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeclareConstraintTransferObject [declareMapTemplate=");
		builder.append(declareMapTemplate);
		builder.append(", minerfulTemplate=");
		builder.append(minerFulTemplate);
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

	public Set<String> getAllParamsTasks() {
		Set<String> allParamsTasks = new TreeSet<String>();

		if (this.parameters != null) {
			for (Set<String> paramTasks : this.parameters) {
				allParamsTasks.addAll(paramTasks);
			}
		}

		return allParamsTasks;
	}

	@Override
	public int compareTo(DeclareConstraintTransferObject o) {
		int result = 0;
		
		result = this.declareMapTemplate.compareTo(o.declareMapTemplate);
		if (result == 0) {
			result = this.minerFulTemplate.getName().compareTo(o.minerFulTemplate.getName());
			if (result == 0) {
				/* Compare the parameters' sizes */
	    		for (int i = 0; i < this.parameters.size() && result == 0; i++) {
	    			if (this.parameters.get(i) == null) {
	    				if (o.parameters.get(i) != null) {
	    					return 1;
	    				}
	    			} else {
	    				if (o.parameters.get(i) == null) {
	    					return -1;
	    				}
	    			}
	    			result = new Integer(this.parameters.get(i).size()).compareTo(o.parameters.get(i).size());
	    			/* Compare the respective parameters' tasks */
	    			if (result == 0) {
	    				Iterator<String>
	    					thisParamsIterator = this.parameters.get(i).iterator(),
	    					oParamsIterator = o.parameters.get(i).iterator();
	    				while (thisParamsIterator.hasNext() && result == 0) {
	    					result = thisParamsIterator.next().compareTo(oParamsIterator.next());
	    				}
	    			}
	    		}
			}
		}
		
		return result;
	}
}