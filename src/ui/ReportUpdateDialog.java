package ui;

import manager.ReportManager;
import manager.LocationManager;
import model.Report;
import model.Location;

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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * ReportUpdateDialog.java
 * The modal "Update Report" form used by ResidentDashboard. It uses the same
 * card design as the Submit Report tab and is pre-filled with the selected
 * report; showDialog() returns true only when the update was saved.
 */
public class ReportUpdateDialog {

    private final Report report;
    private final String residentId;
    private final ReportManager reportManager;
    private final LocationManager locationManager;

    private JDialog dialog;
    private JComboBox<String> typeBox;
    private JComboBox<String> cityBox;
    private JComboBox<String> barangayBox;
    private JTextField streetField;
    private JTextField placeField;
    private JTextField detailField;
    private JTextArea descriptionArea;
    private boolean saved;

    public ReportUpdateDialog(Window owner, Report report, String residentId,
                              ReportManager reportManager, LocationManager locationManager) {
        this.report = report;
        this.residentId = residentId;
        this.reportManager = reportManager;
        this.locationManager = locationManager;
        buildForm(owner);
    }

    public boolean showDialog() {
        dialog.setVisible(true);
        return saved;
    }

    private void buildForm(Window owner) {
        dialog = new JDialog(owner, "Update Report " + report.getReportId());
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel content = UITheme.card();
        content.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel();
        header.setBackground(Color.WHITE);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.add(UITheme.label("Update Report " + report.getReportId(),
                UITheme.FONT_HEADING, UITheme.PRIMARY));
        header.add(Box.createVerticalStrut(2));
        header.add(UITheme.label("Change the details you want, then press Update Report.",
                UITheme.FONT_BASE, UITheme.MUTED));
        content.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        int row = 0;

        typeBox = UITheme.options(new String[] { report.getReportType() });
        typeBox.setEnabled(false);
        UITheme.addFormRow(form, row, "Hazard Type", typeBox);
        row = row + 2;

        cityBox = UITheme.combo(null, locationManager.getMunicipalities());
        cityBox.setSelectedItem(report.getLocation().getCityMunicipality());
        UITheme.addFormRow(form, row, "City / Municipality", cityBox);
        row = row + 2;

        barangayBox = new JComboBox<String>();
        barangayBox.setFont(UITheme.FONT_BASE);
        UITheme.addFormRow(form, row, "Barangay", barangayBox);
        row = row + 2;
        fillBarangays();
        barangayBox.setSelectedItem(report.getLocation().getBarangay());
        cityBox.addActionListener(e -> fillBarangays());

        streetField = UITheme.hintField("Optional (e.g. Casa Road)", 20);
        UITheme.maxLength(streetField, 60);
        streetField.setText(report.getLocation().getStreet());
        UITheme.addFormRow(form, row, "Street", streetField);
        row = row + 2;

        placeField = UITheme.hintField("(e.g. Near Water Refiller)", 20);
        UITheme.maxLength(placeField, 60);
        placeField.setText(report.getLocation().getSpecificPlace());
        UITheme.addFormRow(form, row, "Specific Place", placeField);
        row = row + 2;

        detailField = UITheme.hintField("(e.g. Pothole, Knee deep, Pole 12)", 20);
        UITheme.maxLength(detailField, 80);
        detailField.setText(report.getSpecificDetail());
        UITheme.addFormRow(form, row, "Specific Detail", detailField);
        row = row + 2;

        descriptionArea = UITheme.hintArea("(e.g. Road blocked due to accident)", 4, 20);
        descriptionArea.setText(report.getDescription());
        JScrollPane descriptionScroll = UITheme.borderedScroll(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(10, 90));
        UITheme.addFormRow(form, row, "Description", descriptionScroll);

        content.add(form, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(Color.WHITE);

        JButton cancelButton = UITheme.button("Cancel", UITheme.BORDER, UITheme.TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelButton);

        JButton updateButton = UITheme.button("Update Report", UITheme.ACCENT, Color.WHITE);
        updateButton.addActionListener(e -> save());
        buttonRow.add(updateButton);

        content.add(buttonRow, BorderLayout.SOUTH);

        Dimension preferred = content.getPreferredSize();
        content.setPreferredSize(new Dimension(600, preferred.height));

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
        String city = UITheme.text(cityBox);
        String barangay = UITheme.text(barangayBox);
        String street = ReportManager.oneLine(streetField.getText());
        String place = ReportManager.oneLine(placeField.getText());
        String detail = ReportManager.oneLine(detailField.getText());
        String description = ReportManager.oneLine(descriptionArea.getText());

        if (street.length() == 0) {
            street = "N/A";
        }

        Location location = new Location(city, barangay, street, place);

        String problem = ReportManager.reportProblem(report.getReportType(), location, description,
                detail);
        if (problem != null) {
            JOptionPane.showMessageDialog(dialog, problem);
            return;
        }

        report.setLocation(location);
        report.setSpecificDetail(detail);
        report.setDescription(description);

        if (reportManager.updateResidentReport(report, residentId)) {
            saved = true;
            dialog.dispose();
        } else {
            JOptionPane.showMessageDialog(dialog, "The report could not be updated.");
        }
    }
}
