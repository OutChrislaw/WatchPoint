package manager;

import storage.FileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationManager {

    // ------------------------------------------------------------ FIELDS AND CONSTRUCTION

    private List<String> lines;
    private FileManager fileManager;

    public LocationManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.lines = fileManager.readLines("locations.txt");
    }

    // ------------------------------------------------------------ MUNICIPALITIES

    public List<String> getMunicipalities() {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < lines.size(); i++) {
            String name = municipalityOf(lines.get(i));
            if (name.length() > 0) {
                result.add(name);
            }
        }
        return result;
    }

    // ------------------------------------------------------------ BARANGAYS

    public List<String> getBarangays(String municipality) {
        List<String> result = new ArrayList<String>();
        if (municipality == null) {
            return result;
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (municipalityOf(line).equals(municipality)) {
                String[] parts = line.split("\\|");
                for (int j = 1; j < parts.length; j++) {
                    if (parts[j].trim().length() > 0) {
                        result.add(parts[j]);
                    }
                }
                return result;
            }
        }
        return result;
    }

    public List<String> getAllBarangays() {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\\|");
            for (int j = 1; j < parts.length; j++) {
                String name = parts[j].trim();
                if (name.length() > 0 && !result.contains(name)) {
                    result.add(name);
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    // ------------------------------------------------------------ HELPERS

    private String municipalityOf(String line) {
        int bar = line.indexOf("|");
        if (bar <= 0) {
            return "";
        }
        return line.substring(0, bar);
    }
}
