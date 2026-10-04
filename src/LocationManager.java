// LocationManager.java
// NEW CLASS added for the Bohol scope requirement.
// It keeps a list of Location objects and answers questions the GUI needs,
// such as "what barangays are in this city/municipality?".
// Data is stored in locations.txt.

import java.util.ArrayList;

public class LocationManager {

    private static final String FILE_NAME = "locations.txt";

    private ArrayList<Location> locations;
    private FileManager fileManager;

    public LocationManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.locations = new ArrayList<Location>();
        loadLocations();
        seedDefaultLocationsIfEmpty();
    }

    // Returns every city and municipality in Bohol (no duplicates).
    public ArrayList<String> getCitiesAndMunicipalities() {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < locations.size(); i++) {
            String name = locations.get(i).getCityOrMunicipality();
            if (!result.contains(name)) {
                result.add(name);
            }
        }
        return result;
    }

    // Returns the barangays that belong to one city or municipality.
    public ArrayList<String> getBarangays(String cityOrMunicipality) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            if (loc.getCityOrMunicipality().equals(cityOrMunicipality)
                    && !result.contains(loc.getBarangay())) {
                result.add(loc.getBarangay());
            }
        }
        return result;
    }

    // Returns the streets that belong to one barangay.
    public ArrayList<String> getStreets(String cityOrMunicipality, String barangay) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            if (loc.getCityOrMunicipality().equals(cityOrMunicipality)
                    && loc.getBarangay().equals(barangay)) {
                if (!result.contains(loc.getStreet())) {
                    result.add(loc.getStreet());
                }
            }
        }
        return result;
    }

    // Builds a full address text from the three chosen parts.
    public String buildFullAddress(String cityOrMunicipality, String barangay, String street) {
        Location location = new Location(cityOrMunicipality, barangay, street);
        if (street == null || street.trim().length() == 0) {
            return location.getFullAddress(false);
        }
        return location.getFullAddress();
    }

    // Adds a new location and saves it, unless it already exists.
    public void addLocation(String cityOrMunicipality, String barangay, String street) {
        Location location = new Location(cityOrMunicipality, barangay, street);
        // ArrayList.contains() uses our Location.equals() (Polymorphism).
        if (!locations.contains(location)) {
            locations.add(location);
            saveLocations();
        }
    }

    // Reads locations.txt into the locations list.
    private void loadLocations() {
        locations.clear();
        ArrayList<String> lines = fileManager.readLines(FILE_NAME);
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\\|");
            // Format: cityOrMunicipality|barangay|street
            if (parts.length == 3) {
                locations.add(new Location(parts[0], parts[1], parts[2]));
            }
        }
    }

    // Writes the locations list back to locations.txt.
    private void saveLocations() {
        ArrayList<String> lines = new ArrayList<String>();
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            lines.add(loc.getCityOrMunicipality() + "|" + loc.getBarangay() + "|"
                    + loc.getStreet());
        }
        fileManager.writeLines(FILE_NAME, lines);
    }

    // Fills locations.txt with a starter set of Bohol places the first time.
    // This is sampled data; more can be added later through addLocation().
    private void seedDefaultLocationsIfEmpty() {
        if (locations.size() > 0) {
            return;
        }

        // Tagbilaran City
        addLocation("Tagbilaran City", "Poblacion I", "Rizal Street");
        addLocation("Tagbilaran City", "Poblacion I", "CPG Avenue");
        addLocation("Tagbilaran City", "Poblacion II", "Gallares Street");
        addLocation("Tagbilaran City", "Cogon", "Bagacay Road");

        // Other cities / municipalities
        addLocation("Panglao", "Poblacion", "Panglao Coastal Road");
        addLocation("Dauis", "Poblacion", "Dauis Road");
        addLocation("Cortes", "Poblacion", "Municipal Road");
        addLocation("Marcela", "Poblacion", "Marcela Road");
        addLocation("Ubay", "Poblacion", "Ubay National Road");
        addLocation("Talibon", "Poblacion", "Talibon Wharf Road");
        addLocation("Tubigon", "Poblacion", "Tubigon Port Road");
        addLocation("Anda", "Poblacion", "Anda Beach Road");
        addLocation("Carmen", "Poblacion", "Carmen Road");
        addLocation("Jagna", "Poblacion", "Jagna Port Road");
        addLocation("Loon", "Poblacion", "Loon Road");
        addLocation("Baclayon", "Poblacion", "Baclayon Heritage Road");
        addLocation("Balilihan", "Poblacion", "Balilihan Road");
        addLocation("Bilar", "Poblacion", "Bilar Man-Made Forest Road");
    }
}
