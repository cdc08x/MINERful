package minerful.io;

import java.io.File;

import minerful.concept.TaskCharArchive;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.concept.ProcessSpecification;
import minerful.io.encdec.ProcessSpecificationEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.InputSpecificationParameters;
import minerful.utils.MessagePrinter;

public class ProcessSpecificationLoader {
	/**
	 * Loads a process specification from <code>inputFile</code>, either imported from a JSON-format file (see
	 * {@link ProcessSpecificationEncoderDecoder#readFromJsonFile(File) ProcessSpecificationEncoderDecoder.readFromJsonFile}),
	 * or Declare Map XML-format (see
	 * {@link DeclareMapEncoderDecoder#createMinerFulProcessSpecification() DeclareMapEncoderDecoder.createMinerFulProcessSpecification}),
	 * depending on the <code>inputLanguage</code> parameter.
	 * 
	 * @param inputLanguage The input file formatting language
	 * @param inputFile The input file
	 * @return A {@link ProcessSpecification ProcessSpecification} instance read from <code>inputFile</code>,
	 * or <code>null</code> if such file does not exist or is not properly formatted
	 */
	public ProcessSpecification loadProcessSpecification(InputSpecificationParameters.InputEncoding inputLanguage, File inputFile) {
		ProcessSpecification inputProcess = null;
		
		try {
			switch (inputLanguage) {
			case JSON:
				inputProcess = new ProcessSpecificationEncoderDecoder().readFromJsonFile(inputFile);
				break;
			case DECLARE_MAP:
				inputProcess = new DeclareMapEncoderDecoder(inputFile.getAbsolutePath()).createMinerFulProcessSpecification();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			MessagePrinter.getInstance(this).error("Unreadable process specification from file: " + inputFile.getAbsolutePath()
					+ ". Check the file path or the specified encoding.", e);
		}
	
		return inputProcess;
	}

	/**
	 * Load a process specification from a file with the assurance to respect the given encoding-mapping
	 *
	 * @param inputLanguage
	 * @param inputFile
	 * @param alphabet encoding-mapping
	 * @return
	 */
	public ProcessSpecification loadProcessSpecification(InputSpecificationParameters.InputEncoding inputLanguage, File inputFile, TaskCharArchive alphabet) {
		ProcessSpecification inputProcess = null;

		try {
			switch (inputLanguage) {
			case JSON:
				inputProcess = new ProcessSpecificationEncoderDecoder().readFromJsonFile(inputFile, alphabet);
				break;
			case DECLARE_MAP:
				inputProcess = new DeclareMapEncoderDecoder(inputFile.getAbsolutePath()).createMinerFulProcessSpecification();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			MessagePrinter.getInstance(this).error("Unreadable process specification from file: " + inputFile.getAbsolutePath()
					+ ". Check the file path or the specified encoding.", e);
		}

		return inputProcess;
	}

	public ProcessSpecification loadProcessSpecification(AssignmentModel declareMapModel) {
		return new DeclareMapEncoderDecoder(declareMapModel).createMinerFulProcessSpecification();
	}

/**
 * 
 * @param negativeFile
 * @return
 */
public ProcessSpecification loadNegatedProcessSpecification(File negativeFile) {
	ProcessSpecification negProcessSpecification = null;
	
	try {
		negProcessSpecification = new ProcessSpecificationEncoderDecoder().readFromJsonFile(negativeFile);
	} catch (Exception e) {
		MessagePrinter.getInstance(this).error("Unreadable process specification from file: " + negativeFile.getAbsolutePath()
				+ ". Check the file path or the specified encoding.", e);
	}

	return negProcessSpecification;
}
}