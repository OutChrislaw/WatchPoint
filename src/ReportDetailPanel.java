import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * ReportDetailPanel.java
 * Shows a single hazard report in a form-style layout (captions and values)
 * instead of as a row inside a table. Used by both dashboards.
 */
public class ReportDetailPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JLabel headerLabel;
    private JPanel statusHolder;
    private JPanel body;
    private JScrollPane bodyScroll;

    public ReportDetailPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.CARD);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)));

        headerLabel = new JLabel("No report selected");
        headerLabel.setFont(UITheme.FONT_TITLE);
        headerLabel.setForeground(UITheme.PRIMARY);
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusHolder = new JPanel();
        statusHolder.setOpaque(false);
        statusHolder.setLayout(new BoxLayout(statusHolder, BoxLayout.X_AXIS));
        statusHolder.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        header.add(headerLabel);
        header.add(Box.createVerticalStrut(8));
        header.add(statusHolder);
        add(header, BorderLayout.NORTH);

        body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.setOpaque(false);
        bodyScroll.getViewport().setOpaque(false);
        bodyScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(bodyScroll, BorderLayout.CENTER);

        clear();
    }

    public void clear() {
        headerLabel.setText("No report selected");
        statusHolder.removeAll();
        statusHolder.revalidate();
        statusHolder.repaint();

        body.removeAll();
        JLabel hint = new JLabel("Select a report from the list to view its details.");
        hint.setFont(UITheme.FONT_BASE);
        hint.setForeground(UITheme.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(hint);
        body.revalidate();
        body.repaint();
    }

    public void setReport(Report report) {
        if (report == null) {
            clear();
            return;
        }

        headerLabel.setText("Report " + report.getReportId() + "   -   " + report.getReportType());

        statusHolder.removeAll();
        JLabel badge = UITheme.pill(report.getStatus().name(),
                UITheme.statusColor(report.getStatus()));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusHolder.add(badge);
        statusHolder.revalidate();
        statusHolder.repaint();

        body.removeAll();

        body.add(UITheme.sectionLabel("Report Information"));
        body.add(Box.createVerticalStrut(12));
        JPanel infoGrid = new GridBagLayoutPanel();
        addField(infoGrid, 0, "Report ID", report.getReportId());
        addField(infoGrid, 1, "Report Type", report.getReportType());
        addField(infoGrid, 2, "Reporter ID", report.getReporterId());
        addField(infoGrid, 3, "Date Submitted", report.getDateSubmitted());
        body.add(infoGrid);
        body.add(Box.createVerticalStrut(22));

        Location location = report.getLocation();
        body.add(UITheme.sectionLabel("Location"));
        body.add(Box.createVerticalStrut(12));
        JPanel locationGrid = new GridBagLayoutPanel();
        addField(locationGrid, 0, "City / Municipality", location.getCityMunicipality());
        addField(locationGrid, 1, "Barangay", location.getBarangay());
        addField(locationGrid, 2, "Street", location.getStreet());
        addField(locationGrid, 3, "Specific Place", location.getSpecificPlace());
        body.add(locationGrid);
        body.add(Box.createVerticalStrut(22));

        body.add(UITheme.sectionLabel(detailCaption(report.getReportType())));
        body.add(Box.createVerticalStrut(12));
        body.add(UITheme.fieldValue(report.getSpecificDetail()));
        body.add(Box.createVerticalStrut(22));

        body.add(UITheme.sectionLabel("Description"));
        body.add(Box.createVerticalStrut(12));
        JPanel descriptionWrapper = new JPanel(new BorderLayout());
        descriptionWrapper.setOpaque(false);
        descriptionWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionWrapper.add(buildDescriptionArea(report.getDescription()), BorderLayout.CENTER);
        body.add(descriptionWrapper);

        body.revalidate();
        body.repaint();
        bodyScroll.getVerticalScrollBar().setValue(0);
    }

    private JTextArea buildDescriptionArea(String description) {
        JTextArea area = new JTextArea();
        area.setText(description == null || description.trim().length() == 0
                ? "No description provided." : description);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UITheme.FONT_VALUE);
        area.setForeground(UITheme.TEXT);
        area.setBackground(UITheme.BACKGROUND);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        int length = description == null ? 0 : description.length();
        int rows = length / 70 + 2;
        if (rows < 3) {
            rows = 3;
        }
        if (rows > 12) {
            rows = 12;
        }
        area.setRows(rows);
        return area;
    }

    private void addField(JPanel grid, int index, String caption, String value) {
        int column = index % 2;
        int row = index / 2;

        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.add(UITheme.fieldCaption(caption));
        block.add(Box.createVerticalStrut(2));
        block.add(UITheme.fieldValue(value));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = column;
        constraints.gridy = row;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.insets = new Insets(0, 0, 16, 24);
        grid.add(block, constraints);
    }

    private String detailCaption(String reportType) {
        if (reportType.equals("Road Hazard")) {
            return "Road Hazard Type";
        }
        if (reportType.equals("Flood")) {
            return "Water Level";
        }
        if (reportType.equals("Streetlight")) {
            return "Pole Number";
        }
        return "Hazard Category";
    }

    /** A JPanel that already uses a GridBagLayout and left alignment. */
    private static final class GridBagLayoutPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        GridBagLayoutPanel() {
            super(new GridBagLayout());
            setOpaque(false);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }
    }
}

