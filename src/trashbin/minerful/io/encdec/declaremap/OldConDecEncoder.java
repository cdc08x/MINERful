package trashbin.minerful.io.encdec.declaremap;

import java.util.HashMap;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.relation.*;

import org.apache.commons.lang3.StringEscapeUtils;

@Deprecated
public class OldConDecEncoder {
	private ProcessModel process;
	
	public static HashMap<Class<? extends Constraint>, String> NAME_CONVERTER =
			new HashMap<Class<? extends Constraint>, String>(
					MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES + MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES,
					1.0F);
	public static HashMap<Class<? extends Constraint>, String> LTL_CONVERTER =
			new HashMap<Class<? extends Constraint>, String>(
					MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES + MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES,
					1.0F);
	
	static {
//		NAME_CONVERTER.put(End.class, "end");
		NAME_CONVERTER.put(Init.class, "init");
		NAME_CONVERTER.put(Participation.class, "existence");
		NAME_CONVERTER.put(AtMostOne.class, "absence2");
		// Relation
		NAME_CONVERTER.put(RespondedExistence.class, "responded existence");
		NAME_CONVERTER.put(Response.class, "response");
		NAME_CONVERTER.put(Precedence.class, "precedence");
		NAME_CONVERTER.put(AlternateResponse.class, "alternate response");
		NAME_CONVERTER.put(AlternatePrecedence.class, "alternate precedence");
		NAME_CONVERTER.put(ChainResponse.class, "chain response");
		NAME_CONVERTER.put(ChainPrecedence.class, "chain precedence");
		// Mutual relation
		NAME_CONVERTER.put(CoExistence.class, "coexistence");
		NAME_CONVERTER.put(Succession.class, "succession");
		NAME_CONVERTER.put(AlternateSuccession.class, "alternate succession");
		NAME_CONVERTER.put(ChainSuccession.class, "chain succession");
		// Negation relation
		NAME_CONVERTER.put(NotCoExistence.class, "not coexistence");
		NAME_CONVERTER.put(NotChainSuccession.class, "not chain succession");
		NAME_CONVERTER.put(NotSuccession.class, "not succession");

//		LTL_CONVERTER.put(End.class, "end");
		LTL_CONVERTER.put(Init.class, "( ( \"A.started\" \\/ \"A.cancelled\" ) W \"A\" )");
		LTL_CONVERTER.put(Participation.class, "<> ( \"A\" )");
		LTL_CONVERTER.put(AtMostOne.class, "! ( <> ( ( \"A\" /\\ X(<>(\"A\")) ) ) )");
		// Relation
		LTL_CONVERTER.put(RespondedExistence.class, "( <>(\"A\") -> <>( \"B\" ) )");
		LTL_CONVERTER.put(Response.class, "[]( ( \"A\" -> <>( \"B\" ) ) )");
		LTL_CONVERTER.put(Precedence.class, "( ! (\"B\" ) U \"A\" ) \\/ ([](!(\"B\"))) /\\ ! (\"B\" )");
		LTL_CONVERTER.put(AlternateResponse.class, "[]( ( \"A\" -> X(( !(\"A\") U \"B\" ))))");
		LTL_CONVERTER.put(AlternatePrecedence.class, "( ( !( \"B\" ) U \"A\" ) ) /\\ ( ( \"B\" -> X( ( !( \"B\" ) U \"A\" ) ) ) ) ) /\\ ! (\"B\" )");
		LTL_CONVERTER.put(ChainResponse.class, "[] ( ( \"A\" -> X( \"B\" ) ) )");
		LTL_CONVERTER.put(ChainPrecedence.class, "[]( ( X( \"B\" ) -> \"A\") )/\\ ! (\"B\" )");
		// Mutual relation
		LTL_CONVERTER.put(CoExistence.class, "( ( <>(\"A\") -> <>( \"B\" ) ) /\\ ( <>(\"B\") -> <>( \"A\" ) ) )");
		LTL_CONVERTER.put(Succession.class, "( []( ( \"A\" -> <>( \"B\" ) ) ) /\\ (( ( !(\"B\" ) U \"A\" )) \\/ ([](!(\"B\")))) )");
		LTL_CONVERTER.put(AlternateSuccession.class, "( []( ( \"A\" -> X(( ! ( \"A\" ) U \"B\" ) )) ) /\\ ( ( (! ( \"B\" ) U \"A\") \\/ ([](!(\"B\"))) ) /\\ [] ( ( \"B\" -> X( ( ( ! ( \"B\" ) U \"A\" )\\/([](!(\"B\"))) )) ) ) ) )");
		LTL_CONVERTER.put(ChainSuccession.class, "[]( ( \"A\" = X( \"B\" ) ) )");
		// Negation relation
		LTL_CONVERTER.put(NotCoExistence.class, "(<>A) -> (!(<>B))");
		LTL_CONVERTER.put(NotChainSuccession.class, "[]( ( \"A\" -> X( !( \"B\" ) ) ) )");
		LTL_CONVERTER.put(NotSuccession.class, "[]( ( \"A\" -> !( <>( \"B\" ) ) ) )");
	}

