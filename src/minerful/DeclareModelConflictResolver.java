package minerful;

import java.util.TreeSet;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.io.encdec.declare.DeclareEncoder;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.simplification.ConflictResolver;

public class DeclareModelConflictResolver {
	public static void main(String[] args) throws Exception {
/*
		if (args.length < 1) {
			System.err.println("Usage: java " + DeclareModelConflictResolver.class.getName() + " <dec-miner-xml-in> <dec-miner-out>");
		}
*/
		String xmlFileIn = "/home/claudio/DecMinOutput.xml";
//		String xmlFileOut = args[1];
		
		AbstractMinerFulStarter.configureLogging(DebugLevel.none);

		TreeSet<Constraint> constraintsALaMinerFul = new TreeSet<Constraint>(DeclareEncoder.fromDeclareMinerOutputToMinerfulConstraints(xmlFileIn));
		TreeSet<TaskChar> taskChars = new TreeSet<TaskChar>();
		ProcessModel proMod = new ProcessModel(new TaskCharRelatedConstraintsBag(taskChars));
		
		for (Constraint constraint : constraintsALaMinerFul) {
			if (!constraint.getSubFamily().equals(ConstraintSubFamily.PRECEDENCE_SUB_FAMILY_ID)) {
				proMod.bag.add(constraint.base, constraint);
			} else {
				proMod.bag.add(((RelationConstraint)constraint).implied, constraint);
			}
		}
		
		long timingBeforeConflictResolution = System.currentTimeMillis();

		ConflictResolver coRes = new ConflictResolver(proMod);
		
		coRes.resolveConflicts();
		
		long timingAfterConflictResolution = System.currentTimeMillis();
		
		new MinerFulMinerStarter().printComputationStats(coRes, timingBeforeConflictResolution, timingAfterConflictResolution);
		
		proMod.bag = proMod.bag.createHierarchyUnredundantCopy();
		
		System.out.println(proMod.bag);
	}
}
