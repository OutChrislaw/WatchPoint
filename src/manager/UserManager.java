package manager;

import model.User;
import model.Resident;
import model.Administrator;
import model.AreaOfficer;


import storage.FileManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * UserManager.java
 * Registration, login and account records, plus the resident sign-ups that are
 * still waiting for an Area Officer to accept or reject them.
 */
public class UserManager {

    private List<User> users;


    private FileManager fileManager;

    public UserManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.users = new ArrayList<User>();


        loadUsers();


    }

    public boolean registerResident(String fullName, String username, String password,
                                    String address, String contactNumber) {
        String problem = firstProblem(fullNameProblem(fullName), checkUsername(null, username),
                passwordProblem(password), contactNumberProblem(contactNumber),
                isBlank(address) ? "Please choose your city / municipality and barangay." : null);
        if (problem != null) {
            return false;
        }

        Resident resident = new Resident(generateUserId(), username.trim(), password,
                fullName.trim(), currentTimestamp(), address, contactNumber);
        users.add(resident);
        saveUsers();
        return true;
    }

    public User login(String loginId, String password) {
        if (loginId == null || password == null) {
            return null;
        }

        String id = loginId.trim();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            boolean matchesId = user.getUserId().equalsIgnoreCase(id);
            boolean matchesUsername = false;
            if (user instanceof Resident) {
                matchesUsername = ((Resident) user).getUsername().equalsIgnoreCase(id);
            }

            if ((matchesId || matchesUsername) && user.verifyPassword(password)) {
                return user;
            }
        }
        return null;
    }

    public User getUserById(String userId) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(userId)) {
                return users.get(i);
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<User>(users);
    }
    public boolean updateUser(User user) {
        if (user == null) {
            return false;
        }

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(user.getUserId())) {
                users.set(i, user);
                saveUsers();
                return true;
            }
        }
        return false;
    }

    public boolean deleteUser(String userId) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(userId)) {
                users.remove(i);
                saveUsers();
                return true;
            }
        }
        return false;
    }

    public String getFullNameById(String userId) {
        User user = getUserById(userId);
        if (user == null) {
            return "-";
        }
        return user.getFullName();
    }

    /**
     * The contact number stored on an account, so an officer or administrator
     * can follow up on a report. Returns "-" when the account is gone or has no
     * number.
     */
    public String getContactById(String userId) {
        User user = getUserById(userId);
        if (!(user instanceof Resident)) {
            return "-";
        }
        String contactNumber = ((Resident) user).getContactNumber();
        if (contactNumber == null || contactNumber.trim().length() == 0) {
            return "-";
        }
        return contactNumber;
    }

    public Administrator addAdministrator(String fullName, String password) {
        if (firstProblem(fullNameProblem(fullName), passwordProblem(password)) != null) {
            return null;
        }

        Administrator administrator = new Administrator(generateAdministratorId(), password,
                fullName.trim(), currentTimestamp());
        users.add(administrator);
        saveUsers();
        return administrator;
    }

    public AreaOfficer addAreaOfficer(String fullName, String password, String area) {
        if (firstProblem(fullNameProblem(fullName), passwordProblem(password),
                areaProblem(area)) != null) {
            return null;
        }

        AreaOfficer officer = new AreaOfficer(generateAreaOfficerId(), password, fullName.trim(),
                currentTimestamp(), area.trim());
        users.add(officer);
        saveUsers();
        return officer;
    }

    public String checkUsername(User user, String username) {
        if (isBlank(username)) {
            return "Please enter a username.";
        }

        String name = username.trim();
        String problem = firstProblem(lengthProblem(name, "username", 3, 40),
                hasForbiddenCharacter(name) ? FORBIDDEN_TEXT : null,
                name.matches("(?i)(u|ad|ao)\\d{6}")
                        ? "The username must not look like an account ID." : null);
        if (problem != null) {
            return problem;
        }

        boolean hasLetter = false;
        for (int i = 0; i < name.length(); i++) {
            char letter = name.charAt(i);
            if (isAsciiLetter(letter)) {
                hasLetter = true;
            } else if (!isDigit(letter) && "._@+-".indexOf(letter) < 0) {
                return "Use only letters, numbers, dot, underscore, at sign, plus or dash.";
            }
        }
        if (!hasLetter) {
            return "The username must contain at least one letter.";
        }

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

        return null;
    }

    public String saveDetails(User user, String fullName, String username, String contactNumber,
                              String barangay, String municipality) {
        if (user == null) {
            return "No account is signed in.";
        }

        boolean resident = user instanceof Resident;
        String problem = firstProblem(fullNameProblem(fullName),
                resident ? checkUsername(user, username) : null,
                resident ? contactNumberProblem(contactNumber) : null);
        if (problem != null) {
            return problem;
        }

        if (resident) {
            Resident saved = (Resident) user;
            saved.setUsername(username.trim());
            saved.setContactNumber(contactNumber.trim());
            if (!isBlank(barangay) && !isBlank(municipality)) {
                saved.setAddress(barangay.trim() + ", " + municipality.trim());
            }
        }
        user.setFullName(fullName.trim());

        return updateUser(user) ? null : "Your details could not be saved.";
    }

    public String changePassword(User user, String currentPassword, String newPassword,
                                 String confirmPassword) {
        if (user == null) {
            return "No account is signed in.";
        }
        if (currentPassword == null || !user.verifyPassword(currentPassword)) {
            return "Your current password is not correct.";
        }

        String problem = passwordProblem(newPassword);
        if (problem == null && newPassword.equals(currentPassword)) {
            problem = "The new password must be different from your current password.";
        }
        if (problem == null && !newPassword.equals(confirmPassword)) {
            problem = "The new passwords do not match.";
        }
        if (problem != null) {
            return problem;
        }

        user.setPassword(newPassword);
        return updateUser(user) ? null : "Your password could not be changed.";
    }
    public boolean saveAccountChanges(User user, String fullName, String password, String address,
                                      String contactNumber, String area) {
        if (user == null) {
            return false;
        }

        boolean resident = user instanceof Resident;
        boolean officer = user instanceof AreaOfficer;
        String problem = firstProblem(fullNameProblem(fullName),
                isBlank(password) ? null : passwordProblem(password),
                resident ? contactNumberProblem(contactNumber) : null,
                officer ? areaProblem(area) : null);
        if (problem != null) {
            return false;
        }

        user.setFullName(fullName.trim());
        if (!isBlank(password)) {
            user.setPassword(password);
        }
        if (resident) {
            Resident saved = (Resident) user;
            if (!isBlank(address)) {
                saved.setAddress(address.trim());
            }
            if (contactNumber != null) {
                saved.setContactNumber(contactNumber.trim());
            }
        }
        if (officer && !isBlank(area)) {
            ((AreaOfficer) user).setArea(area.trim());
        }

        return updateUser(user);
    }

    /**
     * The rules for a full name. Returns the problem to show, or null when the
     * name is acceptable.
     */
    public static String fullNameProblem(String fullName) {
        if (isBlank(fullName)) {
            return "Please enter the full name.";
        }

        String name = fullName.trim();
        String problem = firstProblem(lengthProblem(name, "full name", 2, 60),
                hasForbiddenCharacter(name) ? FORBIDDEN_TEXT : null);
        if (problem != null) {
            return problem;
        }

        boolean hasLetter = false;
        for (int i = 0; i < name.length(); i++) {
            char letter = name.charAt(i);
            if (isAsciiLetter(letter)) {
                hasLetter = true;
            } else if (!(letter == ' ' || letter == '.' || letter == '\''
                    || letter == '-' || letter == ',')) {
                return "Use only letters, space, dot, apostrophe, dash or comma in the full name.";
            }
        }
        return hasLetter ? null : "The full name must contain letters.";
    }

    /**
     * The password rules: 8 to 64 characters, no spaces and no pipe character.
     */
    public static String passwordProblem(String password) {
        if (password == null || password.length() == 0) {
            return "Please enter a password.";
        }

        String problem = lengthProblem(password, "password", 8, 64);
        if (problem != null) {
            return problem;
        }
        if (password.indexOf(' ') >= 0) {
            return "The password must not contain spaces.";
        }
        return hasForbiddenCharacter(password)
                ? "The password must not contain the | character or line breaks." : null;
    }

    /**
     * The contact number rules: exactly 11 digits and it must start with 09.
     */
    public static String contactNumberProblem(String contactNumber) {
        if (isBlank(contactNumber)) {
            return "Please enter the contact number.";
        }

        String number = contactNumber.trim();
        for (int i = 0; i < number.length(); i++) {
            if (!isDigit(number.charAt(i))) {
                return "The contact number must contain digits only.";
            }
        }
        if (number.length() != 11) {
            return "The contact number must be exactly 11 digits.";
        }
        return number.startsWith("09") ? null
                : "The contact number must start with 09, like 09171234567.";
    }

    /**
     * The assigned area of an area officer, which is a city/municipality name.
     */
    public static String areaProblem(String area) {
        if (isBlank(area)) {
            return "Please select the area this officer handles.";
        }

        String name = area.trim();
        return firstProblem(maxProblem(name, "area", 60),
                hasForbiddenCharacter(name) ? FORBIDDEN_TEXT : null);
    }

    // ------------------------------------------------------------------- helpers

    private static final String FORBIDDEN_TEXT =
            "The | character and line breaks cannot be used here.";

    /** True when a value is null or only spaces. */
    private static boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }

    /** True when a value holds a pipe or a line break, which the data files cannot store. */
    private static boolean hasForbiddenCharacter(String value) {
        return value != null
                && (value.indexOf('|') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0);
    }

    /** The first message in the list, or null when every value is null. */
    private static String firstProblem(String... problems) {
        for (int i = 0; i < problems.length; i++) {
            if (problems[i] != null) {
                return problems[i];
            }
        }
        return null;
    }

    /** The "at least / at most" rule for a text value, or null. */
    private static String lengthProblem(String text, String caption, int min, int max) {
        if (text.length() < min) {
            return "The " + caption + " must be at least " + min + " characters.";
        }
        if (text.length() > max) {
            return "The " + caption + " must be " + max + " characters or fewer.";
        }
        return null;
    }

    /** The "not more than" rule for a text value, or null. */
    private static String maxProblem(String text, String caption, int max) {
        return text.length() > max
                ? "The " + caption + " must be " + max + " characters or fewer." : null;
    }

    private static boolean isAsciiLetter(char letter) {
        return (letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z');
    }

    private static boolean isDigit(char letter) {
        return letter >= '0' && letter <= '9';
    }

    public int countAdministrators() {
        int total = 0;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i) instanceof Administrator) {
                total++;
            }
        }
        return total;
    }

    public static String municipalityOf(String address) {
        if (address == null) {
            return "";
        }
        int comma = address.lastIndexOf(',');
        if (comma < 0) {
            return address.trim();
        }
        return address.substring(comma + 1).trim();
    }

    public static String barangayOf(String address) {
        if (address == null) {
            return "";
        }
        int comma = address.lastIndexOf(',');
        if (comma < 0) {
            return "";
        }
        return address.substring(0, comma).trim();
    }
    public List<User> searchUsers(String keyword) {
        return searchUsers(keyword, null);
    }

    public List<User> searchUsers(String keyword, String userType) {
        String type = "";
        if (userType != null) {
            type = userType.trim().replace(" ", "");
        }
        boolean allTypes = type.length() == 0 || type.equalsIgnoreCase("AllTypes");

        String text = "";
        if (keyword != null) {
            text = keyword.trim().toLowerCase();
        }

        List<User> result = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            if (!allTypes && !user.getUserType().equalsIgnoreCase(type)) {
                continue;
            }

            boolean matches = text.length() == 0
                    || user.getUserId().toLowerCase().contains(text)
                    || user.getFullName().toLowerCase().contains(text);

            if (user instanceof Resident && text.length() > 0) {
                matches = matches || ((Resident) user).getUsername().toLowerCase().contains(text);
            }

            if (matches) {
                result.add(user);
            }
        }
        return result;
    }


    private void loadUsers() {
        List<String> lines = fileManager.readLines("users.txt");
        for (int i = 0; i < lines.size(); i++) {
            User user = parseUser(lines.get(i));
            if (user != null) {
                users.add(user);
            }
        }
    }

    private void saveUsers() {
        List<String> lines = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            lines.add(users.get(i).toFileString());
        }
        fileManager.writeLines("users.txt", lines);
    }    private User parseUser(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) {
            return null;
        }

        String type = parts[0];
        String userId = parts[1];
        String password = parts[2];
        String fullName = parts[3];

        String dateCreated = "";
        if (parts.length > 4) {
            dateCreated = parts[4];
        }

        if (type.equals("Resident")) {
            String username = "";
            String address = "";
            String contactNumber = "";
            if (parts.length > 5) {
                username = parts[5];
            }
            if (parts.length > 6) {
                address = parts[6];
            }
            if (parts.length > 7) {
                contactNumber = parts[7];
            }
            return new Resident(userId, username, password, fullName, dateCreated, address,
                    contactNumber);
        }
        if (type.equals("Administrator")) {
            return new Administrator(userId, password, fullName, dateCreated);
        }
        if (type.equals("AreaOfficer")) {
            String area = "";
            if (parts.length > 5) {
                area = parts[5];
            }
            return new AreaOfficer(userId, password, fullName, dateCreated, area);
        }
        return null;
    }
    private static String formatId(String prefix, int number) {
        String digits = String.valueOf(number);
        while (digits.length() < 6) {
            digits = "0" + digits;
        }
        return prefix + digits;
    }

    private String generateUserId() {
        int highest = 0;
        for (int i = 0; i < users.size(); i++) {
            String userId = users.get(i).getUserId();
            if (userId != null && userId.startsWith("U")) {
                try {
                    int number = Integer.parseInt(userId.substring(1));
                    if (number > highest) {
                        highest = number;
                    }
                } catch (Exception e) {
                    // ids that are not in the U<number> format are ignored
                }
            }
        }
        return formatId("U", highest + 1);
    }

    private String generateAreaOfficerId() {
        int highest = 0;
        for (int i = 0; i < users.size(); i++) {
            String userId = users.get(i).getUserId();
            if (userId != null && userId.startsWith("AO")) {
                try {
                    int number = Integer.parseInt(userId.substring(2));
                    if (number > highest) {
                        highest = number;
                    }
                } catch (Exception e) {
                    // ids that are not in the AO<number> format are ignored
                }
            }
        }

        return formatId("AO", highest + 1);
    }


    private String generateAdministratorId() {
        int highest = 0;
        for (int i = 0; i < users.size(); i++) {
            String userId = users.get(i).getUserId();
            if (userId != null && userId.startsWith("AD")) {
                try {
                    int number = Integer.parseInt(userId.substring(2));
                    if (number > highest) {
                        highest = number;
                    }
                } catch (Exception e) {
                    // ids that are not in the AD<number> format are ignored
                }
            }
        }

        return formatId("AD", highest + 1);
    }


    private String currentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }
}