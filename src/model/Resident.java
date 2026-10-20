package model;

public class Resident extends User {

    private String address;
    private String contactNumber;

    public Resident(String userId, String username, String password,
                    String fullName, String address, String contactNumber) {
        super(userId, username, password, fullName);
        this.address = address;
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setAddress(String address) {
        this.address = address;
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
        return super.toFileString() + "|" + address + "|" + contactNumber;
    }
}
