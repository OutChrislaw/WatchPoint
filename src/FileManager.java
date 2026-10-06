import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * FileManager.java
 * Handles all text file reading and writing for WatchPoint.
 * Only users.txt and reports.txt are used by the system.
 */
public class FileManager {

    private String dataFolder;

    public FileManager(String dataFolder) {
        this.dataFolder = dataFolder;
        ensureFileExists("users.txt");
        ensureFileExists("reports.txt");
    }

    public List<String> readLines(String fileName) {
        ensureFileExists(fileName);

        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(dataFolder + File.separator + fileName));
            String line = reader.readLine();
            while (line != null) {
                if (line.trim().length() > 0) {
                    lines.add(line);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Error reading " + fileName + ": " + e.getMessage());
        }
        return lines;
    }

    public void writeLines(String fileName, List<String> lines) {
        ensureFileExists(fileName);

        try {
            PrintWriter writer = new PrintWriter(
                    new FileWriter(dataFolder + File.separator + fileName));
            for (int i = 0; i < lines.size(); i++) {
                writer.println(lines.get(i));
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("Error writing " + fileName + ": " + e.getMessage());
        }
    }

    private void ensureFileExists(String fileName) {
        File folder = new File(dataFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(dataFolder + File.separator + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                System.out.println("Error creating " + fileName + ": " + e.getMessage());
            }
        }
    }
}
