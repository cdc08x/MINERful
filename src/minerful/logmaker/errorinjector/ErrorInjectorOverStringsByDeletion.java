package minerful.logmaker.errorinjector;

import java.util.ArrayList;
import java.util.List;

public class ErrorInjectorOverStringsByDeletion extends AbstractErrorInjectorOverStrings {
	public ErrorInjectorOverStringsByDeletion(String[] testBedArray) {
		super(testBedArray);
	}

	@Override
	protected List<List<TargetDataStructure>> prepareTargets() {
		List<List<TargetDataStructure>> targets = new ArrayList<List<TargetDataStructure>>(this.testBed.length);
		List<TargetDataStructure> targetsInString = null;
		logger.trace("Targets for error injection are being prepared...");

		int stringsCounter = 0;
		if (isThereAnyTargetCharacter()) {
			for (; stringsCounter < testBed.length; stringsCounter++) {
				targetsInString = new ArrayList<IErrorInjector.TargetDataStructure>();
				
				// Beware: you can not remove a character which is not already in the string!
				List<Integer> occurrences = super.findOccurrences(
						stringsCounter,
						this.targetChar);
				if (occurrences.size() > 0) {
					for (Integer occurrence : occurrences) {
						logger.trace("Adding occurrence " + occurrence + " in string " + this.testBed[stringsCounter]);
						targetsInString.add(
								new TargetDataStructure(
										stringsCounter,
										occurrence
										)
								);
					}
				}
				
				targets.add(targetsInString);
			}
		}
		else {
			for (StringBuffer testString : testBed) {
				targetsInString = new ArrayList<IErrorInjector.TargetDataStructure>();
				
				for (int charCounter = 0; charCounter < testString.length() -1; charCounter++) {
					targetsInString.add(
							new TargetDataStructure(
									stringsCounter,
									charCounter
									)
							);
				}

				stringsCounter++;
				targets.add(targetsInString);
			}
		}
		logger.trace("Targets for error injection are ready.");
		
		return targets;
	}

	@Override
	protected List<TargetDataStructure> executeErrorInjection(
			double errorInjectionTargetProportionalIndex,
			char injectableChar, List<TargetDataStructure> targetsInString) {
		
		if (targetsInString.size() == 0) {
			logger.trace("No " + injectableChar + " character to delete");
			
			return targetsInString;
		}
		
		int injectedIndex = this.applyAndRound(
				errorInjectionTargetProportionalIndex,
				targetsInString.size() -1
				);

		logger.trace("Error injection: deleting " + injectableChar + " in position " + targetsInString.get(injectedIndex).index + " of " + this.testBed[targetsInString.get(injectedIndex).stringNumber]);
		
		this.testBed[targetsInString.get(injectedIndex).stringNumber].deleteCharAt(
				targetsInString.get(injectedIndex).index
				);
		// Beware: you just removed a char from the string!
		targetsInString.remove(injectedIndex);
		// All the following characters in the same string must turn their index reduced by 1
		for (	int i = injectedIndex;
				i < targetsInString.size();
				i++) {
			targetsInString.get(i).index--;
		}
		
		return targetsInString;
	}
}
