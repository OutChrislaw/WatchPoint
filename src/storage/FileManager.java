package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    /** Keeps the last number issued for every id prefix, so ids are never reused. */
    private static final String ID_FILE = "ids.txt";

    private String dataFolder;

    public FileManager(String dataFolder) {
        this.dataFolder = dataFolder;
        ensureFileExists("users.txt");
        ensureFileExists("reports.txt");
        ensureFileExists("locations.txt");
        ensureFileExists(ID_FILE);
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

    /**
     * The next number that was never used with this prefix, already reserved in
     * ids.txt. Returns -1 when all six digits are used up.
     */
    public int nextNumber(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            return -1;
        }

        int next = lastNumber(prefix) + 1;
        if (next > 999999) {
            return -1;
        }

        reserveNumber(prefix, next);
        return next;
    }

    /**
     * Raises the stored number for the prefix, so an account or report that was
     * deleted can never hand its id to somebody else.
     */
    public void reserveNumber(String prefix, int number) {
        if (prefix == null || prefix.length() == 0 || number <= 0) {
            return;
        }
        if (number <= lastNumber(prefix)) {
            return;
        }
        writeNumber(prefix, number);
    }

    /** The last number stored for the prefix, or 0 when it was never used. */
    public int lastNumber(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            return 0;
        }

        List<String> lines = readLines(ID_FILE);
        int highest = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            int bar = line.indexOf('|');
            if (bar <= 0 || !line.substring(0, bar).equals(prefix)) {
                continue;
            }
            try {
                int number = Integer.parseInt(line.substring(bar + 1).trim());
                if (number > highest) {
                    highest = number;
                }
            } catch (Exception e) {
                // lines that are not prefix|number are ignored
            }
        }
        return highest;
    }

    private void writeNumber(String prefix, int number) {
        List<String> lines = readLines(ID_FILE);
        List<String> kept = new ArrayList<String>();
        boolean written = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            int bar = line.indexOf('|');
            if (bar > 0 && line.substring(0, bar).equals(prefix)) {
                if (!written) {
                    kept.add(prefix + "|" + number);
                    written = true;
                }
                continue;
            }
            if (line.length() > 0) {
                kept.add(line);
            }
        }
        if (!written) {
            kept.add(prefix + "|" + number);
        }
        writeLines(ID_FILE, kept);
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
