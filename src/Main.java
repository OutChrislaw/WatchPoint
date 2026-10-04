// Main.java
// The entry point of the WatchPoint application.
// It creates the managers (the "brains" of the system) and opens the login window.

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Create one FileManager that points to the "data" folder.
        FileManager fileManager = new FileManager("data");

        // Create the managers. They load the .txt files at startup.
        UserManager userManager = new UserManager(fileManager);
        ReportManager reportManager = new ReportManager(fileManager);
        LocationManager locationManager = new LocationManager(fileManager);

        // Start the GUI on the Swing event thread (the correct way to use Swing).
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame(userManager, reportManager, locationManager);
                loginFrame.setVisible(true);
            }
        });
    }
}
