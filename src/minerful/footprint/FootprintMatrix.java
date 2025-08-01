package minerful.footprint;

import minerful.concept.TaskChar;

import java.util.*;

public class FootprintMatrix {
    public enum Relation {
        DIRECTLY_FOLLOW("→"),
        INVERSELY_FOLLOW("←"),
        MUTUALLY_FOLLOW("||"),
        NEVER_FOLLOW("#");

        private final String symbol;

        Relation(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    private final List<TaskChar> taskList;
    private final Map<TaskChar, Integer> taskIndexMap;
    private final Relation[][] matrix;

    public FootprintMatrix(List<TaskChar> taskList) {
        this.taskList = new ArrayList<>(taskList);
        this.taskIndexMap = new HashMap<>();
        int n = taskList.size();
        this.matrix = new Relation[n][n];

        for (int i = 0; i < n; i++) {
            taskIndexMap.put(taskList.get(i), i);
        }

        for (int i = 0; i < n; i++) {
            Arrays.fill(matrix[i], Relation.NEVER_FOLLOW);
        }
    }

    public void setRelation(TaskChar from, TaskChar to, Relation relation) {
        int i = taskIndexMap.get(from);
        int j = taskIndexMap.get(to);
        matrix[i][j] = relation;
    }

    public Relation getRelation(TaskChar from, TaskChar to) {
        int i = taskIndexMap.get(from);
        int j = taskIndexMap.get(to);
        return matrix[i][j];
    }

    public List<TaskChar> getTaskList() {
        return Collections.unmodifiableList(taskList);
    }

   public String getMatrixAsStringWithLabel(String label) {
    List<String> taskNames = new ArrayList<>();
    taskNames.add(""); 
    for (TaskChar t : taskList) {
        taskNames.add(t.getName());
    }

    int[] colWidths = new int[taskList.size() + 1];

    int maxFirstColWidth = 0;
    for (TaskChar t : taskList) {
        maxFirstColWidth = Math.max(maxFirstColWidth, t.getName().length());
    }
    colWidths[0] = Math.max(maxFirstColWidth, 1);

    for (int col = 0; col < taskList.size(); col++) {
        int maxColWidth = taskList.get(col).getName().length(); 
        for (int row = 0; row < taskList.size(); row++) {
            String symbol = getRelation(taskList.get(row), taskList.get(col)).getSymbol();
            maxColWidth = Math.max(maxColWidth, symbol.length());
        }
        colWidths[col + 1] = maxColWidth;
    }

    int totalWidth = 1;
    for (int w : colWidths) {
        totalWidth += w + 3;
    }

    StringBuilder sB = new StringBuilder();
    String headerLine = "=".repeat(totalWidth);

    sB.append("\n").append(headerLine).append("\n");
    sB.append(center("Matrix " + label, totalWidth)).append("\n");
    sB.append(headerLine).append("\n");

    sB.append("|");
    for (int i = 0; i < taskNames.size(); i++) {
        sB.append(" ").append(pad(taskNames.get(i), colWidths[i])).append(" |");
    }
    sB.append("\n");
  
    sB.append("|");
    for (int i = 0; i < colWidths.length; i++) {
        sB.append(" ").append("-".repeat(colWidths[i])).append(" |");
    }
    sB.append("\n");

    for (int row = 0; row < taskList.size(); row++) {
        sB.append("| ").append(pad(taskList.get(row).getName(), colWidths[0])).append(" |");
        for (int col = 0; col < taskList.size(); col++) {
            String symbol = getRelation(taskList.get(row), taskList.get(col)).getSymbol();
            sB.append(" ").append(pad(symbol, colWidths[col + 1])).append(" |");
        }
        sB.append("\n");
    }

    sB.append(headerLine).append("\n");

    return sB.toString();
}

private String pad(String str, int width) {
    if (str == null) str = "";
    if (width <= 0) width = 1;
    return String.format("%-" + width + "s", str);
}

private String center(String text, int width) {
    if (text == null) text = "";
    if (width <= text.length()) return text;
    int padding = (width - text.length()) / 2;
    return " ".repeat(padding) + text;
}

}
