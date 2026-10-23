package validation;

import model.Administrator;
import model.Resident;
import model.User;

import java.util.List;

/**
 * UserRules.java
 * Every rule for an account: the fields the person types, the login form and the
 * rules that decide whether an account may be deleted. The managers and the
 * windows both call these methods, so every message lives in one place.
 */
public class UserRules {

    private UserRules() {
    }

    /**
     * The rules for a full name. Returns the problem to show, or null when the
     * name is acceptable.
     */
    public static String fullNameProblem(String fullName) {
        if (TextRules.isBlank(fullName)) {
            return "Please enter the full name.";
        }

        String name = fullName.trim();
        String problem = TextRules.firstProblem(TextRules.lengthProblem(name, "full name", 2, 60),
                TextRules.hasForbiddenCharacter(name) ? TextRules.FORBIDDEN_TEXT : null);
        if (problem != null) {
            return problem;
        }

        boolean hasLetter = false;
        for (int i = 0; i < name.length(); i++) {
            char letter = name.charAt(i);
            if (TextRules.isAsciiLetter(letter)) {
                hasLetter = true;
            } else if (!(letter == ' ' || letter == '.' || letter == '\''
                    || letter == '-' || letter == ',')) {
                return "Use only letters, space, dot, apostrophe, dash or comma in the full name.";
            }
        }
        return hasLetter ? null : "The full name must contain letters.";
    }

    /** The password rules: 8 to 64 characters, no spaces and no pipe character. */
    public static String passwordProblem(String password) {
        if (password == null || password.length() == 0) {
            return "Please enter a password.";
        }

        String problem = TextRules.lengthProblem(password, "password", 8, 64);
        if (problem != null) {
            return problem;
        }
        if (password.indexOf(' ') >= 0) {
            return "The password must not contain spaces.";
        }
        return TextRules.hasForbiddenCharacter(password)
                ? "The password must not contain the | character or line breaks." : null;
    }

    /** The password is typed twice on every form that sets one, so both must match. */
    public static String passwordConfirmProblem(String password, String confirmPassword) {
        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            return "The two passwords do not match.";
        }
        return null;
    }

    /** The contact number rules: exactly 11 digits and it must start with 09. */
    public static String contactNumberProblem(String contactNumber) {
        if (TextRules.isBlank(contactNumber)) {
            return "Please enter the contact number.";
        }

        String number = contactNumber.trim();
        for (int i = 0; i < number.length(); i++) {
            if (!TextRules.isDigit(number.charAt(i))) {
                return "The contact number must contain digits only.";
            }
        }
        if (number.length() != 11) {
            return "The contact number must be exactly 11 digits.";
        }
        return number.startsWith("09") ? null
                : "The contact number must start with 09, like 09171234567.";
    }

    /** The assigned area of an area officer, which is a city/municipality name. */
    public static String areaProblem(String area) {
        if (TextRules.isBlank(area)) {
            return "Please select the area this officer handles.";
        }

        String name = area.trim();
        return TextRules.firstProblem(TextRules.maxProblem(name, "area", 60),
                TextRules.hasForbiddenCharacter(name) ? TextRules.FORBIDDEN_TEXT : null);
    }
    /**
     * The username rules: shape, allowed characters, an id-like string and
     * uniqueness. The account being edited may be null for a new sign-up, and the
     * account list may be null when only the shape matters.
     */
    public static String usernameProblem(User user, String username, List<User> users) {
        if (TextRules.isBlank(username)) {
            return "Please enter a username.";
        }

        String name = username.trim();
        String problem = TextRules.firstProblem(TextRules.lengthProblem(name, "username", 3, 40),
                TextRules.hasForbiddenCharacter(name) ? TextRules.FORBIDDEN_TEXT : null,
                name.matches("(?i)(u|ad|ao)\\d{6}")
                        ? "The username must not look like an account ID." : null);
        if (problem != null) {
            return problem;
        }

        boolean hasLetter = false;
        for (int i = 0; i < name.length(); i++) {
            char letter = name.charAt(i);
            if (TextRules.isAsciiLetter(letter)) {
                hasLetter = true;
            } else if (!TextRules.isDigit(letter) && "._@+-".indexOf(letter) < 0) {
                return "Use only letters, numbers, dot, underscore, at sign, plus or dash.";
            }
        }
        if (!hasLetter) {
            return "The username must contain at least one letter.";
        }

        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                User other = users.get(i);
                if (user != null && other.getUserId().equals(user.getUserId())) {
                    continue;
                }
                if (other.getUserId().equalsIgnoreCase(name)) {
                    return "That username is already used as an account ID.";
                }
                if (other instanceof Resident
                        && ((Resident) other).getUsername().equalsIgnoreCase(name)) {
                    return "That username is already taken.";
                }
            }
        }

        return null;
    }

    /** The login form needs both a username / id and a password. */
    public static String loginProblem(String loginId, String password) {
        if (TextRules.isBlank(loginId) || password == null || password.length() == 0) {
            return "Please enter your username or ID and password.";
        }
        return null;
    }

    /**
     * An administrator may not delete the account they are signed in with, and the
     * last remaining administrator cannot be deleted at all.
     */
    public static String deletionProblem(User target, User signedIn, int administratorCount) {
        if (target == null) {
            return "Please select a user first.";
        }
        if (signedIn != null && target.getUserId().equals(signedIn.getUserId())) {
            return "You cannot delete the account you are signed in with.";
        }
        if (target instanceof Administrator && administratorCount <= 1) {
            return "The last Administrator cannot be deleted.";
        }
        return null;
    }
}