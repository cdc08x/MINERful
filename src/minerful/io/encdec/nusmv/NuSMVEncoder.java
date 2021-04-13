package minerful.io.encdec.nusmv;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;

public class NuSMVEncoder {
	ProcessModel processModel = null;
	
	public static String OR = "|";

	/**
	 * Constructor of this class
	 * @param processModel
	 */
	public NuSMVEncoder(ProcessModel processModel) {
		this.processModel = processModel;
	}

	public String printAsNuSMV() {
		StringBuilder sBuil = new StringBuilder();
		sBuil.append("MODULE main\n\nVAR\n");
		
		// Printing variables
		for (TaskChar taCh: processModel.getProcessAlphabet()) {
			sBuil.append("  ");
			sBuil.append(taCh.identifier);
			sBuil.append(": boolean; # ");
			sBuil.append(taCh.toString());
			sBuil.append('\n');
		}
		
		sBuil.append('\n');
		
		// Printing constraints
		int i = 1;
		for (Constraint con: processModel.getAllConstraints()) {
			sBuil.append("LTLSPEC NAME P");
			sBuil.append(i++);
			sBuil.append(" := ");
			sBuil.append(con.getLTLpfExpression());
			sBuil.append("; #");
			sBuil.append(con.toString());
			sBuil.append('\n');
		}
		
		return sBuil.toString();
    }

}