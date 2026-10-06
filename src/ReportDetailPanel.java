import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.ScrollPaneConstants;

/**
 * ReportDetailPanel.java
 * Shows a single hazard report in a form-style layout. Every piece of
 * information sits inside its own bordered box so the fields line up and
 * the spacing between them is even. Used by both dashboards.
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
                BorderFactory.createEmptyBorder(22, 28, 22, 28)));

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

        body = new BodyPanel();
        body.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

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
        body.add(Box.createVerticalStrut(10));
        JPanel infoGrid = fieldGrid();
        infoGrid.add(UITheme.fieldBox("Report ID", report.getReportId()));
        infoGrid.add(UITheme.fieldBox("Report Type", report.getReportType()));
        infoGrid.add(UITheme.fieldBox("Reporter ID", report.getReporterId()));
        infoGrid.add(UITheme.fieldBox("Date Submitted", report.getDateSubmitted()));
        body.add(infoGrid);
        body.add(Box.createVerticalStrut(20));

        Location location = report.getLocation();
        body.add(UITheme.sectionLabel("Location"));
        body.add(Box.createVerticalStrut(10));
        JPanel locationGrid = fieldGrid();
        locationGrid.add(UITheme.fieldBox("City / Municipality", location.getCityMunicipality()));
        locationGrid.add(UITheme.fieldBox("Barangay", location.getBarangay()));
        locationGrid.add(UITheme.fieldBox("Street", location.getStreet()));
        locationGrid.add(UITheme.fieldBox("Specific Place", location.getSpecificPlace()));
        body.add(locationGrid);
        body.add(Box.createVerticalStrut(20));

        body.add(UITheme.sectionLabel(detailCaption(report.getReportType())));
        body.add(Box.createVerticalStrut(10));
        JPanel detailBox = UITheme.valueBox(report.getSpecificDetail());
        detailBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(detailBox);
        body.add(Box.createVerticalStrut(20));

        body.add(UITheme.sectionLabel("Description"));
        body.add(Box.createVerticalStrut(10));
        JPanel descriptionWrapper = new JPanel(new BorderLayout());
        descriptionWrapper.setOpaque(false);
        descriptionWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionWrapper.add(buildDescriptionArea(report.getDescription()), BorderLayout.CENTER);
        body.add(descriptionWrapper);

        body.revalidate();
        body.repaint();
        bodyScroll.getVerticalScrollBar().setValue(0);
    }

    private JPanel fieldGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        return grid;
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
        area.setBackground(UITheme.FIELD_BG);
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

    /** A vertical body that always stretches to the width of the scroll pane. */
    private static final class BodyPanel extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;

        BodyPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}

