package minerful.automaton.concept.weight;

public interface AutomatonNonConformityElementButter {
	public abstract boolean isIllegal();
	
	public abstract void setIllegal(boolean illegal);

	public abstract int increaseNonConformityWeight();

	public abstract int addNonConformityWeight(int weight);

	public abstract int getNonConformityWeight();

	public abstract void setNonConformityWeight(int weight);

	public abstract int getNonConformityWeightQuantile();

	public abstract void setNonConformityWeightQuantile(int weightQuantile);

}