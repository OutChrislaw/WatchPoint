/**
 * Administrator.java
 * A city or barangay staff member who manages reports in WatchPoint.
 */
public class Administrator extends User {

    public Administrator(String userId, String username, String password, String fullName) {
        super(userId, username, password, fullName);
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
