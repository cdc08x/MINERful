package minerful.footprint;

import minerful.concept.TaskChar;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;
import minerful.miner.stats.StatsCell;

import java.util.*;

public class FootprintMatrixBuilder {
    private final GlobalStatsTable globalStats;
    private final Set<TaskChar> tasksToQueryFor;
    private final int maxDistance;

    public FootprintMatrixBuilder(GlobalStatsTable globalStats, Set<TaskChar> tasksToQueryFor, int maxDistance) {
        this.globalStats = globalStats;
        this.tasksToQueryFor = tasksToQueryFor;
        this.maxDistance = maxDistance;
    }

    /**
     * Builds a map of footprint matrices for each cumulative level from 1 to maxDistance.
     * Key = distance level d, Value = matrix combining relations up to ±d.
     */
    public Map<Integer, FootprintMatrix> buildAll() {
        List<TaskChar> taskList = new ArrayList<>(tasksToQueryFor);
        Map<Integer, FootprintMatrix> matrixByLevel = new LinkedHashMap<>();

        //Initialise matrices
        for (int distance = 1; distance <= maxDistance; distance++) {
            matrixByLevel.put(distance, new FootprintMatrix(taskList));
        }

        //For each pair of tasks, aggregate relations up to each distance
        for (TaskChar task : taskList) {
            LocalStatsWrapper statsWrapper = globalStats.statsTable.get(task);
            if (statsWrapper == null) continue;

            for (Map.Entry<TaskChar, StatsCell> entry : statsWrapper.interplayStatsTable.entrySet()) {
                TaskChar other = entry.getKey();
                if (!tasksToQueryFor.contains(other) || other.equals(task)) continue;

                StatsCell interplatStats = entry.getValue();

                //For each cumulative level, determine the merged relation
                for (int level = 1; level <= maxDistance; level++) {
                    FootprintMatrix matrix = matrixByLevel.get(level);
                    boolean fwd = false;
                    boolean bwd = false;

                    //Check distances ±1 to ±level
                    for (int d = 1; d <= level; d++) {
                        if (interplatStats.distances.get(d) != null) fwd = true;
                        if (interplatStats.distances.get(-d) != null) bwd = true;
                        if (fwd && bwd) break;
                    }

                    FootprintMatrix.Relation rel;
                    if (fwd && bwd) {
                        rel = FootprintMatrix.Relation.MUTUALLY_FOLLOW;
                    } else if (fwd) {
                        rel = FootprintMatrix.Relation.DIRECTLY_FOLLOW;
                    } else if (bwd) {
                        rel = FootprintMatrix.Relation.INVERSELY_FOLLOW;
                    } else {
                        rel = FootprintMatrix.Relation.NEVER_FOLLOW;
                    }

                    matrix.setRelation(task, other, rel);
                    matrix.setRelation(other, task, inverse(rel));
                }
            }
        }

        return matrixByLevel;
    }

    /**
     * Returns the inverse of a directional relation (swaps → and ←, keeps others unchanged).
     */
    private FootprintMatrix.Relation inverse(FootprintMatrix.Relation rel) {
        switch (rel) {
            case DIRECTLY_FOLLOW:
                return FootprintMatrix.Relation.INVERSELY_FOLLOW;
            case INVERSELY_FOLLOW:
                return FootprintMatrix.Relation.DIRECTLY_FOLLOW;
            default:
                return rel;
        }
    }
}
