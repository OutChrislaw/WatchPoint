package model;

public class Resident extends User {

    private String username;
    private String address;
    private String contactNumber;

    public Resident(String userId, String username, String password,
                    String fullName, String dateCreated, String address, String contactNumber) {
        super(userId, password, fullName, dateCreated);
        this.username = username;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @Override
    public String getUserType() {
        return "Resident";
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + username + "|" + address + "|" + contactNumber;
    }
}
