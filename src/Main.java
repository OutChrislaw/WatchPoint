import javax.swing.SwingUtilities;

/**
 * Main.java
 * Entry point of the WatchPoint desktop application.
 * It creates the shared managers and opens the login window.
 */
public class Main {

    public static void main(String[] args) {
        UITheme.applyGlobalDefaults();

        FileManager fileManager = new FileManager("data");
        UserManager userManager = new UserManager(fileManager);
        final ReportManager reportManager = new ReportManager(fileManager);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame(userManager, reportManager);
                loginFrame.setVisible(true);
            }
        });
    }
}
