package minerful.miner.core;

import java.util.concurrent.Callable;

import minerful.concept.TaskCharArchive;
import minerful.logparser.LogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.OccurrencesStatsBuilder;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;

public class MinerFulKBCore implements Callable<GlobalStatsTable> {
	public static final String KNOWLEDGE_BASE_BUILDING_CODE = "'M-KB'";
	protected static Logger logger;
	protected LogParser logParser;
	protected MinerFulCmdParameters minerFulParams;
	protected TaskCharArchive taskCharArchive;
	public final int jboNum;
	
	{
        if (logger == null) {
    		logger = Logger.getLogger(MinerFulKBCore.class.getCanonicalName());
        }
	}
	
	public MinerFulKBCore(int coreNum, LogParser logParser,
			MinerFulCmdParameters minerFulParams,
			TaskCharArchive taskCharArchive) {
    	this.jboNum = coreNum;
		this.logParser = logParser;
		this.minerFulParams = minerFulParams;
		this.taskCharArchive = taskCharArchive;
	}

	public void setLogParser(LogParser logParser) {
		this.logParser = logParser;
	}

	public GlobalStatsTable discover() {
        logger.info("\nComputing occurrences/distances table...");
        
        Integer branchingLimit = null;
        if (!(minerFulParams.branchingLimit.equals(MinerFulCmdParameters.MINIMUM_BRANCHING_LIMIT)))
        	branchingLimit = minerFulParams.branchingLimit;

        long
        	before = 0L,
        	after = 0L;
        
        before = System.currentTimeMillis();
        // initialize the stats builder
        OccurrencesStatsBuilder statsBuilder =
//                new OccurrencesStatsBuilder(alphabet, TaskCharEncoderDecoder.CONTEMPORANEITY_CHARACTER_DELIMITER, branchingLimit);
        		new OccurrencesStatsBuilder(taskCharArchive, branchingLimit);
        // builds the (empty) stats table
        GlobalStatsTable statsTable = statsBuilder.checkThisOut(logParser);
        logger.info("Done!");
        
        after = System.currentTimeMillis();

        long occuTabTime = after - before;

        this.printComputationStats(occuTabTime);
        // By using LogMF from the extras companion write, you will not incur the cost of parameter construction if debugging is disabled for logger
      	LogMF.trace(logger, "\nStats:\n{0}", statsTable);
        
        return statsTable;
	}

	public void printComputationStats(long occuTabTime) {
		long	totalChrs = logParser.numberOfEvents();
        int		minChrs = logParser.minimumTraceLength(),
        		maxChrs = logParser.maximumTraceLength();
        Double avgChrsPerString = 1.0 * totalChrs / logParser.length();
        
        StringBuffer
        	csvSummaryBuffer = new StringBuffer(),
        	csvSummaryLegendBuffer = new StringBuffer(),
        	csvSummaryComprehensiveBuffer = new StringBuffer();
        csvSummaryBuffer.append(MinerFulKBCore.KNOWLEDGE_BASE_BUILDING_CODE);
        csvSummaryLegendBuffer.append("'Operation code for KB construction'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(jboNum);
        csvSummaryLegendBuffer.append("'Job number'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(logParser.length());
        csvSummaryLegendBuffer.append("'Number of traces'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(minChrs);
        csvSummaryLegendBuffer.append("'Min events per trace'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(maxChrs);
        csvSummaryLegendBuffer.append("'Max events per trace'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(avgChrsPerString);
        csvSummaryLegendBuffer.append("'Avg events per trace'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(totalChrs);
        csvSummaryLegendBuffer.append("'Events read'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(taskCharArchive.size());
        csvSummaryLegendBuffer.append("'Alphabet size'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");

//        csvSummaryLegendBuffer.append("'Total time'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(occuTabTime + exiConTime + relaConTime + pruniTime);
//        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Statistics computation time'");
//        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(occuTabTime);
//        csvSummaryBuffer.append(";");

        csvSummaryComprehensiveBuffer.append("\n\nTimings' summary: \n");
        csvSummaryComprehensiveBuffer.append(csvSummaryLegendBuffer.toString());
        csvSummaryComprehensiveBuffer.append("\n");
        csvSummaryComprehensiveBuffer.append(csvSummaryBuffer.toString());
        
        logger.info(csvSummaryComprehensiveBuffer.toString());
	}

	
	@Override
	public GlobalStatsTable call() throws Exception {
		return this.discover();
	}
}