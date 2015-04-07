/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;

public abstract class ExistenceConstraintsMiner extends ConstraintsMiner {
    public ExistenceConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive) {
		super(globalStats, taskCharArchive);
	}
    
    @Override
    public TaskCharRelatedConstraintsBag discoverConstraints(TaskCharRelatedConstraintsBag constraintsBag) {
        if (constraintsBag == null)
            constraintsBag = new TaskCharRelatedConstraintsBag(taskCharArchive.getTaskChars());
        for (TaskChar task: taskCharArchive.getTaskChars()) {
            LocalStatsWrapper localStats = this.globalStats.statsTable.get(task);
            TaskChar base = task;

            Constraint uniqueness = this.discoverUniquenessConstraint(base, localStats, this.globalStats.logSize);
            if (uniqueness != null)
            	constraintsBag.add(base, uniqueness);
            Constraint participation = this.discoverParticipationConstraint(base, localStats, this.globalStats.logSize);
            if (participation != null)
            	constraintsBag.add(base, participation);
            
            Constraint init = this.discoverEndConstraint(base, localStats, this.globalStats.logSize);
            if (init != null)
                constraintsBag.add(base, init);
            Constraint end = this.discoverInitConstraint(base, localStats, this.globalStats.logSize);
            if (end != null)
                constraintsBag.add(base, end);
        }
        return constraintsBag;
    }

	@Override
	public long howManyPossibleConstraints() {
		return MetaConstraintUtils.NUMBER_OF_POSSIBLE_EXISTENCE_CONSTRAINT_TEMPLATES * taskCharArchive.size();
	}

	protected abstract Constraint discoverParticipationConstraint(TaskChar base,
			LocalStatsWrapper localStats, long testbedSize);

	protected abstract Constraint discoverUniquenessConstraint(TaskChar base,
			LocalStatsWrapper localStats, long testbedSize);

	protected abstract Constraint discoverInitConstraint(TaskChar base,
			LocalStatsWrapper localStats, long testbedSize);

	protected abstract Constraint discoverEndConstraint(TaskChar base,
			LocalStatsWrapper localStats, long testbedSize);
}