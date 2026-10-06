import java.util.ArrayList;
import java.util.List;

/**
 * LocationManager.java
 * Provides the list of Bohol cities/municipalities and their barangays.
 * The data is read from locations.txt through the FileManager.
 */
public class LocationManager {

    private List<String> lines;
    private FileManager fileManager;

    public LocationManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.lines = fileManager.readLines("locations.txt");
    }

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

    private String municipalityOf(String line) {
        int bar = line.indexOf("|");
        if (bar <= 0) {
            return "";
        }
        return line.substring(0, bar);
    }
}
