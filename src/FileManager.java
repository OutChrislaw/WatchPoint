// FileManager.java
// Reads and writes plain text lines in the data folder.
// It hides the low-level Java File I/O from the managers (Abstraction).

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileManager {

    private String dataFolder;

    public FileManager(String dataFolder) {
        // Anchor the data folder to the project root, so a single "data"
        // folder is used no matter which directory the app is run from.
        this.dataFolder = resolveDataFolder(dataFolder);
        ensureFolderExists();
    }

    // Finds the project root and puts the data folder there.
    // We look at where this class was loaded from (the "bin" folder), then
    // step up one level to reach the project root that holds "bin" and "src".
    // If that cannot be found, we fall back to the folder name as given.
    private static String resolveDataFolder(String folderName) {
        try {
            File classFile = new File(FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());

            // classFile is usually the "bin" folder. Its parent is the root.
            File binFolder = classFile;
            File root = binFolder.getParentFile();

            if (root != null && root.isDirectory()) {
                return root.getPath() + File.separator + folderName;
            }
        } catch (Exception e) {
            // If anything goes wrong, just use the folder name as-is.
        }
        return folderName;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    // Reads every line of a file and returns them as a list of strings.
    // If the file does not exist yet, it creates it and returns an empty list.
    public ArrayList<String> readLines(String fileName) {
        ensureFileExists(fileName);

        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(dataFolder + File.separator + fileName));
            String line = reader.readLine();
            while (line != null) {
                // Skip blank lines so the files stay clean.
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

    // Overwrites a file with the given lines.
    public void writeLines(String fileName, ArrayList<String> lines) {
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

    // Appends one line to a file without erasing the others.
    public void appendLine(String fileName, String line) {
        ensureFileExists(fileName);
        try {
            PrintWriter writer = new PrintWriter(
                    new FileWriter(dataFolder + File.separator + fileName, true));
            writer.println(line);
            writer.close();
        } catch (Exception e) {
            System.out.println("Error appending to " + fileName + ": " + e.getMessage());
        }
    }

    // Creates the data folder if it is missing.
    private void ensureFolderExists() {
        File folder = new File(dataFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    // Creates the file if it is missing.
    private void ensureFileExists(String fileName) {
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