	public static String PROCESS_DEF_BEGIN_TEMPLATE =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + "\n" +
			"<model>" + "\n" +
			"  <assignment language=\"ConDec\" name=\"%s\">" + "\n";
	public static String ACTIVITY_DEF_GROUP_BEGIN_STRING =
			"    <activitydefinitions>" + "\n";
	public static String ACTIVITY_DEF_TEMPLATE =
			"      <activity id=\"%1$s\" name=\"%2$s\" />" + "\n";
	public static String ACTIVITY_DEF_GROUP_END_STRING =
			"    </activitydefinitions>" + "\n";
	public static String CONSTRAINTS_GROUP_BEGIN_STRING =
			"    <constraintdefinitions>" + "\n";
	public static String CONSTRAINT_DEF_BEGIN_TEMPLATE =
			"      <constraint id=\"%1$d\" mandatory=\"true\">" + "\n" +
			"        <condition />" + "\n" +
			"        <name>%2$s</name>" + "\n";
	public static String TEMPLATE_DEF_BEGIN_TEMPLATE =
			"        <template>" + "\n" +
			"          <description>%1$s constraint</description>" + "\n" +
			"          <display>%1$s</display>" + "\n" +
			"          <name>%1$s</name>" + "\n" +
			"          <text>%2$s</text>" + "\n";
	public static String PARAMETERS_GROUP_BEGIN_STRING =
			"          <parameters>" + "\n";
	public static String CONSTRAINT_PARAMETER_DEF_TEMPLATE =
			"            <parameter branchable=\"%3$s\" id=\"%1$d\" name=\"%2$s\">" + "\n" +
	// TODO The graphical part consists in dirty cheating
			"              <graphical>" + "\n" +
			"                <style number=\"1\" />" + "\n" +
			"                <begin fill=\"true\" style=\"10\" />" + "\n" +
			"                <middle fill=\"false\" style=\"0\" />" + "\n" +
			"                <end fill=\"false\" style=\"0\" />" + "\n" +
			"              </graphical>" + "\n" +
			"            </parameter>" + "\n";
	public static String PARAMETERS_GROUP_END_STRING =
			"          </parameters>" + "\n";
	public static String STATE_MESSAGES_DEF_STRING =
			"          <statemessages>" + "\n" +
			"            <message state=\"VIOLATED_TEMPORARY\">VIOLATED_TEMPORARY undefined</message>" + "\n" +
			"            <message state=\"SATISFIED\">SATISFIED undefined</message>" + "\n" +
			"            <message state=\"VIOLATED\">VIOLATED undefined</message>" + "\n" +
			"          </statemessages>" + "\n";
	public static String TEMPLATE_DEF_END_STRING =
			"        </template>" + "\n";
	public static String ACTUAL_PARAMETERS_GROUP_BEGIN_STRING =
			"        <constraintparameters>" + "\n";
	public static String ACTUAL_PARAMETER_DEF_TEMPLATE =
			"          <parameter templateparameter=\"%1$d\">" + "\n" +
			"            <branches>" + "\n" +
			"              <branch name=\"%2$s\" />" + "\n" +
			"            </branches>" + "\n" +
			"          </parameter>" + "\n";
	public static String ACTUAL_PARAMETERS_GROUP_END_STRING =
			"        </constraintparameters>" + "\n";
	public static String CONSTRAINT_DEF_END_STRING =
			"      </constraint>" + "\n";
	public static String CONSTRAINTS_GROUP_END_STRING =
			"    </constraintdefinitions>" + "\n";
	// TODO The graphical part consists in dirty cheating
	public static String GRAPHICAL_DETAILS_BEGIN_STRING =
			"    <graphical>" + "\n";
	public static String ACTIVITIES_GRAPHICAL_DETAILS_GROUP_BEGIN_STRING =
			"      <activities>" + "\n";
	public static String ACTIVITY_GRAPHICAL_DETAILS_TEMPLATE =
			"        <cell height=\"30.0\" id=\"%1$s\" width=\"95.0\" x=\"610.3032983966159\" y=\"68.04626585205398\" />" + "\n";
	public static String ACTIVITIES_GRAPHICAL_DETAILS_GROUP_END_STRING =
			"      </activities>" + "\n";
	public static String CONSTRAINTS_GRAPHICAL_DETAILS_GROUP_BEGIN_STRING =
			"      <constraints>" + "\n";
	public static String CONSTRAINT_GRAPHICAL_DETAILS_TEMPLATE =
			"        <cell height=\"1.0\" id=\"%1$d\" width=\"1.0\" x=\"705.9282992379913\" y=\"213.1872845786157\" />" + "\n";
	public static String CONSTRAINTS_GRAPHICAL_DETAILS_GROUP_END_STRING =
			"      </constraints>" + "\n";
	public static String GRAPHICAL_DETAILS_END_STRING =
			"    </graphical>" + "\n";
	public static String PROCESS_DEF_END_STRING =
			"  </assignment>" + "\n" +
			"</model>" + "\n";
	
