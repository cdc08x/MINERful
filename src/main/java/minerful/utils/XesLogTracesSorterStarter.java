package minerful.utils;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.AbstractMinerFulStarter;
import minerful.logmaker.params.XesLogSorterParameters;
import minerful.params.SystemCmdParameters;

public class XesLogTracesSorterStarter extends AbstractMinerFulStarter {
	private static MessagePrinter logger = MessagePrinter.getInstance(XesLogTracesSorterStarter.class);

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options xesLogSorterOptions = XesLogSorterParameters.parseableOptions(),
				systemOptions = SystemCmdParameters.parseableOptions();

    	for (Object opt: xesLogSorterOptions.getOptions()) {
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
		XesLogTracesSorterStarter xesTraceStorter = new XesLogTracesSorterStarter();
		Options cmdLineOptions = xesTraceStorter.setupOptions();

		XesLogSorterParameters xesLogSorterParams =
				new XesLogSorterParameters(
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
		if (!areEventLogsProvided(cmdLineOptions, xesLogSorterParams, systemParams)) {
			System.exit(1);
		}

		MessagePrinter.configureLogging(systemParams.debugLevel);

		if (!areEventLogsProvided(cmdLineOptions, xesLogSorterParams, systemParams)) {
			System.exit(1);
		}
		
		XesLogTracesSorterLauncher xeSorter = new XesLogTracesSorterLauncher(xesLogSorterParams);
		xeSorter.sortAndStoreXesLog();
	}

	public static boolean areEventLogsProvided(Options cmdLineOptions, XesLogSorterParameters xesSortParams,
			SystemCmdParameters systemParams) {
		if (xesSortParams.inputXesFile == null) {
			systemParams.printHelpForWrongUsage("Input XES log file missing! Please use the " +
					XesLogSorterParameters.INPUT_XES_PARAM_NAME + 
					" option.",
					cmdLineOptions);
			return false;
		}
		if (xesSortParams.outputXesFile == null) {
			systemParams.printHelpForWrongUsage("Output XES log file missing! Please use the " +
					XesLogSorterParameters.OUTPUT_XES_PARAM_NAME + 
					" option.",
					cmdLineOptions);
			return false;
		}
		return true;
	}
}