package model;

public class Administrator extends User {

    // ------------------------------------------------------------ CONSTRUCTION

    public Administrator(String userId, String password, String fullName, String dateCreated) {
        super(userId, password, fullName, dateCreated);
    }

    @Override

    // ------------------------------------------------------------ USER TYPE

    public String getUserType() {
        return "Administrator";
    }

    @Override

    // ------------------------------------------------------------ SERIALISATION

    public String toFileString() {
        return super.toFileString();
    }
}
