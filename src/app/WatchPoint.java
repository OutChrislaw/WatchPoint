package app;

import manager.LocationManager;
import manager.ReportManager;
import manager.UserManager;
import storage.FileManager;
import ui.LoginFrame;

import javax.swing.SwingUtilities;

public class WatchPoint {

    public static void main(String[] args) {

        FileManager fileManager = new FileManager("data");
        UserManager userManager = new UserManager(fileManager);
        final ReportManager reportManager = new ReportManager(fileManager);
        final LocationManager locationManager = new LocationManager(fileManager);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginFrame loginFrame = new LoginFrame(userManager, reportManager, locationManager);
                loginFrame.setVisible(true);
            }
        });
    }
}
