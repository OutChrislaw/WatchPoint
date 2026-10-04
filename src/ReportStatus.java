// ReportStatus.java
// This is an enumeration (enum) of the possible statuses of a report.
// An enum is a special type that lists a fixed set of named values.

public enum ReportStatus {

    PENDING,      // newly submitted
    VERIFIED,     // confirmed by an administrator
    IN_PROGRESS,  // being addressed
    RESOLVED;     // fixed

    // A method inside the enum that returns a friendly name to show in the GUI.
    // The switch checks which value this is and returns the matching text.
    public String getLabel() {
        if (this == PENDING) {
            return "Pending";
        } else if (this == VERIFIED) {
            return "Verified";
        } else if (this == IN_PROGRESS) {
            return "In Progress";
        } else {
            return "Resolved";
        }
    }

    // Turns a status name (from the file or from user input) back into a value.
    // Returns null if the text does not match any status.
    public static ReportStatus fromString(String text) {
        if (text == null) {
            return null;
        }
        if (text.equalsIgnoreCase("PENDING")) {
            return PENDING;
        } else if (text.equalsIgnoreCase("VERIFIED")) {
            return VERIFIED;
        } else if (text.equalsIgnoreCase("IN_PROGRESS")) {
            return IN_PROGRESS;
        } else if (text.equalsIgnoreCase("RESOLVED")) {
            return RESOLVED;
        } else {
            return null;
        }
    }
}
