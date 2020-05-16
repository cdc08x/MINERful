package minerful.utils;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.AbstractMinerFulStarter;
import minerful.MinerFulMinerLauncher;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.logparser.LogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;

public class MinerFulLogStatsPrinter extends AbstractMinerFulStarter {
	private static MessagePrinter logger = MessagePrinter.getInstance(MinerFulLogStatsPrinter.class);

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options inputOptions = InputLogCmdParameters.parseableOptions(),
				systemOptions = SystemCmdParameters.parseableOptions();

    	for (Object opt: inputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
		
		return cmdLineOptions;
	}

	/**
	 * @param args
	 *            the command line arguments: [regular expression] [number of
	 *            strings] [minimum number of characters per string] [maximum
	 *            number of characters per string] [alphabet]...
	 */
	public static void main(String[] args) {
		MinerFulLogStatsPrinter minerMinaStarter = new MinerFulLogStatsPrinter();
		Options cmdLineOptions = minerMinaStarter.setupOptions();

		InputLogCmdParameters inputParams =
				new InputLogCmdParameters(
						cmdLineOptions,
						args);
		MinerFulCmdParameters minerFulParams =
				new MinerFulCmdParameters(
						cmdLineOptions,
						args);
		SystemCmdParameters systemParams =
				new SystemCmdParameters(
						cmdLineOptions,
						args);

		if (systemParams.help) {
			systemParams.printHelp(cmdLineOptions);
			System.exit(0);
		}
		if (!isEventLogGiven(cmdLineOptions, inputParams, systemParams)) {
			System.exit(1);
		}

		MessagePrinter.configureLogging(systemParams.debugLevel);

		logger.info("Loading log...");

		LogParser logParser = MinerFulMinerLauncher.deriveLogParserFromLogFile(
				inputParams,
				minerFulParams);

		TaskCharArchive taskCharArchive = logParser.getTaskCharArchive();
		
		MessagePrinter.printlnOut("Log file: " + inputParams.inputLogFile);
		MessagePrinter.printlnOut("Number of traces: " + logParser.length());
		MessagePrinter.printlnOut("Numer of events: " + logParser.numberOfEvents());
		MessagePrinter.printlnOut("Minimum trace length: " + logParser.minimumTraceLength());
		MessagePrinter.printlnOut("Maximum trace length: " + logParser.maximumTraceLength());
		MessagePrinter.printlnOut("Event classifier: " + inputParams.eventClassification);
		MessagePrinter.printlnOut("Event classes (raw): " + taskCharArchive);
		MessagePrinter.printlnOut("Event classes (list): " + taskCharArchive.getTaskChars());
		
	}

	public static boolean isEventLogGiven(Options cmdLineOptions, InputLogCmdParameters inputParams,
			SystemCmdParameters systemParams) {
		if (inputParams.inputLogFile == null) {
			systemParams.printHelpForWrongUsage("Input log file missing! Please use the " +
					InputLogCmdParameters.INPUT_LOGFILE_PATH_PARAM_NAME + 
					" option.",
					cmdLineOptions);
			return false;
		}
		return true;
	}
}