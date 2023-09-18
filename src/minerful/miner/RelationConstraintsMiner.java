package minerful.miner;

import java.util.Set;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintMeasuresManager;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.ConstraintsBag;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;

public abstract class RelationConstraintsMiner extends AbstractConstraintsMiner {

    public RelationConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, Set<TaskChar> tasksToQueryFor) {
        super(globalStats, taskCharArchive, tasksToQueryFor);
    }

	@Override
	public long howManyPossibleConstraints() {
		return MetaConstraintUtils.NUMBER_OF_DISCOVERABLE_RELATION_CONSTRAINT_TEMPLATES * taskCharArchive.size() * (taskCharArchive.size() - 1);
	}

    protected abstract Set<? extends Constraint> refineRelationConstraints(
            Set<Constraint> setOfConstraints);

    protected abstract Set<? extends Constraint> discoverRelationConstraints(TaskChar taskChUnderAnalysis,
    				ConstraintsBag constraintsBag);
}