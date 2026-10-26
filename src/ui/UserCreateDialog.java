package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;

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
import validation.UserRules;
import model.Administrator;
import model.AreaOfficer;

/**
 * UserCreateDialog.java
 * The form an Administrator uses to create an Area Officer or another
 * Administrator. Only administrators create accounts, so this form has options
 * that the resident sign-up does not: the account type and, for an area
 * officer, the area it handles.
 */
public class UserCreateDialog {

    private static final String AREA_OFFICER = "Area Officer";
    private static final String ADMINISTRATOR = "Administrator";

    private final UserManager userManager;
    private final LocationManager locationManager;

    private JDialog dialog;
    private JComboBox<String> typeBox;
    private JTextField fullNameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> areaBox;
    private boolean created;

    public UserCreateDialog(Window owner, UserManager userManager, LocationManager locationManager) {
        this.userManager = userManager;
        this.locationManager = locationManager;
        buildForm(owner);
    }

    public boolean showDialog() {
        dialog.setVisible(true);
        return created;
    }

    private void buildForm(Window owner) {
        dialog = new JDialog(owner, "Create User");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel content = UITheme.card();
        content.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel();
        header.setBackground(UITheme.CARD);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.add(UITheme.label("Create User", UITheme.FONT_HEADING, UITheme.PRIMARY));
        header.add(Box.createVerticalStrut(2));
        header.add(UITheme.label("Only an administrator can create accounts.",
                UITheme.FONT_BASE, UITheme.MUTED));
        content.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD);

        int row = 0;

        typeBox = UITheme.options(new String[] { AREA_OFFICER, ADMINISTRATOR });
        typeBox.addActionListener(e -> typeChanged());
        UITheme.addFormRow(form, row, "User Type", typeBox);
        row = row + 2;

        fullNameField = UITheme.hintField("Full name", 20);
        UITheme.maxLength(fullNameField, 60);
        UITheme.addFormRow(form, row, "Full Name", fullNameField);
        row = row + 2;

        passwordField = UITheme.passwordField(20);
        UITheme.addFormRow(form, row, "Password (8+ characters)", passwordField);
        row = row + 2;

        confirmPasswordField = UITheme.passwordField(20);
        UITheme.addFormRow(form, row, "Confirm Password", confirmPasswordField);
        row = row + 2;

        areaBox = UITheme.combo(null, locationManager.getMunicipalities());
        UITheme.addFormRow(form, row, "Assigned Area (area officers only)", areaBox);

        content.add(form, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(UITheme.CARD);

        JButton cancelButton = UITheme.button("Cancel", UITheme.BORDER, UITheme.TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelButton);

        JButton createButton = UITheme.button("Create User", UITheme.ACCENT, UITheme.CARD);
        createButton.addActionListener(e -> save());
        buttonRow.add(createButton);

        content.add(buttonRow, BorderLayout.SOUTH);

        typeChanged();

        Dimension preferred = content.getPreferredSize();
        content.setPreferredSize(new Dimension(620, preferred.height));

        dialog.add(content, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
    }

    private void typeChanged() {
        boolean areaOfficer = AREA_OFFICER.equals(UITheme.text(typeBox));
        areaBox.setEnabled(areaOfficer);
    }

    private void save() {
        String type = UITheme.text(typeBox);
        String fullName = fullNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        String problem = UserRules.fullNameProblem(fullName);
        if (problem == null) {
            problem = UserRules.passwordProblem(password);
        }
        if (problem == null) {
            problem = UserRules.passwordConfirmProblem(password, confirmPassword);
        }
        if (problem == null && AREA_OFFICER.equals(type)) {
            problem = UserRules.areaProblem(UITheme.text(areaBox));
        }
        if (problem != null) {
            JOptionPane.showMessageDialog(dialog, problem);
            return;
        }

        if (AREA_OFFICER.equals(type)) {
            String area = UITheme.text(areaBox);

            AreaOfficer officer = userManager.addAreaOfficer(fullName, password, area);
            if (officer == null) {
                JOptionPane.showMessageDialog(dialog, "The account could not be created.");
                return;
            }

            created = true;
            JOptionPane.showMessageDialog(dialog, "Area Officer created. " + officer.getFullName()
                    + " logs in with the ID " + officer.getUserId() + ".");
        } else {
            Administrator administrator = userManager.addAdministrator(fullName, password);
            if (administrator == null) {
                JOptionPane.showMessageDialog(dialog, "The account could not be created.");
                return;
            }

            created = true;
            JOptionPane.showMessageDialog(dialog, "Administrator created. "
                    + administrator.getFullName() + " logs in with the ID "
                    + administrator.getUserId() + ".");
        }

        dialog.dispose();
    }
}