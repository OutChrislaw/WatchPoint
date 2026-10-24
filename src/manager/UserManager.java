package manager;

import model.User;
import model.Resident;
import model.Administrator;
import model.AreaOfficer;

import storage.FileManager;
import validation.TextRules;
import validation.UserRules;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private List<User> users;

    private FileManager fileManager;

    public UserManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.users = new ArrayList<User>();

        loadUsers();
        fileManager.reserveNumber("U", highestUserNumber("U"));
        fileManager.reserveNumber("AD", highestUserNumber("AD"));
        fileManager.reserveNumber("AO", highestUserNumber("AO"));
    }

    public boolean registerResident(String fullName, String username, String password,
                                    String address, String contactNumber) {
        String problem = TextRules.firstProblem(UserRules.fullNameProblem(fullName),
                checkUsername(null, username), UserRules.passwordProblem(password),
                UserRules.contactNumberProblem(contactNumber),
                TextRules.isBlank(address)
                        ? "Please choose your city / municipality and barangay." : null);
        if (problem != null) {
            return false;
        }

        String userId = generateUserId();
        if (userId == null) {
            return false;
        }

        Resident resident = new Resident(userId, username.trim(), password,
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

    public Administrator addAdministrator(String fullName, String password) {
        if (TextRules.firstProblem(UserRules.fullNameProblem(fullName),
                UserRules.passwordProblem(password)) != null) {
            return null;
        }

        String administratorId = generateAdministratorId();
        if (administratorId == null) {
            return null;
        }

        Administrator administrator = new Administrator(administratorId, password,
                fullName.trim(), currentTimestamp());
        users.add(administrator);
        saveUsers();
        return administrator;
    }

    public AreaOfficer addAreaOfficer(String fullName, String password, String area) {
        if (TextRules.firstProblem(UserRules.fullNameProblem(fullName),
                UserRules.passwordProblem(password), UserRules.areaProblem(area)) != null) {
            return null;
        }

        String officerId = generateAreaOfficerId();
        if (officerId == null) {
            return null;
        }

        AreaOfficer officer = new AreaOfficer(officerId, password, fullName.trim(),
                currentTimestamp(), area.trim());
        users.add(officer);
        saveUsers();
        return officer;
    }

    public String checkUsername(User user, String username) {
        return UserRules.usernameProblem(user, username, users);
    }

    public String saveDetails(User user, String fullName, String username, String contactNumber,
                              String barangay, String municipality) {
        if (user == null) {
            return "No account is signed in.";
        }

        boolean resident = user instanceof Resident;
        String problem = TextRules.firstProblem(UserRules.fullNameProblem(fullName),
                resident ? checkUsername(user, username) : null,
                resident ? UserRules.contactNumberProblem(contactNumber) : null);
        if (problem != null) {
            return problem;
        }

        if (resident) {
            Resident saved = (Resident) user;
            saved.setUsername(username.trim());
            saved.setContactNumber(contactNumber.trim());
            if (!TextRules.isBlank(barangay) && !TextRules.isBlank(municipality)) {
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

        String problem = UserRules.passwordProblem(newPassword);
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
        String problem = TextRules.firstProblem(UserRules.fullNameProblem(fullName),
                TextRules.isBlank(password) ? null : UserRules.passwordProblem(password),
                resident ? UserRules.contactNumberProblem(contactNumber) : null,
                officer ? UserRules.areaProblem(area) : null);
        if (problem != null) {
            return false;
        }

        user.setFullName(fullName.trim());
        if (!TextRules.isBlank(password)) {
            user.setPassword(password);
        }
        if (resident) {
            Resident saved = (Resident) user;
            if (!TextRules.isBlank(address)) {
                saved.setAddress(address.trim());
            }
            if (contactNumber != null) {
                saved.setContactNumber(contactNumber.trim());
            }
        }
        if (officer && !TextRules.isBlank(area)) {
            ((AreaOfficer) user).setArea(area.trim());
        }

        return updateUser(user);
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
        return idFor("U");
    }

    private String generateAreaOfficerId() {
        return idFor("AO");
    }

    private String generateAdministratorId() {
        return idFor("AD");
    }

    private String idFor(String prefix) {
        int number = fileManager.nextNumber(prefix);
        if (number < 0) {
            return null;
        }
        return formatId(prefix, number);
    }

    private int highestUserNumber(String prefix) {
        int highest = 0;
        for (int i = 0; i < users.size(); i++) {
            int number = numberOf(users.get(i).getUserId(), prefix);
            if (number > highest) {
                highest = number;
            }
        }
        return highest;
    }

    private static int numberOf(String id, String prefix) {
        if (id == null || !id.startsWith(prefix)) {
            return 0;
        }
        try {
            return Integer.parseInt(id.substring(prefix.length()));
        } catch (Exception e) {
            return 0;
        }
    }

    private String currentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }
}
