import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * ReportFilterBar.java
 * A row of drop-down controls used to filter reports by city/municipality
 * and barangay and to sort them. It is shared by the resident dashboard and
 * the administrator dashboard. Because it is a GUI class it may use Swing
 * freely.
 */
public class ReportFilterBar extends JPanel {

    private static final long serialVersionUID = 1L;

    public static final String ALL_MUNICIPALITIES = "All Municipalities";
    public static final String ALL_BARANGAYS = "All Barangays";

    public static final String SORT_ID_ASC = "Report ID (Oldest first)";
    public static final String SORT_ID_DESC = "Report ID (Newest first)";
    public static final String SORT_DATE_DESC = "Date and Time (Newest first)";
    public static final String SORT_DATE_ASC = "Date and Time (Oldest first)";
    public static final String SORT_STATUS = "Status";
    public static final String SORT_TYPE = "Hazard Type";

    private LocationManager locationManager;
    private ActionListener onChange;
    private boolean updating = false;

    private JComboBox<String> municipalityBox;
    private JComboBox<String> barangayBox;
    private JComboBox<String> sortBox;

    public ReportFilterBar(LocationManager locationManager, ActionListener onChange) {
        this.locationManager = locationManager;
        this.onChange = onChange;

        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JLabel cityCaption = new JLabel("City / Municipality:");
        cityCaption.setFont(UITheme.FONT_BASE);
        cityCaption.setForeground(UITheme.TEXT);

        JLabel barangayCaption = new JLabel("Barangay:");
        barangayCaption.setFont(UITheme.FONT_BASE);
        barangayCaption.setForeground(UITheme.TEXT);

        JLabel sortCaption = new JLabel("Sort by:");
        sortCaption.setFont(UITheme.FONT_BASE);
        sortCaption.setForeground(UITheme.TEXT);

        municipalityBox = new JComboBox<String>();
        municipalityBox.setFont(UITheme.FONT_BASE);
        municipalityBox.addItem(ALL_MUNICIPALITIES);
        List<String> municipalities = locationManager.getMunicipalities();
        for (int i = 0; i < municipalities.size(); i++) {
            municipalityBox.addItem(municipalities.get(i));
        }

        barangayBox = new JComboBox<String>();
        barangayBox.setFont(UITheme.FONT_BASE);

        sortBox = new JComboBox<String>();
        sortBox.setFont(UITheme.FONT_BASE);
        sortBox.addItem(SORT_ID_ASC);
        sortBox.addItem(SORT_ID_DESC);
        sortBox.addItem(SORT_DATE_DESC);
        sortBox.addItem(SORT_DATE_ASC);
        sortBox.addItem(SORT_STATUS);
        sortBox.addItem(SORT_TYPE);

        municipalityBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }
                updateBarangayBox();
                fireChange();
            }
        });

        barangayBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }
                fireChange();
            }
        });

        sortBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }
                fireChange();
            }
        });

        add(cityCaption);
        add(municipalityBox);
        add(barangayCaption);
        add(barangayBox);
        add(sortCaption);
        add(sortBox);

        updateBarangayBox();
    }

    private void updateBarangayBox() {
        updating = true;
        barangayBox.removeAllItems();
        barangayBox.addItem(ALL_BARANGAYS);
        if (municipalityBox.getSelectedItem() != null) {
            String municipality = municipalityBox.getSelectedItem().toString();
            if (!municipality.equals(ALL_MUNICIPALITIES)) {
                List<String> barangays = locationManager.getBarangays(municipality);
                for (int i = 0; i < barangays.size(); i++) {
                    barangayBox.addItem(barangays.get(i));
                }
            }
        }
        barangayBox.setSelectedIndex(0);
        updating = false;
    }

    private void fireChange() {
        if (onChange != null) {
            onChange.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "filter"));
        }
    }

    /** Returns a new list containing only the reports that match the selected
     *  city/municipality and barangay, sorted by the selected sort option. */
    public List<Report> apply(List<Report> reports) {
        List<Report> result = new ArrayList<Report>();
        String municipality = selectedValue(municipalityBox, ALL_MUNICIPALITIES);
        String barangay = selectedValue(barangayBox, ALL_BARANGAYS);

        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            boolean matchMunicipality = municipality == null
                    || report.getLocation().getCityMunicipality().equals(municipality);
            boolean matchBarangay = barangay == null
                    || report.getLocation().getBarangay().equals(barangay);
            if (matchMunicipality && matchBarangay) {
                result.add(report);
            }
        }

        Collections.sort(result, comparator());
        return result;
    }

    private String selectedValue(JComboBox<String> box, String allOption) {
        if (box.getSelectedItem() == null) {
            return null;
        }
        String value = box.getSelectedItem().toString();
        if (value.equals(allOption)) {
            return null;
        }
        return value;
    }

    private Comparator<Report> comparator() {
        final String sort = (sortBox.getSelectedItem() == null)
                ? SORT_ID_ASC : sortBox.getSelectedItem().toString();
        return new Comparator<Report>() {
            @Override
            public int compare(Report first, Report second) {
                if (sort.equals(SORT_ID_DESC)) {
                    return second.getReportId().compareTo(first.getReportId());
                }
                if (sort.equals(SORT_DATE_DESC)) {
                    return second.getDateSubmitted().compareTo(first.getDateSubmitted());
                }
                if (sort.equals(SORT_DATE_ASC)) {
                    return first.getDateSubmitted().compareTo(second.getDateSubmitted());
                }
                if (sort.equals(SORT_STATUS)) {
                    return statusRank(first) - statusRank(second);
                }
                if (sort.equals(SORT_TYPE)) {
                    return first.getReportType().compareTo(second.getReportType());
                }
                return first.getReportId().compareTo(second.getReportId());
            }
        };
    }

    private int statusRank(Report report) {
        if (report.getStatus() == ReportStatus.PENDING) {
            return 0;
        }
        if (report.getStatus() == ReportStatus.VERIFIED) {
            return 1;
        }
        if (report.getStatus() == ReportStatus.IN_PROGRESS) {
            return 2;
        }
        return 3;
    }
}

