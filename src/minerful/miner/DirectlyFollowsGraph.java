package minerful.miner;

import java.util.*;
import org.apache.log4j.Logger;
import minerful.concept.TaskChar;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;
import minerful.miner.stats.StatsCell;

public class DirectlyFollowsGraph {
    private static final Logger logger = Logger.getLogger(DirectlyFollowsGraph.class.getCanonicalName());

    private final GlobalStatsTable globalStats;
    private final Set<TaskChar> tasksToQueryFor;
    private final Map<TaskChar, ActNode> nodes;
    private final List<Arc> arcs;

    private final ActNode initNode;
    private final ActNode endNode;

    private long totalOccurrences;

    public DirectlyFollowsGraph(GlobalStatsTable globalStats, Set<TaskChar> tasksToQueryFor) {
        this(globalStats, tasksToQueryFor, false);
    }

    public DirectlyFollowsGraph(GlobalStatsTable globalStats, Set<TaskChar> tasksToQueryFor, boolean foreseeingDistances) {
        this.globalStats = globalStats;
        this.tasksToQueryFor = tasksToQueryFor;
        this.nodes = new HashMap<>();
        this.arcs = new ArrayList<>();
        this.totalOccurrences = 0;

        this.initNode = new ActNode("INIT", 0);
        this.endNode = new ActNode("END", 0);
    }

    public void buildGraph() {
        for (TaskChar task : tasksToQueryFor) {
            LocalStatsWrapper stats = globalStats.statsTable.get(task);
            long occurrences = stats.getTotalAmountOfOccurrences();

            ActNode node = new ActNode(task.getName(), occurrences);
            nodes.put(task, node);

            if (stats.getOccurrencesAsFirst() > 0) {
                Arc arc = new Arc(initNode, node, stats.getOccurrencesAsFirst());
                arcs.add(arc);
                initNode.addOutArc(arc);
                node.addInArc(arc);
                totalOccurrences += stats.getOccurrencesAsFirst();
            }
            if (stats.getOccurrencesAsLast() > 0) {
                Arc arc = new Arc(node, endNode, stats.getOccurrencesAsLast());
                arcs.add(arc);
                node.addOutArc(arc);
                endNode.addInArc(arc);
            }
        }

        for (TaskChar task : tasksToQueryFor) {
            LocalStatsWrapper stats = globalStats.statsTable.get(task);
            ActNode fromNode = nodes.get(task);

            for (TaskChar nextTask : stats.interplayStatsTable.keySet()) {
                if (!nextTask.equals(task) && nodes.containsKey(nextTask)) {
                    StatsCell interplayStats = stats.interplayStatsTable.get(nextTask);
                    double occurrences = 0.0;

                    if (interplayStats.distances.containsKey(1)) {
                        occurrences = interplayStats.distances.get(1);
                    }

                    if (occurrences > 0) {
                        ActNode toNode = nodes.get(nextTask);
                        Arc arc = new Arc(fromNode, toNode, (long) occurrences);
                        arcs.add(arc);
                        fromNode.addOutArc(arc);
                        toNode.addInArc(arc);
                    }
                }
            }
        }
    }

    public void printGraph() {
        logger.info("Directly Follows Graph:");
        for (ActNode node : nodes.values()) {
            logger.info(node);
            logger.info("  Incoming Arcs: " + node.getInArcs());
            logger.info("  Outgoing Arcs: " + node.getOutArcs());
        }
        logger.info("Total occurrences from INIT: " + totalOccurrences);
    }

    public static class ActNode {
        private final String name;
        private final long occurrences;
        private final List<Arc> inArcs;
        private final List<Arc> outArcs;

        public ActNode(String name, long occurrences) {
            this.name = name;
            this.occurrences = occurrences;
            this.inArcs = new ArrayList<>();
            this.outArcs = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public long getOccurrences() {
            return occurrences;
        }

        public void addInArc(Arc arc) {
            this.inArcs.add(arc);
        }

        public void addOutArc(Arc arc) {
            this.outArcs.add(arc);
        }

        public List<Arc> getInArcs() {
            return inArcs;
        }

        public List<Arc> getOutArcs() {
            return outArcs;
        }

        @Override
        public String toString() {
            return "ActNode{" + "name='" + name + '\'' + ", occurrences=" + occurrences + '}';
        }
    }

    public static class Arc {
        private final ActNode from;
        private final ActNode to;
        private final long occurrences;

        public Arc(ActNode from, ActNode to, long occurrences) {
            this.from = from;
            this.to = to;
            this.occurrences = occurrences;
        }

        @Override
        public String toString() {
            return "Arc{" + "from=" + from.getName() + ", to=" + to.getName() + ", occurrences=" + occurrences + '}';
        }
    }
}
