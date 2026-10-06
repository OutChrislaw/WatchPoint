/**
 * User.java
 * Abstract parent class of Resident and Administrator.
 * Holds the account details that every user of WatchPoint shares.
 */
public abstract class User {

    private String userId;
    private String username;
    private String password;
    private String fullName;

    protected User(String userId, String username, String password, String fullName) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean verifyPassword(String input) {
        if (input == null) {
            return false;
        }
        return input.equals(password);
    }

    public abstract String getUserType();

    public String toFileString() {
        return getUserType() + "|" + userId + "|" + username + "|" + password + "|" + fullName;
    }
}
