package minerful.miner.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.logparser.LogParser;
import minerful.miner.ConstraintsMiner;
import minerful.miner.ProbabilisticExistenceConstraintsMiner;
import minerful.miner.ProbabilisticRelationBranchedConstraintsMiner;
import minerful.miner.ProbabilisticRelationConstraintsMiner;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.postprocessing.params.PostProcessingCmdParameters;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

public class MinerFulQueryingCore implements Callable<ConstraintsBag> {
	public static final String KB_QUERYING_CODE = "'M-Q'";
	protected static Logger logger;
	protected LogParser logParser;
	protected MinerFulCmdParameters minerFulParams;
	protected PostProcessingCmdParameters postPrarams;
	protected TaskCharArchive taskCharArchive;
	protected GlobalStatsTable statsTable;
	private Set<TaskChar> tasksToQueryFor;
	protected ConstraintsBag bag; 
	public final int jobNum;

	{
        if (logger == null) {
    		logger = Logger.getLogger(MinerFulQueryingCore.class.getCanonicalName());
        }
	}

    public MinerFulQueryingCore(int coreNum, LogParser logParser,
			MinerFulCmdParameters minerFulParams, PostProcessingCmdParameters postPrarams,
			TaskCharArchive taskCharArchive,
			GlobalStatsTable globalStatsTable) {
    	this(coreNum,logParser,minerFulParams,postPrarams,taskCharArchive,globalStatsTable,null,null);
	}

	public MinerFulQueryingCore(int coreNum,
			LogParser logParser,
			MinerFulCmdParameters minerFulParams, PostProcessingCmdParameters postPrarams,
			TaskCharArchive taskCharArchive,
			GlobalStatsTable globalStatsTable, Set<TaskChar> tasksToQueryFor) {
		this(coreNum,logParser,minerFulParams,postPrarams,taskCharArchive,globalStatsTable,tasksToQueryFor,null);
	}

	public MinerFulQueryingCore(int coreNum,
			LogParser logParser,
			MinerFulCmdParameters minerFulParams, PostProcessingCmdParameters postPrarams,
			TaskCharArchive taskCharArchive,
			GlobalStatsTable globalStatsTable,
			ConstraintsBag bag) {
		this(coreNum,logParser,minerFulParams,postPrarams,taskCharArchive,globalStatsTable,null,bag);
	}

	public MinerFulQueryingCore(int coreNum,
			LogParser logParser,
			MinerFulCmdParameters minerFulParams, PostProcessingCmdParameters postPrarams,
			TaskCharArchive taskCharArchive,
			GlobalStatsTable globalStatsTable, Set<TaskChar> tasksToQueryFor,
			ConstraintsBag bag) {
		this.jobNum = coreNum;
		this.logParser = logParser;
		this.minerFulParams = minerFulParams;
		this.postPrarams = postPrarams;
		this.taskCharArchive = taskCharArchive;
		this.statsTable = globalStatsTable;
		if (tasksToQueryFor == null) {
			this.tasksToQueryFor = taskCharArchive.getTaskChars();
		} else {
			this.tasksToQueryFor = tasksToQueryFor;
		}
		this.bag = (bag == null ? new ConstraintsBag(this.tasksToQueryFor) : bag);
	}

	public void setStatsTable(GlobalStatsTable statsTable) {
		this.statsTable = statsTable;
	}

