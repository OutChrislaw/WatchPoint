// UserManager.java
// Handles user accounts: register, log in, find, load and save.
// It reads and writes users.txt through FileManager.

import java.util.ArrayList;

public class UserManager {

    private static final String FILE_NAME = "users.txt";

    private ArrayList<User> users;
    private FileManager fileManager;

    public UserManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.users = new ArrayList<User>();
        loadUsers();
        createDefaultAdminIfNeeded();
    }

    // Registers a new resident. Returns false if the username is already taken.
    public boolean registerResident(String fullName, String username, String password,
                                    String address, String contactNumber) {
        // First check the inputs are not empty (Logical Operators: || and &&).
        if (fullName == null || fullName.trim().length() == 0
                || username == null || username.trim().length() == 0
                || password == null || password.trim().length() == 0) {
            return false;
        }

        if (isUsernameTaken(username)) {
            return false;
        }

        String userId = generateUserId();
        Resident resident = new Resident(userId, username, password, fullName,
                address, contactNumber);
        users.add(resident);
        saveUsers();
        return true;
    }

    // Returns the matching user, or null if the username or password is wrong.
    public User login(String username, String password) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getUsername().equals(username) && user.verifyPassword(password)) {
                return user;
            }
        }
        return null;
    }

    // Finds a user by ID. Used to show a reporter's name from a report.
    public User getUserById(String userId) {
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    // Returns a copy of all users (so callers cannot change our list directly).
    public ArrayList<User> getAllUsers() {
        return new ArrayList<User>(users);
    }

    // Reads users.txt into the users list.
    private void loadUsers() {
        users.clear();
        ArrayList<String> lines = fileManager.readLines(FILE_NAME);
        for (int i = 0; i < lines.size(); i++) {
            User user = parseUser(lines.get(i));
            if (user != null) {
                users.add(user);
            }
        }
    }

    // Writes the users list back to users.txt.
    private void saveUsers() {
        ArrayList<String> lines = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            lines.add(users.get(i).toFileString());
        }
        fileManager.writeLines(FILE_NAME, lines);
    }

    // Turns one line into a Resident or an Administrator.
    // The first field tells us which subclass to build.
    private User parseUser(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 5) {
            return null; // not a valid line
        }

        String type = parts[0];
        String userId = parts[1];
        String username = parts[2];
        String password = parts[3];
        String fullName = parts[4];

        if (type.equals("Resident")) {
            // A Resident line needs the address and contact number.
            if (parts.length < 7) {
                return null;
            }
            return new Resident(userId, username, password, fullName, parts[5], parts[6]);
        } else if (type.equals("Administrator")) {
            return new Administrator(userId, username, password, fullName);
        } else {
            return null;
        }
    }

    // Checks whether a username is already used.
    private boolean isUsernameTaken(String username) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    // Creates the next unique user ID, such as U001, U002, and so on.
    private String generateUserId() {
        int highest = 0;
        for (int i = 0; i < users.size(); i++) {
            String id = users.get(i).getUserId(); // looks like "U001"
            if (id != null && id.length() > 1) {
                try {
                    int number = Integer.parseInt(id.substring(1));
                    if (number > highest) {
                        highest = number;
                    }
                } catch (Exception e) {
                    // ignore bad IDs
                }
            }
        }
        int next = highest + 1;
        // Build a zero-padded number: 1 -> "001".
        String text = "" + next;
        while (text.length() < 3) {
            text = "0" + text;
        }
        return "U" + text;
    }

    // Adds a default admin account the first time the app runs.
    // There is no administrator sign-up, so we create one automatically.
    private void createDefaultAdminIfNeeded() {
        boolean hasAdmin = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i) instanceof Administrator) {
                hasAdmin = true;
            }
        }
        if (!hasAdmin) {
            users.add(new Administrator("U999", "admin", "admin123", "Barangay Admin"));
            saveUsers();
        }
    }
}
