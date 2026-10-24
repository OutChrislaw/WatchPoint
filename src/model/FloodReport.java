package model;

    // ------------------------------------------------------------ CONSTRUCTION

public class FloodReport extends Report {

    private String waterLevel;

    public FloodReport(String reportId, String reporterId, Location location,
                       String description, ReportStatus status, String dateSubmitted,
                       String waterLevel) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.waterLevel = waterLevel;
    }

    @Override

    // ------------------------------------------------------------ TYPE NAME

    public String getReportType() {
        return "Flood";
    }

    @Override

    // ------------------------------------------------------------ HAZARD DETAIL

    public String getSpecificDetail() {
        return waterLevel;
    }

    @Override
    public void setSpecificDetail(String specificDetail) {
        this.waterLevel = specificDetail;
    }
}
