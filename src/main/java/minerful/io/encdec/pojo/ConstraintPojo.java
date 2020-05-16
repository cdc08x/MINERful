package minerful.io.encdec.pojo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConstraintPojo implements Comparable<ConstraintPojo> {
	public String template;
	public List<Set<String>> parameters;
	public Double support;
	public Double confidence;
	public Double interestFactor;

	public ConstraintPojo() {
		this.parameters = new ArrayList<Set<String>>();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeclareConstraintPojo [template=");
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

	@Override
	public int compareTo(ConstraintPojo o) {
		int result = 0;
		/* Compare the template name */
		if (this.template == null) {
			if (o.template != null) {
				return 1;
			}
		} else {
			if (o.template == null) {
				return -1;
			}
//if (this.template == "End" && o.template == "Participation") 
//System.err.println(this.template + " vs " + o.template + " = " + this.template.compareTo(o.template));
			result = this.template.compareTo(o.template);
		}
		
		/* Compare the number of parameters */
		if (result == 0) {
			if (this.parameters == null) {
				if (o.parameters != null) {
					return 1;
				}
			} else {
				if (o.parameters == null) {
					return -1;
				}
			}
			result = new Integer(this.parameters.size()).compareTo(o.parameters.size());
		}
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
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result
				+ ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return (this.compareTo((ConstraintPojo)obj) == 0);
	}
}