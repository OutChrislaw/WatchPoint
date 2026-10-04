// StreetlightReport.java
// Inherits from Report. Represents a broken streetlight.

public class StreetlightReport extends Report {

    private String poleNumber; // the pole ID of the broken light

    public StreetlightReport(String reportId, String reporterId, String location,
                             String description, ReportStatus status, String dateSubmitted,
                             String poleNumber) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.poleNumber = poleNumber;
    }

    public String getPoleNumber() {
        return poleNumber;
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
