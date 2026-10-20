package model;

/**
 * Administrator.java
 * Staff account that manages every report and account. It has no username and
 * logs in with its id (for example U999).
 */
public class Administrator extends User {

    public Administrator(String userId, String password, String fullName) {
        super(userId, password, fullName);
    }

    @Override
    public String getUserType() {
        return "Administrator";
    }

    @Override
    public String toFileString() {
        return super.toFileString();
    }
}