// Resident.java
// Inherits from User. A resident is the person who reports hazards.
// It uses "extends" to inherit, and "super(...)" to call the parent constructor.

public class Resident extends User {

    // Extra attributes that only a Resident has.
    private String address;
    private String contactNumber;

    // Constructor. super(...) runs the User constructor first.
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

    // Overriding getUserType() from User. Polymorphism: the same method name
    // behaves differently depending on the real object type.
    @Override
    public String getUserType() {
        return "Resident";
    }

    // Overriding toFileString() from User.
    // Format: Resident|userId|username|password|fullName|address|contactNumber
    @Override
    public String toFileString() {
        return "Resident|" + getUserId() + "|" + getUsername() + "|" + getPasswordRaw()
                + "|" + getFullName() + "|" + address + "|" + contactNumber;
    }
}
