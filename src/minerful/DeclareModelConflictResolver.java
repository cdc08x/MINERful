package minerful;

import java.util.TreeSet;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.index.LinearConstraintsIndexFactory;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.postprocessing.params.PostProcessingCmdParams;
import minerful.postprocessing.pruning.ConflictAndRedundancyResolver;

public class DeclareModelConflictResolver {
	public static void main(String[] args) throws Exception {
/*
		if (args.length < 1) {
			System.err.println("Usage: java " + DeclareModelConflictResolver.class.getName() + " <dec-miner-xml-in> <dec-miner-out>");
		}
*/
		String xmlFileIn = args[0];
		String xmlFileOut = args[1];
//		String xmlFileIn = "/home/claudio/Downloads/DeclareMinerExperiments/A24_C255_NoHier.xml";
//		String xmlFileOut = args[1];
		
		AbstractMinerFulStarter.configureLogging(DebugLevel.all);

		TreeSet<TaskChar> taskChars = new TreeSet<TaskChar>();
		TaskCharEncoderDecoder taChEnDe = new TaskCharEncoderDecoder();
		taChEnDe.encode(taskChars);
		TaskCharArchive taChAr = new TaskCharArchive(taChEnDe.getTranslationMap());
		ProcessModel proMod = new ProcessModel(taChAr, new ConstraintsBag(taskChars));
		
		System.out.println(LinearConstraintsIndexFactory.getAllConstraints(proMod.bag));
		
		long timingBeforeConflictResolution = System.currentTimeMillis();

		ConflictAndRedundancyResolver coRes = new ConflictAndRedundancyResolver(proMod, new PostProcessingCmdParams());
		
		proMod = coRes.resolveConflictsOrRedundancies();
		
		long timingAfterConflictResolution = System.currentTimeMillis();
		
		coRes.printComputationStats(timingBeforeConflictResolution, timingAfterConflictResolution);

		proMod.bag.removeMarkedConstraints();
		System.out.println(proMod.bag);
		
		new DeclareMapEncoderDecoder(coRes.getSafeProcess()).marshal(xmlFileOut);
	}
}
