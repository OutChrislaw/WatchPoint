package model;

    // ------------------------------------------------------------ CONSTRUCTION

public class StreetlightReport extends Report {

    private String poleNumber;

    public StreetlightReport(String reportId, String reporterId, Location location,
                             String description, ReportStatus status, String dateSubmitted,
                             String poleNumber) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.poleNumber = poleNumber;
    }

    @Override

    // ------------------------------------------------------------ TYPE NAME

    public String getReportType() {
        return "Streetlight";
    }

    @Override

    // ------------------------------------------------------------ HAZARD DETAIL

    public String getSpecificDetail() {
        return poleNumber;
    }

    @Override
    public void setSpecificDetail(String specificDetail) {
        this.poleNumber = specificDetail;
    }
}
