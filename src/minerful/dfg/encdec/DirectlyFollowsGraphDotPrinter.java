package minerful.dfg.encdec;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import minerful.dfg.DirectlyFollowsGraph;

public class DirectlyFollowsGraphDotPrinter {
    public String getDotRepresentation(DirectlyFollowsGraph graph) {
        StringBuilder sBuilder = new StringBuilder();
        
        sBuilder.append("digraph DFG {\n");
        sBuilder.append("    rankdir=LR;\n");
        sBuilder.append("    node [shape=box, style=\"rounded,filled\", fillcolor=lightgray];\n");

        for (DirectlyFollowsGraph.ActNode node : graph.getNodes().values()) {
            sBuilder.append(String.format("    \"%s\" [label=\"%s\\n%d\"];\n",
                    node.getName(), node.getName(), node.getOccurrences()));
        }

        //INIT symbol ▶
        sBuilder.append("    \"INIT\" [shape=circle, label=\"▶\", style=filled, fillcolor=white, width=0.4, height=0.4];\n");

        // END symbol ◼ 
        sBuilder.append("    \"END\" [shape=circle, label=\"◼\", style=filled, fillcolor=white, width=0.4, height=0.4];\n");

        for (DirectlyFollowsGraph.Arc arc : graph.getArcs()) {
            sBuilder.append(String.format(Locale.ROOT, "    \"%s\" -> \"%s\" [label=\"%d\", penwidth=%.1f];\n",
                    arc.getFrom().getName(),       
                    arc.getTo().getName(),         
                    arc.getOccurrences(),          
                    arc.getThickness()));          
        }
        

        sBuilder.append("}\n");
        
        return sBuilder.toString();
    }
}
