// Administrator.java
// Inherits from User. An administrator reviews reports.
// It has the same data as User, so it only adds behavior.

public class Administrator extends User {

    // Constructor calls the parent (User) constructor with super(...).
    public Administrator(String userId, String username, String password, String fullName) {
        super(userId, username, password, fullName);
    }

    // Overriding getUserType() from User.
    @Override
    public String getUserType() {
        return "Administrator";
    }

    // Overriding toFileString() from User.
    // Format: Administrator|userId|username|password|fullName
    @Override
    public String toFileString() {
        return "Administrator|" + getUserId() + "|" + getUsername() + "|"
                + getPasswordRaw() + "|" + getFullName();
    }
}
