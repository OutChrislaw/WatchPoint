/**
 * StreetlightReport.java
 * A report about a broken or unlit streetlight.
 */
public class StreetlightReport extends Report {

    private String poleNumber;

    public StreetlightReport(String reportId, String reporterId, Location location,
                             String description, ReportStatus status, String dateSubmitted,
                             String poleNumber) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.poleNumber = poleNumber;
    }

    @Override
    public String getReportType() {
        return "Streetlight";
    }

    @Override
    public String getSpecificDetail() {
        return poleNumber;
    }
}
