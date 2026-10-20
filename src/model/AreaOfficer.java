package model;

/**
 * AreaOfficer.java
 * A city/municipal officer. The account is tied to ONE area (a municipality or
 * a city), can only review the reports filed inside that area, and accepts or
 * rejects the residents who register there. It has no username and logs in with
 * its id (for example AO001).
 */
public class AreaOfficer extends User {

    private String area;

    public AreaOfficer(String userId, String password, String fullName, String dateCreated,
                       String area) {
        super(userId, password, fullName, dateCreated);
        this.area = area;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String getUserType() {
        return "AreaOfficer";
    }

    @Override
    public String toFileString() {
        return super.toFileString() + "|" + area;
    }
}