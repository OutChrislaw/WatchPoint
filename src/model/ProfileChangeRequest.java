package model;

/**
 * ProfileChangeRequest.java
 * A resident who moves to another city/municipality waits here until the Area
 * Officer of the NEW area approves or rejects the change. Stored in
 * profilechanges.txt; the resident keeps the old address until it is approved.
 */
public class ProfileChangeRequest {

    private String residentId;
    private String residentName;
    private String barangay;
    private String municipality;
    private String dateRequested;

    public ProfileChangeRequest(String residentId, String residentName, String barangay,
                                String municipality, String dateRequested) {
        this.residentId = residentId;
        this.residentName = residentName;
        this.barangay = barangay;
        this.municipality = municipality;
        this.dateRequested = dateRequested;
    }

    public String getResidentId() {
        return residentId;
    }

    public String getResidentName() {
        return residentName;
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
        return residentId + "|" + residentName + "|" + barangay + "|" + municipality + "|"
                + dateRequested;
    }
}