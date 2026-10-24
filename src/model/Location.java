package model;

public class Location {

    // ------------------------------------------------------------ FIELDS AND CONSTRUCTION

    private String cityMunicipality;
    private String barangay;
    private String street;
    private String specificPlace;

    public Location(String cityMunicipality, String barangay, String street, String specificPlace) {
        this.cityMunicipality = cityMunicipality;
        this.barangay = barangay;
        this.street = street;
        this.specificPlace = specificPlace;
    }

    // ------------------------------------------------------------ GETTERS

    public String getCityMunicipality() {
        return cityMunicipality;
    }

    public String getBarangay() {
        return barangay;
    }

    public String getStreet() {
        return street;
    }

    public String getSpecificPlace() {
        return specificPlace;
    }

    // ------------------------------------------------------------ FULL LOCATION

    public String getFullLocation() {
        String text = street + ", " + barangay + ", " + cityMunicipality;
        if (specificPlace != null && specificPlace.trim().length() > 0) {
            text = text + " (" + specificPlace + ")";
        }
        return text;
    }
}
