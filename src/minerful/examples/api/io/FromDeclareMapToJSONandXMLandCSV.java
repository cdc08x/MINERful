package minerful.examples.api.io;

import java.io.File;

import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.ProcessModel;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.OutputModelParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;

/**
 * This example class demonstrates how to use MINERful to convert an existing Declare map XML file into multiple formats.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class FromDeclareMapToJSONandXMLandCSV {
	public static void main(String[] args) {
		OutputModelParameters outParams =
				new OutputModelParameters();
		ViewCmdParameters viewParams =
				new ViewCmdParameters();
		SystemCmdParameters systemParams =
				new SystemCmdParameters();

		/*
		 * There are two possible methods of DeclareEncoderDecoder to create a
		 * minerful.concept.ProcessModel out of a Declare Map:
		 * 1)	public static ProcessModel fromDeclareMapToMinerfulProcessModel(String declareMapFilePath)
		 * 2)	public static ProcessModel fromDeclareMapToMinerfulProcessModel(org.processmining.plugins.declareminer.visualizing.AssignmentModel declareMapModel) {
		 * The first one is used here, and reads an XML representation of the Declare map.
		 * The second one can be used to pass in-memory representations of the Declare map.
		 */
		ProcessModel proMod =
				new DeclareMapEncoderDecoder(
						"/home/claudio/Declare-map-model.xml"
				).createMinerFulProcessModel();

		outParams.fileToSaveAsXML = new File("/home/claudio/MINERful-declarative-model.xml");
		outParams.fileToSaveAsJSON = new File("/home/claudio/MINERful-declarative-model.json");
		outParams.fileToSaveConstraintsAsCSV = new File("/home/claudio/MINERful-declarative-model.csv");
		
		MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
		outputMgt.manageOutput(proMod, viewParams, outParams, systemParams);
	}
}
