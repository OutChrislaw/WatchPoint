package validation;

import model.Location;
import model.Report;
import model.ReportStatus;

import java.util.List;

/**
 * ReportRules.java
 * Every rule for filing a report, for rejecting one and for moving it through the
 * workflow. The order of the statuses themselves lives in model.ReportStatus.
 */
public class ReportRules {

    /** The hazard types the system accepts, matching the report classes. */
    public static final String[] HAZARD_TYPES = { "Road Hazard", "Flood", "Streetlight", "Other" };

    private ReportRules() {
    }

    /**
     * The rules for a new report. Returns the problem to show, or null when the
     * report is acceptable.
     */
    public static String reportProblem(String hazardType, Location location, String description,
                                       String specificDetail) {
        if (!isHazardType(hazardType)) {
            return "Please choose one of the listed hazard types.";
        }
        if (location == null) {
            return "Please select a City / Municipality and a Barangay.";
        }

        String city = TextRules.oneLine(location.getCityMunicipality());
        String barangay = TextRules.oneLine(location.getBarangay());
        String street = TextRules.oneLine(location.getStreet());
        String place = TextRules.oneLine(location.getSpecificPlace());
        String detail = TextRules.oneLine(specificDetail);
        String text = TextRules.oneLine(description);

        return TextRules.firstProblem(
                city.length() == 0 ? "Please select a City / Municipality." : null,
                barangay.length() == 0 ? "Please select a Barangay." : null,
                pipeProblem(street, place, detail, text),
                place.length() < 2
                        ? "Please enter the specific place, like Near Water Refiller." : null,
                TextRules.maxProblem(place, "specific place", 60),
                TextRules.maxProblem(street, "street", 60),
                detail.length() < 2
                        ? "Please enter the specific detail, like the pole number or depth." : null,
                TextRules.maxProblem(detail, "specific detail", 80),
                text.length() < 10 ? "Please describe the hazard in at least 10 characters." : null,
                TextRules.maxProblem(text, "description", 500));
    }

    /** Rules for the reason written when a report is rejected. */
    public static String remarkProblem(String remark) {
        if (remark == null || remark.trim().length() < 5) {
            return "Please write at least 5 characters to explain why the report is rejected.";
        }

        String text = TextRules.oneLine(remark);
        return TextRules.firstProblem(
                TextRules.maxProblem(text, "reason", 200),
                TextRules.hasPipe(text) ? "The | character cannot be used in the reason." : null);
    }

    /**
     * The workflow rule for one status change. Returns the message to show, or
     * null when the change is allowed.
     */
    public static String statusProblem(Report report, ReportStatus target, String reason) {
        if (report == null) {
            return "That report is no longer in the records.";
        }
        if (target == null) {
            return "Please choose the new status.";
        }

        // Report.setStatus(...) is public, so a hand-built report can still carry no status.
        ReportStatus current = report.getStatus();
        if (current == null) {
            return "This report has no status yet.";
        }
        if (target == current) {
            return "Report " + report.getReportId() + " is already " + current.displayName() + ".";
        }
        if (current == ReportStatus.REJECTED) {
            return "A rejected report cannot be changed. Only an administrator can reopen it.";
        }
        if (current == ReportStatus.RESOLVED) {
            return "This report is already RESOLVED and can no longer be changed.";
        }
        if (!current.canMoveTo(target)) {
            return "A " + current.displayName() + " report can only move to "
                    + nextStatusesText(current) + ".";
        }
        return target == ReportStatus.REJECTED ? remarkProblem(reason) : null;
    }

    /** True when the text is one of the listed hazard types. */
    public static boolean isHazardType(String hazardType) {
        for (int i = 0; i < HAZARD_TYPES.length; i++) {
            if (HAZARD_TYPES[i].equals(hazardType)) {
                return true;
            }
        }
        return false;
    }

    /** The pipe rule for every text of a report, or null. */
    private static String pipeProblem(String... values) {
        for (int i = 0; i < values.length; i++) {
            if (TextRules.hasPipe(values[i])) {
                return "The | character cannot be used in a report.";
            }
        }
        return null;
    }

    private static String nextStatusesText(ReportStatus status) {
        List<ReportStatus> next = status.nextStatuses();
        String text = "";
        for (int i = 0; i < next.size(); i++) {
            if (i > 0) {
                text = text + (i == next.size() - 1 ? " or " : ", ");
            }
            text = text + next.get(i).displayName();
        }
        return text;
    }
}