/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import jdk.nashorn.internal.ir.CallNode.EvalArgs;
import minerful.checking.params.CheckingCmdParameters;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.io.params.InputModelParameters;
import minerful.io.params.OutputModelParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulFitnessCheckStarter extends MinerFulMinerStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulFitnessCheckStarter.class);

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options systemOptions = SystemCmdParameters.parseableOptions(),
				outputOptions = OutputModelParameters.parseableOptions(),
				postPrOptions = PostProcessingCmdParameters.parseableOptions(),
				viewOptions = ViewCmdParameters.parseableOptions(),
				chkOptions = CheckingCmdParameters.parseableOptions(),
				inputLogOptions = InputLogCmdParameters.parseableOptions(),
				inpuModlOptions = InputModelParameters.parseableOptions();
		
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: outputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: postPrOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: viewOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: chkOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: inputLogOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: inpuModlOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	
    	return cmdLineOptions;
	}
	
    public static void main(String[] args) {
    	MinerFulFitnessCheckStarter checkStarter = new MinerFulFitnessCheckStarter();
    	Options cmdLineOptions = checkStarter.setupOptions();
    	
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
		OutputModelParameters outParams =
				new OutputModelParameters(
						cmdLineOptions,
						args);
		PostProcessingCmdParameters preProcParams =
				new PostProcessingCmdParameters(
						cmdLineOptions,
						args);
		CheckingCmdParameters chkParams =
				new CheckingCmdParameters(
						cmdLineOptions,
						args);
		InputLogCmdParameters inputLogParams =
				new InputLogCmdParameters(
						cmdLineOptions,
						args);
		InputModelParameters inpuModlParams =
				new InputModelParameters(
						cmdLineOptions,
						args);
		ViewCmdParameters viewParams =
				new ViewCmdParameters(
						cmdLineOptions,
						args);

		MessagePrinter.configureLogging(systemParams.debugLevel);

		if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
        MinerFulFitnessCheckLauncher miFuCheLa = new MinerFulFitnessCheckLauncher(inpuModlParams, preProcParams, inputLogParams, chkParams, systemParams);
        
        ModelFitnessEvaluation evaluationOutput = miFuCheLa.check();
        ProcessModel processModel = miFuCheLa.getProcessSpecification();

        new MinerFulOutputManagementLauncher().manageOutput(processModel, viewParams, outParams, systemParams);
    }
 }