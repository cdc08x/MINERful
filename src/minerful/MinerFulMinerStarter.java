package minerful;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.io.encdec.TaskTracesFromStringsLog;
import minerful.io.encdec.XesDecoder;
import minerful.miner.IConstraintsMiner;
import minerful.miner.ProbabilisticExistenceConstraintsMiner;
import minerful.miner.ProbabilisticRelationBranchedConstraintsMiner;
import minerful.miner.ProbabilisticRelationConstraintsMiner;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.OccurrencesStatsBuilder;
import minerful.params.InputCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.LogMF;

public class MinerFulMinerStarter extends AbstractMinerFulStarter {

	public static final String MINERFUL_XMLNS = "http://www.dis.uniroma1.it/minerful";

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options minerfulOptions = MinerFulCmdParameters.parseableOptions(),
				inputOptions = InputCmdParameters.parseableOptions(),
				systemOptions = SystemCmdParameters.parseableOptions(),
				viewOptions = ViewCmdParameters.parseableOptions();
		
    	for (Object opt: minerfulOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: inputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: viewOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
		
		return cmdLineOptions;
	}
	
    /**
     * @param args the command line arguments:
     * 	[regular expression]
     *  [number of strings]
     *  [minimum number of characters per string]
     *  [maximum number of characters per string]
     *  [alphabet]...
     */
    public static void main(String[] args) {
    	MinerFulMinerStarter minerMinaStarter = new MinerFulMinerStarter();
    	Options cmdLineOptions = minerMinaStarter.setupOptions();
    	
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
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
        
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
    	if (inputParams.inputFile == null) {
    		systemParams.printHelpForWrongUsage("Input file missing!", cmdLineOptions);
    		System.exit(1);
    	}
        
        configureLogging(systemParams.debugLevel);
        
        String[] testBedArray = null;
        Character[] alphabet = null;
        TaskCharEncoderDecoder taChaEncoDeco = new TaskCharEncoderDecoder();
        boolean alphabetRequiresEncodingAndDecoding = false;
        
        switch(inputParams.inputLanguage) {
	        case xes :
	        	alphabetRequiresEncodingAndDecoding = true;
	        	break;
	        case strings:
	        	alphabetRequiresEncodingAndDecoding = false;
	        	break;
	    	default:
	    		throw new UnsupportedOperationException("This encoding (" + inputParams.inputLanguage + ") is not supported yet");
        }       
        
        logger.info("Loading log...");
        
        switch(inputParams.inputLanguage) {
	        case xes :
	        	XesDecoder xesDeco = null;
	            List<List<String>> inputTestBed = null;
	
	            try {
	    			xesDeco = new XesDecoder(inputParams.inputFile);
	    			inputTestBed = xesDeco.decode();
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			System.exit(1);
	    		}
	            
	            testBedArray = taChaEncoDeco.encode(inputTestBed);
	            
	            // Remove from the analysed alphabet those activities that are specified in a user-defined list
	            taChaEncoDeco.excludeThese(minerFulParams.activitiesToExcludeFromResult);
	            
	            alphabet = taChaEncoDeco.encodedTasks();

	            // Let us try to free memory from the unused XesDecoder!
	            System.gc();
	        	break;
	        case strings:
	        	TaskTracesFromStringsLog taTraFroSLog = null;
				try {
					taTraFroSLog = new TaskTracesFromStringsLog(inputParams.inputFile);
					testBedArray = taTraFroSLog.extractTraces();
				} catch (Exception e) {
	    			e.printStackTrace();
	    			System.exit(1);
				}
				alphabet = taTraFroSLog.getAlphabet();
				break;
			default:
	    		throw new UnsupportedOperationException("This encoding (" + inputParams.inputLanguage + ") is not supported yet");
        }
        
        TaskCharArchive taskCharArchive = null;
        if (alphabetRequiresEncodingAndDecoding) {
        	taskCharArchive = new TaskCharArchive(taChaEncoDeco.getTranslationMap());
        } else {
        	taskCharArchive = new TaskCharArchive(alphabet);
        }

        TaskCharRelatedConstraintsBag bag =
        		minerMinaStarter.mine(testBedArray, minerFulParams, viewParams, systemParams, taskCharArchive);
        
        new MinerFulProcessViewerStarter().print(bag, viewParams, systemParams, testBedArray);
    	
        if (minerFulParams.processSchemeOutputFile != null) {
        	File procSchmOutFile =  minerFulParams.processSchemeOutputFile;
        	try {
				marshalMinedProcessScheme(bag, procSchmOutFile);
			} catch (PropertyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
    }

	public static void marshalMinedProcessScheme(
			TaskCharRelatedConstraintsBag bag, File procSchmOutFile)
			throws JAXBException, PropertyException, FileNotFoundException,
			IOException {
		String pkgName = bag.getClass().getCanonicalName().toString();
		pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));
		JAXBContext jaxbCtx = JAXBContext.newInstance(pkgName);
		Marshaller marsh = jaxbCtx.createMarshaller();
		marsh.setProperty("jaxb.formatted.output", true);
		StringWriter strixWriter = new StringWriter();
		marsh.marshal(bag, strixWriter);
		strixWriter.flush();
		StringBuffer strixBuffer = strixWriter.getBuffer();
		
		// OINK
		strixBuffer.replace(strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3), strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
				" xmlns=\"" + MinerFulMinerStarter.MINERFUL_XMLNS + "\"");
		FileWriter strixFileWriter = new FileWriter(procSchmOutFile);
		strixFileWriter.write(strixBuffer.toString());
		strixFileWriter.flush();
		strixFileWriter.close();
	}
	
	public TaskCharRelatedConstraintsBag mine(String[] testBedArray,
			MinerFulCmdParameters minerFulParams, ViewCmdParameters viewParams,
			SystemCmdParameters systemParams, Character[] alphabet) {
		TaskCharArchive taskCharArchive = new TaskCharArchive(alphabet);
		return this.mine(testBedArray, minerFulParams, viewParams, systemParams, taskCharArchive);
	}
	
    public TaskCharRelatedConstraintsBag mine(String[] testBedArray, MinerFulCmdParameters minerFulParams, ViewCmdParameters viewParams, SystemCmdParameters systemParams, TaskCharArchive taskCharArchive) {
        logger.info("\nComputing occurrences/distances table...");
        
        Integer branchingLimit = null;
        if (!(minerFulParams.branchingLimit.equals(MinerFulCmdParameters.MINIMUM_BRANCHING_LIMIT)))
        	branchingLimit = minerFulParams.branchingLimit;

        long maxMemUsage = 0L;
        long
        	possibleNumberOfConstraints = 0L,
        	possibleNumberOfExistenceConstraints = 0L,
        	possibleNumberOfRelationConstraints = 0L,
        	numOfConstraintsAboveThresholds = 0L,
        	numOfExistenceConstraintsAboveThresholds = 0L,
        	numOfRelationConstraintsAboveThresholds = 0L,
        	numOfConstraintsBeforeHierarchyBasedPruning = 0L,
        	numOfExistenceConstraintsBeforeHierarchyBasedPruning = 0L,
        	numOfRelationConstraintsBeforeHierarchyBasedPruning = 0L,
        	numOfPrunedByHierarchyConstraints = 0L,
        	numOfPrunedByHierarchyExistenceConstraints = 0L,
        	numOfPrunedByHierarchyRelationConstraints = 0L,
        	numOfConstraintsAfterPruningAndThresholding = 0L,
        	numOfExistenceConstraintsAfterPruningAndThresholding = 0L,
        	numOfRelationConstraintsAfterPruningAndThresholding = 0L;
        Character[] alphabet = taskCharArchive.getAlphabetArray();
        
        long before = System.currentTimeMillis();
        // initialize the stats builder
        OccurrencesStatsBuilder statsBuilder =
                new OccurrencesStatsBuilder(alphabet, TaskCharEncoderDecoder.CONTEMPORANEITY_CHARACTER_DELIMITER, branchingLimit);
        // builds the (empty) stats table
        GlobalStatsTable statsTable = statsBuilder.checkThisOut(testBedArray);
        logger.info("Done!");
        long after = System.currentTimeMillis();

        long occuTabTime = after - before;

        logger.trace("Occurrences/distances table, computed in: " + occuTabTime + " msec");
        // By using LogMF from the extras companion write, you will not incur the cost of parameter construction if debugging is disabled for logger
        LogMF.trace(logger, "\nStats:\n{0}", statsTable);
        logger.info("Discovering existence constraints...");
        
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

        before = System.currentTimeMillis();
        
        // search for existence constraints
        IConstraintsMiner exiConMiner = new ProbabilisticExistenceConstraintsMiner(statsTable, taskCharArchive);
        exiConMiner.setSupportThreshold(viewParams.supportThreshold);
        exiConMiner.setConfidenceThreshold(viewParams.confidenceThreshold);
        exiConMiner.setInterestFactorThreshold(viewParams.interestThreshold);
        TaskCharRelatedConstraintsBag bag = exiConMiner.discoverConstraints();
        
        after = System.currentTimeMillis();

        long exiConTime = after - before;
        logger.debug("Existence constraints, computed in: " + exiConTime + " msec");
        possibleNumberOfExistenceConstraints = exiConMiner.howManyPossibleConstraints();
        possibleNumberOfConstraints += possibleNumberOfExistenceConstraints;
        numOfExistenceConstraintsAboveThresholds = exiConMiner.getComputedConstraintsAboveTresholds();
        numOfConstraintsAboveThresholds += numOfExistenceConstraintsAboveThresholds;
        logger.info("Discovering relation constraints...");

        before = System.currentTimeMillis();
        // search for relation constraints
        
        long relaConTime = 0;
        IConstraintsMiner relaConMiner = null;
        
        if (minerFulParams.branchingLimit.equals(MinerFulCmdParameters.MINIMUM_BRANCHING_LIMIT)) {
        	relaConMiner = new ProbabilisticRelationConstraintsMiner(statsTable, taskCharArchive, minerFulParams.foreseeDistances);
        } else {
        	relaConMiner = new ProbabilisticRelationBranchedConstraintsMiner(statsTable, taskCharArchive, minerFulParams.branchingLimit);
        }
        relaConMiner.setSupportThreshold(viewParams.supportThreshold);
        relaConMiner.setConfidenceThreshold(viewParams.confidenceThreshold);
        relaConMiner.setInterestFactorThreshold(viewParams.interestThreshold);

        bag = relaConMiner.discoverConstraints(bag);
        after = System.currentTimeMillis();

        relaConTime = after - before;

        // Calculate how much was the space for data structures
        if (minerFulParams.memSpaceShowingRequested) {
        	maxMemUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        }
        // Let us try to free memory from the unused statsTable!
        System.gc();
        
        logger.debug("Relation constraints, computed in: " + relaConTime + " msec");
        possibleNumberOfRelationConstraints = relaConMiner.howManyPossibleConstraints();
        possibleNumberOfConstraints += possibleNumberOfRelationConstraints;
        
        numOfRelationConstraintsAboveThresholds = relaConMiner.getComputedConstraintsAboveTresholds();
        numOfConstraintsAboveThresholds += numOfRelationConstraintsAboveThresholds;

        numOfConstraintsBeforeHierarchyBasedPruning = bag.howManyConstraints();
        numOfExistenceConstraintsBeforeHierarchyBasedPruning = bag.howManyExistenceConstraints();
        // If it is not soup, it is wet bread
        numOfRelationConstraintsBeforeHierarchyBasedPruning = numOfConstraintsBeforeHierarchyBasedPruning - numOfExistenceConstraintsBeforeHierarchyBasedPruning;

        if (minerFulParams.avoidRedundancy) {
        	bag = bag.createHierarchyUnredundantCopy();
            // Let us try to free memory from the unused clone of bag!
            System.gc();
            numOfPrunedByHierarchyConstraints = bag.howManyConstraints();
            numOfPrunedByHierarchyExistenceConstraints = bag.howManyExistenceConstraints();
            // If it is not soup, it is wet bread
            numOfPrunedByHierarchyRelationConstraints = numOfPrunedByHierarchyConstraints - numOfPrunedByHierarchyExistenceConstraints;
        } else {
        	numOfPrunedByHierarchyConstraints = numOfConstraintsBeforeHierarchyBasedPruning;
        	numOfPrunedByHierarchyExistenceConstraints = numOfExistenceConstraintsBeforeHierarchyBasedPruning;
        	numOfPrunedByHierarchyRelationConstraints = numOfRelationConstraintsBeforeHierarchyBasedPruning;
        }
        bag = bag.createCopyPrunedByThresholdConfidenceAndInterest(viewParams.supportThreshold, viewParams.confidenceThreshold, viewParams.interestThreshold);
        // Let us try to free memory from the unused clone of bag!
        System.gc();

        after = System.currentTimeMillis();
        relaConTime = after - before;

        numOfConstraintsAfterPruningAndThresholding = bag.howManyConstraints();
        numOfExistenceConstraintsAfterPruningAndThresholding = bag.howManyExistenceConstraints();
        // If it is not soup, it is wet bread
        numOfRelationConstraintsAfterPruningAndThresholding = numOfConstraintsAfterPruningAndThresholding - numOfExistenceConstraintsAfterPruningAndThresholding;

        	
        printComputationStats(testBedArray, alphabet, occuTabTime,
				exiConTime, relaConTime, maxMemUsage,
				possibleNumberOfConstraints,
				possibleNumberOfExistenceConstraints,
				possibleNumberOfRelationConstraints,
				numOfConstraintsAboveThresholds,
				numOfExistenceConstraintsAboveThresholds,
				numOfRelationConstraintsAboveThresholds,
				numOfConstraintsBeforeHierarchyBasedPruning,
				numOfExistenceConstraintsBeforeHierarchyBasedPruning,
				numOfRelationConstraintsBeforeHierarchyBasedPruning,
				numOfPrunedByHierarchyConstraints,
				numOfPrunedByHierarchyExistenceConstraints,
				numOfPrunedByHierarchyRelationConstraints,
				numOfConstraintsAfterPruningAndThresholding,
				numOfExistenceConstraintsAfterPruningAndThresholding,
				numOfRelationConstraintsAfterPruningAndThresholding);

        return bag;
    }

	public void printComputationStats(String[] testBedArray,
			Character[] encodedAlphabet, long occuTabTime, long exiConTime,
			long relaConTime, long maxMemUsage,
			long possibleNumberOfConstraints,
			long possibleNumberOfExistenceConstraints,
			long possibleNumberOfRelationConstraints,
			long numOfConstraintsAboveThresholds,
			long numOfExistenceConstraintsAboveThresholds,
			long numOfRelationConstraintsAboveThresholds,
			long numOfConstraintsBeforePruning,
			long numOfExistenceConstraintsBeforePruning,
			long numOfRelationConstraintsBeforePruning,
			long numOfPrunedByHierarchyConstraints,
			long numOfPrunedByHierarchyExistenceConstraints,
			long numOfPrunedByHierarchyRelationConstraints,
			long numOfConstraintsAfterPruningAndThresholding,
			long numOfExistenceConstraintsAfterPruningAndThresholding,
			long numOfRelationConstraintsAfterPruningAndThresholding) {
		long	totalChrs = 0L;
        int		minChrs = 0,
        		maxChrs = 0;
        for (String string: testBedArray) {
        	totalChrs += string.length();
        	if (minChrs > string.length()) {
        		minChrs = string.length();
        	}
        	if (maxChrs < string.length()) {
        		maxChrs = string.length();
        	}
        }
        Double avgChrsPerString = 1.0 * totalChrs / testBedArray.length;
        
        StringBuffer
        	csvSummaryBuffer = new StringBuffer(),
        	csvSummaryLegendBuffer = new StringBuffer(),
        	csvSummaryComprehensiveBuffer = new StringBuffer();
        csvSummaryBuffer.append(testBedArray.length);
        csvSummaryLegendBuffer.append("'Number of traces'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(minChrs);
        csvSummaryLegendBuffer.append("'Minimum characters per string'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(maxChrs);
        csvSummaryLegendBuffer.append("'Maximum characters per string'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(avgChrsPerString);
        csvSummaryLegendBuffer.append("'Average characters per string'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(totalChrs);
        csvSummaryLegendBuffer.append("'Total characters read'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(encodedAlphabet.length);
        csvSummaryLegendBuffer.append("'Alphabet size'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");

        csvSummaryLegendBuffer.append("'Total time'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(occuTabTime + exiConTime + relaConTime);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Statistics computation time'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(occuTabTime);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Total mining time'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(exiConTime + relaConTime);
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
        csvSummaryLegendBuffer.append("'Constraints before hierarchy-based pruning'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(numOfConstraintsBeforePruning);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Existence constraints before hierarchy-based pruning'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(numOfExistenceConstraintsBeforePruning);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Relation constraints before hierarchy-based pruning'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(numOfRelationConstraintsBeforePruning);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Constraints before threshold-based pruning'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(numOfPrunedByHierarchyConstraints);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Existence constraints before threshold-based pruning'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(numOfPrunedByHierarchyExistenceConstraints);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Relation onstraints before threshold-based pruning'");
        csvSummaryLegendBuffer.append(";");
        csvSummaryBuffer.append(numOfPrunedByHierarchyRelationConstraints);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Constraints after pruning';");
        csvSummaryBuffer.append(numOfConstraintsAfterPruningAndThresholding);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Existence constraints after pruning';");
        csvSummaryBuffer.append(numOfExistenceConstraintsAfterPruningAndThresholding);
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append("'Relation constraints after pruning'");
        csvSummaryBuffer.append(numOfRelationConstraintsAfterPruningAndThresholding);

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
				auxDecodedTask = StringEscapeUtils.escapeXml(taskCharArchive.getTaskChar(match.group(1).charAt(0)).name);
				match.appendReplacement(sBuf, "task=\"" + auxDecodedTask + "\"");
			}
			match.appendTail(sBuf);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
			out.print(sBuf);
			out.flush();
			out.close();
    	}
    }
}