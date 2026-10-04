// RoadHazardReport.java
// Inherits from Report. Represents potholes, cracks or debris on the road.

public class RoadHazardReport extends Report {

    private String roadHazardType; // for example: pothole, crack, debris

    public RoadHazardReport(String reportId, String reporterId, String location,
                            String description, ReportStatus status, String dateSubmitted,
                            String roadHazardType) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.roadHazardType = roadHazardType;
    }

    public String getRoadHazardType() {
        return roadHazardType;
    }

    // Overriding the abstract method from Report.
    @Override
    public String getReportType() {
        return "Road Hazard";
    }

    // Overriding the abstract method from Report.
    @Override
    public String getSpecificDetail() {
        return roadHazardType;
    }
}
