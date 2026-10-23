package validation;

/**
 * TextRules.java
 * Small text helpers shared by every rule in this package. Only oneLine(...) is
 * public; the rest is package private because the rest of the system talks to
 * the rule classes, not to these building blocks.
 */
public class TextRules {

    static final String FORBIDDEN_TEXT =
            "The | character and line breaks cannot be used here.";

    private TextRules() {
    }

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

    /** True when a value is null or only spaces. */
    public static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    /** True when a value holds a pipe or a line break, which the data files cannot store. */
    static boolean hasForbiddenCharacter(String value) {
        return value != null
                && (value.indexOf('|') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0);
    }

    /** True when the text holds the pipe character. */
    static boolean hasPipe(String text) {
        return text != null && text.indexOf('|') >= 0;
    }

    /** The first message in the list, or null when every value is null. */
    public static String firstProblem(String... problems) {
        for (int i = 0; i < problems.length; i++) {
            if (problems[i] != null) {
                return problems[i];
            }
        }
        return null;
    }

    /** The "at least / at most" rule for a text value, or null. */
    static String lengthProblem(String text, String caption, int min, int max) {
        if (text.length() < min) {
            return "The " + caption + " must be at least " + min + " characters.";
        }
        if (text.length() > max) {
            return "The " + caption + " must be " + max + " characters or fewer.";
        }
        return null;
    }

    /** The "not more than" rule for a text value, or null. */
    static String maxProblem(String text, String caption, int max) {
        return text.length() > max
                ? "The " + caption + " must be " + max + " characters or fewer." : null;
    }

    static boolean isAsciiLetter(char letter) {
        return (letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z');
    }

    static boolean isDigit(char letter) {
        return letter >= '0' && letter <= '9';
    }
}