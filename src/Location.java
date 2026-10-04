// Location.java
// NEW CLASS added for the Bohol scope requirement.
// It represents ONE place where a hazard can happen.
// The Province is always Bohol. A Location is made of three parts:
//   cityOrMunicipality -> barangay -> street
// Example: "Tagbilaran City" -> "Poblacion" -> "Rizal Street"

public class Location {

    // The province is the same for all locations, so it is a constant (final).
    public static final String PROVINCE = "Bohol";

    // Private attributes (Encapsulation).
    private String cityOrMunicipality; // a City OR a Municipality in Bohol
    private String barangay;
    private String street;

    // Constructor. All three parts are required.
    public Location(String cityOrMunicipality, String barangay, String street) {
        this.cityOrMunicipality = cityOrMunicipality;
        this.barangay = barangay;
        this.street = street;
    }

    // Getters.
    public String getCityOrMunicipality() {
        return cityOrMunicipality;
    }

    public String getBarangay() {
        return barangay;
    }

    public String getStreet() {
        return street;
    }

    // Overloading example: the same method name, getFullAddress, but with
    // different parameters. (Java chooses which one to run based on arguments.)

    // Full address including the street.
    public String getFullAddress() {
        return street + ", " + barangay + ", " + cityOrMunicipality + ", " + PROVINCE;
    }

    // Full address without the street (used when a street is not provided).
    public String getFullAddress(boolean includeStreet) {
        if (includeStreet) {
            return getFullAddress();
        } else {
            return barangay + ", " + cityOrMunicipality + ", " + PROVINCE;
        }
    }

    // Overriding toString() from Object to show a short readable name.
    @Override
    public String toString() {
        return cityOrMunicipality + " - " + barangay + " - " + street;
    }

    // Overriding equals() from Object so two locations can be compared by value.
    // This is used to avoid saving the same location twice.
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Location)) {
            return false;
        }
        Location otherLocation = (Location) other;
        return cityOrMunicipality.equals(otherLocation.cityOrMunicipality)
                && barangay.equals(otherLocation.barangay)
                && street.equals(otherLocation.street);
    }
}
