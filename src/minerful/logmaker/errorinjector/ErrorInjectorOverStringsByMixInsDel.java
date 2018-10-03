package minerful.logmaker.errorinjector;

public class ErrorInjectorOverStringsByMixInsDel extends AbstractErrorInjectorByMixImpl {
	public ErrorInjectorOverStringsByMixInsDel(String[] testBedArray) {
		super(testBedArray);
	}

	@Override
	public String[] injectErrors() {
		String[] alteredTestBedArray = null;
		
		double
			insertionErrorsInjectionPercentage =
				Math.random() * errorsInjectionPercentage,
			deletionErrorsInjectionPercentage =
				errorsInjectionPercentage - insertionErrorsInjectionPercentage;
		
		// Phase 1: apply insertion errors over strings
		AbstractErrorInjectorOverStrings errorInjex =
				new ErrorInjectorOverStringsByInsertion(
						super.testBedArray());
    	alteredTestBedArray =
    			this.applyErrorsInjectionPhase(
    					errorInjex,
    					errorsInjectionPercentage);

		// Phase 2: apply deletion errors over strings
    	errorInjex =
				new ErrorInjectorOverStringsByDeletion(
						alteredTestBedArray);
    	alteredTestBedArray =
    			this.applyErrorsInjectionPhase(
    					errorInjex,
    					deletionErrorsInjectionPercentage);

    	return alteredTestBedArray;
	}
}