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

    public User login(String username, String password) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getUsername().equals(username) && user.verifyPassword(password)) {
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
    }    public boolean addUser(User user) {
        if (user == null) {
            return false;
        }
        if (isUsernameTaken(user.getUsername())) {
            return false;
        }

        users.add(user);
        saveUsers();
        return true;
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

    public boolean addAdministrator(String fullName, String username, String password) {
        if (fullName == null || fullName.trim().length() == 0
                || username == null || username.trim().length() == 0
                || password == null || password.trim().length() == 0) {
            return false;
        }
        if (isUsernameTaken(username)) {
            return false;
        }

        users.add(new Administrator(generateUserId(), username.trim(), password, fullName.trim()));
        saveUsers();
        return true;
    }

    public boolean addAreaOfficer(String fullName, String username, String password, String area) {
        if (fullName == null || fullName.trim().length() == 0
                || username == null || username.trim().length() == 0
                || password == null || password.trim().length() == 0
                || area == null || area.trim().length() == 0) {
            return false;
        }
        if (isUsernameTaken(username)) {
            return false;
        }

        users.add(new AreaOfficer(generateAreaOfficerId(), username.trim(), password,
                fullName.trim(), area.trim()));
        saveUsers();
        return true;
    }

    public boolean updateAreaOfficer(AreaOfficer officer, String fullName, String password,
                                     String area) {
        if (officer == null || fullName == null || fullName.trim().length() == 0
                || password == null || password.trim().length() == 0
                || area == null || area.trim().length() == 0) {
            return false;
        }

        AreaOfficer updated = new AreaOfficer(officer.getUserId(), officer.getUsername(), password,
                fullName.trim(), area.trim());
        return updateUser(updated);
    }

    public boolean updateAccount(User existing, String fullName, String password,
                                 String address, String contactNumber) {
        if (existing == null || fullName == null || fullName.trim().length() == 0
                || password == null || password.trim().length() == 0) {
            return false;
        }

        User updated;
        if (existing instanceof Resident) {
            updated = new Resident(existing.getUserId(), existing.getUsername(), password,
                    fullName.trim(), address, contactNumber);
        } else if (existing instanceof AreaOfficer) {
            // an officer owns an area, so it has to be saved through updateAreaOfficer(...)
            return false;
        } else {
            updated = new Administrator(existing.getUserId(), existing.getUsername(), password,
                    fullName.trim());
        }
        return updateUser(updated);
    }

    private boolean isUsernameTaken(String username) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(username.trim())) {
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
        String[] parts = line.split("\\|");
        if (parts.length < 5) {
            return null;
        }

        String type = parts[0];
        String userId = parts[1];
        String username = parts[2];
        String password = parts[3];
        String fullName = parts[4];

        if (type.equals("Resident")) {
            String address = "";
            String contactNumber = "";
            if (parts.length >= 7) {
                address = parts[5];
                contactNumber = parts[6];
            }
            return new Resident(userId, username, password, fullName, address, contactNumber);
        }
        if (type.equals("Administrator")) {
            return new Administrator(userId, username, password, fullName);
        }
        if (type.equals("AreaOfficer")) {
            String area = "";
            if (parts.length >= 6) {
                area = parts[5];
            }
            return new AreaOfficer(userId, username, password, fullName, area);
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