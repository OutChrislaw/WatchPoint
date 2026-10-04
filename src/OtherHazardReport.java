// OtherHazardReport.java
// Inherits from Report. Represents any other unsafe condition.

public class OtherHazardReport extends Report {

    private String hazardCategory; // category for other unsafe conditions

    public OtherHazardReport(String reportId, String reporterId, String location,
                             String description, ReportStatus status, String dateSubmitted,
                             String hazardCategory) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.hazardCategory = hazardCategory;
    }

    public String getHazardCategory() {
        return hazardCategory;
    }

    @Override
    public String getReportType() {
        return "Other";
    }

    @Override
    public String getSpecificDetail() {
        return hazardCategory;
    }
}
