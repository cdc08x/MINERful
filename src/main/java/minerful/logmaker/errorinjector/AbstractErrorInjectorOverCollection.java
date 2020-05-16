package minerful.logmaker.errorinjector;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractErrorInjectorOverCollection extends AbstractErrorInjectorImpl {
	public AbstractErrorInjectorOverCollection(String[] testBedArray) {
		super(testBedArray);
	}

	@Override
	protected List<List<TestBedCandidate>> decideErrorInjectionPoints() {
		int		numOfErrors = 0,
				upperBound = 0;
		List<List<TestBedCandidate>> errorInjectionPointsCollector = new ArrayList<List<TestBedCandidate>>(1);
		List<TestBedCandidate> errorInjectionPoints = new ArrayList<TestBedCandidate>();

		// If there was a target character to insert/remove…
		if (this.isThereAnyTargetCharacter()) {
			// … the upper bound for the errors you can inject is given by the number of occurrences of it in the whole collection
			upperBound = this.countOccurrences(this.targetChar);
		} else {
			// … otherwise, the upper bound is the number of the characters appearing in the collection
			upperBound = this.countOccurrences();
		}
		// Decide the number of errors to inject
		numOfErrors = this.applyErrorInjectionPercentage(upperBound);
		// Until you have not counted all of the errors you decided to inject…
		while (numOfErrors-- > 0) {
			// … keep on putting entries in the error injection data structure
			errorInjectionPoints.add(
					new TestBedCandidate(
							Math.random()
					)
			);
		}
		
		errorInjectionPointsCollector.add(errorInjectionPoints);

		return errorInjectionPointsCollector;
	}
}