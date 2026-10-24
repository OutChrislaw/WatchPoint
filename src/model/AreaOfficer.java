package model;

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
