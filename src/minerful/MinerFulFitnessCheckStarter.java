/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.checking.params.CheckingCmdParameters;
import minerful.checking.relevance.dao.SpecificationFitnessEvaluation;
import minerful.concept.ProcessSpecification;
import minerful.io.params.InputSpecificationParameters;
import minerful.io.params.OutputSpecificationParameters;
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
				outputOptions = OutputSpecificationParameters.parseableOptions(),
				postPrOptions = PostProcessingCmdParameters.parseableOptions(),
				viewOptions = ViewCmdParameters.parseableOptions(),
				chkOptions = CheckingCmdParameters.parseableOptions(),
				inputLogOptions = InputLogCmdParameters.parseableOptions(),
				inpuModlOptions = InputSpecificationParameters.parseableOptions();
		
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
		OutputSpecificationParameters outParams =
				new OutputSpecificationParameters(
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
		InputSpecificationParameters inpuModlParams =
				new InputSpecificationParameters(
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
        
        SpecificationFitnessEvaluation evaluationOutput = miFuCheLa.check();
        ProcessSpecification processSpecification = miFuCheLa.getProcessSpecification();

        new MinerFulOutputManagementLauncher().manageOutput(processSpecification, viewParams, outParams, systemParams);
    }
 }