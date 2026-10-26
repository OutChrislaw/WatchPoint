package model;

public class AreaOfficer extends User {

    // ------------------------------------------------------------ FIELDS AND CONSTRUCTION

    private String area;

    public AreaOfficer(String userId, String password, String fullName, String dateCreated,
                       String area) {
        super(userId, password, fullName, dateCreated);
        this.area = area;
    }

    // ------------------------------------------------------------ GETTERS AND SETTERS

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override

    // ------------------------------------------------------------ OVERRIDES

    public String getUserType() {
        return "AreaOfficer";
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + area;
    }
}
