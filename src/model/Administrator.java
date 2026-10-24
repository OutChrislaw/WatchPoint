package model;

public class Administrator extends User {

    public Administrator(String userId, String password, String fullName, String dateCreated) {
        super(userId, password, fullName, dateCreated);
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
