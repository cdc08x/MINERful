package minerful.logmaker.errorinjector;

import org.apache.log4j.Logger;

import minerful.logmaker.errorinjector.ErrorInjector.ErrorType;
import minerful.logmaker.errorinjector.ErrorInjector.SpreadingPolicy;


public class ErrorInjectorFactory {
	private static Logger logger = Logger.getLogger(ErrorInjectorFactory.class.getCanonicalName());

	public ErrorInjector createErrorInjector(SpreadingPolicy policy, ErrorType type, String[] testBedArray) {
		logger.trace("\"" + type + "-over-" + policy + "\" error injection requested, on a " + testBedArray.length + " strings long collection");
		
		switch (policy) {
		case string:
			switch (type) {
			case ins:
				return new ErrorInjectorOverStringsByInsertion(testBedArray);
			case del:
				return new ErrorInjectorOverStringsByDeletion(testBedArray);
			case insdel:
				return new ErrorInjectorOverStringsByMixInsDel(testBedArray);
			default:
				break;
			}
		case collection: 
			switch (type) {
			case ins:
				return new ErrorInjectorOverCollectionByInsertion(testBedArray);
			case del:
				return new ErrorInjectorOverCollectionByDeletion(testBedArray);
			case insdel:
				return new ErrorInjectorOverCollectionByMixInsDel(testBedArray);
			default:
				break;
			}
			default:
				break;
		}
		throw new UnsupportedOperationException("The \"" + type + "-over-" + policy + "\" error injection is not provided, yet");
	}
}