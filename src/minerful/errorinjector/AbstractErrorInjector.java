package minerful.errorinjector;
/**
 * @(#)ErrorInjector.java
 *
 *
 * @author S. Simoncini, C. Di Ciccio
 * @version 1.5 2012/8/28
 */

import org.apache.log4j.Logger;

abstract class AbstractErrorInjector implements ErrorInjector, IErrorInjector {	
	protected static Logger logger = Logger.getLogger(AbstractErrorInjector.class.getCanonicalName());

    protected StringBuffer[] testBed;
	protected double errorsInjectionPercentage;
	protected Character targetChar;
	protected Character[] alphabet;

    AbstractErrorInjector(String[] testBedArray) {
		this.setTestBed(testBedArray);
	}
	StringBuffer[] getTestBed() {
		return testBed;
	}
	void setTestBed(String[] testBedArray) {
		this.testBed = new StringBuffer[testBedArray.length];
		for (int i = 0; i < testBedArray.length; i++) {
			this.testBed[i] = new StringBuffer(testBedArray[i]);
		}
	}
	
	@Override
	public double getErrorsInjectionPercentage() {
		return errorsInjectionPercentage;
	}
	@Override
	public void setErrorsInjectionPercentage(double errorsInjectionPercentage) {
		this.errorsInjectionPercentage = errorsInjectionPercentage;
	}
	@Override
	public Character getTargetChar() {
		return targetChar;
	}
	@Override
	public void setTargetChar(Character targetChar) {
		this.targetChar = targetChar;
	}
	@Override
	public void unsetTargetChar(Character targetChar) {
		this.targetChar = null;
	}
	@Override
	public Character[] getAlphabet() {
		return alphabet;
	}
	@Override
	public void setAlphabet(Character[] alphabet) {
		this.alphabet = alphabet;
	}
	@Override
	public boolean isThereAnyTargetCharacter() {
    	return this.targetChar != null;
    }

	protected String[] testBedArray() {
		String[] testBedArray = new String[this.testBed.length];
		int i = 0;
		for (StringBuffer sBuffer : this.testBed) {
			testBedArray[i++] = sBuffer.toString();
		}
		return testBedArray;
	}

}