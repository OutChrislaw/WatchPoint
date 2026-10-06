/**
 * FloodReport.java
 * A report about flooding in a street or barangay.
 */
public class FloodReport extends Report {

    private String waterLevel;

    public FloodReport(String reportId, String reporterId, Location location,
                       String description, ReportStatus status, String dateSubmitted,
                       String waterLevel) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.waterLevel = waterLevel;
    }

    @Override
    public String getReportType() {
        return "Flood";
    }

    @Override
    public String getSpecificDetail() {
        return waterLevel;
    }
}
