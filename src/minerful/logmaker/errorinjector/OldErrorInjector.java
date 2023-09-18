package minerful.logmaker.errorinjector;
/**
 * @(#)ErrorInjector.java
 *
 *
 * @author S. Simoncini, C. Di Ciccio
 * @version 1.5 2012/8/28
 */

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

@Deprecated
public class OldErrorInjector {
	public enum SpreadingPolicy {
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
	public enum ErrorType {
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
	
    private static Logger logger = Logger.getLogger(OldErrorInjector.class.getCanonicalName());

    private String[] testBedArray;
	private SpreadingPolicy errorInjectionSpreadingPolicy;
	private ErrorType errorType;
	private double errorsInjectionPercentage;
	private Character targetChar;
	private Character[] alphabet;
	private int totalChrs = 0;

    public OldErrorInjector(String[] testBedArray) {
		this.setTestBedArray(testBedArray);
	}

	private void updateCharsTotalInTestBed() {
		this.totalChrs = 0;
		for (String testString: testBedArray) {
			this.totalChrs += testString.length();
		}
	}
	public String[] getTestBedArray() {
		return testBedArray;
	}
	public void setTestBedArray(String[] testBedArray) {
		this.testBedArray = testBedArray;
		this.updateCharsTotalInTestBed();
	}
	public SpreadingPolicy getErrorInjectionSpreadingPolicy() {
		return errorInjectionSpreadingPolicy;
	}
	public void setErrorInjectionSpreadingPolicy(SpreadingPolicy errorInjectionSpreadingPolicy) {
		this.errorInjectionSpreadingPolicy = errorInjectionSpreadingPolicy;
	}
	public ErrorType getErrorType() {
		return errorType;
	}
	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}
	public double getErrorsInjectionPercentage() {
		return errorsInjectionPercentage;
	}
	public void setErrorsInjectionPercentage(double errorsInjectionPercentage) {
		this.errorsInjectionPercentage = errorsInjectionPercentage;
	}
	public Character getTargetChar() {
		return targetChar;
	}
	public void setTargetChar(Character targetChar) {
		this.targetChar = targetChar;
	}
	public Character[] getAlphabet() {
		return alphabet;
	}
	public void setAlphabet(Character[] alphabet) {
		this.alphabet = alphabet;
	}
	public int getTotalChrs() {
		return totalChrs;
	}

	public boolean isThereAnyTargetCharacter() {
    	return this.targetChar != null;
    }

    /**
	 * Generates a random number, e.g., used to decide where to make a
	 * modification in the string. The random number can range from 1 up to the
	 * given upper bound.
	 * 
	 * @param upperBound
	 *            The upper bound for the random number. It must be greater
	 *            than 0.
	 * @return The random number.
	 */
    private static int generateBoundedRandom(int upperBound) {
    	if (upperBound <= 0)
    		throw new IllegalArgumentException(
    				"Invalid upper bound: " + upperBound);
		double x = Math.random() * upperBound;
		int pos = (int)x;
		return pos;
	}

	private static ArrayList<Integer> findTargetIndexes(String stringToScan, char targetCharacter) {
		ArrayList<Integer> targetIndexes = new ArrayList<Integer>();
		int k = stringToScan.indexOf(targetCharacter);
		while (k > -1) {
			targetIndexes.add(k);
			k = stringToScan.indexOf(targetCharacter, k);
		}
    	return targetIndexes;
	}

	public String[] injectErrors() {
		switch (errorInjectionSpreadingPolicy) {
		case string :
			switch (errorType) {
				case del:
					if (isThereAnyTargetCharacter()) {
						deleteGivenCharacterPerString(targetChar);
					} else {
						deleteRandomCharactersPerString();
					}
				break;
				case ins:
					if (isThereAnyTargetCharacter()) {
						insertGivenCharacterPerString(targetChar);
					} else {
						insertRandomCharactersPerString();
					}
				break;
				case insdel:
					if (isThereAnyTargetCharacter()) {
						insertOrDeleteGivenCharacterPerString(targetChar);
					} else {
						insertOrDeleteRandomCharactersPerString();
					}
				break;
			}
			break;
		case collection:
			switch (errorType) {
				case del:
					if (isThereAnyTargetCharacter()) {
						deleteGivenCharacterOverCollection(targetChar);
					} else {
						deleteRandomCharactersOverCollection();
					}
				break;
				case ins:
					if (isThereAnyTargetCharacter()) {
						insertGivenCharacterOverCollection(targetChar);
					} else {
						insertRandomCharactersOverCollection();
					}
				break;
				case insdel:
					if (isThereAnyTargetCharacter()) {
						insertOrDeleteGivenCharacterOverCollection(targetChar);
					} else {
						insertOrDeleteRandomCharactersOverCollection();
					}
				break;
			}
			break;
		}
		return this.testBedArray;
	}
	
