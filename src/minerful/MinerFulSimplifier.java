/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.core.MinerFulPruningCore;
import minerful.io.ConstraintsPrinter;
import minerful.io.encdec.ProcessModelEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.InputModelParameters;
import minerful.io.params.OutputModelParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.pruning.ConflictAndRedundancyResolver;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MinerFulSimplifier extends MinerFulMinerStarter {

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options systemOptions = SystemCmdParameters.parseableOptions(),
				postProptions = PostProcessingCmdParameters.parseableOptions(),
				viewOptions = ViewCmdParameters.parseableOptions(),
				outputOptions = OutputModelParameters.parseableOptions(),
				inputOptions = InputModelParameters.parseableOptions();
		
    	for (Object opt: postProptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: viewOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: outputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: inputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	
    	return cmdLineOptions;
	}
	
    public static void main(String[] args) {
    	MinerFulSimplifier prunerStarter = new MinerFulSimplifier();
    	Options cmdLineOptions = prunerStarter.setupOptions();
    	
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
		PostProcessingCmdParameters postPrarams =
				new PostProcessingCmdParameters(
						cmdLineOptions,
						args);
		OutputModelParameters outParams =
				new OutputModelParameters(
						cmdLineOptions,
						args);
		InputModelParameters inputParams =
				new InputModelParameters(
						cmdLineOptions,
						args);
		ViewCmdParameters viewParams =
				new ViewCmdParameters(
						cmdLineOptions,
						args);
        
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
		if (inputParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process model file missing!",
					cmdLineOptions);
			System.exit(1);
		}
        
        configureLogging(systemParams.debugLevel);
        
        ProcessModel
        	inputProcess = null,
        	outputProcess = null;
        try {
	        inputProcess =
	        		(	inputParams.inputLanguage.equals(InputModelParameters.InputEncoding.MINERFUL)
	        			?	new ProcessModelEncoderDecoder().unmarshalProcessModel(inputParams.inputFile)
	        			:	DeclareMapEncoderDecoder.fromDeclareMapToMinerfulProcessModel(inputParams.inputFile.getAbsolutePath()));
        } catch (Exception e) {
        	System.err.println("Unreadable process model from file: " + inputParams.inputFile.getAbsolutePath() + ". Check the file path or the specified encoding.");
        	e.printStackTrace(System.err);
        	System.exit(1);
        }

        MinerFulPruningCore miFuPruNi = new MinerFulPruningCore(inputProcess, postPrarams);
        miFuPruNi.massageConstraints();
        outputProcess = miFuPruNi.getProcessModel();
        
        
        new MinerFulProcessOutputMgtStarter().manageOutput(outputProcess, viewParams, outParams, systemParams);
    }
 }