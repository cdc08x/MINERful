package minerful.automaton.concept.weight;

public interface AutomatonElementQuantileButter extends AutomatonElementButter {
	public static final int UNASSIGNED_QUANTILE = -1;

	public abstract int getWeightQuantile();
	public abstract void setWeightQuantile(int weightQuantile);
}