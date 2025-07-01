package minerful.dfg;

import minerful.concept.TaskChar;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;
import minerful.miner.stats.StatsCell;

import java.util.*;

public class DirectlyFollowsGraphBuilder {
    private final GlobalStatsTable globalStats;
    private final Set<TaskChar> tasksToQueryFor;

    public DirectlyFollowsGraphBuilder(GlobalStatsTable globalStats, Set<TaskChar> tasksToQueryFor) {
        this.globalStats = globalStats;
        this.tasksToQueryFor = tasksToQueryFor;
    }
    
    //builder for the directly follows graph
    public DirectlyFollowsGraph build() {
        DirectlyFollowsGraph graph = new DirectlyFollowsGraph();
        
        //special nodes init and end
        DirectlyFollowsGraph.ActNode initNode = new DirectlyFollowsGraph.ActNode("INIT", 0);
        DirectlyFollowsGraph.ActNode endNode = new DirectlyFollowsGraph.ActNode("END", 0);
        
        //create a graph node for each task and connect it to INIT and END nodes
        for (TaskChar task : tasksToQueryFor) {
            LocalStatsWrapper stats = globalStats.statsTable.get(task);
            long occurrences = stats.getTotalAmountOfOccurrences();

            //create a node for the current task
            DirectlyFollowsGraph.ActNode node = new DirectlyFollowsGraph.ActNode(task.getName(), occurrences);
            graph.addNode(task, node);
            //init node 
            if (stats.getOccurrencesAsFirst() > 0) {
                DirectlyFollowsGraph.Arc arc = new DirectlyFollowsGraph.Arc(initNode, node, stats.getOccurrencesAsFirst());
                graph.addArc(arc);
                initNode.addOutArc(arc);
                node.addInArc(arc);
                graph.addTotalOccurrences(stats.getOccurrencesAsFirst());
            }
            //end node
            if (stats.getOccurrencesAsLast() > 0) {
                DirectlyFollowsGraph.Arc arc = new DirectlyFollowsGraph.Arc(node, endNode, stats.getOccurrencesAsLast());
                graph.addArc(arc);
                node.addOutArc(arc);
                endNode.addInArc(arc);
            }
        }

        //add arcs between directly-following tasks based on 1-distance occurrences
        for (TaskChar task : tasksToQueryFor) {
            LocalStatsWrapper stats = globalStats.statsTable.get(task);
            DirectlyFollowsGraph.ActNode fromNode = graph.getNode(task);

            for (Map.Entry<TaskChar, StatsCell> entry : stats.interplayStatsTable.entrySet()) {
                TaskChar nextTask = entry.getKey();
                StatsCell interplayStats = entry.getValue();

                //only add arc if there is a valid next task, no self-loop, and direct (distance 1) follow
                if (!nextTask.equals(task) && graph.getNode(nextTask) != null && interplayStats.distances.containsKey(1)) {
                    long occurrences = (long) interplayStats.distances.get(1);
                    if (occurrences > 0) {
                        DirectlyFollowsGraph.ActNode toNode = graph.getNode(nextTask);
                        DirectlyFollowsGraph.Arc arc = new DirectlyFollowsGraph.Arc(fromNode, toNode, occurrences);
                        graph.addArc(arc);
                        fromNode.addOutArc(arc);
                        toNode.addInArc(arc);
                    }
                }
            }
        }
        //set visual thickness of arcs based on quartile distribution of occurrence counts
        calculateQuartileThickness(graph);
        return graph;
    }

    private void calculateQuartileThickness(DirectlyFollowsGraph graph) {
        List<Long> occurrencesList = new ArrayList<>();
        for (DirectlyFollowsGraph.Arc arc : graph.getArcs()) {
            occurrencesList.add(arc.getOccurrences());
        }

        if (occurrencesList.isEmpty()) return;

        Collections.sort(occurrencesList);

        long Q1 = getPercentile(occurrencesList, 25);
        long Q2 = getPercentile(occurrencesList, 50);
        long Q3 = getPercentile(occurrencesList, 75);

        for (DirectlyFollowsGraph.Arc arc : graph.getArcs()) {
            long occ = arc.getOccurrences();
            if (occ <= Q1) {
                arc.setThickness(1.0);
            } else if (occ <= Q2) {
                arc.setThickness(2.5);
            } else if (occ <= Q3) {
                arc.setThickness(4.0);
            } else {
                arc.setThickness(5.5);
            }
        }
    }

    private long getPercentile(List<Long> sortedList, int percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
        return sortedList.get(Math.max(0, index));
    }

    public void printGraph(DirectlyFollowsGraph graph) {
        System.out.println("Directly Follows Graph:");

        // Print nodes
        System.out.println("Nodes:");
        for (DirectlyFollowsGraph.ActNode node : graph.getNodes().values()) {
            System.out.println("  " + node.getName() + " (Occurrences: " + node.getOccurrences() + ")");
        }

        // Print arcs
        System.out.println("Arcs:");
        for (DirectlyFollowsGraph.Arc arc : graph.getArcs()) {
            System.out.println("  " + arc.getFrom().getName() + " -> " + arc.getTo().getName() +
                    " (Occurrences: " + arc.getOccurrences() + ", Thickness: " + arc.getThickness() + ")");
        }

        System.out.println("Total Occurrences: " + graph.getTotalOccurrences());
    }
}
