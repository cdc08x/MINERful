package minerful.logmaker.errorinjector;

public class ErrorInjectorOverCollectionByMixInsDel extends AbstractErrorInjectorByMixImpl {
	public ErrorInjectorOverCollectionByMixInsDel(String[] testBedArray) {
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
		AbstractErrorInjectorOverCollection errorInjex =
				new ErrorInjectorOverCollectionByInsertion(
						super.testBedArray());
    	alteredTestBedArray =
    			this.applyErrorsInjectionPhase(
    					errorInjex,
    					errorsInjectionPercentage);

		// Phase 2: apply deletion errors over strings
    	errorInjex =
				new ErrorInjectorOverCollectionByDeletion(
						alteredTestBedArray);
    	alteredTestBedArray =
    			this.applyErrorsInjectionPhase(
    					errorInjex,
    					deletionErrorsInjectionPercentage);

    	return alteredTestBedArray;
	}
}