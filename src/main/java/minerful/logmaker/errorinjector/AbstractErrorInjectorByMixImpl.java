package minerful.logmaker.errorinjector;

public abstract class AbstractErrorInjectorByMixImpl extends AbstractErrorInjector {
	
	AbstractErrorInjectorByMixImpl(String[] testBedArray) {
		super(testBedArray);
	}

	protected String[] applyErrorsInjectionPhase(ErrorInjector errorInjector, double percentage) {
		errorInjector.setAlphabet(alphabet);
		errorInjector.setErrorsInjectionPercentage(percentage);
    	if (this.isThereAnyTargetCharacter())
    		errorInjector.setTargetChar(targetChar);
    	return errorInjector.injectErrors();
	}

}