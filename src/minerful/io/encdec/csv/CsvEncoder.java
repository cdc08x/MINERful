package minerful.io.encdec.csv;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import minerful.concept.ProcessSpecification;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;

/**
 * Encodes a declarative process specification into a CSV format.
 * @author cdc08x
 */
public class CsvEncoder {
	
	public enum PRINT_OUT_ELEMENT implements Comparable<PRINT_OUT_ELEMENT> {
		FULL_NAME("Constraint"),
		TEMPLATE_NAME("Template"),
		ACTIVATION("Activation"),
		TARGET("Target"),
		CONFIDENCE("Confidence"),
		COVERAGE("Coverage"),
		SUPPORT("Support"),
		TRACE_CONFIDENCE("Trace confidence"),
		TRACE_COVERAGE("Trace coverage"),
		TRACE_SUPPORT("Trace support"),
		TRACE_FITNESS("Trace fitness"),
		MARKED_FOR_EXCLUSION("Marked for exclusion"),
		BELOW_TRHESHOLD("Below thresholds"),
		REDUNDANT("Redudant"),
		CONFLICTING("Conflicting"),
		;
		
		private final String label;
		
		private PRINT_OUT_ELEMENT(String label) {
			this.label = label;
		}
		public String toString() {
			return this.label;
		}
	};

	/**
	 * Prints the CSV format of the constraints bag. The columns appearing in the file can be customised.
	 * @param columns A sorted set of columns. See the <code>PRINT_OUT_ELEMENT</code> enumeration.
	 * @param proSpec A declarative process specification.
	 * @return A CSV string containing the constraints bag.
	 */
	public String printAsCsv(Collection<PRINT_OUT_ELEMENT> columns, ProcessSpecification proSpec) {
		StringBuilder
	        	superSbuf = new StringBuilder(),
	        	sottoSbuf = new StringBuilder();
		for (PRINT_OUT_ELEMENT col : columns) {
			if (columns.contains(col)) {
				sottoSbuf.append("';'");
				sottoSbuf.append(col.toString());
			}
		}
		superSbuf.append(sottoSbuf.substring(2)+"'");
		superSbuf.append("\n");
		sottoSbuf = new StringBuilder();
        for (TaskChar key : proSpec.bag.getTaskChars()) {
        	for (Constraint c : proSpec.bag.getConstraintsOf(key)) {
				for (PRINT_OUT_ELEMENT col : columns) {
					if (columns.contains(col)) {
						sottoSbuf.append(';');
						switch(col) {
						case FULL_NAME:
							sottoSbuf.append("'" + c.toString() + "'");
							break;
						case TEMPLATE_NAME:
							sottoSbuf.append("'" + c.getTemplateName() + "'");
							break;
						case ACTIVATION:
							sottoSbuf.append(c.getActivators() == null? "''" : "'" + Arrays.toString(c.getActivators()) + "'");
							break;
						case TARGET:
							sottoSbuf.append(c.getTargets() == null? "''" : "'" + Arrays.toString(c.getTargets()) + "'");
							break;
						case SUPPORT:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getEventBasedMeasures().getSupport()));
							break;
						case CONFIDENCE:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getEventBasedMeasures().getConfidence()));
							break;
						case COVERAGE:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getEventBasedMeasures().getCoverage()));
							break;
						case TRACE_SUPPORT:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getTraceBasedMeasures().getSupport()));
							break;
						case TRACE_CONFIDENCE:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getTraceBasedMeasures().getConfidence()));
							break;
						case TRACE_COVERAGE:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getTraceBasedMeasures().getCoverage()));
							break;
						case TRACE_FITNESS:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getTraceBasedMeasures().isFitnessComputed() ? c.getTraceBasedMeasures().getFitness() : ""));
							break;
						case MARKED_FOR_EXCLUSION:
							sottoSbuf.append(c.isMarkedForExclusion() ? "TRUE" : "FALSE");
							break;
						case BELOW_TRHESHOLD:
							sottoSbuf.append(c.isBelowThresholds() ? "TRUE" : "FALSE");
							break;
						case REDUNDANT:
							sottoSbuf.append(c.isRedundant() ? "TRUE" : "FALSE");
							break;
						case CONFLICTING:
							sottoSbuf.append(c.isConflicting() ? "TRUE" : "FALSE");
							break;
						default:
							break;
								
						}
					}
				}
				superSbuf.append(sottoSbuf.substring(1));
				superSbuf.append("\n");
				sottoSbuf = new StringBuilder();
        	}
        }
		return superSbuf.toString();
	}

}
