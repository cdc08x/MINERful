package minerful.logmaker.errorinjector;

import java.util.ArrayList;
import java.util.List;

public class ErrorInjectorOverStringsByInsertion extends AbstractErrorInjectorOverStrings {
	public ErrorInjectorOverStringsByInsertion(String[] testBedArray) {
		super(testBedArray);
	}

	@Override
	List<List<TargetDataStructure>> prepareTargets() {
		List<List<TargetDataStructure>> targets = new ArrayList<List<TargetDataStructure>>(this.testBed.length);
		List<TargetDataStructure> targetsInString = null;
		logger.trace("Targets for error injection are being prepared...");

		int stringsCounter = 0;
		for (StringBuffer testString : testBed) {
			targetsInString = new ArrayList<IErrorInjector.TargetDataStructure>();
			
			for (int charCounter = 0; charCounter < testString.length(); charCounter++) {
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
		
		logger.trace("Targets for error injection are ready.");

		return targets;
	}

	@Override
	List<TargetDataStructure> executeErrorInjection(
			double errorInjectionTargetProportionalIndex,
			char injectableChar, List<TargetDataStructure> targetsInString) {
		int injectedIndex = this.applyAndRound(
				errorInjectionTargetProportionalIndex,
				targetsInString.size() -1
				);

		logger.trace(
				"Error injection: inserting "
						+ injectableChar
						+ " in position "
						+ targetsInString.get(injectedIndex).index
						+ " of "
						+ this.testBed[targetsInString.get(injectedIndex).stringNumber]);
		this.testBed[targetsInString.get(injectedIndex).stringNumber].insert(
				targetsInString.get(injectedIndex).index,
				injectableChar
				);
		// Beware: you just inserted a char into the string: thus, the range of possible insertions raises, by 1.
		logger.trace("Adding the new \"last\" position (" + (targetsInString.size()+1) + ") in " + this.testBed[targetsInString.get(injectedIndex).stringNumber]);
		targetsInString.add(
				new TargetDataStructure(
						targetsInString.get(injectedIndex).stringNumber,
						targetsInString.size()+1
						)
				);

		return targetsInString;
	}
}