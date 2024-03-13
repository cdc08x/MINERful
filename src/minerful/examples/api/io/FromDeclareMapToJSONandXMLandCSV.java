package minerful.examples.api.io;

import java.io.File;

import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.ProcessSpecification;
import minerful.io.encdec.csv.CsvEncoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.OutputSpecificationParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;

/**
 * This example class demonstrates how to use MINERful to convert an existing Declare map XML file into multiple formats.
 * Here it is also shown how to limit the columns to be printed in the CSV.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class FromDeclareMapToJSONandXMLandCSV {
	public static void main(String[] args) {
		OutputSpecificationParameters outParams =
				new OutputSpecificationParameters();
		ViewCmdParameters viewParams =
				new ViewCmdParameters();
		SystemCmdParameters systemParams =
				new SystemCmdParameters();

		/*
		 * There are two possible methods of DeclareEncoderDecoder to create a
		 * minerful.concept.ProcessSpecification out of a Declare Map:
		 * 1)	public static ProcessSpecification fromDeclareMapToMinerfulProcessSpecification(String declareMapFilePath)
		 * 2)	public static ProcessSpecification fromDeclareMapToMinerfulProcessSpecification(org.processmining.plugins.declareminer.visualizing.AssignmentModel declareMapModel) {
		 * The first one is used here, and reads an XML representation of the Declare map.
		 * The second one can be used to pass in-memory representations of the Declare map.
		 */
		ProcessSpecification proSpec =
				new DeclareMapEncoderDecoder(
//						"/home/cdc08x/Code/MINERful-dev/specifications/examples-FMM/DeclareMap1.xml"
						"/home/cdc08x/Code/MINERful-dev/specifications/examples-FMM/DeclareMap2.xml"
						//"/Users/ceciliaiacometta/Desktop/examples/uni-condec.xml"
//						/home/cdc08x/Code/MINERful-dev/specifications/examples-FMM/DeclareMap3.xml"
				).createMinerFulProcessSpecification();

		outParams.fileToSaveAsXML = new File("/home/cdc08x/MINERful-declarative-specification.xml");
		outParams.fileToSaveAsJSON =  new File("/home/cdc08x/MINERful-declarative-specification.json");
		//outParams.fileToSaveAsJSON = new File("/Users/ceciliaiacometta/Desktop/examples/uni-condec.xml");
		outParams.fileToSaveConstraintsAsCSV = new File("/home/cdc08x/MINERful-declarative-specification.csv");
		outParams.csvColumnsToPrint = new CsvEncoder.PRINT_OUT_ELEMENT[]{
				CsvEncoder.PRINT_OUT_ELEMENT.FULL_NAME, // ("Constraint"),
				CsvEncoder.PRINT_OUT_ELEMENT.TEMPLATE_NAME, //("Template"),
				CsvEncoder.PRINT_OUT_ELEMENT.ACTIVATION, //("Activation"),
				CsvEncoder.PRINT_OUT_ELEMENT.TARGET, //("Target"),
				/* The following are commented out, because default ConDec models do not bear support, confidence, and interest factor. */ 
				// CsvEncoder.PRINT_OUT_ELEMENT.SUPPORT, //("Support"),
				// CsvEncoder.PRINT_OUT_ELEMENT.CONFIDENCE_LEVEL, //("Confidence level"),
				// CsvEncoder.PRINT_OUT_ELEMENT.INTEREST_FACTOR, //("Interest factor"),
		};
		
		MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
		outputMgt.manageOutput(proSpec, viewParams, outParams, systemParams);
	}
}
