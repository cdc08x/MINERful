package minerful;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.concept.ProcessModel;
import minerful.concept.TaskCharArchive;
import minerful.io.ConstraintsPrinter;
import minerful.io.params.OutputModelParameters;
import minerful.logparser.LogParser;
import minerful.miner.core.MinerFulKBCore;
import minerful.miner.core.MinerFulQueryingCore;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.params.InputLogCmdParameters;
import minerful.params.SlidingCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulMinerSlider extends MinerFulMinerStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulMinerSlider.class);

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = super.setupOptions();
		
		Options slidingOptions = SlidingCmdParameters.parseableOptions();
		
    	for (Object opt: slidingOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
		
		return cmdLineOptions;
	}

	public static void main(String[] args) {
		MinerFulMinerSlider minerMinaSlider = new MinerFulMinerSlider();
		Options cmdLineOptions = minerMinaSlider.setupOptions();

		SlidingCmdParameters slideParams =
				new SlidingCmdParameters(
						cmdLineOptions,
						args);
		InputLogCmdParameters inputParams =
				new InputLogCmdParameters(
						cmdLineOptions,
						args);
		MinerFulCmdParameters minerFulParams =
				new MinerFulCmdParameters(
						cmdLineOptions,
						args);
		ViewCmdParameters viewParams =
				new ViewCmdParameters(
						cmdLineOptions,
						args);
		OutputModelParameters outParams =
				new OutputModelParameters(
						cmdLineOptions,
						args);
		SystemCmdParameters systemParams =
				new SystemCmdParameters(
						cmdLineOptions,
						args);
		PostProcessingCmdParameters postParams =
				new PostProcessingCmdParameters(
						cmdLineOptions,
						args);

		if (systemParams.help) {
			systemParams.printHelp(cmdLineOptions);
			System.exit(0);
		}
		if (inputParams.inputLogFile == null) {
			systemParams.printHelpForWrongUsage("Input log file missing!",
					cmdLineOptions);
			System.exit(1);
		}

		MessagePrinter.configureLogging(systemParams.debugLevel);

		logger.info("Loading log...");

		LogParser logParser = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputParams,
				minerFulParams);

		TaskCharArchive taskCharArchive = logParser.getTaskCharArchive();

		MinerFulOutputManagementLauncher minerFulOutputMgr = new MinerFulOutputManagementLauncher();
		
		ProcessModel processModel = minerMinaSlider.slideAndMine(logParser, slideParams, inputParams, minerFulParams, postParams, taskCharArchive, minerFulOutputMgr, viewParams, outParams, systemParams);
	}

	public ProcessModel slideAndMine(LogParser logParser, SlidingCmdParameters slideParams, InputLogCmdParameters inputParams, MinerFulCmdParameters minerFulParams, PostProcessingCmdParameters postParams, TaskCharArchive taskCharArchive, MinerFulOutputManagementLauncher minerFulOutputMgr, ViewCmdParameters viewParams, OutputModelParameters outParams, SystemCmdParameters systemParams) {
		PostProcessingCmdParameters noPostProcParams = PostProcessingCmdParameters.makeParametersForNoPostProcessing();
		ProcessModel proMod = null;
		int from = 0, to = 0;

		proMod = ProcessModel.generateNonEvaluatedBinaryModel(taskCharArchive);
		proMod.setName(makeDiscoveredProcessName(inputParams));
		
		GlobalStatsTable
			statsTable = new GlobalStatsTable(taskCharArchive, minerFulParams.branchingLimit),
			globalStatsTable = null;
		if (!slideParams.stickTail) {
			globalStatsTable = new GlobalStatsTable(taskCharArchive, minerFulParams.branchingLimit);
		}
		
		LogParser slicedLogParser = logParser.takeASlice(inputParams.startFromTrace, inputParams.subLogLength);

		statsTable = computeKB(slicedLogParser, minerFulParams,
				taskCharArchive, statsTable);
		if (!slideParams.stickTail) {
			globalStatsTable.mergeAdditively(statsTable);
		}

		proMod.bag = queryForConstraints(slicedLogParser, minerFulParams,
				noPostProcParams,
				taskCharArchive, statsTable, proMod.bag);
		
		int step = slideParams.slidingStep;
		
		GlobalStatsTable slicedStatsTable = null;
		
		MinerFulKBCore kbCore = new MinerFulKBCore(
				0,
				slicedLogParser,
				minerFulParams, taskCharArchive);
		MinerFulQueryingCore qCore = new MinerFulQueryingCore(0,
				logParser, minerFulParams, noPostProcParams, taskCharArchive,
				statsTable, proMod.bag);
		ConstraintsPrinter cPrin = new ConstraintsPrinter(proMod);

		from = inputParams.startFromTrace;
		to = inputParams.startFromTrace + slicedLogParser.length();
		
		PrintWriter outWriter = setUpCSVPrintWriter(slideParams);
    	outWriter.println(
    			"'From';'To';" +
    			cPrin.printBagAsMachineReadable(false,false,true).replace(
    					"\n", "\n" + from + ";" + to + ";").replace(
    							from + ";" + to + ";'",";;'"));
    					// The last one is to avoid, e.g., “0;297;'Support';'Confidence'” in the header
		minerFulOutputMgr.setAdditionalFileSuffix(String.format("-%06d-%06d", from, to));
		minerFulOutputMgr.manageOutput(proMod, viewParams, outParams, systemParams, logParser);

		if (slideParams.slidingStep > 0) {   		
    		//
    		// In the ASCII picture below, every column is a trace.
    		// The '=' symbols denote the original sub-log.
    		// The length of the shift is denoted with '>'
    		// The '-' symbol denotes the traces for which entries have to be removed from the KB
    		// The '+' symbol indicates the traces for which entries have to be added to the KB
    		//
    		//  ========>>>>     ===     +++
    		//  ----    ++++     ---     +++
    		//  ----    ++++     ---     +++
    		//  ----    ++++     ---     +++
    		//  ----    ++++     ---     +++
    		//  ========>>>>     ===     >>>
    		//
    		int
    		subtraLen = Math.min(step, inputParams.subLogLength),
    		addiStartGap = (step < inputParams.subLogLength ? inputParams.subLogLength : step),
    		addiLen = Math.min(step, inputParams.subLogLength);

    		for (int i = 0; inputParams.startFromTrace + i + addiStartGap + addiLen <= logParser.wholeLength(); i += step) {
				if (!slideParams.stickTail) {
					slicedLogParser = logParser.takeASlice(
							inputParams.startFromTrace + i,
							subtraLen
					);
					kbCore.setLogParser(slicedLogParser);
	
					slicedStatsTable = kbCore.discover();
					// subtract the tail
					statsTable.mergeSubtractively(slicedStatsTable);
				}
				slicedLogParser = logParser.takeASlice(inputParams.startFromTrace + i + addiStartGap, addiLen);
				kbCore.setLogParser(slicedLogParser);
			
				slicedStatsTable = kbCore.discover();
				
				// add the head
				statsTable.mergeAdditively(slicedStatsTable);
				if (!slideParams.stickTail) {
					globalStatsTable.mergeAdditively(slicedStatsTable);
				}
				
				// wipe out existing constraints
				proMod.bag.wipeOutConstraints();
				// query the altered knowledge base!
				qCore.discover();
				
				from = inputParams.startFromTrace + i + step;
				to = inputParams.startFromTrace + i + addiStartGap + addiLen;
				
				outWriter.println(
						from + ";" +
						to + ";" +
						cPrin.printBagAsMachineReadable(false,false,false)
				);
				
				minerFulOutputMgr.setAdditionalFileSuffix(String.format("-%06d-%06d", from, to));
				minerFulOutputMgr.manageOutput(proMod, viewParams, outParams, systemParams, logParser);
			}
			
			if (!slideParams.stickTail) {
				proMod.bag.wipeOutConstraints();
				qCore.setStatsTable(globalStatsTable);
				qCore.discover();
			}
    	}

		outWriter.flush();
		outWriter.close();
		
		super.pruneConstraints(proMod, minerFulParams, postParams);

		return proMod;
	}


	public PrintWriter setUpCSVPrintWriter(SlidingCmdParameters slideParams) {
		PrintWriter outWriter = null;
    	try {
    		outWriter = new PrintWriter(slideParams.intermediateOutputCsvFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.warn("Redirecting intermediate model measures to standard output");
			outWriter = new PrintWriter(System.out);
		}
		return outWriter;
	}

}