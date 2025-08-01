package minerful.io.encdec;

import minerful.miner.stats.GlobalStatsTable;

import java.io.*;
import java.util.Base64;

public class GlobalStatsTableEncoderDecoder {

    public String toBinaryStringFromGlobalStatsTable(GlobalStatsTable statsTable) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream objectOut = new ObjectOutputStream(byteOut)) {

            objectOut.writeObject(statsTable);
            objectOut.flush();
            return Base64.getEncoder().encodeToString(byteOut.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize GlobalStatsTable to binary string", e);
        }
    }

    public GlobalStatsTable fromBinaryStringToGlobalStatsTable(String base64String) {
        try {
            byte[] data = Base64.getDecoder().decode(base64String);
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
                 ObjectInputStream objectIn = new ObjectInputStream(byteIn)) {

                return (GlobalStatsTable) objectIn.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize GlobalStatsTable from binary string", e);
        }
    }

    public void saveToBinaryFile(GlobalStatsTable statsTable, File binaryFile) {
        try (FileOutputStream fos = new FileOutputStream(binaryFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            System.out.println("Saving GlobalStatsTable to binary file: " + binaryFile.getAbsolutePath());
            oos.writeObject(statsTable);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save GlobalStatsTable to binary file", e);
        }
    }

    public GlobalStatsTable loadFromBinaryFile(File binaryFile) {
        try (FileInputStream fis = new FileInputStream(binaryFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            System.out.println("Loading GlobalStatsTable from binary file: " + binaryFile.getAbsolutePath());
            return (GlobalStatsTable) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load GlobalStatsTable from binary file", e);
        }
    }
}
