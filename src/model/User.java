package model;

public abstract class User {

    // ------------------------------------------------------------ FIELDS

    private String userId;
    private String password;
    private String fullName;
    private String dateCreated;

    // ------------------------------------------------------------ CONSTRUCTION

    protected User(String userId, String password, String fullName, String dateCreated) {
        this.userId = userId;
        this.password = password;
        this.fullName = fullName;
        this.dateCreated = dateCreated;
    }

    // ------------------------------------------------------------ GETTERS

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    // ------------------------------------------------------------ SETTERS

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // ------------------------------------------------------------ PASSWORD CHECK

    public boolean verifyPassword(String input) {
        if (input == null) {
            return false;
        }
        return input.equals(password);
    }

    // ------------------------------------------------------------ TYPE AND SERIALISATION

    public abstract String getUserType();

    public String toFileString() {
        return getUserType() + "|" + userId + "|" + password + "|" + fullName + "|" + dateCreated;
    }
}
