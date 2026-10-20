package ui;

import java.awt.BorderLayout;
import java.awt.Color;
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
 * UserUpdateDialog.java
 * The modal form an Administrator uses to edit an account. It uses the same
 * card design as ReportUpdateDialog and RegistrationFrame, and it shows only
 * the fields that belong to the account type.
 */
public class UserUpdateDialog {

    private final User user;
    private final UserManager userManager;
    private final LocationManager locationManager;

    private JDialog dialog;
    private JTextField fullNameField;
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField contactField;
    private JComboBox<String> cityBox;
    private JComboBox<String> barangayBox;
    private JComboBox<String> areaBox;
    private boolean saved;

    public UserUpdateDialog(Window owner, User user, UserManager userManager,
                            LocationManager locationManager) {
        this.user = user;
        this.userManager = userManager;
        this.locationManager = locationManager;
        buildForm(owner);
    }

    public boolean showDialog() {
        dialog.setVisible(true);
        return saved;
    }

    private void buildForm(Window owner) {
        dialog = new JDialog(owner, "Update Account " + user.getUserId());
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel content = UITheme.card();
        content.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.add(UITheme.label("Update Account " + user.getUserId(),
                UITheme.FONT_HEADING, UITheme.PRIMARY));
        header.add(Box.createVerticalStrut(2));
        header.add(UITheme.label(user.getUserType() + "  -  " + user.getUsername(),
                UITheme.FONT_BASE, UITheme.MUTED));
        content.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        int row = 0;

        fullNameField = UITheme.hintField("Full name", 20);
        fullNameField.setText(user.getFullName());
        UITheme.addFormRow(form, row, "Full Name", fullNameField);
        row = row + 2;

        usernameField = UITheme.hintField("", 20);
        usernameField.setText(user.getUsername());
        usernameField.setEnabled(false);
        UITheme.addFormRow(form, row, "Username (cannot be changed)", usernameField);
        row = row + 2;

        passwordField = UITheme.hintField("Leave blank to keep the current password", 20);
        UITheme.addFormRow(form, row, "New Password", passwordField);
        row = row + 2;

        if (user instanceof Resident) {
            Resident resident = (Resident) user;

            contactField = UITheme.hintField("09XXXXXXXXX", 20);
            contactField.setText(resident.getContactNumber());
            UITheme.addFormRow(form, row, "Contact Number", contactField);
            row = row + 2;

            cityBox = UITheme.combo(null, locationManager.getMunicipalities());
            UITheme.selectItem(cityBox, UserManager.municipalityOf(resident.getAddress()));
            UITheme.addFormRow(form, row, "City / Municipality", cityBox);
            row = row + 2;

            barangayBox = new JComboBox<String>();
            barangayBox.setFont(UITheme.FONT_BASE);
            UITheme.addFormRow(form, row, "Barangay", barangayBox);
            fillBarangays();
            UITheme.selectItem(barangayBox, UserManager.barangayOf(resident.getAddress()));
            cityBox.addActionListener(e -> fillBarangays());
        }

        if (user instanceof AreaOfficer) {
            AreaOfficer officer = (AreaOfficer) user;

            areaBox = UITheme.combo(null, locationManager.getMunicipalities());
            UITheme.selectItem(areaBox, officer.getArea());
            UITheme.addFormRow(form, row, "Assigned Area (city / municipality)", areaBox);
        }

        content.add(form, BorderLayout.CENTER);        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(Color.WHITE);

        JButton cancelButton = UITheme.button("Cancel", UITheme.BORDER, UITheme.TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelButton);

        JButton saveButton = UITheme.button("Save Changes", UITheme.ACCENT, Color.WHITE);
        saveButton.addActionListener(e -> save());
        buttonRow.add(saveButton);

        content.add(buttonRow, BorderLayout.SOUTH);

        Dimension preferred = content.getPreferredSize();
        content.setPreferredSize(new Dimension(620, preferred.height));

        dialog.add(content, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
    }

    private void fillBarangays() {
        barangayBox.removeAllItems();
        List<String> barangays = locationManager.getBarangays(UITheme.text(cityBox));
        for (int i = 0; i < barangays.size(); i++) {
            barangayBox.addItem(barangays.get(i));
        }
    }

    private void save() {
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();

        if (fullName.length() == 0) {
            JOptionPane.showMessageDialog(dialog, "Please enter the full name.");
            return;
        }

        String address = null;
        String contactNumber = null;
        String area = null;

        if (user instanceof Resident) {
            String city = UITheme.text(cityBox);
            String barangay = UITheme.text(barangayBox);
            if (city.length() == 0 || barangay.length() == 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select a City / Municipality and a Barangay.");
                return;
            }
            address = barangay + ", " + city;
            contactNumber = contactField.getText().trim();
        }

        if (user instanceof AreaOfficer) {
            area = UITheme.text(areaBox);
            if (area.length() == 0) {
                JOptionPane.showMessageDialog(dialog, "Please select the area this officer handles.");
                return;
            }
        }

        if (userManager.saveAccountChanges(user, fullName, password, address, contactNumber, area)) {
            saved = true;
            dialog.dispose();
        } else {
            JOptionPane.showMessageDialog(dialog, "The account could not be updated.");
        }
    }
}