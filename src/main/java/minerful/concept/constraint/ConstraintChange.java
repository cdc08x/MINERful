package minerful.concept.constraint;

public class ConstraintChange {
	public final Constraint constraint;
	public final ChangedProperty property;
	public final Object value;
	
	public enum ChangedProperty {
		SUPPORT,
		CONFIDENCE,
		INTEREST_FACTOR,
		FITNESS,
		BELOW_SUPPORT_THRESHOLD,
		BELOW_CONFIDENCE_THRESHOLD,
		BELOW_INTEREST_FACTOR_THRESHOLD,
		BELOW_FITNESS_THRESHOLD,
		REDUNDANT,
		CONFLICTING,
		EVALUATED_ON_LOG,
	}

	public ConstraintChange(Constraint constraint, ChangedProperty type, Object value) {
		this.constraint = constraint;
		this.property = type;
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConstraintChange [constraint=");
		builder.append(constraint);
		builder.append(", property=");
		builder.append(property);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}