	private char randomCharacter() {
		return alphabet[generateBoundedRandom(alphabet.length)];
	}

	private void deleteRandomCharactersPerString() {
		for (int stringIndex = 0; stringIndex < testBedArray.length; stringIndex++) {
			int wordLength = testBedArray[stringIndex].length();
    		int howManyCharsToDelete =
    				(int)(wordLength * errorsInjectionPercentage / 100.0);
    		deleteRandomCharactersInString(howManyCharsToDelete, stringIndex);
		}
	}

	private void insertRandomCharactersPerString() {
		for (int stringIndex = 0; stringIndex < testBedArray.length; stringIndex++) {
			int wordLength = testBedArray[stringIndex].length();
			int howManyCharsToInsert =
					(int)(wordLength * errorsInjectionPercentage / 100.0);
			insertRandomCharactersInString(howManyCharsToInsert, stringIndex);
		}
	}

	private void insertOrDeleteRandomCharactersPerString() {
		for (int stringIndex = 0; stringIndex < testBedArray.length; stringIndex++) {
			int wordLength = testBedArray[stringIndex].length();
			int numberOfErrors = (int)(wordLength * errorsInjectionPercentage/100.0);
			int numberOfDeletions = (int)(Math.random() * numberOfErrors);
			int numberOfInsertions = numberOfErrors - numberOfDeletions;

			deleteRandomCharactersInString(numberOfDeletions, stringIndex);
			insertRandomCharactersInString(numberOfInsertions, stringIndex);
		}
	}

	private void deleteRandomCharactersInString(int numberOfDeletions, int stringIndex) {
		int wordLength = testBedArray[stringIndex].length();
		if (numberOfDeletions <= wordLength) {
			for (int j=0; j<numberOfDeletions; j++) {
				int position = generateBoundedRandom(wordLength);
				testBedArray[stringIndex] =
						new StringBuffer(testBedArray[stringIndex])
						.deleteCharAt(position)
						.toString();
				wordLength--;
			}
		}
	}

	private void insertRandomCharactersInString(int numberOfInsertions, int stringIndex) {
		int wordLength = testBedArray[stringIndex].length();
		for (int j=0; j<numberOfInsertions; j++) {
			int position = generateBoundedRandom(wordLength);
			testBedArray[stringIndex] =
					new StringBuffer(testBedArray[stringIndex])
					.insert(position, randomCharacter())
					.toString();
			wordLength++;
		}
	}

	private void deleteRandomCharactersOverCollection() {
		int numberOfErrors = (int)(totalChrs * errorsInjectionPercentage/100.0);
		deleteRandomCharactersOverCollection(numberOfErrors);
	}

	private void insertRandomCharactersOverCollection() {
		int numberOfErrors = (int)(totalChrs * errorsInjectionPercentage/100.0);
		insertRandomCharactersOverCollection(numberOfErrors);
	}

	private void insertOrDeleteRandomCharactersOverCollection() {
		int numberOfErrors = (int)(totalChrs * errorsInjectionPercentage/100.0);
		int numberOfDeletions = (int)(Math.random() * numberOfErrors);
		int numberOfInsertions = numberOfErrors - numberOfDeletions;

		deleteRandomCharactersOverCollection(numberOfDeletions);
		insertRandomCharactersOverCollection(numberOfInsertions);
	}

