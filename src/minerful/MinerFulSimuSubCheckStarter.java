package minerful;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.index.LinearConstraintsIndexFactory;
import minerful.logparser.LogParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.simplification.SubsumptionChecker;
import minerful.stringsmaker.MinerFulStringTracesMaker;
import minerful.stringsmaker.params.StringTracesMakerCmdParameters;

import org.apache.commons.cli.Options;

public class MinerFulSimuSubCheckStarter extends MinerFulSimuStarter {
    public static void main(String[] args) {
    	MinerFulSimuStarter minerSimuStarter = new MinerFulSimuStarter();
    	Options cmdLineOptions = minerSimuStarter.setupOptions();
    	
        ViewCmdParameters viewParams =
        		new ViewCmdParameters(
        				cmdLineOptions,
        				args);
    	StringTracesMakerCmdParameters tracesMakParams =
    			new StringTracesMakerCmdParameters(
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

        configureLogging(systemParams.debugLevel);
        
        String[] testBedArray = new String[0];
        
        testBedArray = new MinerFulStringTracesMaker().makeTraces(tracesMakParams);
		try {
			LogParser stringLogParser = new StringLogParser(testBedArray, ClassificationType.NAME);
			TaskCharArchive taskCharArchive = new TaskCharArchive(stringLogParser.getEventEncoderDecoder().getTranslationMap());

	        ProcessModel processModel = minerSimuStarter.mine(stringLogParser, minerFulParams, viewParams, systemParams, taskCharArchive);
	        MinerFulProcessViewerStarter proViewStarter = new MinerFulProcessViewerStarter(); 
	        proViewStarter.print(processModel, viewParams, systemParams, stringLogParser);
	        /*
				AlternateResponse(a, {b,c})
	        	ChainPrecedence({a,b}, c)
				Precedence({a,b,c,d}, e)
				RespondedExistence(a, {b,c,d,e})
				Response(a, {b,c})
				ChainPrecedence({a,b,d}, c)
	         */
	        
	        TaskChar
	        		a = taskCharArchive.getTaskChar('A'),
	        		b = taskCharArchive.getTaskChar('B'),
	        		c = taskCharArchive.getTaskChar('C'),
	        		d = taskCharArchive.getTaskChar('D'),
	        		e = taskCharArchive.getTaskChar('E');

	        Constraint[] model = new Constraint[]{
	        		new AlternateResponse(new TaskCharSet(a), new TaskCharSet(Arrays.asList(new TaskChar[]{b,c}))),
	        		new ChainPrecedence(new TaskCharSet(Arrays.asList(new TaskChar[]{a,b})), new TaskCharSet(c)),
	        		new Precedence(new TaskCharSet(Arrays.asList(new TaskChar[]{a,b,c,d})), new TaskCharSet(e)),
	        		new RespondedExistence(new TaskCharSet(a), new TaskCharSet(Arrays.asList(new TaskChar[]{b,c,d,e}))),
	        		new Response(new TaskCharSet(a), new TaskCharSet(Arrays.asList(new TaskChar[]{b,c}))),
	        		new ChainPrecedence(new TaskCharSet(Arrays.asList(new TaskChar[]{a,b,d})), new TaskCharSet(c)),
	        };
	        
	        SubsumptionChecker suChe = new SubsumptionChecker(model);
	        Collection<Constraint> cns = LinearConstraintsIndexFactory.getAllConstraints(processModel.bag);
	        // Leave out all non-relation constraints
	        Iterator<Constraint> cnsIt = cns.iterator();
	        while (cnsIt.hasNext()) {
	        	Constraint current = cnsIt.next();
	        	if (!current.getFamily().equals(ConstraintFamily.RELATION) || !current.getSubFamily().equals(RelationConstraintSubFamily.SINGLE_ACTIVATION)) {
	        		cnsIt.remove();
	        	}
	        }
	        suChe.checkSubsumption(cns);
	        
	        System.out.println(suChe.csv());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
    }
}