package minerful.dfg;

import minerful.concept.TaskChar;
import java.util.*;

public class DirectlyFollowsGraph {
    private final Map<TaskChar, ActNode> nodes = new HashMap<>();
    private final List<Arc> arcs = new ArrayList<>();
    private long totalOccurrences = 0;

    public Map<TaskChar, ActNode> getNodes() {
        return nodes;
    }

    public List<Arc> getArcs() {
        return arcs;
    }

    public long getTotalOccurrences() {
        return totalOccurrences;
    }

    public void addNode(TaskChar taskChar, ActNode node) {
        nodes.put(taskChar, node);
    }

    public ActNode getNode(TaskChar taskChar) {
        return nodes.get(taskChar);
    }

    public void addArc(Arc arc) {
        arcs.add(arc);
    }

    public void addTotalOccurrences(long occurrences) {
        this.totalOccurrences += occurrences;
    }


    public static class ActNode {
        private final String name;
        private final long occurrences;
        private final List<Arc> inArcs = new ArrayList<>();
        private final List<Arc> outArcs = new ArrayList<>();

        public ActNode(String name, long occurrences) {
            this.name = name;
            this.occurrences = occurrences;
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
        private double thickness;

        public Arc(ActNode from, ActNode to, long occurrences) {
            this.from = from;
            this.to = to;
            this.occurrences = occurrences;
        }

        public ActNode getFrom() {
            return from;
        }

        public ActNode getTo() {
            return to;
        }

        public long getOccurrences() {
            return occurrences;
        }

        public double getThickness() {
            return thickness;
        }

        public void setThickness(double thickness) {
            this.thickness = thickness;
        }

        @Override
        public String toString() {
            return "Arc{" + "from=" + from.getName() + ", to=" + to.getName() + ", occurrences=" + occurrences + ", thickness=" + thickness + '}';
        }
    }
}
