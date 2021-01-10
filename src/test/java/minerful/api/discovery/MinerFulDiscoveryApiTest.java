package minerful.api.discovery;

import java.io.File;

import org.junit.jupiter.api.Test;

import minerful.MinerFulMinerLauncher;
import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.ProcessModel;
import minerful.io.params.OutputModelParameters;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.params.InputLogCmdParameters.InputEncoding;
import minerful.postprocessing.params.PostProcessingCmdParameters;

/**
 * Tests for Discovery API
 */
class MinerFulDiscoveryApiTest {
	
	static final String INPUTPATH = "src/test/resources/discovery/testlog.txt";
	static final String OUTPUTPATH = "src/test/resources/discovery/output/testlog_model.xml";
	
	@Test
	 void testDiscovery() {
		
		InputLogCmdParameters inputParams =
				new InputLogCmdParameters();
		MinerFulCmdParameters minerFulParams =
				new MinerFulCmdParameters();
		ViewCmdParameters viewParams =
				new ViewCmdParameters();
		OutputModelParameters outParams =
				new OutputModelParameters();
		SystemCmdParameters systemParams =
				new SystemCmdParameters();
		PostProcessingCmdParameters postParams =
				new PostProcessingCmdParameters();
		
		inputParams.inputLogFile = new File(INPUTPATH);
		inputParams.inputLanguage = InputEncoding.strings;

		MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
		ProcessModel processModel = miFuMiLa.mine();
		
		outParams.fileToSaveAsXML = new File(OUTPUTPATH);
		
		MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
		outputMgt.manageOutput(processModel, viewParams, outParams, systemParams);
	}
}
