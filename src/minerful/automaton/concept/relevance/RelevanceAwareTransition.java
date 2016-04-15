package minerful.automaton.concept.relevance;

import javax.xml.bind.annotation.XmlAttribute;

import minerful.utils.RandomCharGenerator;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class RelevanceAwareTransition extends Transition {
	private static final long serialVersionUID = 4844924976055163637L;

	private TransitionRelevance relevance = TransitionRelevance.RELEVANT;

	private String name = "";
	private String taskName = null;
//	private String transitionUUID = UUID.randomUUID().toString(); //ID String would be too long
	private String transitionUUID = "t" + RandomCharGenerator.generateChar(6);

	
	@XmlAttribute(name="id")
	public String getTransitionUUID() {
		return transitionUUID;
	}

	public void setTransitionUUID(String transitionUUID) {
		this.transitionUUID = transitionUUID;
	}

	public RelevanceAwareTransition(char event, State to, String taskName) {
		super(event, to);
		this.taskName = taskName;
	}
	
	@XmlAttribute(name="to")
	public String getDestinationStateUUID(){
		return ((ActivationStatusAwareState) getDest()).getStateUUID();
	}
	
	@Override
	public char getMin() {
		return super.getMin();
	}

	@Override
	public char getMax() {
		return super.getMax();
	}

	@Override
	public State getDest() {
		return super.getDest();
	}

	public TransitionRelevance getRelevance() {
		return relevance;
	}

	public void setRelevance(TransitionRelevance relevance) {
		this.relevance = relevance;
	}

	@XmlAttribute
	public String getName() {
		name = getMin() == getMax() ? "" + getMin() : "" + getMin() + getMax(); //Character of the transition (should not be more than one character)
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RelevanceAwareTransition [relevance=");
		builder.append(relevance);
		builder.append(", name=");
		builder.append(name);
		builder.append(", taskName=");
		builder.append(taskName);
		builder.append("]");
		return builder.toString();
	}
}