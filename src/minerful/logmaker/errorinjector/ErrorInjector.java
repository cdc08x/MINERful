package minerful.logmaker.errorinjector;

public interface ErrorInjector {
	enum SpreadingPolicy {
		/**
		 * Inject the errors calculating percentages over each single string. I.e.,
		 * if the percentage is equal to 10% then 1 character over 10 FOR EACH
		 * STRING will be affected by the error.
		 * It can be a valid value for the "-eS" parameter.
		 */
		string,
		/**
		 * Inject the errors calculating percentages over the whole collection of
		 * strings. I.e., if the percentage is equal to 10% then 1 character over 10
		 * OVER THE WHOLE COLLECTION will be affected by the error.
		 * It can be a valid value for the "-eS" parameter.
		 * It is the DEFAULT value for the "-eS" parameter.
		 */
		collection;
		
		public static SpreadingPolicy getDefault() { return collection; }
	}
	
	enum ErrorType {
		/**
		 * Errors are insertions of spurious characters.
		 * It can be a valid value for the "-eT" parameter.
		 */
		ins,
		/**
		 * Errors are deletions of characters.
		 * It can be a valid value for the "-eT" parameter.
		 */
		del,
		/**
		 * Errors are either insertions or deletions of characters, as based on a random decision.
		 * It can be a valid value for the "-eT" parameter.
 		 * It is the DEFAULT value for the "-eS" parameter.
		 */
		insdel;
		
		public static ErrorType getDefault() { return insdel; }
	}

	String[] injectErrors();

	double getErrorsInjectionPercentage();

	void setErrorsInjectionPercentage(double errorsInjectionPercentage);

	Character getTargetChar();

	void setTargetChar(Character targetChar);

	void unsetTargetChar(Character targetChar);

	Character[] getAlphabet();

	void setAlphabet(Character[] alphabet);

	boolean isThereAnyTargetCharacter();
}