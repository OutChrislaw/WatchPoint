package model;

/**
 * AreaOfficer.java
 * A city/municipal officer. The account is tied to ONE area (a municipality or
 * a city), can only review the reports filed inside that area, and accepts or
 * rejects the residents who register there. Only an Administrator can create
 * this account, and its id always starts with AO (for example AO001).
 */
public class AreaOfficer extends User {

    private String area;

    public AreaOfficer(String userId, String username, String password,
                       String fullName, String area) {
        super(userId, username, password, fullName);
        this.area = area;
    }

    public String getArea() {
        return area;
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