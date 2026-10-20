package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import manager.LocationManager;
import manager.UserManager;
import model.AreaOfficer;
import model.Resident;
import model.User;

/**
 * ProfileDialog.java
 * The "Update Profile" window of the signed-in account. It opens from the
 * button in the header of every dashboard and uses a two-column layout: a menu
 * on the left (Account Details, Logout) and the profile form on the right.
 */
public class ProfileDialog {

    private final User user;
    private final UserManager userManager;
    private final LocationManager locationManager;

    private JDialog dialog;
    private JTextField fullNameField;
    private JTextField usernameField;
    private JTextField contactField;
    private JTextField passwordField;
    private JComboBox<String> cityBox;
    private JComboBox<String> barangayBox;
    private JTextField areaField;
    private boolean saved;
    private boolean logoutRequested;

    public ProfileDialog(Window owner, User user, UserManager userManager,
                         LocationManager locationManager) {
        this.user = user;
        this.userManager = userManager;
        this.locationManager = locationManager;
        buildWindow(owner);
    }

    public boolean showDialog() {
        dialog.setVisible(true);
        return saved;
    }

    public boolean isLogoutRequested() {
        return logoutRequested;
    }

    private void buildWindow(Window owner) {
        dialog = new JDialog(owner, "Update Profile");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel page = UITheme.page();

        JPanel columns = new JPanel(new BorderLayout(20, 0));
        columns.setOpaque(false);
        columns.add(buildMenu(), BorderLayout.WEST);
        columns.add(buildForm(), BorderLayout.CENTER);

        page.add(columns, BorderLayout.CENTER);
        dialog.add(page, BorderLayout.CENTER);

        dialog.pack();
        if (dialog.getWidth() < 900) {
            dialog.setSize(900, dialog.getHeight());
        }
        dialog.setLocationRelativeTo(owner);
    }

    private JPanel buildMenu() {
        JPanel menu = UITheme.card();
        menu.setLayout(new javax.swing.BoxLayout(menu, javax.swing.BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(220, 10));

        JButton detailsButton = UITheme.button("Account Details", UITheme.ACCENT, Color.WHITE);
        detailsButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        detailsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton logoutButton = UITheme.button("Logout", Color.WHITE, UITheme.PRIMARY);
        logoutButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutButton.addActionListener(e -> requestLogout());

        menu.add(detailsButton);
        menu.add(Box.createVerticalStrut(8));
        menu.add(logoutButton);
        menu.add(Box.createVerticalGlue());
        return menu;
    }    private JPanel buildForm() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 16));

        card.add(UITheme.label("Account Settings", UITheme.FONT_TITLE, UITheme.TEXT), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        int row = 0;

        fullNameField = UITheme.hintField("Full name", 30);
        fullNameField.setText(user.getFullName());
        UITheme.addFormRow(form, row, "Full Name", fullNameField);
        row = row + 2;

        usernameField = UITheme.hintField("", 30);
        usernameField.setText(user.getUsername());
        usernameField.setEnabled(false);
        UITheme.addFormRow(form, row, "Username (cannot be changed)", usernameField);
        row = row + 2;

        if (user instanceof Resident) {
            Resident resident = (Resident) user;

            contactField = UITheme.hintField("09XXXXXXXXX", 30);
            contactField.setText(resident.getContactNumber());
            UITheme.addFormRow(form, row, "Contact Number", contactField);
            row = row + 2;

            cityBox = UITheme.combo(null, locationManager.getMunicipalities());
            UITheme.selectItem(cityBox, UserManager.municipalityOf(resident.getAddress()));
            cityBox.addActionListener(e -> fillBarangays());
            UITheme.addFormRow(form, row, "City / Municipality", cityBox);
            row = row + 2;

            barangayBox = new JComboBox<String>();
            barangayBox.setFont(UITheme.FONT_BASE);
            UITheme.addFormRow(form, row, "Barangay", barangayBox);
            fillBarangays();
            UITheme.selectItem(barangayBox, UserManager.barangayOf(resident.getAddress()));
            row = row + 2;
        }

        if (user instanceof AreaOfficer) {
            AreaOfficer officer = (AreaOfficer) user;

            areaField = UITheme.hintField("", 30);
            areaField.setText(officer.getArea());
            areaField.setEnabled(false);
            UITheme.addFormRow(form, row, "Assigned Area (ask an administrator to change)", areaField);
            row = row + 2;
        }

        passwordField = UITheme.hintField("Leave blank to keep your password", 30);
        UITheme.addFormRow(form, row, "New Password", passwordField);

        card.add(form, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(Color.WHITE);

        JButton cancelButton = UITheme.button("Cancel", UITheme.BORDER, UITheme.TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelButton);

        JButton saveButton = UITheme.button("Save Changes", UITheme.ACCENT, Color.WHITE);
        saveButton.addActionListener(e -> save());
        buttonRow.add(saveButton);

        card.add(buttonRow, BorderLayout.SOUTH);
        return card;
    }

    private void fillBarangays() {
        barangayBox.removeAllItems();
        List<String> barangays = locationManager.getBarangays(UITheme.text(cityBox));
        for (int i = 0; i < barangays.size(); i++) {
            barangayBox.addItem(barangays.get(i));
        }
    }

    private void requestLogout() {
        logoutRequested = true;
        dialog.dispose();
    }

    private void save() {
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();

        if (fullName.length() == 0) {
            JOptionPane.showMessageDialog(dialog, "Please enter your full name.");
            return;
        }

        String contactNumber = null;
        String barangay = null;
        String municipality = null;

        if (user instanceof Resident) {
            String city = UITheme.text(cityBox);
            String selectedBarangay = UITheme.text(barangayBox);
            if (city.length() == 0 || selectedBarangay.length() == 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select your City / Municipality and Barangay.");
                return;
            }
            contactNumber = contactField.getText().trim();
            barangay = selectedBarangay;
            municipality = city;
        }

        if (userManager.saveProfile(user, fullName, password, contactNumber, barangay, municipality)) {
            saved = true;
            JOptionPane.showMessageDialog(dialog, "Your profile was saved.");
            dialog.dispose();
        } else {
            JOptionPane.showMessageDialog(dialog, "Your profile could not be saved.");
        }
    }
}