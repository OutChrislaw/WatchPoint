package model;

/**
 * User.java
 * Every account has an id, a password and a full name. Only a Resident also
 * has a username; administrators and area officers log in with their id.
 */
public abstract class User {

    private String userId;
    private String password;
    private String fullName;

    protected User(String userId, String password, String fullName) {
        this.userId = userId;
        this.password = password;
        this.fullName = fullName;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean verifyPassword(String input) {
        if (input == null) {
            return false;
        }
        return input.equals(password);
    }

    public abstract String getUserType();

    public String toFileString() {
        return getUserType() + "|" + userId + "|" + password + "|" + fullName;
    }
}