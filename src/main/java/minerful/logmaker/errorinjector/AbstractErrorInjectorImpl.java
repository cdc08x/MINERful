package minerful.logmaker.errorinjector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractErrorInjectorImpl extends AbstractErrorInjector {
	abstract List<TargetDataStructure> executeErrorInjection(
			double errorInjectionTargetProportionalIndex,
			char injectableChar,
			List<TargetDataStructure> targets);

	protected abstract List<List<TestBedCandidate>> decideErrorInjectionPoints();

	abstract List<List<TargetDataStructure>> prepareTargets();

	AbstractErrorInjectorImpl(String[] testBedArray) {
		super(testBedArray);
	}

	/**
	 * Generates a random number, e.g., used to decide where to make a
	 * modification in the string. The random number can range from 1 up to the
	 * given upper bound (excluded).
	 * 
	 * @param upperBound
	 *            The upper bound for the random number. It must be greater
	 *            or equal than 0.
	 * @return The random number.
	 */
    protected int decideBoundedRandom(int upperBound) {
    	if (upperBound < 0)
    		throw new IllegalArgumentException(
    				"Invalid upper bound: " + upperBound);
		int pos = this.applyAndRound(Math.random(), upperBound);
		return pos;
	}
    
    protected int applyAndRound(double value, int number) {
    	return (int)StrictMath.round(value * number);
    }
    
    protected char decideRandomChar() {
    	return this.alphabet[this.decideBoundedRandom(this.alphabet.length-1)];
    }
    
    protected List<Integer> findOccurrences(int indexOfTheStringToScan, char targetCharacter) {
    	int		k = this.testBed[indexOfTheStringToScan].indexOf(
				String.valueOf(targetCharacter)
				);
		logger.trace("Searching occurrences of "
				+ targetCharacter
				+ " into "
				+ this.testBed[indexOfTheStringToScan]
				+ "... ");

		List<Integer> occurrences = new ArrayList<Integer>();
		
		while (k > -1) {
			occurrences.add(k);
			k = this.testBed[indexOfTheStringToScan].indexOf(String.valueOf(targetCharacter), k+1);
		}
		
		logger.trace(occurrences.size());
		
		return occurrences;
   }
    
    protected int countOccurrences(int indexOfTheStringToScan, char targetCharacter) {
    	int		occurrences = 0,	
    			k = this.testBed[indexOfTheStringToScan].indexOf(
    					String.valueOf(targetCharacter)
    					);
		logger.trace("Counting occurrences of "
				+ targetCharacter
				+ " into "
				+ this.testBed[indexOfTheStringToScan]
				+ "... ");

		while (k > -1) {
			occurrences++;
			k = this.testBed[indexOfTheStringToScan].indexOf(String.valueOf(targetCharacter), k+1);
		}
		logger.trace(occurrences);
    	return occurrences;
    }
    
    protected int countOccurrences(char targetCharacter) {
    	int	occurrences = 0;
    	for (int i = 0; i < this.testBed.length; i++) {
    		occurrences += this.countOccurrences(i, targetCharacter);
    	}
    	return occurrences;
    }
    
    protected int countOccurrences() {
    	int amount = 0;
    	for (int i = 0; i < this.testBed.length; i++) {
    		amount += this.testBed[i].length();
    	}
    	return amount;
    }
	
	protected int applyErrorInjectionPercentage(int number) {
		double rawPercentageApplication = number * this.errorsInjectionPercentage / 100.0;
		if (	StrictMath.ceil(rawPercentageApplication)
				!=
				StrictMath.floor(rawPercentageApplication)
				) {
			boolean preferFloorValue = ( this.applyAndRound(Math.random(), 1) == 1);
			return (int)(
					preferFloorValue ?
							StrictMath.floor(rawPercentageApplication) :
								StrictMath.ceil(rawPercentageApplication)
					);
		}
		return (int)StrictMath.round(rawPercentageApplication);
	}

	@Override
	public String[] injectErrors() {
		return this.executeErrorInjection(
				this.decideErrorInjectionPoints(),
				this.prepareTargets());
	}
	
	protected String[] executeErrorInjection(
			List<List<TestBedCandidate>> errorInjectionPoints,
			List<List<TargetDataStructure>> targets) {
		logger.trace("errorInjectionPoints.size() = " + errorInjectionPoints.size());
		logger.trace("targets.size() = " + targets.size());
		if (errorInjectionPoints.size() != targets.size())
			throw new IllegalArgumentException("Error injection points and targets are not sized the same! " +
					"They must be long the same.");
		
		Iterator<List<TestBedCandidate>> errorInjIterator = errorInjectionPoints.iterator();
		Iterator<List<TargetDataStructure>> targetIterator = targets.iterator();
		
		List<TestBedCandidate> errorInjectionPointsInString = null;
		List<TargetDataStructure> targetsInString = null;
		// If there is a target character, you have to insert it, and only it.
		Character injectableChar = (
				isThereAnyTargetCharacter() ?
						this.getTargetChar() :
							null);
		
		while (errorInjIterator.hasNext()) {
			errorInjectionPointsInString = errorInjIterator.next();
			targetsInString = targetIterator.next();
			
			// Apply the error
			for (TestBedCandidate errorInjection : errorInjectionPointsInString) {
				// If there is not a target character, you have to decide one at each loop.
				if (!isThereAnyTargetCharacter()) {
					injectableChar = this.decideRandomChar();
				}
				// Now, you can insert the error.
logger.trace("Length of target indexes, before: " + targetsInString.size());
				//targetsInString = this.executeErrorInjection(injectedIndex, injectableChar, targetsInString);
				this.executeErrorInjection(errorInjection.candidateProportionalIndex, injectableChar, targetsInString);
logger.trace("Length of target indexes, after: " + targetsInString.size());
			}
		}
		
		return this.testBedArray();
	}
}