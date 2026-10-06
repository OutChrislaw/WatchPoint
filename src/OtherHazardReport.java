/**
 * OtherHazardReport.java
 * A report for any hazard that does not fit the other three types.
 */
public class OtherHazardReport extends Report {

    private String hazardCategory;

    public OtherHazardReport(String reportId, String reporterId, Location location,
                             String description, ReportStatus status, String dateSubmitted,
                             String hazardCategory) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.hazardCategory = hazardCategory;
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
