package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import manager.ReportManager;
import model.Report;

/**
 * ReportRejectDialog.java
 * The small form an Area Officer or Administrator fills in to reject a report:
 * it asks for the reason, which the resident then sees on their own report.
 * showDialog() returns the reason, or null when the officer cancels.
 */
public class ReportRejectDialog {

    private final Report report;

    private JDialog dialog;
    private JTextArea reasonArea;
    private String reason;

    public ReportRejectDialog(Window owner, Report report) {
        this.report = report;
        buildForm(owner);
    }

    public String showDialog() {
        dialog.setVisible(true);
        return reason;
    }

    private void buildForm(Window owner) {
        dialog = new JDialog(owner, "Reject Report " + report.getReportId());
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel content = UITheme.card();
        content.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel();
        header.setBackground(UITheme.CARD);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.add(UITheme.label("Reject Report " + report.getReportId(), UITheme.FONT_HEADING,
                UITheme.DANGER));
        header.add(Box.createVerticalStrut(2));
        header.add(UITheme.label(report.getReportType() + "  -  "
                + report.getLocation().getBarangay() + ", "
                + report.getLocation().getCityMunicipality(), UITheme.FONT_BASE, UITheme.MUTED));
        content.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD);

        reasonArea = UITheme.hintArea("(e.g. Duplicate of R008, not a public hazard)", 3, 30);
        JScrollPane reasonScroll = UITheme.borderedScroll(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(10, 84));
        UITheme.addFormRow(form, 0, "Reason for rejecting this report", reasonScroll);
        UITheme.addFormRow(form, 2, "The resident sees this reason, so keep it short and clear.",
                UITheme.label("At least 5 characters. It cannot contain the | character.",
                        UITheme.FONT_BASE, UITheme.MUTED));

        content.add(form, BorderLayout.CENTER);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(UITheme.CARD);

        JButton cancelButton = UITheme.button("Cancel", UITheme.BORDER, UITheme.TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelButton);

        JButton rejectButton = UITheme.button("Reject Report", UITheme.DANGER, UITheme.CARD);
        rejectButton.addActionListener(e -> save());
        buttonRow.add(rejectButton);

        content.add(buttonRow, BorderLayout.SOUTH);

        Dimension preferred = content.getPreferredSize();
        content.setPreferredSize(new Dimension(560, preferred.height));

        dialog.add(content, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
    }

    private void save() {
        String text = ReportManager.oneLine(reasonArea.getText());
        String problem = ReportManager.remarkProblem(text);
        if (problem != null) {
            JOptionPane.showMessageDialog(dialog, problem);
            return;
        }

        reason = text;
        dialog.dispose();
    }
}
