// User.java
// This is an ABSTRACT class. It is the parent of Resident and Administrator.
// Abstract means we cannot create a "User" object directly; we must create
// one of its subclasses. It defines the common attributes and methods.

public abstract class User {

    // All attributes are private (Encapsulation).
    private String userId;
    private String username;
    private String password;
    private String fullName;

    // Constructor. The "super(...)" call in the subclasses reaches this.
    public User(String userId, String username, String password, String fullName) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    // Getter methods. They expose the data in a controlled way.
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    // Checks a password without exposing the real one. Returns true or false.
    public boolean verifyPassword(String input) {
        if (input == null) {
            return false;
        }
        return input.equals(password);
    }

    // A protected helper used by subclasses so they can write the password
    // into the file. "protected" means only this class and its subclasses
    // can use it, so the password is still not public.
    protected String getPasswordRaw() {
        return password;
    }

    // Abstract methods. Each subclass MUST provide its own version.
    public abstract String getUserType();

    public abstract String toFileString();
}