	private void deleteRandomCharactersOverCollection(int numberOfDeletions) {
		for (int j=0; j<numberOfDeletions; j++) {
			int position = generateBoundedRandom(totalChrs);
			for (int stringIndex = 0;  stringIndex < testBedArray.length; stringIndex++) {
				if (testBedArray[stringIndex].length() <= position)
					position -= testBedArray[stringIndex].length();
				else {
					testBedArray[stringIndex] =
							new StringBuffer(testBedArray[stringIndex])
							.deleteCharAt(position)
							.toString();
					break;
				}
			}
			totalChrs--;
		}
	}

	private void insertRandomCharactersOverCollection(int numberOfInsertions) {
		for (int j=0; j<numberOfInsertions; j++) {
			int position = generateBoundedRandom(totalChrs);
			for (int stringIndex = 0;  stringIndex < testBedArray.length; stringIndex++) {
				if (testBedArray[stringIndex].length() <= position)
					position-=testBedArray[stringIndex].length();
				else {
					testBedArray[stringIndex] =
							new StringBuffer(testBedArray[stringIndex])
							.insert(position, randomCharacter())
							.toString();
					break;
				}
			}
			totalChrs++;
		}
	 }

	private void deleteGivenCharacterPerString(char targetCharacter) {
	 	for (int stringIndex = 0; stringIndex < testBedArray.length; stringIndex++) {
	 		String stringToScan = testBedArray[stringIndex];
	 		ArrayList<Integer> targetIndexes = findTargetIndexes(stringToScan, targetCharacter);
	 		int numberOfChars =
	 				generateBoundedRandom(
	 						(int)(targetIndexes.size() * errorsInjectionPercentage / 100.0));
			int[] deletionPositions = new int[numberOfChars];
			int w=0;
			for (int j=0; j<numberOfChars; j++) {
				int position = generateBoundedRandom(targetIndexes.size());
				deletionPositions[w]=targetIndexes.get(position);
				w++;
				targetIndexes.remove(position);
				targetIndexes.trimToSize();
			}
    		Arrays.sort(deletionPositions);
    		for (int j=deletionPositions.length-1; j>=0; j--) {
				testBedArray[stringIndex] =
						new StringBuffer(testBedArray[stringIndex])
						.deleteCharAt(deletionPositions[j])
						.toString();
	 		}
	 	}
	 }

	private void deleteGivenCharacterOverCollection(char targetCharacter) {
	 	String stringToScan="";
	 	for (int stringIndex = 0; stringIndex < testBedArray.length; stringIndex++) {
	 		stringToScan = stringToScan.concat(testBedArray[stringIndex]);
	 	}
		ArrayList<Integer> targetIndexes = findTargetIndexes(stringToScan, targetCharacter);
	 	if (targetIndexes.size()>=1) {
	 		int numberOfChars =
	 				generateBoundedRandom(
	 						(int)(targetIndexes.size() * errorsInjectionPercentage / 100.0));
			deleteFromTargetIndexes(targetIndexes, numberOfChars);
	 	}
	 	else
	 		logger.error("The given character is not in the testbed");
	}

	private void deleteFromTargetIndexes(ArrayList<Integer> targetIndexes, int numberOfChars) {
		int[] deletionPositions = new int[numberOfChars];
		int w=0;
		for (int j=0; j<numberOfChars; j++) {
			int position = generateBoundedRandom(targetIndexes.size());
			deletionPositions[w]=targetIndexes.get(position);
			w++;
			targetIndexes.remove(position);
			targetIndexes.trimToSize();
		}
    	Arrays.sort(deletionPositions);

    	for (int j=deletionPositions.length-1; j>=0; j--) {
			int position = deletionPositions[j];
			for (int stringIndex = 0;  stringIndex < testBedArray.length; stringIndex++) {
				if (testBedArray[stringIndex].length() <= position)
					position -= testBedArray[stringIndex].length();
				else {
					testBedArray[stringIndex] =
							new StringBuffer(testBedArray[stringIndex])
							.deleteCharAt(position)
							.toString();
					break;
				}
			}
		}
	}

	private void insertGivenCharacterOverCollection(char targetCharacter) {
		int numberOfErrors = (int)(totalChrs * errorsInjectionPercentage/100.0);
		insertGivenCharacterOverCollection(numberOfErrors, targetCharacter);
	}

