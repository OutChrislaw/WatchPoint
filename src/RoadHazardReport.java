/**
 * RoadHazardReport.java
 * A report about a road problem such as a pothole or a broken sidewalk.
 */
public class RoadHazardReport extends Report {

    private String roadHazardType;

    public RoadHazardReport(String reportId, String reporterId, Location location,
                            String description, ReportStatus status, String dateSubmitted,
                            String roadHazardType) {
        super(reportId, reporterId, location, description, status, dateSubmitted);
        this.roadHazardType = roadHazardType;
    }

    @Override
    public String getReportType() {
        return "Road Hazard";
    }

    @Override
    public String getSpecificDetail() {
        return roadHazardType;
    }
}
