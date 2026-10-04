// ReportManager.java
// Handles hazard reports: submit, view, update status, delete.
// It reads and writes reports.txt through FileManager.

import java.time.LocalDate;
import java.util.ArrayList;

public class ReportManager {

    private static final String FILE_NAME = "reports.txt";

    private ArrayList<Report> reports;
    private FileManager fileManager;

    public ReportManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.reports = new ArrayList<Report>();
        loadReports();
    }

    // Builds the right Report subclass from the hazard type, sets PENDING and
    // today's date, saves it, and returns the new report.
    // The "hazardType" text decides which subclass to create (Abstraction +
    // Inheritance working together).
    public Report submitReport(String reporterId, String hazardType, String location,
                              String description, String specificDetail) {
        String reportId = generateReportId();
        String today = LocalDate.now().toString();
        Report report = null;

        if (hazardType.equalsIgnoreCase("Road Hazard")) {
            report = new RoadHazardReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, today, specificDetail);
        } else if (hazardType.equalsIgnoreCase("Streetlight")) {
            report = new StreetlightReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, today, specificDetail);
        } else if (hazardType.equalsIgnoreCase("Flood")) {
            report = new FloodReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, today, specificDetail);
        } else {
            report = new OtherHazardReport(reportId, reporterId, location, description,
                    ReportStatus.PENDING, today, specificDetail);
        }

        reports.add(report);
        saveReports();
        return report;
    }

    // Returns all reports (the Administrator view).
    public ArrayList<Report> getAllReports() {
        return new ArrayList<Report>(reports);
    }

    // Returns only one resident's reports (the Resident view).
    public ArrayList<Report> getReportsByReporter(String reporterId) {
        ArrayList<Report> result = new ArrayList<Report>();
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getReporterId().equals(reporterId)) {
                result.add(reports.get(i));
            }
        }
        return result;
    }

    // Changes the status of a report (also used to verify a report), then saves.
    // Returns false if no report has that ID.
    public boolean updateReportStatus(String reportId, ReportStatus status) {
        Report report = findReport(reportId);
        if (report == null) {
            return false;
        }
        report.setStatus(status);
        saveReports();
        return true;
    }

    // Removes an invalid report, then saves. Returns false if not found.
    public boolean deleteReport(String reportId) {
        Report report = findReport(reportId);
        if (report == null) {
            return false;
        }
        reports.remove(report);
        saveReports();
        return true;
    }

    // Reads reports.txt into the reports list.
    private void loadReports() {
        reports.clear();
        ArrayList<String> lines = fileManager.readLines(FILE_NAME);
        for (int i = 0; i < lines.size(); i++) {
            Report report = parseReport(lines.get(i));
            if (report != null) {
                reports.add(report);
            }
        }
    }

    // Writes the reports list back to reports.txt.
    private void saveReports() {
        ArrayList<String> lines = new ArrayList<String>();
        for (int i = 0; i < reports.size(); i++) {
            lines.add(reports.get(i).toFileString());
        }
        fileManager.writeLines(FILE_NAME, lines);
    }

    // Turns one line into the right Report subclass.
    private Report parseReport(String line) {
        String[] parts = line.split("\\|");
        // Format: type|id|reporterId|location|description|status|date|specificDetail
        if (parts.length < 8) {
            return null;
        }

        String type = parts[0];
        String reportId = parts[1];
        String reporterId = parts[2];
        String location = parts[3];
        String description = parts[4];
        ReportStatus status = ReportStatus.fromString(parts[5]);
        String dateSubmitted = parts[6];
        String specificDetail = parts[7];

        if (status == null) {
            status = ReportStatus.PENDING;
        }

        if (type.equalsIgnoreCase("Road Hazard")) {
            return new RoadHazardReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        } else if (type.equalsIgnoreCase("Streetlight")) {
            return new StreetlightReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        } else if (type.equalsIgnoreCase("Flood")) {
            return new FloodReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        } else {
            return new OtherHazardReport(reportId, reporterId, location, description,
                    status, dateSubmitted, specificDetail);
        }
    }

    // Looks up a report by ID.
    private Report findReport(String reportId) {
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getReportId().equals(reportId)) {
                return reports.get(i);
            }
        }
        return null;
    }

    // Creates the next unique report ID, such as R001, R002, and so on.
    private String generateReportId() {
        int highest = 0;
        for (int i = 0; i < reports.size(); i++) {
            String id = reports.get(i).getReportId(); // looks like "R001"
            if (id != null && id.length() > 1) {
                try {
                    int number = Integer.parseInt(id.substring(1));
                    if (number > highest) {
                        highest = number;
                    }
                } catch (Exception e) {
                    // ignore bad IDs
                }
            }
        }
        int next = highest + 1;
        String text = "" + next;
        while (text.length() < 3) {
            text = "0" + text;
        }
        return "R" + text;
    }
}
