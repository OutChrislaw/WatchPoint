package validation;

/**
 * LocationRules.java
 * The one location rule a form needs on its own: both boxes must be chosen. The
 * report location rules live in ReportRules, because they also cover the street,
 * the specific place and the description.
 */
public class LocationRules {

    private LocationRules() {
    }

    /** Sign-up and the account edit form need a city / municipality and a barangay. */
    public static String selectionProblem(String cityMunicipality, String barangay) {
        if (TextRules.isBlank(cityMunicipality) || TextRules.isBlank(barangay)) {
            return "Please select a City / Municipality and a Barangay.";
        }
        return null;
    }
}