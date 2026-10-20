package manager;

import model.Report;
import model.RoadHazardReport;
import model.FloodReport;
import model.StreetlightReport;
import model.OtherHazardReport;
import model.Location;
import model.ReportStatus;
import storage.FileManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        if (reportProblem(hazardType, location, description, specificDetail) != null) {
            return null;
        }

        description = oneLine(description);
        specificDetail = specificDetail.trim();
        location = new Location(location.getCityMunicipality().trim(),
                location.getBarangay().trim(), location.getStreet().trim(),
                location.getSpecificPlace().trim());

        String reportId = generateReportId();
        String dateSubmitted = currentTimestamp();
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

    /**
     * The hazard types the system accepts, matching the report classes.
     */
    public static final String[] HAZARD_TYPES = { "Road Hazard", "Flood", "Streetlight", "Other" };

    /**
     * Collapses every run of spaces, tabs and line breaks into one space, so a
     * description typed on several lines is still saved as a single line.
     */
    public static String oneLine(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * The rules for a new report. Returns the problem to show, or null when the
     * report is acceptable.
     */
    public static String reportProblem(String hazardType, Location location, String description,
                                       String specificDetail) {
        boolean knownType = false;
        for (int i = 0; i < HAZARD_TYPES.length; i++) {
            if (HAZARD_TYPES[i].equals(hazardType)) {
                knownType = true;
            }
        }
        if (!knownType) {
            return "Please choose one of the listed hazard types.";
        }
        if (location == null) {
            return "Please select a City / Municipality and a Barangay.";
        }

        String city = oneLine(location.getCityMunicipality());
        if (city.length() == 0) {
            return "Please select a City / Municipality.";
        }
        String barangay = oneLine(location.getBarangay());
        if (barangay.length() == 0) {
            return "Please select a Barangay.";
        }

        String street = oneLine(location.getStreet());
        String place = oneLine(location.getSpecificPlace());
        String detail = oneLine(specificDetail);
        String text = oneLine(description);

        String forbidden = forbiddenCharacterProblem(street, place, detail, text);
        if (forbidden != null) {
            return forbidden;
        }

        if (place.length() < 2) {
            return "Please enter the specific place, like Near Water Refiller.";
        }
        if (place.length() > 60) {
            return "The specific place must be 60 characters or fewer.";
        }
        if (street.length() > 60) {
            return "The street must be 60 characters or fewer.";
        }
        if (detail.length() < 2) {
            return "Please enter the specific detail, like the pole number or depth.";
        }
        if (detail.length() > 80) {
            return "The specific detail must be 80 characters or fewer.";
        }
        if (text.length() < 10) {
            return "Please describe the hazard in at least 10 characters.";
        }
        if (text.length() > 500) {
            return "The description must be 500 characters or fewer.";
        }
        return null;
    }

    private static String forbiddenCharacterProblem(String street, String place, String detail,
                                                    String description) {
        String[] values = { street, place, detail, description };
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (value == null) {
                continue;
            }
            for (int j = 0; j < value.length(); j++) {
                if (value.charAt(j) == '|') {
                    return "The | character cannot be used in a report.";
                }
            }
        }
        return null;
    }

    /**
     * True when this resident already has a PENDING report with the same hazard
     * type, barangay and specific detail.
     */
    public boolean hasPendingDuplicate(String reporterId, String hazardType, String barangay,
                                       String specificDetail) {
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            if (report.getStatus() != ReportStatus.PENDING) {
                continue;
            }
            if (!report.getReporterId().equals(reporterId)) {
                continue;
            }
            if (!report.getReportType().equals(hazardType)) {
                continue;
            }
            if (!report.getLocation().getBarangay().equalsIgnoreCase(barangay.trim())) {
                continue;
            }
            if (report.getSpecificDetail().equalsIgnoreCase(specificDetail.trim())) {
                return true;
            }
        }
        return false;
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
        return updateReportStatusInCity(reportId, status, null);
    }

    public boolean updateReportStatusInCity(String reportId, ReportStatus status,
                                            String cityMunicipality) {
        Report report = getReportById(reportId);
        if (report == null) {
            return false;
        }
        if (cityMunicipality != null
                && !report.getLocation().getCityMunicipality().equals(cityMunicipality)) {
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

    public List<Report> getReports(String cityMunicipality, String barangay, String sortMode) {
        List<Report> result = new ArrayList<Report>();
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            if (cityMunicipality != null && !report.getLocation().getCityMunicipality().equals(cityMunicipality)) {
                continue;
            }
            if (barangay != null && !report.getLocation().getBarangay().equals(barangay)) {
                continue;
            }
            result.add(report);
        }

        sort(result, sortMode);
        return result;
    }

    private static void sort(List<Report> list, String sortMode) {
        if (sortMode == null) {
            return;
        }

        final String mode = sortMode;
        Collections.sort(list, new Comparator<Report>() {
            @Override
            public int compare(Report first, Report second) {
                if (mode.equals("Report ID (Newest first)")) {
                    return second.getReportId().compareTo(first.getReportId());
                }
                if (mode.equals("Date and Time (Newest first)")) {
                    return second.getDateSubmitted().compareTo(first.getDateSubmitted());
                }
                if (mode.equals("Date and Time (Oldest first)")) {
                    return first.getDateSubmitted().compareTo(second.getDateSubmitted());
                }
                if (mode.equals("Status")) {
                    return first.getStatus().name().compareTo(second.getStatus().name());
                }
                if (mode.equals("Hazard Type")) {
                    return first.getReportType().compareTo(second.getReportType());
                }
                return first.getReportId().compareTo(second.getReportId());
            }
        });
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

    private String currentTimestamp() {
        String now = LocalDateTime.now().toString().replace('T', ' ');
        if (now.length() > 16) {
            now = now.substring(0, 16);
        }
        return now;
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
