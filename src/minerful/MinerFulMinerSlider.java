package minerful;

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
import minerful.params.InputCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulMinerSlider extends MinerFulMinerStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulMinerSlider.class);

	/**
	 * @param args
	 *            the command line arguments: [regular expression] [number of
	 *            strings] [minimum number of characters per string] [maximum
	 *            number of characters per string] [alphabet]...
	 */
	public static void main(String[] args) {
		MinerFulMinerSlider minerMinaSlider = new MinerFulMinerSlider();
		Options cmdLineOptions = minerMinaSlider.setupOptions();

		InputCmdParameters inputParams =
				new InputCmdParameters(
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

		LogParser logParser = deriveLogParserFromLogFile(inputParams,
				minerFulParams);

		TaskCharArchive taskCharArchive = logParser.getTaskCharArchive();

		ProcessModel processModel = minerMinaSlider.slideAndMine(logParser, inputParams, minerFulParams, taskCharArchive);

		new MinerFulOutputManagementLauncher().manageOutput(processModel, viewParams, outParams, systemParams, logParser);
	}
	
	public ProcessModel slideAndMine(LogParser logParser, InputCmdParameters inputParams, MinerFulCmdParameters minerFulParams, TaskCharArchive taskCharArchive) {
		GlobalStatsTable globalStatsTable = new GlobalStatsTable(taskCharArchive, minerFulParams.branchingLimit);
		PostProcessingCmdParameters noPostProcParams = PostProcessingCmdParameters.makeParametersForNoPostProcessing();
		
		globalStatsTable = computeKB(logParser, minerFulParams,
				taskCharArchive, globalStatsTable);
		
		ProcessModel proMod = ProcessModel.generateNonEvaluatedBinaryModel(taskCharArchive);
		proMod.setName(makeDiscoveredProcessName(inputParams));
		
		proMod.bag = queryForConstraints(logParser, minerFulParams,
				noPostProcParams,
				taskCharArchive, globalStatsTable, proMod.bag);

		// FIXME Make this parametric
		int step = 2;
		
		LogParser slicedLogParser = null;
		GlobalStatsTable slicedStatsTable = null;
		
		MinerFulKBCore kbCore = new MinerFulKBCore(
				0,
				slicedLogParser,
				minerFulParams, taskCharArchive);
		MinerFulQueryingCore qCore = new MinerFulQueryingCore(0,
				logParser, minerFulParams, noPostProcParams, taskCharArchive,
				globalStatsTable, proMod.bag);
		ConstraintsPrinter cPrin = new ConstraintsPrinter(proMod);
		
		for (int i = 0; inputParams.startFromTrace + inputParams.subLogLength + i < logParser.length() - step; i += step) {
			slicedLogParser = logParser.takeASlice(inputParams.startFromTrace + i, step);
			kbCore.setLogParser(slicedLogParser);

			slicedStatsTable = kbCore.discover();
			// subtract the tail
			globalStatsTable.mergeSubtractively(slicedStatsTable);
			
			slicedLogParser = logParser.takeASlice(inputParams.startFromTrace + inputParams.subLogLength + i, step);
			kbCore.setLogParser(slicedLogParser);
			
			slicedStatsTable = kbCore.discover();
			
			// add the head
			globalStatsTable.mergeAdditively(slicedStatsTable);
			
			// wipe out existing constraints
			proMod.bag.wipeOutConstraints();
			// query the altered knowledge base!
			qCore.discover();
			
			// FIXME Make this customisable
			System.out.println(cPrin.printBagAsMachineReadable());
		}
		
		return proMod;
	}

}