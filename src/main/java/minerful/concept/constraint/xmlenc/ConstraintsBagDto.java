package minerful.concept.constraint.xmlenc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;

@XmlRootElement(name="constraintsSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConstraintsBagDto {
	@XmlElementRef(required=true)
	public List<Constraint> constraints;

	public ConstraintsBagDto() {
		this.constraints = new ArrayList<Constraint>();
	}
	
	public ConstraintsBagDto(ConstraintsBag bag) {
		this.constraints = new ArrayList<Constraint>(bag.howManyConstraints());
		this.constraints.addAll(bag.getAllConstraints());
	}
	
	public ConstraintsBag toConstraintsBag() {
		ConstraintsBag bag = new ConstraintsBag();
		for (Constraint cns : constraints) {
			cns.setParameters(cns.getParameters());
			bag.add(cns.getBase(), cns);
		}
		return bag;
	}
}