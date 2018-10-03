package minerful.logmaker.errorinjector;

import java.util.ArrayList;
import java.util.List;

public class ErrorInjectorOverCollectionByInsertion extends AbstractErrorInjectorOverCollection {
	public ErrorInjectorOverCollectionByInsertion(String[] testBedArray) {
		super(testBedArray);
	}

	@Override
	protected List<List<TargetDataStructure>> prepareTargets() {
		List<List<TargetDataStructure>> targets = new ArrayList<List<TargetDataStructure>>(1);
		List<TargetDataStructure> targetsInString = null;
		
		if (!isThereAnyTargetCharacter()) {
			targetsInString = new ArrayList<TargetDataStructure>(super.countOccurrences() + testBed.length);
		} else {
			targetsInString = new ArrayList<TargetDataStructure>();
		}
		
		logger.trace("Targets for error injection are being prepared...");

		int stringsCounter = 0;
		
		for (StringBuffer testString : testBed) {
			// Differently from the over-string policy, the over-collection insertion should be able to insert characters also in empty strings.
			for (int charCounter = 0; charCounter <= testString.length(); charCounter++) {
				targetsInString.add(
						new TargetDataStructure(
								stringsCounter,
								charCounter
							)
						);
			}

			stringsCounter++;
		}
		
		targets.add(targetsInString);
		logger.trace("Targets for error injection are ready.");

		return targets;
	}

	@Override
	protected List<TargetDataStructure> executeErrorInjection(
			double errorInjectionTargetProportionalIndex,
			char injectableChar, List<TargetDataStructure> targets) {
		int 	injectedIndex = this.applyAndRound(
					errorInjectionTargetProportionalIndex,
					targets.size() -1
					),
				injectedStringNumber = targets.get(injectedIndex).stringNumber;;

		logger.trace("Error injection: inserting " + injectableChar + " in position " + targets.get(injectedIndex).index + " of " + this.testBed[injectedStringNumber]);
		this.testBed[targets.get(injectedIndex).stringNumber].insert(
				targets.get(injectedIndex).index,
				injectableChar
				);
		// Beware: you just inserted a char into the string: thus, the range of possible insertions raises, by 1, in the same string.
		targets.add(
				injectedIndex,
				new TargetDataStructure(
						injectedStringNumber,
						targets.get(injectedIndex).index
						)
				);
		for (	int i = injectedIndex+1;
				i < targets.size()
				&&	targets.get(i).stringNumber == injectedStringNumber;
				i++) {
			logger.trace("Moving index " + targets.get(i).index + " of string \"" + this.testBed[targets.get(i).stringNumber] + "\" to " + (targets.get(i).index + 1));
			targets.get(i).index++;
		}

		return targets;
	}
}
