package model;

    // ------------------------------------------------------------ CONSTRUCTION

public class OtherHazardReport extends Report {

    private String hazardCategory;

    public OtherHazardReport(String reportId, String reporterId, Location location,
                             String description, ReportStatus status, String dateSubmitted,
                             String hazardCategory) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.hazardCategory = hazardCategory;
    }

    @Override

    // ------------------------------------------------------------ TYPE NAME

    public String getReportType() {
        return "Other";
    }

    @Override

    // ------------------------------------------------------------ HAZARD DETAIL

    public String getSpecificDetail() {
        return hazardCategory;
    }

    @Override
    public void setSpecificDetail(String specificDetail) {
        this.hazardCategory = specificDetail;
    }
}
