package validation;

public class LocationRules {

    private LocationRules() {
    }

    public static String selectionProblem(String cityMunicipality, String barangay) {
        if (TextRules.isBlank(cityMunicipality) || TextRules.isBlank(barangay)) {
            return "Please select a City / Municipality and a Barangay.";
        }
        return null;
    }
}
