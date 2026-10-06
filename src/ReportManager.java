import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ReportManager.java
 * Handles submitting, searching, updating, and deleting hazard reports.
 * Reports are stored in reports.txt through the FileManager.
 */
public class ReportManager {

    private List<Report> reports;
    private FileManager fileManager;

    public ReportManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.reports = new ArrayList<Report>();
        loadReports();
    }

    public Report submitReport(String reporterId, String hazardType, Location location,
                               String description, String specificDetail) {
        String reportId = generateReportId();
        String dateSubmitted = LocalDate.now().toString();
        Report report;

        if (hazardType.equals("Road Hazard")) {
            report = new RoadHazardReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, dateSubmitted, specificDetail);
        } else if (hazardType.equals("Flood")) {
            report = new FloodReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, dateSubmitted, specificDetail);
        } else if (hazardType.equals("Streetlight")) {
            report = new StreetlightReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, dateSubmitted, specificDetail);
        } else {
            report = new OtherHazardReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, dateSubmitted, specificDetail);
        }

        reports.add(report);
        saveReports();
        return report;
    }

    public Report getReportById(String reportId) {
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getReportId().equals(reportId)) {
                return reports.get(i);
            }
        }
        return null;
    }

    public List<Report> getReportsByReporter(String reporterId) {
        List<Report> result = new ArrayList<Report>();
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getReporterId().equals(reporterId)) {
                result.add(reports.get(i));
            }
        }
        return result;
    }

    public List<Report> getAllReports() {
        return new ArrayList<Report>(reports);
    }

    public boolean updateResidentReport(Report report, String residentId) {
        if (report == null) {
            return false;
        }
        if (!report.getReporterId().equals(residentId)) {
            return false;
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            return false;
        }

        saveReports();
        return true;
    }

    public boolean deleteResidentReport(String reportId, String residentId) {
        Report report = getReportById(reportId);
        if (report == null) {
            return false;
        }
        if (!report.getReporterId().equals(residentId)) {
            return false;
        }
        if (report.getStatus() != ReportStatus.PENDING) {
            return false;
        }

        reports.remove(report);
        saveReports();
        return true;
    }

    public boolean updateReportStatus(String reportId, ReportStatus status) {
        Report report = getReportById(reportId);
        if (report == null) {
            return false;
        }

        report.setStatus(status);
        saveReports();
        return true;
    }

    public boolean deleteInvalidReport(String reportId) {
        Report report = getReportById(reportId);
        if (report == null) {
            return false;
        }

        reports.remove(report);
        saveReports();
        return true;
    }

    private void loadReports() {
        List<String> lines = fileManager.readLines("reports.txt");
        for (int i = 0; i < lines.size(); i++) {
            Report report = parseReport(lines.get(i));
            if (report != null) {
                reports.add(report);
            }
        }
    }

    private void saveReports() {
        List<String> lines = new ArrayList<String>();
        for (int i = 0; i < reports.size(); i++) {
            lines.add(reports.get(i).toFileString());
        }
        fileManager.writeLines("reports.txt", lines);
    }

    private Report parseReport(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 11) {
            return null;
        }

        String type = parts[0];
        String reportId = parts[1];
        String reporterId = parts[2];
        Location location = new Location(parts[3], parts[4], parts[5], parts[6]);
        String description = parts[7];
        String specificDetail = parts[8];

        ReportStatus status;
        if (parts[9].equals("VERIFIED")) {
            status = ReportStatus.VERIFIED;
        } else if (parts[9].equals("IN_PROGRESS")) {
            status = ReportStatus.IN_PROGRESS;
        } else if (parts[9].equals("RESOLVED")) {
            status = ReportStatus.RESOLVED;
        } else {
            status = ReportStatus.PENDING;
        }

        String dateSubmitted = parts[10];

        if (type.equals("Road Hazard")) {
            return new RoadHazardReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        }
        if (type.equals("Flood")) {
            return new FloodReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        }
        if (type.equals("Streetlight")) {
            return new StreetlightReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        }
        return new OtherHazardReport(reportId, reporterId, location, description,
                status, dateSubmitted, specificDetail);
    }

    private String generateReportId() {
        int highest = 0;
        for (int i = 0; i < reports.size(); i++) {
            String reportId = reports.get(i).getReportId();
            if (reportId != null && reportId.length() > 1) {
                try {
                    int number = Integer.parseInt(reportId.substring(1));
                    if (number > highest) {
                        highest = number;
                    }
                } catch (Exception e) {
                    // ids that are not in the R<number> format are ignored
                }
            }
        }

        String number = "" + (highest + 1);
        while (number.length() < 3) {
            number = "0" + number;
        }
        return "R" + number;
    }
}
