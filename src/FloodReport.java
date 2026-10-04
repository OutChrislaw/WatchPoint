// FloodReport.java
// Inherits from Report. Represents a flooded area.

public class FloodReport extends Report {

    private String waterLevel; // for example: ankle, knee or waist deep

    public FloodReport(String reportId, String reporterId, String location,
                       String description, ReportStatus status, String dateSubmitted,
                       String waterLevel) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.waterLevel = waterLevel;
    }

    public String getWaterLevel() {
        return waterLevel;
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
