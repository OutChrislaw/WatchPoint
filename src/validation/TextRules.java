package validation;

public class TextRules {

    static final String FORBIDDEN_TEXT =
            "The | character and line breaks cannot be used here.";

    private TextRules() {
    }

    public static String oneLine(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    static boolean hasForbiddenCharacter(String value) {
        return value != null
                && (value.indexOf('|') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0);
    }

    static boolean hasPipe(String text) {
        return text != null && text.indexOf('|') >= 0;
    }

    public static String firstProblem(String... problems) {
        for (int i = 0; i < problems.length; i++) {
            if (problems[i] != null) {
                return problems[i];
            }
        }
        return null;
    }

    static String lengthProblem(String text, String caption, int min, int max) {
        if (text.length() < min) {
            return "The " + caption + " must be at least " + min + " characters.";
        }
        if (text.length() > max) {
            return "The " + caption + " must be " + max + " characters or fewer.";
        }
        return null;
    }

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
