package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import manager.LocationManager;
import manager.UserManager;
import model.AreaOfficer;
import model.Resident;
import model.User;

/**
 * ProfileDialog.java
 * The Settings window of the signed-in account. It opens from the Settings
 * button in the header of every dashboard and uses a two-column layout: a menu
 * on the left (Account Details, Change Password, Logout) and the matching card
 * on the right.
 */
public class ProfileDialog {

    private static final String DETAILS_CARD = "details";
    private static final String PASSWORD_CARD = "password";

    private final User user;
    private final UserManager userManager;
    private final LocationManager locationManager;

    private JDialog dialog;
    private JPanel cards;
    private JButton detailsButton;
    private JButton passwordButton;

    private JTextField fullNameField;
    private JTextField usernameField;
    private JTextField contactField;
    private JComboBox<String> cityBox;
    private JComboBox<String> barangayBox;
    private JTextField areaField;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

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
        dialog = new JDialog(owner, "Settings");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel page = UITheme.page();

        JPanel columns = new JPanel(new BorderLayout(20, 0));
        columns.setOpaque(false);
        columns.add(buildMenu(), BorderLayout.WEST);

        cards = new JPanel(new CardLayout());
        cards.setOpaque(false);
        cards.add(buildDetailsCard(), DETAILS_CARD);
        cards.add(buildPasswordCard(), PASSWORD_CARD);
        columns.add(cards, BorderLayout.CENTER);

        page.add(columns, BorderLayout.CENTER);
        dialog.add(page, BorderLayout.CENTER);

        showCard(DETAILS_CARD);

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

        detailsButton = menuButton("Account Details");
        detailsButton.addActionListener(e -> showCard(DETAILS_CARD));

        passwordButton = menuButton("Change Password");
        passwordButton.addActionListener(e -> showCard(PASSWORD_CARD));

        JButton logoutButton = menuButton("Logout");
        logoutButton.addActionListener(e -> requestLogout());

        menu.add(detailsButton);
        menu.add(Box.createVerticalStrut(8));
        menu.add(passwordButton);
        menu.add(Box.createVerticalStrut(8));
        menu.add(logoutButton);
        menu.add(Box.createVerticalGlue());
        return menu;
    }

    private JButton menuButton(String text) {
        JButton button = UITheme.button(text, Color.WHITE, UITheme.PRIMARY);
        button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return button;
    }

    private void showCard(String name) {
        CardLayout layout = (CardLayout) cards.getLayout();
        layout.show(cards, name);

        boolean detailsActive = name.equals(DETAILS_CARD);
        paintMenuButton(detailsButton, detailsActive);
        paintMenuButton(passwordButton, !detailsActive);
    }

    private static void paintMenuButton(JButton button, boolean selected) {
        button.setBackground(selected ? UITheme.ACCENT : Color.WHITE);
        button.setForeground(selected ? Color.WHITE : UITheme.PRIMARY);
    }    private JPanel buildDetailsCard() {
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

        boolean canEditUsername = user instanceof Resident;
        usernameField = UITheme.hintField("", 30);
        usernameField.setText(user.getUsername());
        usernameField.setEnabled(canEditUsername);
        if (canEditUsername) {
            UITheme.addFormRow(form, row, "Username (3-40 characters: letters, numbers, . _ @ + -)",
                    usernameField);
        } else {
            UITheme.addFormRow(form, row, "Username (cannot be changed)", usernameField);
        }
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
        }

        card.add(form, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(Color.WHITE);

        JButton cancelButton = UITheme.button("Cancel", UITheme.BORDER, UITheme.TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelButton);

        JButton saveButton = UITheme.button("Save Changes", UITheme.ACCENT, Color.WHITE);
        saveButton.addActionListener(e -> saveDetails());
        buttonRow.add(saveButton);

        card.add(buttonRow, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildPasswordCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 16));

        card.add(UITheme.label("Change Password", UITheme.FONT_TITLE, UITheme.TEXT), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        currentPasswordField = UITheme.passwordField(30);
        UITheme.addFormRow(form, 0, "Current Password", currentPasswordField);

        newPasswordField = UITheme.passwordField(30);
        UITheme.addFormRow(form, 2, "New Password (at least 6 characters)", newPasswordField);

        confirmPasswordField = UITheme.passwordField(30);
        UITheme.addFormRow(form, 4, "Confirm New Password", confirmPasswordField);

        card.add(form, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(Color.WHITE);

        JButton cancelButton = UITheme.button("Cancel", UITheme.BORDER, UITheme.TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelButton);

        JButton changeButton = UITheme.button("Change Password", UITheme.ACCENT, Color.WHITE);
        changeButton.addActionListener(e -> savePassword());
        buttonRow.add(changeButton);

        card.add(buttonRow, BorderLayout.SOUTH);
        return card;
    }    private void fillBarangays() {
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

    private void saveDetails() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();

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

        String problem = userManager.saveDetails(user, fullName, username, contactNumber,
                barangay, municipality);
        if (problem != null) {
            JOptionPane.showMessageDialog(dialog, problem);
            return;
        }

        saved = true;
        JOptionPane.showMessageDialog(dialog, "Your details were saved.");
        dialog.dispose();
    }

    private void savePassword() {
        String problem = userManager.changePassword(user,
                new String(currentPasswordField.getPassword()),
                new String(newPasswordField.getPassword()),
                new String(confirmPasswordField.getPassword()));

        if (problem != null) {
            JOptionPane.showMessageDialog(dialog, problem);
            return;
        }

        saved = true;
        JOptionPane.showMessageDialog(dialog, "Your password was changed.");
        dialog.dispose();
    }
}