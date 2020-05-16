package minerful.automaton.concept.weight;

import javax.xml.bind.annotation.XmlAttribute;

import minerful.utils.RandomCharGenerator;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class WeightedTransition extends Transition implements AutomatonElementQuantileButter, AutomatonNonConformityElementButter {
	private static final long serialVersionUID = -135495105218792952L;
	
	private int weight = 0;
	private int nonConformityWeight = 0;
	private int weightQuantile = 0;
	private int nonConformityWeightQuantile = 0;
	private boolean illegal = false;
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

	public WeightedTransition(char event, State to, String taskName) {
		super(event, to);
		this.taskName = taskName;
	}
	
	@XmlAttribute(name="to")
	public String getDestinationStateUUID(){
		return ((WeightedState) getDest()).getStateUUID();
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

	@Override
	public int increaseWeight() {
		return this.addWeight(SINGLE_WEIGHT_INCREASE);
	}
	@Override
	public int addWeight(int weight) {
		setWeight(this.weight + weight);
		return getWeight();
	}
	@Override
	@XmlAttribute
	public int getWeight() {
		return weight;
	}
	@Override
	public void setWeight(int weight) {
		this.weight = weight;
	}
	@Override
	@XmlAttribute
	public int getWeightQuantile() {
		return weightQuantile;
	}
	@Override
	public void setWeightQuantile(int weightQuantile) {
		this.weightQuantile = weightQuantile;
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
	public int increaseNonConformityWeight() {
		return this.addNonConformityWeight(SINGLE_WEIGHT_INCREASE);
	}

	@Override
	public int addNonConformityWeight(int weight) {
		setNonConformityWeight(this.weight + weight);
		return getNonConformityWeight();
	}

	@Override
	@XmlAttribute
	public int getNonConformityWeight() {
		return this.nonConformityWeight;
	}

	@Override
	public void setNonConformityWeight(int weight) {
		this.nonConformityWeight = weight;
	}

	@Override
	@XmlAttribute
	public int getNonConformityWeightQuantile() {
		return this.nonConformityWeightQuantile;
	}

	@Override
	public void setNonConformityWeightQuantile(int weightQuantile) {
		this.nonConformityWeightQuantile = weightQuantile;
	}
	
	@Override
	@XmlAttribute
	public boolean isIllegal() {
		return this.illegal;
	}
	
	@Override
	public void setIllegal(boolean illegal) {
		this.illegal = illegal;
	}

	@Override
	public String toString() {
		StringBuilder sBuildo = new StringBuilder();
		sBuildo.append(super.toString());
		sBuildo.append("; ");
		sBuildo.append("weight=");
		sBuildo.append(this.weight);
		if (this.weightQuantile != UNASSIGNED_QUANTILE) {
			sBuildo.append(" (");
			sBuildo.append(this.weightQuantile + 1);
			sBuildo.append(". quantile");
			sBuildo.append(')');
		}
		
		return sBuildo.toString();
	}
}