	public ConstraintsBag discover() {
        long
	    	possibleNumberOfConstraints = 0L,
	    	possibleNumberOfExistenceConstraints = 0L,
	    	possibleNumberOfRelationConstraints = 0L,
	    	numOfConstraintsAboveThresholds = 0L,
	    	numOfExistenceConstraintsAboveThresholds = 0L,
	    	numOfRelationConstraintsAboveThresholds = 0L,

	    	before = 0L,
        	after = 0L,
        	exiConTime = 0L,
        	relaConTime = 0L;
        
        if (minerFulParams.statsOutputFile != null) {
        	try {
				this.marshalStats(statsTable, minerFulParams.statsOutputFile, taskCharArchive);
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        logger.info("Discovering existence constraints...");
        before = System.currentTimeMillis();

        // search for existence constraints
        ConstraintsMiner exiConMiner = new ProbabilisticExistenceConstraintsMiner(statsTable, taskCharArchive, tasksToQueryFor);
        exiConMiner.setSupportThreshold(postPrarams.supportThreshold);
        exiConMiner.setConfidenceThreshold(postPrarams.confidenceThreshold);
        exiConMiner.setInterestFactorThreshold(postPrarams.interestFactorThreshold);
//        ConstraintsBag updatedBag = 
        exiConMiner.discoverConstraints(this.bag);

        after = System.currentTimeMillis();

        exiConTime = after - before;
        
        logger.debug("Existence constraints, computed in: " + exiConTime + " msec");
        possibleNumberOfExistenceConstraints = exiConMiner.howManyPossibleConstraints();
        possibleNumberOfConstraints += possibleNumberOfExistenceConstraints;
        numOfExistenceConstraintsAboveThresholds = exiConMiner.getComputedConstraintsAboveTresholds();
        numOfConstraintsAboveThresholds += numOfExistenceConstraintsAboveThresholds;
        logger.info("Discovering relation constraints...");

        before = System.currentTimeMillis();
        // search for relation constraints
        
        relaConTime = 0;
        ConstraintsMiner relaConMiner = null;
        
        if (!minerFulParams.isBranchingRequired()) {
        	relaConMiner = new ProbabilisticRelationConstraintsMiner(statsTable, taskCharArchive, tasksToQueryFor, minerFulParams.foreseeDistances);
        } else {
        	relaConMiner = new ProbabilisticRelationBranchedConstraintsMiner(statsTable, taskCharArchive, tasksToQueryFor, minerFulParams.branchingLimit);
        }
        relaConMiner.setSupportThreshold(postPrarams.supportThreshold);
        relaConMiner.setConfidenceThreshold(postPrarams.confidenceThreshold);
        relaConMiner.setInterestFactorThreshold(postPrarams.interestFactorThreshold);

//        updatedBag = relaConMiner.discoverConstraints(updatedBag);
        relaConMiner.discoverConstraints(this.bag);
        after = System.currentTimeMillis();

        relaConTime = after - before;

        /*
        // Calculate how much was the space for data structures
        if (minerFulParams.memSpaceShowingRequested) {
        	maxMemUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        }
        */
        // Let us try to free memory from the unused statsTable!
//        System.gc();
        
        logger.info("Done!");

        logger.debug("Relation constraints, computed in: " + relaConTime + " msec");
        possibleNumberOfRelationConstraints = relaConMiner.howManyPossibleConstraints();
        possibleNumberOfConstraints += possibleNumberOfRelationConstraints;
        
        numOfRelationConstraintsAboveThresholds = relaConMiner.getComputedConstraintsAboveTresholds();
        numOfConstraintsAboveThresholds += numOfRelationConstraintsAboveThresholds;

        printComputationStats(// occuTabTime,
				exiConTime, relaConTime, //maxMemUsage,
				0,
				possibleNumberOfConstraints,
				possibleNumberOfExistenceConstraints,
				possibleNumberOfRelationConstraints,
				numOfConstraintsAboveThresholds,
				numOfExistenceConstraintsAboveThresholds,
				numOfRelationConstraintsAboveThresholds);

        return this.bag;
    }

	public void printComputationStats(
			//long occuTabTime, 
			long exiConTime, 
			long relaConTime, long maxMemUsage,
			long possibleNumberOfConstraints,
			long possibleNumberOfExistenceConstraints,
			long possibleNumberOfRelationConstraints,
			long numOfConstraintsAboveThresholds,
			long numOfExistenceConstraintsAboveThresholds,
			long numOfRelationConstraintsAboveThresholds) {
        StringBuffer
        	csvSummaryBuffer = new StringBuffer(),
        	csvSummaryLegendBuffer = new StringBuffer(),
        	csvSummaryComprehensiveBuffer = new StringBuffer();
        csvSummaryBuffer.append(MinerFulQueryingCore.KB_QUERYING_CODE);
        csvSummaryLegendBuffer.append("'Operation code for KB querying'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(jobNum);
        csvSummaryLegendBuffer.append("'Job number'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(tasksToQueryFor.size());
        csvSummaryLegendBuffer.append("'Number of inspected activities'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");

//        csvSummaryLegendBuffer.append("'Total time'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(occuTabTime + exiConTime + relaConTime + pruniTime);
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Statistics computation time'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(occuTabTime);
//        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Total querying time'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(exiConTime + relaConTime);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Constraints discovery time'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(exiConTime + relaConTime);
        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Subsumption hierarchy pruning time'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(pruniTime);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Relation constraints discovery time'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(relaConTime);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Existence constraints discovery time'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(exiConTime);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Maximum memory usage'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(maxMemUsage);
        csvSummaryBuffer.append(";");

        csvSummaryLegendBuffer.append("'Total number of discoverable constraints'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(possibleNumberOfConstraints);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Total number of discoverable existence constraints'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(possibleNumberOfExistenceConstraints);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Total number of discoverable relation constraints'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(possibleNumberOfRelationConstraints);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Total number of discovered constraints above thresholds'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(numOfConstraintsAboveThresholds);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Total number of discovered existence constraints above thresholds'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(numOfExistenceConstraintsAboveThresholds);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Total number of discovered relation constraints above thresholds'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(numOfRelationConstraintsAboveThresholds);
        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Constraints before hierarchy-based pruning'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(numOfConstraintsBeforePruning);
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Existence constraints before hierarchy-based pruning'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(numOfExistenceConstraintsBeforePruning);
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Relation constraints before hierarchy-based pruning'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(numOfRelationConstraintsBeforePruning);
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Constraints before threshold-based pruning'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(numOfPrunedByHierarchyConstraints);
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Existence constraints before threshold-based pruning'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(numOfPrunedByHierarchyExistenceConstraints);
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Relation onstraints before threshold-based pruning'");
//        csvSummaryLegendBuffer.append(";");
//        csvSummaryBuffer.append(numOfPrunedByHierarchyRelationConstraints);
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Constraints after pruning';");
//        csvSummaryBuffer.append(numOfConstraintsAfterPruningAndThresholding);
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Existence constraints after pruning';");
//        csvSummaryBuffer.append(numOfExistenceConstraintsAfterPruningAndThresholding);
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append("'Relation constraints after pruning'");
//        csvSummaryBuffer.append(numOfRelationConstraintsAfterPruningAndThresholding);

        csvSummaryComprehensiveBuffer.append("\n\nTimings' summary: \n");
        csvSummaryComprehensiveBuffer.append(csvSummaryLegendBuffer.toString());
        csvSummaryComprehensiveBuffer.append("\n");
        csvSummaryComprehensiveBuffer.append(csvSummaryBuffer.toString());
        
        logger.info(csvSummaryComprehensiveBuffer.toString());
	}
    
    public void marshalStats(GlobalStatsTable statsTable, File outFile, TaskCharArchive taskCharArchive) throws JAXBException, IOException {
    	String pkgName = statsTable.getClass().getCanonicalName().toString();
    	pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));
    	JAXBContext jaxbCtx = JAXBContext.newInstance(pkgName);
    	Marshaller marsh = jaxbCtx.createMarshaller();
    	marsh.setProperty("jaxb.formatted.output", true);
    	
    	if (taskCharArchive == null) {
    		OutputStream os = new FileOutputStream(outFile);
        	marsh.marshal(statsTable, os);
        	os.flush();
        	os.close();
    	} else {
    		// TODO AWFUL but probably less time-consuming 
    		StringWriter sWri = new StringWriter();
    		marsh.marshal(statsTable, sWri);
    		Pattern p = Pattern.compile("task=\"(.)\"");
    		String rawXml = sWri.toString();
    		StringBuffer sBuf = new StringBuffer(rawXml.length());
    		Matcher match = p.matcher(rawXml);
    		String auxDecodedTask = null;
			while (match.find()) {
				auxDecodedTask = StringEscapeUtils.escapeXml(taskCharArchive.getTaskChar(match.group(1).charAt(0)).getName());
				match.appendReplacement(sBuf, "task=\"" + auxDecodedTask + "\"");
			}
			match.appendTail(sBuf);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
			out.print(sBuf);
			out.flush();
			out.close();
    	}
    }

	@Override
	public ConstraintsBag call() throws Exception {
		return this.discover();
	}
}