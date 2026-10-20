package model;

/**
 * RegistrationRequest.java
 * A resident sign-up that is still waiting for a MunicipalOfficer to accept or
 * reject it. It is stored in registrations.txt and only becomes a Resident
 * after it is accepted.
 */
public class RegistrationRequest {

    private String fullName;
    private String username;
    private String password;
    private String contactNumber;
    private String barangay;
    private String municipality;
    private String dateRequested;

    public RegistrationRequest(String fullName, String username, String password,
                               String contactNumber, String barangay, String municipality,
                               String dateRequested) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.contactNumber = contactNumber;
        this.barangay = barangay;
        this.municipality = municipality;
        this.dateRequested = dateRequested;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getBarangay() {
        return barangay;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getDateRequested() {
        return dateRequested;
    }

    public String getAddress() {
        return barangay + ", " + municipality;
    }

    public String toFileString() {
        return fullName + "|" + username + "|" + password + "|" + contactNumber + "|"
                + barangay + "|" + municipality + "|" + dateRequested;
    }
}