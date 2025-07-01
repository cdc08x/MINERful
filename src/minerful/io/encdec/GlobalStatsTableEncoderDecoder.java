package minerful.io.encdec;

import com.google.gson.*;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.miner.stats.GlobalStatsTable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.TreeSet;

public class GlobalStatsTableEncoderDecoder {

    private final Gson gson;

    public GlobalStatsTableEncoderDecoder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TaskChar.class, new TaskCharDeserializer());
        gsonBuilder.registerTypeAdapter(TaskCharArchive.class, new TaskCharArchiveDeserializer());
        this.gson = gsonBuilder.setPrettyPrinting().create();
    }

    //Serialize GlobalStatsTable to JSON string
    public String toJsonStringFromGlobalStatsTable(GlobalStatsTable statsTable) {
        return this.gson.toJson(statsTable);
    }

    //Deserialize GlobalStatsTable from JSON string
    public GlobalStatsTable fromJsonStringToGlobalStatsTable(String jsonString) {
        return this.gson.fromJson(jsonString, GlobalStatsTable.class);
    }

    //Deserialize GlobalStatsTable from JSON file
    public GlobalStatsTable loadFromFile(File jsonFile) throws IOException {
        try (FileReader reader = new FileReader(jsonFile)) {
            System.out.println("Loading GlobalStatsTable from file: " + jsonFile.getAbsolutePath());
            return this.gson.fromJson(reader, GlobalStatsTable.class);
        }
    }

    //Serialize GlobalStatsTable to JSON file
    public void saveToFile(GlobalStatsTable statsTable, File jsonFile) throws IOException {
        try (FileWriter writer = new FileWriter(jsonFile)) {
            System.out.println("Saving GlobalStatsTable to file: " + jsonFile.getAbsolutePath());
            this.gson.toJson(statsTable, writer);
        }
    }

    //Deserializer for TaskChar
    private static class TaskCharDeserializer implements JsonDeserializer<TaskChar> {
        @Override
        public TaskChar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new TaskChar(json.getAsString().charAt(0));
            } else if (json.isJsonObject()) {
                return new TaskChar(json.getAsJsonObject().get("identifier").getAsString().charAt(0));
            } else {
                throw new JsonParseException("Unexpected JSON type for TaskChar: " + json);
            }
        }
    }

    //Deserializer for TaskCharArchive
    private static class TaskCharArchiveDeserializer implements JsonDeserializer<TaskCharArchive> {
        @Override
        public TaskCharArchive deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            TreeSet<TaskChar> taskChars = new TreeSet<>();
            JsonArray array = json.getAsJsonObject().getAsJsonArray("taskChars");
            for (JsonElement element : array) {
                taskChars.add(context.deserialize(element, TaskChar.class));
            }
            return new TaskCharArchive(taskChars);
        }
    }
}
