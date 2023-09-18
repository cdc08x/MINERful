package minerful.logmaker.errorinjector;

import java.util.ArrayList;
import java.util.List;

public class ErrorInjectorOverCollectionByDeletion extends AbstractErrorInjectorOverCollection {
	public ErrorInjectorOverCollectionByDeletion(String[] testBedArray) {
		super(testBedArray);
	}

	@Override
	protected List<List<TargetDataStructure>> prepareTargets() {
		List<List<TargetDataStructure>> targets = new ArrayList<List<TargetDataStructure>>(1);
		List<TargetDataStructure> targetsInString = null;

		if (!isThereAnyTargetCharacter()) {
			targetsInString = new ArrayList<TargetDataStructure>(super.countOccurrences());
		} else {
			targetsInString = new ArrayList<TargetDataStructure>();
		}

		logger.trace("Targets for error injection are being prepared...");

		int stringsCounter = 0;
		if (isThereAnyTargetCharacter()) {
			for (; stringsCounter < testBed.length; stringsCounter++) {
				// Beware: you can not remove a character which is not already in the string!
				List<Integer> occurrences = super.findOccurrences(
						stringsCounter,
						this.targetChar);
				if (occurrences.size() > 1) {
					for (Integer occurrence : occurrences) {
						targetsInString.add(
								new TargetDataStructure(
										stringsCounter,
										occurrence
										)
								);
					}
				}
			}
		}
		else {
			for (StringBuffer testString : testBed) {
				for (int charCounter = 0; charCounter < testString.length() -1; charCounter++) {
					targetsInString.add(
							new TargetDataStructure(
									stringsCounter,
									charCounter
									)
							);
				}

				stringsCounter++;
			}
		}
		targets.add(targetsInString);

		logger.trace("Targets for error injection are ready.");
		
		return targets;
	}

	@Override
	protected List<TargetDataStructure> executeErrorInjection(
			double errorInjectionTargetProportionalIndex,
			char injectableChar, List<TargetDataStructure> targets) {
		
		if (targets.size() == 0) {
			logger.trace("No " + injectableChar + " character to delete");
			
			return targets;
		}
		
		int injectedIndex = this.applyAndRound(
				errorInjectionTargetProportionalIndex,
				targets.size() -1
				),
			injectedStringNumber = targets.get(injectedIndex).stringNumber;

		logger.trace("Error injection: deleting " + injectableChar + " in position " + targets.get(injectedIndex).index + " of " + this.testBed[injectedStringNumber]);
		
		this.testBed[injectedStringNumber].deleteCharAt(
				targets.get(injectedIndex).index
				);
		// Beware: you just removed a char from the string!
		targets.remove(injectedIndex);
		// All the following characters in the same string must turn their index reduced by 1
		for (	int i = injectedIndex;
					i < targets.size()
				&&	injectedStringNumber == targets.get(i).stringNumber;
				i++) {
			logger.trace("Moving index " + targets.get(i).index + " of string \"" + this.testBed[targets.get(i).stringNumber] + "\" to " + (targets.get(i).index - 1));
			targets.get(i).index--;
		}
		
		return targets;
	}
}