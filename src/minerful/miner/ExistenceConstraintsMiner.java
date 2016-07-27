/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner;

import java.util.Set;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.ConstraintsBag;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;

public abstract class ExistenceConstraintsMiner extends AbstractConstraintsMiner {
    public ExistenceConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, Set<TaskChar> tasksToQueryFor) {
		super(globalStats, taskCharArchive, tasksToQueryFor);
	}
    
    @Override
    public ConstraintsBag discoverConstraints(ConstraintsBag constraintsBag) {
        if (constraintsBag == null)
            constraintsBag = new ConstraintsBag(tasksToQueryFor);
        for (TaskChar task: tasksToQueryFor) {
            LocalStatsWrapper localStats = this.globalStats.statsTable.get(task);
            TaskChar base = task;

            Constraint uniqueness = this.discoverAtMostOnceConstraint(base, localStats, this.globalStats.logSize);
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
		return MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_EXISTENCE_CONSTRAINT_TEMPLATES * tasksToQueryFor.size();
	}

	protected abstract Constraint discoverParticipationConstraint(TaskChar base,
			LocalStatsWrapper localStats, long testbedSize);

	protected abstract Constraint discoverAtMostOnceConstraint(TaskChar base,
			LocalStatsWrapper localStats, long testbedSize);

	protected abstract Constraint discoverInitConstraint(TaskChar base,
			LocalStatsWrapper localStats, long testbedSize);

	protected abstract Constraint discoverEndConstraint(TaskChar base,
			LocalStatsWrapper localStats, long testbedSize);
}