package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import manager.ReportManager;
import model.Report;
import model.ReportStatus;

/**
 * ReportStatusDialog.java
 * The window that opens from the Update Status button. It offers only the
 * statuses the workflow allows next (PENDING -> VERIFIED or REJECTED, and so
 * on), asks for a reason when the report is rejected, and lets an administrator
 * reopen a rejected report. The rules themselves live in model/ReportStatus and
 * manager/ReportManager, never here.
 */
public class ReportStatusDialog {

    private final Report report;
    private final boolean administrator;

    private JDialog dialog;
    private final List<ReportStatus> targets = new ArrayList<ReportStatus>();
    private final List<JRadioButton> buttons = new ArrayList<JRadioButton>();
    private JTextArea reasonArea;
    private JCheckBox reopenCheck;
    private JLabel errorLabel;
    private JButton saveButton;

    private ReportStatus selectedStatus;
    private String reason = "";
    private boolean reopenRequested;

    public ReportStatusDialog(Window owner, Report report, boolean administrator) {
        this.report = report;
        this.administrator = administrator;
        buildForm(owner);
    }

    public boolean showDialog() {
        dialog.setVisible(true);
        return selectedStatus != null || reopenRequested;
    }

    public ReportStatus getSelectedStatus() {
        return selectedStatus;
    }

    public String getReason() {
        return reason;
    }

    public boolean isReopenRequested() {
        return reopenRequested;
    }

    private void buildForm(Window owner) {
        dialog = new JDialog(owner, "Update Status - " + report.getReportId());
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());

        JPanel content = UITheme.card();
        content.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel();
        header.setBackground(UITheme.CARD);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        header.add(UITheme.label("Update Status - " + report.getReportId(), UITheme.FONT_HEADING,
                UITheme.PRIMARY));
        header.add(Box.createVerticalStrut(2));
        header.add(UITheme.label(report.getReportType() + "  -  "
                + report.getLocation().getBarangay() + ", "
                + report.getLocation().getCityMunicipality(), UITheme.FONT_BASE, UITheme.MUTED));
        header.add(Box.createVerticalStrut(6));
        header.add(UITheme.label("Current status: " + report.getStatus().displayName(),
                UITheme.FONT_BOLD, UITheme.statusColor(report.getStatus().name())));
        content.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD);

        int row = 0;
        targets.addAll(report.getStatus().nextStatuses());

        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(UITheme.CARD);
        statusPanel.setLayout(new javax.swing.BoxLayout(statusPanel, javax.swing.BoxLayout.Y_AXIS));

        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < targets.size(); i++) {
            final ReportStatus target = targets.get(i);
            JRadioButton option = new JRadioButton(target.displayName());
            option.setFont(UITheme.FONT_BASE);
            option.setForeground(UITheme.TEXT);
            option.setOpaque(false);
            option.setFocusPainted(false);
            option.addActionListener(e -> {
                updateReasonState();
                updateSaveState();
                clearError();
            });
            group.add(option);
            buttons.add(option);
            statusPanel.add(option);
            if (i == 0) {
                option.setSelected(true);
            }
        }

        UITheme.addFormRow(form, row, "New status", statusPanel);
        row = row + 2;

        if (targets.isEmpty()) {
            form.add(notePanel(), noteConstraints(0, row));
            row = row + 2;
        }

        reasonArea = UITheme.hintArea("(e.g. Duplicate of R008, not a public hazard)", 3, 30);
        JScrollPane reasonScroll = UITheme.borderedScroll(reasonArea);
        reasonScroll.setPreferredSize(new Dimension(10, 84));
        UITheme.addFormRow(form, row, "Reason (needed when rejecting, the resident sees it)",
                reasonScroll);
        content.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setBackground(UITheme.CARD);

        errorLabel = UITheme.label("", UITheme.FONT_BASE, UITheme.DANGER);
        errorLabel.setVisible(false);
        south.add(errorLabel, BorderLayout.NORTH);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRow.setBackground(UITheme.CARD);

        JButton cancelButton = UITheme.button("Cancel", UITheme.BORDER, UITheme.TEXT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonRow.add(cancelButton);

        saveButton = UITheme.button("Save Status", UITheme.ACCENT, UITheme.CARD);
        saveButton.addActionListener(e -> save());
        buttonRow.add(saveButton);

        south.add(buttonRow, BorderLayout.SOUTH);
        content.add(south, BorderLayout.SOUTH);

        updateReasonState();
        updateSaveState();

        Dimension preferred = content.getPreferredSize();
        content.setPreferredSize(new Dimension(560, preferred.height));

        dialog.add(content, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
    }

    /** The text shown for a report that has no next status. */
    private JPanel notePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UITheme.CARD);
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

        if (report.getStatus() == ReportStatus.REJECTED) {
            panel.add(UITheme.label("A rejected report is final, so no other status can be chosen.",
                    UITheme.FONT_BASE, UITheme.MUTED));
            if (administrator) {
                reopenCheck = new JCheckBox("Reopen this report (back to PENDING)");
                reopenCheck.setFont(UITheme.FONT_BASE);
                reopenCheck.setForeground(UITheme.TEXT);
                reopenCheck.setOpaque(false);
                reopenCheck.setFocusPainted(false);
                reopenCheck.addActionListener(e -> {
                    updateReasonState();
                    updateSaveState();
                    clearError();
                });
                panel.add(Box.createVerticalStrut(4));
                panel.add(reopenCheck);
            } else {
                panel.add(Box.createVerticalStrut(4));
                panel.add(UITheme.label("Only an administrator can reopen a rejected report.",
                        UITheme.FONT_BASE, UITheme.DANGER));
            }
        } else {
            panel.add(UITheme.label("This report is RESOLVED and can no longer be changed.",
                    UITheme.FONT_BASE, UITheme.MUTED));
        }
        return panel;
    }

    private java.awt.GridBagConstraints noteConstraints(int x, int y) {
        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        constraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        constraints.insets = new java.awt.Insets(0, 0, 8, 0);
        return constraints;
    }

    private void updateReasonState() {
        boolean rejecting = getChosenStatus() == ReportStatus.REJECTED;
        reasonArea.setEnabled(rejecting);
        reasonArea.setBackground(rejecting ? UITheme.CARD : UITheme.PAGE);
    }

    private void updateSaveState() {
        boolean reopen = reopenCheck != null && reopenCheck.isSelected();
        saveButton.setEnabled(reopen || getChosenStatus() != null);
    }

    private ReportStatus getChosenStatus() {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).isSelected()) {
                return targets.get(i);
            }
        }
        return null;
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void save() {
        if (reopenCheck != null && reopenCheck.isSelected()) {
            reopenRequested = true;
            selectedStatus = null;
            reason = "";
            dialog.dispose();
            return;
        }

        ReportStatus target = getChosenStatus();
        if (target == null) {
            showError("Please choose the new status.");
            return;
        }

        String text = ReportManager.oneLine(reasonArea.getText());
        String problem = ReportManager.statusProblem(report, target, text);
        if (problem != null) {
            showError(problem);
            return;
        }

        selectedStatus = target;
        reason = text;
        dialog.dispose();
    }
}
