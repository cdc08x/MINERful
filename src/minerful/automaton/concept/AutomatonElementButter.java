package minerful.automaton.concept;

public interface AutomatonElementButter {
	public static final int UNASSIGNED_QUANTILE = -1;

	public static final int SINGLE_WEIGHT_INCREASE = 1;

	public abstract int increaseWeight();

	public abstract int addWeight(int weight);

	public abstract int getWeight();

	public abstract void setWeight(int weight);

	public abstract int getWeightQuantile();

	public abstract void setWeightQuantile(int weightQuantile);

}