	private void insertGivenCharacterPerString(char targetCharacter) {
		for (int stringIndex = 0; stringIndex < testBedArray.length; stringIndex++) {
			int wordLength = testBedArray[stringIndex].length();
			int howManyCharToInsert = generateBoundedRandom(wordLength);
			for (int j=0; j<howManyCharToInsert; j++) {
				int position = generateBoundedRandom(wordLength);
				testBedArray[stringIndex] =
						new StringBuffer(testBedArray[stringIndex])
						.insert(position, targetCharacter)
						.toString();
				wordLength++;
			}
		}
	}

	private void del3_tmp(int numberOfDeletions, int stringIndex, char targetCharacter, ArrayList<Integer> targetIndexes) {
		int[] tmp = new int[numberOfDeletions];
		int w=0;
		for (int j=0; j<numberOfDeletions; j++) {
			int position = generateBoundedRandom(targetIndexes.size());
			tmp[w]=targetIndexes.get(position);
			w++;
			targetIndexes.remove(position);
			targetIndexes.trimToSize();
		}
    	Arrays.sort(tmp);
    	for (int j=tmp.length-1; j>=0; j--) {
			testBedArray[stringIndex] =
					new StringBuffer(testBedArray[stringIndex])
					.deleteCharAt(tmp[j])
					.toString();
	 	}
	 }


	 private void ins3_tmp(int numberOfInsertions, int stringIndex, char targetCharacter, int wordLength) {
		for (int j=0; j<numberOfInsertions; j++) {
			int position = generateBoundedRandom(wordLength);
			testBedArray[stringIndex] =
					new StringBuffer(testBedArray[stringIndex])
					.insert(position, targetCharacter)
					.toString();
			wordLength++;
		}
	}

	private void insertOrDeleteGivenCharacterPerString(char targetCharacter) {
		for (int stringIndex = 0; stringIndex < testBedArray.length; stringIndex++) {
			String stringToScan = testBedArray[stringIndex];
	 		ArrayList<Integer> targetIndexes = findTargetIndexes(stringToScan, targetCharacter);
			int wordLength = testBedArray[stringIndex].length();
			int numberOfErrors = (int)(wordLength * errorsInjectionPercentage/100.0);
	 		int numberOfDeletions = generateBoundedRandom(targetIndexes.size());
	 		int numberOfInsertions = numberOfErrors - numberOfDeletions;

			if (numberOfErrors >= numberOfDeletions)
				del3_tmp(numberOfDeletions, stringIndex, targetCharacter, targetIndexes);
			ins3_tmp(numberOfInsertions, stringIndex, targetCharacter, wordLength);
		}
	}

	private void del4_tmp(int numberOfDeletions, char targetCharacter) {
	 	String stringToScan="";
	 	for (int stringIndex = 0; stringIndex < testBedArray.length; stringIndex++) {
	 		stringToScan = stringToScan.concat(testBedArray[stringIndex]);
	 	}
		ArrayList<Integer> targetIndexes = findTargetIndexes(stringToScan, targetCharacter);
		if (targetIndexes.size()>=1) {
			deleteFromTargetIndexes(targetIndexes, numberOfDeletions);
	 	}
	 	else
	 		logger.error("The given character is not in the testbed");
	}

	private void insertGivenCharacterOverCollection(int numberOfInsertions, char targetCharacter) {
		for (int j = 0; j < numberOfInsertions; j++) {
			int position = generateBoundedRandom(totalChrs);
			for (int stringIndex = 0;  stringIndex < testBedArray.length; stringIndex++) {
				if (testBedArray[stringIndex].length() <= position)
					position -= testBedArray[stringIndex].length();
				else if (testBedArray[stringIndex].length() > position) {
					testBedArray[stringIndex] =
							new StringBuffer(testBedArray[stringIndex])
							.insert(position, targetCharacter)
							.toString();
					break;
				}
			}
			totalChrs++;
		}
	}

	private void insertOrDeleteGivenCharacterOverCollection(char targetCharacter) {
		int numberOfErrors = (int)(totalChrs * errorsInjectionPercentage/100.0);
		int numberOfDeletions = (int)(Math.random() * numberOfErrors);
		int numberOfInsertions = numberOfErrors-numberOfDeletions;

		del4_tmp(numberOfDeletions, targetCharacter);
		insertGivenCharacterOverCollection(numberOfInsertions, targetCharacter);
	}
}