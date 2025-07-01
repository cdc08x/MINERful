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

    public void printMatrix() {
        System.out.printf("%10s", "");
        for (TaskChar col : taskList) {
            System.out.printf("%10s", col.getName());
        }
        System.out.println();
        for (TaskChar row : taskList) {
            System.out.printf("%10s", row.getName());
            for (TaskChar col : taskList) {
                System.out.printf("%10s", getRelation(row, col).getSymbol());
            }
            System.out.println();
        }
    }
}
