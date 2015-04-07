package minerful;

import java.util.TreeSet;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.index.LinearConstraintsIndexFactory;
import minerful.io.encdec.declare.DeclareEncoderDecoder;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.simplification.ConflictAndRedundancyResolver;

public class DeclareModelConflictResolver {
	public static void main(String[] args) throws Exception {
/*
		if (args.length < 1) {
			System.err.println("Usage: java " + DeclareModelConflictResolver.class.getName() + " <dec-miner-xml-in> <dec-miner-out>");
		}
*/
		String xmlFileIn = args[0];
//		String xmlFileIn = "/home/claudio/Downloads/DeclareMinerExperiments/A24_C255_NoHier.xml";
//		String xmlFileOut = args[1];
		
		AbstractMinerFulStarter.configureLogging(DebugLevel.trace);

		TreeSet<Constraint> constraintsALaMinerFul = new TreeSet<Constraint>(DeclareEncoderDecoder.fromDeclareMinerOutputToMinerfulConstraints(xmlFileIn));
		TreeSet<TaskChar> taskChars = new TreeSet<TaskChar>();
		ProcessModel proMod = new ProcessModel(new TaskCharRelatedConstraintsBag(taskChars));
		
		for (Constraint constraint : constraintsALaMinerFul) {
			if (!constraint.getSubFamily().equals(ConstraintSubFamily.PRECEDENCE)) {
				proMod.bag.add(constraint.base, constraint);
			} else {
				proMod.bag.add(((RelationConstraint)constraint).implied, constraint);
			}
		}
		
		System.out.println(LinearConstraintsIndexFactory.getAllConstraints(proMod.bag));
		
		long timingBeforeConflictResolution = System.currentTimeMillis();

		ConflictAndRedundancyResolver coRes = new ConflictAndRedundancyResolver(proMod);
		
		coRes.resolveConflicts();
		
		long timingAfterConflictResolution = System.currentTimeMillis();
		
		new MinerFulMinerStarter().printComputationStats(coRes, timingBeforeConflictResolution, timingAfterConflictResolution);
		
		System.out.println(coRes.getSafeProcess().bag.createHierarchyUnredundantCopy());
	}
}
