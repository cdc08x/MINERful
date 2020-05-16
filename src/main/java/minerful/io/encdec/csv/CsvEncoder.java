package minerful.io.encdec.csv;

import java.util.Collection;
import java.util.Locale;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;

public class CsvEncoder {
	
	public enum PRINT_OUT_ELEMENT implements Comparable<PRINT_OUT_ELEMENT> {
		FULL_NAME("Constraint"),
		TEMPLATE_NAME("Template"),
		ACTIVATION("Activation"),
		TARGET("Target"),
		SUPPORT("Support"),
		CONFIDENCE_LEVEL("Confidence level"),
		INTEREST_FACTOR("Interest factor");
		
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
	 * @param proMod A declarative process model.
	 * @return A CSV string containing the constraints bag.
	 */
	public String printAsCsv(Collection<PRINT_OUT_ELEMENT> columns, ProcessModel proMod) {
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
        for (TaskChar key : proMod.bag.getTaskChars()) {
        	for (Constraint c : proMod.bag.getConstraintsOf(key)) {
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
							sottoSbuf.append("'" + c.getBase() + "'");
							break;
						case TARGET:
							sottoSbuf.append(c.getImplied() == null ? "" : "'" + c.getImplied() + "'");
							break;
						case SUPPORT:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getSupport()));
							break;
						case CONFIDENCE_LEVEL:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getConfidence()));
							break;
						case INTEREST_FACTOR:
							sottoSbuf.append(String.format(Locale.ENGLISH, "%.3f", c.getInterestFactor()));
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
