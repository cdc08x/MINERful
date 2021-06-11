package minerful.io.encdec.nusmv;

import java.util.Iterator;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;

public class NuSMVEncoder {
	ProcessModel processModel = null;
	
	public static String OR = "|";
	
	/** Used in {@link #printAsTaskAssignableNuSMV this.printAsTaskAssignableNuSMV()} */
	public static String VAR_ASGN_COMPACT_NUSMV = "DEFINE %2$s := %1$s = _%2$s; -- %3$s";
	
	/** Used in {@link #printAsNuSMV this.printAsNuSMV()}.
	 * TODO: Make it configurable or decide on a version. */
	public static Boolean USE_ONE_VAR = true;

	/**
	 * Constructor of this class
	 * @param processModel
	 */
	public NuSMVEncoder(ProcessModel processModel) {
		this.processModel = processModel;
	}

	/**
	 * Invokes {@link #printAsNuSMV printAsNuSMV()} setting <code>oneVar</code> as {@link #USE_ONE_VAR USE_ONE_VAR}.
	 * @return A NuSMV LTL specification.
	 */
	public String printAsNuSMV() {
		return printAsNuSMV(USE_ONE_VAR);
	}

	/**
	 * Prints the specification as a NuSMV LTLspec using either {@link #printAsMultiVarNuSMV printAsMultiVarNuSMV()} or {@link #printAsTaskAssignableNuSMV printAsTaskAssignableNuSMV()}
	 * according to whether <code>oneVar</code> is <code>false</code> or <code>true</code>, respectively.
	 * @param oneVar Determines whether {@link #printAsMultiVarNuSMV printAsMultiVarNuSMV()} or {@link #printAsTaskAssignableNuSMV printAsTaskAssignableNuSMV()} should be used to print the NuSVM specification.
	 * @return A NuSMV LTL specification.
	 */
	public String printAsNuSMV(boolean oneVar) {
		if (oneVar) {
			return printAsOneVarNuSMV();
		}
		return printAsMultiVarNuSMV();
	}

	/**
	 * Prints the specification as a NuSMV LTLspec wherein every task corresponds to a variable.
	 * @return A NuSMV LTL specification.
	 */
	public String printAsMultiVarNuSMV() {
		StringBuilder
			scriptBuil = new StringBuilder(),
			disjInvaBuil = new StringBuilder(),
			xorInvaBuil = new StringBuilder(),
			noneOfOtheBuil = new StringBuilder();
		
		scriptBuil.append("MODULE main\n\nVAR\n");
		disjInvaBuil.append("\nINVAR\n    ");
		xorInvaBuil.append("\nINVAR\n    ");
		
		Iterator<TaskChar> taChIt = processModel.getProcessAlphabet().iterator();
		TaskChar taCh = null;
		String taChNumId = null;
		// Printing variables
		while (taChIt.hasNext()) {
			taCh = taChIt.next();
			taChNumId = taCh.getTaskNumericId();
			scriptBuil.append("    ");
			scriptBuil.append(taChNumId);
			scriptBuil.append(": boolean; -- ");
			scriptBuil.append(taCh.toString());
			scriptBuil.append('\n');
			
			// Preparing the invariant that at any instant there should be a task executed
			disjInvaBuil.append(taChNumId);
			disjInvaBuil.append( (taChIt.hasNext() ? " | " : ";\n") );

			// Preparing the consequent of INVAR \wedge_{a,b \in P, a\not=b} a \rightarrow \neg b
			noneOfOtheBuil.append(" & !");
			noneOfOtheBuil.append(taChNumId);
		}
		// Complete the INVAR \wedge_{a,b \in P, a\not=b} a \rightarrow \neg b
		String noneOfTheOtherTasks = noneOfOtheBuil.toString();
		taChIt = processModel.getProcessAlphabet().iterator();
		while (taChIt.hasNext()) {
			taChNumId = taChIt.next().getTaskNumericId();
			xorInvaBuil.append(taChNumId);
			xorInvaBuil.append(" -> ");
			xorInvaBuil.append(noneOfTheOtherTasks.replace(" & !" + taChNumId, "").substring(3)); //" & ".length());
			xorInvaBuil.append( (taChIt.hasNext() ? "\n  & " : ";\n") );
		}
		
		scriptBuil.append('\n');		
		// Printing the invariant that at any instant there should be a task executed
		scriptBuil.append(disjInvaBuil.toString());
		// Printing the invariant that at any instant only one task is executed
		scriptBuil.append(xorInvaBuil.toString());		
		scriptBuil.append('\n');
		
		// Printing constraints
		scriptBuil.append(printNuSMVLTLspec());
		return scriptBuil.toString();
    }

	/**
	 * Prints the specification as a NuSMV LTLspec wherein every task corresponds to an assignable value for a unique variable named as {@link #minerful.concept.TaskChar.TASK_NUM_PREFIX TaskChar.TASK_NUM_PREFIX}.
	 * @return A NuSMV LTL specification.
	 */
	public String printAsOneVarNuSMV() {
		StringBuilder
			scriptBuil = new StringBuilder(),
			tasklistBuil = new StringBuilder();
		scriptBuil.append("MODULE main\n\nVAR ");
		scriptBuil.append(TaskChar.TASK_NUM_PREFIX);
		scriptBuil.append(": {_");
		
		// Printing variables
		for (TaskChar taCh: processModel.getProcessAlphabet()) {
			// Add the task numeric id in the list of assignable values for variable "TASK_NUM_PREFIX"
			scriptBuil.append(taCh.getTaskNumericId());
			scriptBuil.append(", _");
			// Append in the comment what that identifier refers to
			tasklistBuil.append('\n');
			tasklistBuil.append(String.format(
					VAR_ASGN_COMPACT_NUSMV,
					TaskChar.TASK_NUM_PREFIX, taCh.getTaskNumericId(), taCh.getName()));
		}

		tasklistBuil.append('\n');
		scriptBuil.delete(scriptBuil.length() - 1, scriptBuil.length()); // Remove the last "_"
		scriptBuil.append("None}\n");		
		scriptBuil.append(tasklistBuil.toString());

		// Printing constraints
		scriptBuil.append(printNuSMVLTLspec());
		return scriptBuil.toString();
    }

	private String printNuSMVLTLspec() {
		StringBuilder ltlSBuil = new StringBuilder();
		// Printing constraints
		ltlSBuil.append('\n');		
		int i = 1;
		for (Constraint con: processModel.getAllConstraints()) {
			ltlSBuil.append("LTLSPEC NAME R");
			ltlSBuil.append(i++);
			ltlSBuil.append(" := ");
			ltlSBuil.append(con.getLTLpfExpression());
			ltlSBuil.append("; -- ");
			ltlSBuil.append(con.toString());
			ltlSBuil.append('\n');
		}
		
		return ltlSBuil.toString();
	}

}