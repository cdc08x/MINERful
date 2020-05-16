package minerful.automaton.concept.weight;

import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import minerful.utils.MessagePrinter;
import minerful.utils.RandomCharGenerator;

@XmlAccessorType(XmlAccessType.NONE)
public class WeightedState extends State implements AutomatonElementQuantileButter, AutomatonNonConformityElementButter {

	private static final long serialVersionUID = -3665359375777248550L;

	public static MessagePrinter logger = MessagePrinter.getInstance(WeightedState.class);
	
	private int weight = 0;
	private int weightQuantile = UNASSIGNED_QUANTILE;
	private int nonConformityWeight = 0;
	private int nonConformityWeightQuantile = 0;
	private boolean illegal = false;
	
//	private String stateUUID = UUID.randomUUID().toString(); //ID String would be too long
	private String stateUUID = "s" + RandomCharGenerator.generateChar(6);
	
	private NavigableMap<Character, Transition> transitionMap = new TreeMap<Character, Transition>();
	
	@Override
	@XmlElementWrapper(name="transitions")
	@XmlElement(name="transition")
	@XmlJavaTypeAdapter(WeightedTransitionXmlAdapter.class)
	public Set<Transition> getTransitions() {
		return super.getTransitions();
	}
	

	@XmlAttribute(name="id")
	public String getStateUUID() {
		return stateUUID;
	}

	public void setStateUUID(String stateUUID) {
		this.stateUUID = stateUUID;
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
	public void setAccept(boolean accept) {
		super.setAccept(accept);
	}
	
	@Override
	@XmlAttribute
	public boolean isAccept() {
		return super.isAccept();
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
	public void addTransition(Transition transition) {
		for (char fire = transition.getMin(); fire <= transition.getMax(); fire++)
			this.transitionMap.put(fire, transition);

		super.addTransition(transition);
	}
	
	public WeightedState stepAndIncreaseTransitionWeight(char chr) {
		try {
			((WeightedTransition) this.transitionMap.get(chr)).increaseWeight();
		} catch (NullPointerException nPEx) {
			logger.error("Unallowed transition requested!");
			// nPEx.printStackTrace();
			logger.error("Transition map: " + this.transitionMap);
			logger.error("Searched chr: " + chr);
			logger.error("State: " + super.toString());
			return null;
		}
		
		return (WeightedState) super.step(chr);
	}

	public WeightedState stepAndIncreaseTransitionsNonConformityWeight(char chr) {
		try {
			((WeightedTransition) this.transitionMap.get(chr)).increaseNonConformityWeight();
		} catch (NullPointerException nPEx) {
			// All right here: it happens!
		}
		return (WeightedState) super.step(chr);
	}
	
	@Override
	public String toString() {
		StringBuilder sBuildo = new StringBuilder();
		sBuildo.append(super.toString());
		sBuildo.append('\n');
		sBuildo.append("weight=");
		sBuildo.append(this.weight);
		if (this.weightQuantile != UNASSIGNED_QUANTILE) {
			sBuildo.append(" (");
			sBuildo.append(this.weightQuantile + 1);
			sBuildo.append(". quantile");
			sBuildo.append(')');
		}
		sBuildo.append('\n');
		
		return sBuildo.toString();
	}

}