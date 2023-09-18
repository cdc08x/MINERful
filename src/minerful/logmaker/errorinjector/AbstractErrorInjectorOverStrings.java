package minerful.logmaker.errorinjector;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractErrorInjectorOverStrings extends AbstractErrorInjectorImpl {
	public AbstractErrorInjectorOverStrings(String[] testBedArray) {
		super(testBedArray);
	}

	@Override
	protected List<List<TestBedCandidate>> decideErrorInjectionPoints() {
		int	numOfErrors = 0,
				upperBound = 0;
		List<List<TestBedCandidate>> errorInjectionPointsCollector = new ArrayList<List<TestBedCandidate>>(
				this.testBed.length);
		List<TestBedCandidate> errorInjectionPoints = null;

		logger.trace("Error injection points are being decided...");
		
		// For each string in the testbed
		for (int i = 0; i < this.testBed.length; i++) {
			
			errorInjectionPoints = new ArrayList<TestBedCandidate>();
			
			// If there was a target character to insert/remove…
			if (this.isThereAnyTargetCharacter()) {
				// … the upper bound for the errors you can inject is given by the number of occurrences of it in the current string
				upperBound = this.countOccurrences(i, this.targetChar);
			} else {
				// … otherwise, the upper bound is the length of the string itself
				upperBound = this.testBed[i].length();
			}
			// Decide the number of errors to inject
			numOfErrors = this.applyErrorInjectionPercentage(upperBound);
			
			if (this.isThereAnyTargetCharacter())
				logger.trace(numOfErrors + " errors are being injected in string " + this.testBed[i] + ", which has " + upperBound + " " + this.targetChar + "'s in.");
			else
				logger.trace(numOfErrors + " errors are being injected in string " + this.testBed[i] + ", which is " + this.testBed[i].length() + " chr's long.");
			
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
		}
		
		logger.trace("Error injection points have been decided.");		
		
		return errorInjectionPointsCollector;
	}
}