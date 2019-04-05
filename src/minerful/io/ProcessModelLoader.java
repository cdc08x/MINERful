package minerful.io;

import java.io.File;

import minerful.concept.TaskCharArchive;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.concept.ProcessModel;
import minerful.io.encdec.ProcessModelEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.InputModelParameters;
import minerful.utils.MessagePrinter;

public class ProcessModelLoader {
	/**
	 * Loads a process model from <code>inputFile</code>, either imported from a JSON-format file (see
	 * {@link ProcessModelEncoderDecoder#readFromJsonFile(File) ProcessModelEncoderDecoder.readFromJsonFile}),
	 * MINERful XML-format (see 
	 * {@link ProcessModelEncoderDecoder#unmarshalProcessModel(File) ProcessModelEncoderDecoder.unmarshalProcessModel }),
	 * or Declare Map XML-format (see
	 * {@link DeclareMapEncoderDecoder#createMinerFulProcessModel() DeclareMapEncoderDecoder.createMinerFulProcessModel}),
	 * depending on the <code>inputLanguage</code> parameter.
	 * 
	 * @param inputLanguage The input file formatting language
	 * @param inputFile The input file
	 * @return A {@link ProcessModel ProcessModel} instance read from <code>inputFile</code>,
	 * or <code>null</code> if such file does not exist or is not properly formatted
	 */
	public ProcessModel loadProcessModel(InputModelParameters.InputEncoding inputLanguage, File inputFile) {
		ProcessModel inputProcess = null;

		try {
			switch (inputLanguage) {
			case MINERFUL:
				inputProcess = new ProcessModelEncoderDecoder().unmarshalProcessModel(inputFile);
				break;
			case JSON:
				inputProcess = new ProcessModelEncoderDecoder().readFromJsonFile(inputFile);
				break;
			case DECLARE_MAP:
				inputProcess = new DeclareMapEncoderDecoder(inputFile.getAbsolutePath()).createMinerFulProcessModel();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			MessagePrinter.getInstance(this).error("Unreadable process model from file: " + inputFile.getAbsolutePath()
					+ ". Check the file path or the specified encoding.", e);
		}
	
		return inputProcess;
	}

	/**
	 * Load a process model from a file with the assurance to respect the given encoding-mapping
	 *
	 * @param inputLanguage
	 * @param inputFile
	 * @param alphabet encoding-mapping
	 * @return
	 */
	public ProcessModel loadProcessModel(InputModelParameters.InputEncoding inputLanguage, File inputFile, TaskCharArchive alphabet) {
		ProcessModel inputProcess = null;

		try {
			switch (inputLanguage) {
			case MINERFUL:
				inputProcess = new ProcessModelEncoderDecoder().unmarshalProcessModel(inputFile);
				break;
			case JSON:
				inputProcess = new ProcessModelEncoderDecoder().readFromJsonFile(inputFile, alphabet);
				break;
			case DECLARE_MAP:
				inputProcess = new DeclareMapEncoderDecoder(inputFile.getAbsolutePath()).createMinerFulProcessModel();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			MessagePrinter.getInstance(this).error("Unreadable process model from file: " + inputFile.getAbsolutePath()
					+ ". Check the file path or the specified encoding.", e);
		}

		return inputProcess;
	}

	public ProcessModel loadProcessModel(AssignmentModel declareMapModel) {
		return new DeclareMapEncoderDecoder(declareMapModel).createMinerFulProcessModel();
	}
}