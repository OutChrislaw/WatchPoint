package manager;

import model.User;
import model.Resident;
import model.Administrator;
import model.AreaOfficer;
import model.RegistrationRequest;

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
    private List<RegistrationRequest> requests;

    private FileManager fileManager;

    public UserManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.users = new ArrayList<User>();
        this.requests = new ArrayList<RegistrationRequest>();

        loadUsers();
        loadRequests();

    }

    public boolean registerResident(String fullName, String username, String password,
                                    String address, String contactNumber) {
        if (fullName == null || fullName.trim().length() == 0
                || username == null || username.trim().length() == 0
                || password == null || password.trim().length() == 0) {
            return false;
        }
        if (isUsernameTaken(username)) {
            return false;
        }

        Resident resident = new Resident(generateUserId(), username.trim(), password,
                fullName.trim(), address, contactNumber);
        users.add(resident);
        saveUsers();
        return true;
    }

    public boolean requestRegistration(String fullName, String username, String password,
                                       String barangay, String municipality, String contactNumber) {
        if (fullName == null || fullName.trim().length() == 0
                || username == null || username.trim().length() == 0
                || password == null || password.trim().length() == 0
                || barangay == null || barangay.trim().length() == 0
                || municipality == null || municipality.trim().length() == 0) {
            return false;
        }
        if (isUsernameTaken(username) || isUsernamePending(username)) {
            return false;
        }

        String contact = "";
        if (contactNumber != null) {
            contact = contactNumber.trim();
        }

        requests.add(new RegistrationRequest(fullName.trim(), username.trim(), password, contact,
                barangay.trim(), municipality.trim(), currentTimestamp()));
        saveRequests();
        return true;
    }

    public List<RegistrationRequest> getPendingRegistrations(String area) {
        List<RegistrationRequest> result = new ArrayList<RegistrationRequest>();
        for (int i = 0; i < requests.size(); i++) {
            RegistrationRequest request = requests.get(i);
            if (area == null || request.getMunicipality().equals(area)) {
                result.add(request);
            }
        }
        return result;
    }

    public RegistrationRequest getPendingRegistration(String username) {
        if (username == null) {
            return null;
        }
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getUsername().equalsIgnoreCase(username.trim())) {
                return requests.get(i);
            }
        }
        return null;
    }

    public boolean isUsernamePending(String username) {
        return getPendingRegistration(username) != null;
    }

    public boolean approveRegistration(RegistrationRequest request) {
        if (request == null) {
            return false;
        }
        if (!registerResident(request.getFullName(), request.getUsername(), request.getPassword(),
                request.getAddress(), request.getContactNumber())) {
            return false;
        }

        removeRequest(request);
        saveRequests();
        return true;
    }

    public boolean rejectRegistration(RegistrationRequest request) {
        if (request == null) {
            return false;
        }
        if (!removeRequest(request)) {
            return false;
        }
        saveRequests();
        return true;
    }

    private boolean removeRequest(RegistrationRequest request) {
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i) == request) {
                requests.remove(i);
                return true;
            }
        }
        return false;
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
        if (fullName == null || fullName.trim().length() == 0
                || password == null || password.trim().length() == 0) {
            return null;
        }

        Administrator administrator = new Administrator(generateUserId(), password, fullName.trim());
        users.add(administrator);
        saveUsers();
        return administrator;
    }

    public AreaOfficer addAreaOfficer(String fullName, String password, String area) {
        if (fullName == null || fullName.trim().length() == 0
                || password == null || password.trim().length() == 0
                || area == null || area.trim().length() == 0) {
            return null;
        }

        AreaOfficer officer = new AreaOfficer(generateAreaOfficerId(), password, fullName.trim(),
                area.trim());
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
        }

        for (int i = 0; i < users.size(); i++) {
            User other = users.get(i);
            if (!(other instanceof Resident)) {
                continue;
            }
            if (user != null && other.getUserId().equals(user.getUserId())) {
                continue;
            }
            if (((Resident) other).getUsername().equalsIgnoreCase(name)) {
                return "That username is already taken.";
            }
        }

        if (isUsernamePending(name)) {
            return "That username is waiting for approval.";
        }

        return null;
    }

    public String saveDetails(User user, String fullName, String username, String contactNumber,
                              String barangay, String municipality) {
        if (user == null || fullName == null || fullName.trim().length() == 0) {
            return "Please enter your full name.";
        }

        if (user instanceof Resident) {
            String problem = checkUsername(user, username);
            if (problem != null) {
                return problem;
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
        if (newPassword == null || newPassword.length() < 6) {
            return "The new password must be at least 6 characters.";
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
        if (user == null || fullName == null || fullName.trim().length() == 0) {
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

        if (type.equals("Resident")) {
            String username = "";
            String address = "";
            String contactNumber = "";
            if (parts.length > 4) {
                username = parts[4];
            }
            if (parts.length > 5) {
                address = parts[5];
            }
            if (parts.length > 6) {
                contactNumber = parts[6];
            }
            return new Resident(userId, username, password, fullName, address, contactNumber);
        }
        if (type.equals("Administrator")) {
            return new Administrator(userId, password, fullName);
        }
        if (type.equals("AreaOfficer")) {
            String area = "";
            if (parts.length > 4) {
                area = parts[4];
            }
            return new AreaOfficer(userId, password, fullName, area);
        }
        return null;
    }
    private void loadRequests() {
        List<String> lines = fileManager.readLines("registrations.txt");
        for (int i = 0; i < lines.size(); i++) {
            RegistrationRequest request = parseRequest(lines.get(i));
            if (request != null) {
                requests.add(request);
            }
        }
    }

    private void saveRequests() {
        List<String> lines = new ArrayList<String>();
        for (int i = 0; i < requests.size(); i++) {
            lines.add(requests.get(i).toFileString());
        }
        fileManager.writeLines("registrations.txt", lines);
    }

    private RegistrationRequest parseRequest(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) {
            return null;
        }
        return new RegistrationRequest(parts[0], parts[1], parts[2], parts[3],
                parts[4], parts[5], parts[6]);
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
        return "U" + (highest + 1);
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

        String digits = String.valueOf(highest + 1);
        while (digits.length() < 3) {
            digits = "0" + digits;
        }
        return "AO" + digits;
    }

    private String currentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.now().format(formatter);
    }
}