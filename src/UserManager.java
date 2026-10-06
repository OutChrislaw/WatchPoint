import java.util.ArrayList;
import java.util.List;

/**
 * UserManager.java
 * Handles registration, login, and user records of WatchPoint.
 * Users are stored in users.txt through the FileManager.
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
    }

    public boolean addUser(User user) {
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
    }

    private User parseUser(String line) {
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
        return null;
    }

    private String generateUserId() {
        int highest = 0;
        for (int i = 0; i < users.size(); i++) {
            String userId = users.get(i).getUserId();
            if (userId != null && userId.length() > 1) {
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
}