	public static String COMPLETE_EVENT_SUFFIX = "-complete";
	
	public OldConDecEncoder(ProcessModel process) {
		this.process = process;
	}
	

	private String makeItACompleteEvent(TaskChar tCh) {
		return StringEscapeUtils.escapeXml(tCh.getName()) + COMPLETE_EVENT_SUFFIX;
	}
	
	public String encode() {
		StringBuilder sBuil = new StringBuilder();
		
		sBuil.append(String.format(PROCESS_DEF_BEGIN_TEMPLATE, this.process.getName()));
		sBuil.append(ACTIVITY_DEF_GROUP_BEGIN_STRING);
		
		for (TaskChar tCh : this.process.bag.getTaskChars()) {
			sBuil.append(String.format(ACTIVITY_DEF_TEMPLATE, StringEscapeUtils.escapeXml(tCh.identifier.toString()), this.makeItACompleteEvent(tCh)));
		}
		
		sBuil.append(ACTIVITY_DEF_GROUP_END_STRING);
		sBuil.append(CONSTRAINTS_GROUP_BEGIN_STRING);
		
		int i = 1, j = 1, k = 1;
		for (TaskChar tCh : this.process.bag.getTaskChars()) {
			for (Constraint c : this.process.bag.getConstraintsOf(tCh)) {
				j = 1;
				k = 1;
				char ci = 'A';
				String declareConstraintName = (
						NAME_CONVERTER.containsKey(c.getClass())
						?	StringEscapeUtils.escapeXml(NAME_CONVERTER.get(c.getClass()))
						:	null);
				
				if (declareConstraintName != null) {
					sBuil.append(String.format(CONSTRAINT_DEF_BEGIN_TEMPLATE, i++, declareConstraintName));
					sBuil.append(String.format(TEMPLATE_DEF_BEGIN_TEMPLATE, declareConstraintName, StringEscapeUtils.escapeXml(LTL_CONVERTER.get(c.getClass()))));
					sBuil.append(PARAMETERS_GROUP_BEGIN_STRING);
					sBuil.append(String.format(CONSTRAINT_PARAMETER_DEF_TEMPLATE, j++, ci++,
							((c instanceof RelationConstraint) ? "true" : "false")
					));
					if (c instanceof RelationConstraint) {
						// TODO This trick is not going to work with branching Declare constraints!
						sBuil.append(String.format(CONSTRAINT_PARAMETER_DEF_TEMPLATE, j++, ci, "true"));
					}
					sBuil.append(PARAMETERS_GROUP_END_STRING);
					sBuil.append(STATE_MESSAGES_DEF_STRING);
					sBuil.append(TEMPLATE_DEF_END_STRING);
					sBuil.append(ACTUAL_PARAMETERS_GROUP_BEGIN_STRING);
					
					sBuil.append(String.format(ACTUAL_PARAMETER_DEF_TEMPLATE, k++, this.makeItACompleteEvent(c.getBase().getTaskCharsArray()[0])));
					if (c instanceof RelationConstraint) {
						sBuil.append(String.format(ACTUAL_PARAMETER_DEF_TEMPLATE, k++, this.makeItACompleteEvent(((RelationConstraint) c).getImplied().getTaskCharsArray()[0])));
					}
					sBuil.append(ACTUAL_PARAMETERS_GROUP_END_STRING);
					sBuil.append(CONSTRAINT_DEF_END_STRING);
				}
			}
		}
		
		sBuil.append(CONSTRAINTS_GROUP_END_STRING);
		
		sBuil.append(GRAPHICAL_DETAILS_BEGIN_STRING);
		sBuil.append(ACTIVITIES_GRAPHICAL_DETAILS_GROUP_BEGIN_STRING);
		
		for (TaskChar tCh : this.process.bag.getTaskChars()) {
			sBuil.append(String.format(ACTIVITY_GRAPHICAL_DETAILS_TEMPLATE, StringEscapeUtils.escapeXml(tCh.identifier.toString())));
		}
		
		sBuil.append(ACTIVITIES_GRAPHICAL_DETAILS_GROUP_END_STRING);
		sBuil.append(CONSTRAINTS_GRAPHICAL_DETAILS_GROUP_BEGIN_STRING);
		
		i = 1;
		for (TaskChar tCh : this.process.bag.getTaskChars()) {
			for (Constraint c : this.process.bag.getConstraintsOf(tCh)) {
				if (NAME_CONVERTER.containsKey(c.getClass())) {
					sBuil.append(String.format(CONSTRAINT_GRAPHICAL_DETAILS_TEMPLATE, i++));
				}
			}
		}
		sBuil.append(CONSTRAINTS_GRAPHICAL_DETAILS_GROUP_END_STRING);
		sBuil.append(GRAPHICAL_DETAILS_END_STRING);
		
		sBuil.append(PROCESS_DEF_END_STRING);
		
		return sBuil.toString();
	}
}