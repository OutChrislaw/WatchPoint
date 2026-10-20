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
        if (fullNameProblem(fullName) != null || checkUsername(null, username) != null
                || passwordProblem(password) != null || contactNumberProblem(contactNumber) != null
                || address == null || address.trim().length() == 0) {
            return false;
        }
        if (isUsernameTaken(username)) {
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

    public Administrator addAdministrator(String fullName, String password) {
        if (fullNameProblem(fullName) != null || passwordProblem(password) != null) {
            return null;
        }

        Administrator administrator = new Administrator(generateAdministratorId(), password,
                fullName.trim(), currentTimestamp());
        users.add(administrator);
        saveUsers();
        return administrator;
    }

    public AreaOfficer addAreaOfficer(String fullName, String password, String area) {
        if (fullNameProblem(fullName) != null || passwordProblem(password) != null
                || areaProblem(area) != null) {
            return null;
        }

        AreaOfficer officer = new AreaOfficer(generateAreaOfficerId(), password, fullName.trim(),
                currentTimestamp(), area.trim());
        users.add(officer);
        saveUsers();
        return officer;
    }

    public String checkUsername(User user, String username) {
        if (username == null || username.trim().length() == 0) {
            return "Please enter a username.";
        }

        String name = username.trim();
        if (name.length() < 3) {
            return "The username must be at least 3 characters.";
        }
        if (name.length() > 40) {
            return "The username must be 40 characters or fewer.";
        }

        String forbidden = forbiddenCharacterProblem(name);
        if (forbidden != null) {
            return forbidden;
        }
        if (looksLikeAccountId(name)) {
            return "The username must not look like an account ID.";
        }

        boolean hasLetter = false;
        for (int i = 0; i < name.length(); i++) {
            char letter = name.charAt(i);
            boolean allowed = (letter >= 'a' && letter <= 'z')
                    || (letter >= 'A' && letter <= 'Z')
                    || (letter >= '0' && letter <= '9')
                    || letter == '.' || letter == '_' || letter == '@'
                    || letter == '+' || letter == '-';
            if (!allowed) {
                return "Use only letters, numbers, dot, underscore, at sign, plus or dash.";
            }
            if ((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z')) {
                hasLetter = true;
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
            if (!(other instanceof Resident)) {
                continue;
            }
            if (((Resident) other).getUsername().equalsIgnoreCase(name)) {
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

        String nameProblem = fullNameProblem(fullName);
        if (nameProblem != null) {
            return nameProblem;
        }

        if (user instanceof Resident) {
            String problem = checkUsername(user, username);
            if (problem != null) {
                return problem;
            }

            String contactProblem = contactNumberProblem(contactNumber);
            if (contactProblem != null) {
                return contactProblem;
            }

            Resident resident = (Resident) user;
            resident.setUsername(username.trim());
            if (contactNumber != null) {
                resident.setContactNumber(contactNumber.trim());
            }
            if (barangay != null && municipality != null
                    && barangay.trim().length() > 0 && municipality.trim().length() > 0) {
                resident.setAddress(barangay.trim() + ", " + municipality.trim());
            }
        }

        user.setFullName(fullName.trim());

        if (updateUser(user)) {
            return null;
        }
        return "Your details could not be saved.";
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
        if (problem != null) {
            return problem;
        }
        if (newPassword.equals(currentPassword)) {
            return "The new password must be different from your current password.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "The new passwords do not match.";
        }

        user.setPassword(newPassword);
        if (updateUser(user)) {
            return null;
        }
        return "Your password could not be changed.";
    }
    public boolean saveAccountChanges(User user, String fullName, String password, String address,
                                      String contactNumber, String area) {
        if (user == null || fullNameProblem(fullName) != null) {
            return false;
        }
        if (password != null && password.trim().length() > 0 && passwordProblem(password) != null) {
            return false;
        }
        if (user instanceof Resident && contactNumberProblem(contactNumber) != null) {
            return false;
        }
        if (user instanceof AreaOfficer && areaProblem(area) != null) {
            return false;
        }

        user.setFullName(fullName.trim());
        if (password != null && password.trim().length() > 0) {
            user.setPassword(password);
        }

        if (user instanceof Resident) {
            Resident resident = (Resident) user;
            if (address != null && address.trim().length() > 0) {
                resident.setAddress(address.trim());
            }
            if (contactNumber != null) {
                resident.setContactNumber(contactNumber.trim());
            }
        }
        if (user instanceof AreaOfficer) {
            AreaOfficer officer = (AreaOfficer) user;
            if (area != null && area.trim().length() > 0) {
                officer.setArea(area.trim());
            }
        }

        return updateUser(user);
    }

    /**
     * The rules for a full name. Returns the problem to show, or null when the
     * name is acceptable.
     */
    public static String fullNameProblem(String fullName) {
        if (fullName == null || fullName.trim().length() == 0) {
            return "Please enter the full name.";
        }

        String name = fullName.trim();
        if (name.length() < 2) {
            return "The full name must be at least 2 characters.";
        }
        if (name.length() > 60) {
            return "The full name must be 60 characters or fewer.";
        }

        String forbidden = forbiddenCharacterProblem(name);
        if (forbidden != null) {
            return forbidden;
        }

        boolean hasLetter = false;
        for (int i = 0; i < name.length(); i++) {
            char letter = name.charAt(i);
            if ((letter >= 'a' && letter <= 'z') || (letter >= 'A' && letter <= 'Z')) {
                hasLetter = true;
            } else if (!(letter == ' ' || letter == '.' || letter == '\''
                    || letter == '-' || letter == ',')) {
                return "Use only letters, space, dot, apostrophe, dash or comma in the full name.";
            }
        }
        if (!hasLetter) {
            return "The full name must contain letters.";
        }
        return null;
    }

    /**
     * The password rules: 8 to 64 characters, no spaces and no pipe character.
     */
    public static String passwordProblem(String password) {
        if (password == null || password.length() == 0) {
            return "Please enter a password.";
        }
        if (password.length() < 8) {
            return "The password must be at least 8 characters.";
        }
        if (password.length() > 64) {
            return "The password must be 64 characters or fewer.";
        }

        for (int i = 0; i < password.length(); i++) {
            char letter = password.charAt(i);
            if (letter == ' ') {
                return "The password must not contain spaces.";
            }
            if (letter == '|' || letter == '\n' || letter == '\r') {
                return "The password must not contain the | character or line breaks.";
            }
        }
        return null;
    }

    /**
     * The contact number rules: exactly 11 digits and it must start with 09.
     */
    public static String contactNumberProblem(String contactNumber) {
        if (contactNumber == null || contactNumber.trim().length() == 0) {
            return "Please enter the contact number.";
        }

        String number = contactNumber.trim();
        for (int i = 0; i < number.length(); i++) {
            char digit = number.charAt(i);
            if (digit < '0' || digit > '9') {
                return "The contact number must contain digits only.";
            }
        }
        if (number.length() != 11) {
            return "The contact number must be exactly 11 digits.";
        }
        if (!number.startsWith("09")) {
            return "The contact number must start with 09, like 09171234567.";
        }
        return null;
    }

    /**
     * The assigned area of an area officer, which is a city/municipality name.
     */
    public static String areaProblem(String area) {
        if (area == null || area.trim().length() == 0) {
            return "Please select the area this officer handles.";
        }

        String name = area.trim();
        if (name.length() > 60) {
            return "The area must be 60 characters or fewer.";
        }
        return forbiddenCharacterProblem(name);
    }

    private static String forbiddenCharacterProblem(String value) {
        if (value == null) {
            return null;
        }
        for (int i = 0; i < value.length(); i++) {
            char letter = value.charAt(i);
            if (letter == '|' || letter == '\n' || letter == '\r') {
                return "The | character and line breaks cannot be used here.";
            }
        }
        return null;
    }

    private static boolean looksLikeAccountId(String text) {
        String upper = text.toUpperCase();
        int digitsStart;
        if (upper.startsWith("AD") || upper.startsWith("AO")) {
            digitsStart = 2;
        } else if (upper.startsWith("U")) {
            digitsStart = 1;
        } else {
            return false;
        }
        if (upper.length() != digitsStart + 6) {
            return false;
        }
        for (int i = digitsStart; i < upper.length(); i++) {
            char digit = upper.charAt(i);
            if (digit < '0' || digit > '9') {
                return false;
            }
        }
        return true;
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

    private boolean isUsernameTaken(String username) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user instanceof Resident
                    && ((Resident) user).getUsername().equalsIgnoreCase(username.trim())) {
                return true;
            }
        }
        return false;
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