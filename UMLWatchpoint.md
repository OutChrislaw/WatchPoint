# WatchPoint — UML / Class Design Reference

Community Safety Reporting System (Java + Java Swing) for the province of **Bohol, Philippines**.
This file lists every class of the system with its **attributes** and **methods** (names exactly as they
appear in the source code) and describes how the classes are **connected** to each other.
It is a textual description of the UML design, not a drawn diagram.

The code is split into five packages. Source files live under `src/`, the compiled classes under
`bin/`, and the data files under `data/`. GUI classes are documented by their **methods** only.

| Package | Folder | Contents |
| --- | --- | --- |
| `app` | `src/app` | the entry point |
| `model` | `src/model` | the domain classes |
| `manager` | `src/manager` | logic, validation and persistence |
| `storage` | `src/storage` | file reading and writing |
| `ui` | `src/ui` | Java Swing windows, panels and look-and-feel helpers |

---

## 1. System overview

| Layer | Classes |
| --- | --- |
| Entry point | `WatchPoint` (`app`) |
| Domain model (no Swing, no file access) | `User`, `Resident`, `Administrator`, `AreaOfficer`, `Location`, `Report`, `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport`, `ReportStatus` |
| Managers (validation, logic, persistence) | `UserManager`, `ReportManager`, `LocationManager` |
| Storage | `FileManager` |
| GUI (Swing) | `UITheme`, `FolderTabbedPaneUI`, `LoginFrame`, `RegistrationFrame`, `ResidentDashboard`, `AreaOfficerDashboard`, `AdminDashboard`, `OverviewPanel`, `ProfileDialog`, `UserCreateDialog`, `UserUpdateDialog`, `ReportUpdateDialog`, `ReportStatusDialog` |
| Data files (plain text) | `data/users.txt`, `data/reports.txt`, `data/locations.txt` |

Inheritance at a glance:

```
User   <-- Resident, Administrator, AreaOfficer
Report <-- RoadHazardReport, FloodReport, StreetlightReport, OtherHazardReport
```

Layering: `ui` calls `manager`, `manager` uses `model` and `storage`, and only `storage` touches the
text files. The GUI never reads or writes a data file; the managers never use Swing.

---

## 2. Entry point

### `app.WatchPoint`
- **Attributes:** none.
- **Methods:**
  - `public static void main(String[] args)`
- **Relationships:**
  - Creates `FileManager` (folder `"data"`), then `UserManager`, `ReportManager` and `LocationManager`
    from that `FileManager`.
  - Creates `LoginFrame(userManager, reportManager, locationManager)` inside
    `SwingUtilities.invokeLater(...)`.
  - Started with `java -cp bin app.WatchPoint`.

---

## 3. Domain model (`model`)

### `User` (abstract)
- **Attributes**
  - `private String userId`
  - `private String password`
  - `private String fullName`
  - `private String dateCreated`
- **Methods**
  - `protected User(String userId, String password, String fullName, String dateCreated)`
  - `public String getUserId()`
  - `public String getFullName()`
  - `public String getDateCreated()`
  - `public void setDateCreated(String dateCreated)`
  - `public void setFullName(String fullName)`
  - `public void setPassword(String password)`
  - `public boolean verifyPassword(String input)`
  - `public abstract String getUserType()`
  - `public String toFileString()`
- **Relationships**
  - Superclass of `Resident`, `Administrator` and `AreaOfficer`; each subclass calls `super(...)`.
  - `toFileString()` writes `type|userId|password|fullName|dateCreated` and is extended by the
    subclasses (polymorphism); `UserManager.saveUsers()` and `parseUser(...)` use it.
  - `UserManager` stores `User` objects and calls `verifyPassword(...)`, `getUserId()`,
    `getFullName()`, `getDateCreated()` and `toFileString()`.
  - There is **no username on `User`**: only a `Resident` has one. Administrators and area officers
    sign in with their id.

### `Resident` (extends `User`)
- **Attributes**
  - `private String username`
  - `private String address`
  - `private String contactNumber`
- **Methods**
  - `public Resident(String userId, String username, String password, String fullName, String dateCreated, String address, String contactNumber)`
  - `public String getUsername()`
  - `public void setUsername(String username)`
  - `public String getAddress()`
  - `public void setAddress(String address)`
  - `public String getContactNumber()`
  - `public void setContactNumber(String contactNumber)`
  - `@Override public String getUserType()` — returns `"Resident"`
  - `@Override public String toFileString()` — appends `username`, `address`, `contactNumber`
- **Relationships**
  - `extends User` (inherits `userId`, `password`, `fullName`, `dateCreated`).
  - Created by `UserManager.registerResident(...)` and `UserManager.parseUser(...)`.
  - `UserManager.login(...)` matches the username case-insensitively as well as the id, so residents
    can sign in with either.
  - `UserManager.getContactById(...)` returns `contactNumber` for the report detail of the Area
    Officer and the Administrator.
  - Edited through `ProfileDialog.saveDetails()` (full name, username, contact, address) and
    `UserUpdateDialog.save()`.
  - Held by `ResidentDashboard` as `Resident currentUser`. `address` is composed as
    `barangay + ", " + cityMunicipality`; `UserManager.municipalityOf(...)` / `barangayOf(...)` split
    it again for the drop-downs.

### `Administrator` (extends `User`)
- **Attributes:** none of its own (inherits all `User` attributes).
- **Methods**
  - `public Administrator(String userId, String password, String fullName, String dateCreated)`
  - `@Override public String getUserType()` — returns `"Administrator"`
  - `@Override public String toFileString()`
- **Relationships**
  - `extends User`.
  - Created by `UserManager.addAdministrator(...)` and `UserManager.parseUser(...)`.
  - Held by `AdminDashboard` as `Administrator currentUser`; the id (`AD` + six digits) is the login.
  - `AdminDashboard` passes `true` to `ReportManager.reopenReport(report, administrator)`, which is the
    only way a rejected report can be reopened.

### `AreaOfficer` (extends `User`)
- **Attributes**
  - `private String area`
- **Methods**
  - `public AreaOfficer(String userId, String password, String fullName, String dateCreated, String area)`
  - `public String getArea()`
  - `public void setArea(String area)`
  - `@Override public String getUserType()` — returns `"Area Officer"`
  - `@Override public String toFileString()` — appends `area`
- **Relationships**
  - `extends User`.
  - Created by `UserManager.addAreaOfficer(...)` and `UserManager.parseUser(...)`; the area is checked
    by `UserManager.areaProblem(...)` and chosen from `LocationManager.getMunicipalities()`.
  - Held by `AreaOfficerDashboard` as `AreaOfficer currentOfficer`; every report query it makes is
    limited to `getArea()` and every status change goes through
    `ReportManager.changeStatusInCity(..., currentOfficer.getArea())`.
  - The area can only be changed by an administrator (`UserUpdateDialog`).

### `Location`
- **Attributes**
  - `private String cityMunicipality`
  - `private String barangay`
  - `private String street`
  - `private String specificPlace`
- **Methods**
  - `public Location(String cityMunicipality, String barangay, String street, String specificPlace)`
  - `public String getCityMunicipality()`
  - `public String getBarangay()`
  - `public String getStreet()`
  - `public String getSpecificPlace()`
  - `public String getFullLocation()`
- **Relationships**
  - **Composition:** every `Report` *has-a* `Location` (`Report.location`).
  - Created by `ResidentDashboard.submitReport()`, `ReportUpdateDialog.save()` and
    `ReportManager.parseReport(...)`; `ReportManager.submitReport(...)` rebuilds it with trimmed
    fields before saving.
  - `getCityMunicipality()` / `getBarangay()` drive filtering (`ReportManager.getReports(...)`),
    the Area Officer's area check and the `OverviewPanel` counts.

### `ReportStatus` (enum)
- **Constants:** `PENDING`, `VERIFIED`, `IN_PROGRESS`, `RESOLVED`, `REJECTED`
- **Methods**
  - `public boolean isFinal()`
  - `public boolean canMoveTo(ReportStatus target)`
  - `public List<ReportStatus> nextStatuses()`
  - `public String displayName()`
- **Allowed transitions** (the rule itself lives in `canMoveTo(...)`):

| From | Allowed next status |
| --- | --- |
| `PENDING` | `VERIFIED`, `REJECTED` |
| `VERIFIED` | `IN_PROGRESS`, `REJECTED` |
| `IN_PROGRESS` | `RESOLVED`, `REJECTED` |
| `RESOLVED` | none (final) |
| `REJECTED` | none (final; an administrator may reopen it through `ReportManager.reopenReport(...)`) |

- **Relationships**
  - Type of `Report.status`.
  - `ReportManager.parseStatus(...)` reads it from `reports.txt`; `Report.toFileString()` writes
    `name()`.
  - `ReportManager.statusProblem(...)` and `changeStatus(...)` enforce the table above; `REJECTED`
    also requires a reason (`ReportManager.remarkProblem(...)`).
  - `UITheme.statusColor(String)` gives each status its colour for tables and badges;
    `OverviewPanel` counts reports per status.
  - `ReportStatusDialog` builds its radio buttons from `nextStatuses()` and prints `displayName()`.

### `Report` (abstract)
- **Attributes**
  - `private String reportId`
  - `private String reporterId`
  - `private Location location`
  - `private String description`
  - `private ReportStatus status`
  - `private String dateSubmitted`
  - `private String remark`
- **Methods**
  - `protected Report(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted)`
  - `public String getReportId()`
  - `public String getReporterId()`
  - `public Location getLocation()`
  - `public String getDescription()`
  - `public ReportStatus getStatus()`
  - `public String getDateSubmitted()`
  - `public void setLocation(Location location)`
  - `public void setDescription(String description)`
  - `public void setStatus(ReportStatus status)`
  - `public boolean canChangeStatusTo(ReportStatus target)`
  - `public String getRemark()`
  - `public void setRemark(String remark)`
  - `public abstract String getReportType()`
  - `public abstract String getSpecificDetail()`
  - `public abstract void setSpecificDetail(String specificDetail)`
  - `public String toFileString()`
- **Relationships**
  - Superclass of `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport`.
  - **Composition:** `Report` *has-a* `Location` (`location`) and *has-a* `ReportStatus` (`status`);
    the constructor sets `remark` to `""`.
  - `Remark` carries the reason written when the report is rejected (empty for every other status)
    and is shown by the resident, the Area Officer and the Administrator.
  - `toFileString()` writes the 12 fields of `reports.txt`; `ReportManager.saveReports()` writes every
    report through it and `parseReport(...)` rebuilds them.
  - `ReportManager` creates, stores, updates and deletes reports; both dashboards hold
    `List<Report>` and render one with `reportForm(Report report)`.

### `RoadHazardReport` (extends `Report`)
- **Attributes**
  - `private String roadHazardType`
- **Methods**
  - `public RoadHazardReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String roadHazardType)`
  - `@Override public String getReportType()` — returns `"Road Hazard"`
  - `@Override public String getSpecificDetail()` — returns `roadHazardType`
  - `@Override public void setSpecificDetail(String specificDetail)` — replaces `roadHazardType`
- **Relationships**
  - Created by `ReportManager.submitReport(...)` when the hazard type is `"Road Hazard"`, and by
    `ReportManager.parseReport(...)`.
  - `getSpecificDetail()` is shown under the caption `"Road Hazard Type"`
    (`UITheme.detailCaption(...)`).

### `FloodReport` (extends `Report`)
- **Attributes**
  - `private String waterLevel`
- **Methods**
  - `public FloodReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String waterLevel)`
  - `@Override public String getReportType()` — returns `"Flood"`
  - `@Override public String getSpecificDetail()` — returns `waterLevel`
  - `@Override public void setSpecificDetail(String specificDetail)`
- **Relationships**
  - Created by `ReportManager.submitReport(...)` / `parseReport(...)` for `"Flood"`.
  - Displayed under the caption `"Water Level"`.

### `StreetlightReport` (extends `Report`)
- **Attributes**
  - `private String poleNumber`
- **Methods**
  - `public StreetlightReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String poleNumber)`
  - `@Override public String getReportType()` — returns `"Streetlight"`
  - `@Override public String getSpecificDetail()` — returns `poleNumber`
  - `@Override public void setSpecificDetail(String specificDetail)`
- **Relationships**
  - Created by `ReportManager.submitReport(...)` / `parseReport(...)` for `"Streetlight"`.
  - Displayed under the caption `"Pole Number"`.

### `OtherHazardReport` (extends `Report`)
- **Attributes**
  - `private String hazardCategory`
- **Methods**
  - `public OtherHazardReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String hazardCategory)`
  - `@Override public String getReportType()` — returns `"Other"`
  - `@Override public String getSpecificDetail()` — returns `hazardCategory`
  - `@Override public void setSpecificDetail(String specificDetail)`
- **Relationships**
  - Created by `ReportManager.submitReport(...)` / `parseReport(...)` for every hazard type that is not
    `"Road Hazard"`, `"Flood"` or `"Streetlight"` (the accepted list is
    `ReportManager.HAZARD_TYPES`).
  - Displayed under the caption `"Hazard Category"`.

---

## 4. Managers (`manager`)

### `UserManager`
- **Attributes**
  - `private List<User> users`
  - `private FileManager fileManager`
- **Methods**
  - `public UserManager(FileManager fileManager)` — calls `loadUsers()`
  - `public boolean registerResident(String fullName, String username, String password, String address, String contactNumber)`
  - `public User login(String loginId, String password)`
  - `public User getUserById(String userId)`
  - `public List<User> getAllUsers()`
  - `public boolean updateUser(User user)`
  - `public boolean deleteUser(String userId)`
  - `public String getFullNameById(String userId)`
  - `public String getContactById(String userId)`
  - `public Administrator addAdministrator(String fullName, String password)`
  - `public AreaOfficer addAreaOfficer(String fullName, String password, String area)`
  - `public String checkUsername(User user, String username)`
  - `public String saveDetails(User user, String fullName, String username, String contactNumber, String barangay, String municipality)`
  - `public String changePassword(User user, String currentPassword, String newPassword, String confirmPassword)`
  - `public boolean saveAccountChanges(User user, String fullName, String password, String address, String contactNumber, String area)`
  - `public int countAdministrators()`
  - `public List<User> searchUsers(String keyword)`
  - `public List<User> searchUsers(String keyword, String userType)`
  - `public static String fullNameProblem(String fullName)`
  - `public static String passwordProblem(String password)`
  - `public static String contactNumberProblem(String contactNumber)`
  - `public static String areaProblem(String area)`
  - `public static String municipalityOf(String address)`
  - `public static String barangayOf(String address)`
  - `private static String forbiddenCharacterProblem(String value)`
  - `private static boolean looksLikeAccountId(String text)`
  - `private boolean isUsernameTaken(String username)`
  - `private void loadUsers()`
  - `private void saveUsers()`
  - `private static String formatId(String prefix, int number)`
  - `private String generateUserId()`
  - `private String generateAreaOfficerId()`
  - `private String generateAdministratorId()`
  - `private String currentTimestamp()`
  - `private User parseUser(String line)`
- **Relationships**
  - Depends on `FileManager` (`users.txt`); aggregates `User` objects and *creates* `Resident`,
    `Administrator` and `AreaOfficer` (register / add / `parseUser` methods).
  - The four static `...Problem(...)` methods are the validation rules shared with the GUI; every
    account method re-checks them before saving, so no window can bypass them.
  - `municipalityOf(...)` / `barangayOf(...)` split the stored address for `ProfileDialog` and
    `UserUpdateDialog`; `currentTimestamp()` fills `dateCreated`; `formatId(...)` builds
    `U` / `AD` / `AO` + six digits.
  - Used by `LoginFrame` (`login(...)`), `RegistrationFrame` (validation + `registerResident(...)`),
    `ProfileDialog` (`saveDetails(...)`, `changePassword(...)`), `UserCreateDialog`
    (`addAdministrator(...)`, `addAreaOfficer(...)`), `UserUpdateDialog` (`saveAccountChanges(...)`)
    and `AdminDashboard` (`searchUsers(...)`, `deleteUser(...)`, `countAdministrators()`,
    `getFullNameById(...)`, `getContactById(...)`).

### `ReportManager`
- **Attributes**
  - `private List<Report> reports`
  - `private FileManager fileManager`
- **Constants**
  - `public static final String[] HAZARD_TYPES = { "Road Hazard", "Flood", "Streetlight", "Other" }`
- **Methods**
  - `public ReportManager(FileManager fileManager)` — calls `loadReports()`
  - `public Report submitReport(String reporterId, String hazardType, Location location, String description, String specificDetail)`
  - `public Report getReportById(String reportId)`
  - `public List<Report> getReportsByReporter(String reporterId)`
  - `public List<Report> getAllReports()`
  - `public boolean updateResidentReport(Report report, String residentId)`
  - `public boolean deleteResidentReport(String reportId, String residentId)`
  - `public boolean deleteInvalidReport(String reportId)`
  - `public boolean updateReportStatus(String reportId, ReportStatus status)`
  - `public boolean updateReportStatus(String reportId, ReportStatus status, String remark)`
  - `public boolean updateReportStatusInCity(String reportId, ReportStatus status, String cityMunicipality)`
  - `public boolean updateReportStatusInCity(String reportId, ReportStatus status, String cityMunicipality, String remark)`
  - `public String changeStatus(Report report, ReportStatus target, String reason)`
  - `public String changeStatusInCity(Report report, ReportStatus target, String reason, String cityMunicipality)`
  - `public String reopenReport(Report report, boolean administrator)`
  - `public static String statusProblem(Report report, ReportStatus target, String reason)`
  - `public static ReportStatus parseStatus(String text)`
  - `public static String remarkProblem(String remark)`
  - `public List<Report> getReports(String cityMunicipality, String barangay, String sortMode)`
  - `public List<Report> getReportsInCity(String cityMunicipality)`
  - `public int countReports(String cityMunicipality, ReportStatus status)`
  - `public List<String[]> countByBarangay(String cityMunicipality)`
  - `public List<String[]> countByHazardType(String cityMunicipality)`
  - `public Report oldestPending(String cityMunicipality)`
  - `public static int daysWaiting(String dateSubmitted)`
  - `public boolean hasPendingDuplicate(String reporterId, String hazardType, String barangay, String specificDetail)`
  - `public static String oneLine(String text)`
  - `public static String reportProblem(String hazardType, Location location, String description, String specificDetail)`
  - `private static String forbiddenCharacterProblem(String street, String place, String detail, String description)`
  - `private static String nextStatusesText(ReportStatus status)`
  - `private List<String[]> countBy(String cityMunicipality, boolean byBarangay)`
  - `private static void addTotal(List<String> names, List<Integer> totals, String name)`
  - `private static void sort(List<Report> list, String sortMode)`
  - `private void loadReports()`
  - `private void saveReports()`
  - `private Report parseReport(String line)`
  - `private String currentTimestamp()`
  - `private String generateReportId()`
- **Relationships**
  - Depends on `FileManager` (`reports.txt`); aggregates `Report` objects and *creates* the four
    concrete report types in `submitReport(...)` and `parseReport(...)`.
  - **Workflow gatekeeper:** `statusProblem(...)` applies `ReportStatus.canMoveTo(...)` and the
    rejection-reason rule; `changeStatus(...)` / `changeStatusInCity(...)` validate, set the status
    (and remark), then call `saveReports()`; `reopenReport(...)` turns a `REJECTED` report back into
    `PENDING` and only runs for an administrator. The old `updateReportStatus...` methods delegate to
    them, so both the GUI and any other caller follow the same rules.
  - **Statistics for the overview:** `countReports(...)`, `countByBarangay(...)`,
    `countByHazardType(...)`, `oldestPending(...)` and `daysWaiting(...)` feed `OverviewPanel`.
  - **Validation:** `reportProblem(...)` checks hazard type, location, description and specific
    detail; `remarkProblem(...)` checks rejection reasons; `oneLine(...)` collapses line breaks so a
    description stays on one line; `hasPendingDuplicate(...)` blocks the same pending hazard twice.
  - Used by `ResidentDashboard` (`submitReport`, `getReports`, `getReportsByReporter`,
    `updateResidentReport`, `deleteResidentReport`), `ReportUpdateDialog`
    (`updateResidentReport`, `reportProblem`, `oneLine`), `AreaOfficerDashboard`
    (`getReports`, `changeStatusInCity`, `oldestPending` through the panel), `AdminDashboard`
    (`getReports`, `changeStatus`, `reopenReport`, `deleteInvalidReport`, counts) and
    `OverviewPanel`.

### `LocationManager`
- **Attributes**
  - `private List<String> lines`
  - `private FileManager fileManager`
- **Methods**
  - `public LocationManager(FileManager fileManager)` — reads `locations.txt`
  - `public List<String> getMunicipalities()`
  - `public List<String> getBarangays(String municipality)`
  - `private String municipalityOf(String line)`
- **Relationships**
  - Depends on `FileManager` (`locations.txt`).
  - Supplies the Bohol city/municipality and barangay drop-down data to `RegistrationFrame`,
    `ProfileDialog`, `UserCreateDialog` (area list), `UserUpdateDialog` (area list),
    `ResidentDashboard` (submit form and filters), `ReportUpdateDialog` (city/barangay),
    `AdminDashboard` (filters) and `AreaOfficerDashboard` (barangay filter only, because the area is
    fixed to the officer's own municipality).

---

## 5. Storage (`storage`)

### `FileManager`
- **Attributes**
  - `private String dataFolder`
- **Methods**
  - `public FileManager(String dataFolder)` — also calls `ensureFileExists("users.txt")`,
    `ensureFileExists("reports.txt")`, `ensureFileExists("locations.txt")`
  - `public List<String> readLines(String fileName)`
  - `public void writeLines(String fileName, List<String> lines)`
  - `private void ensureFileExists(String fileName)`
- **Relationships**
  - **Lowest layer:** used by `UserManager`, `ReportManager` and `LocationManager` (each keeps it as
    `private FileManager fileManager`) and created once in `app.WatchPoint`.
  - `readLines(...)` skips blank lines and `writeLines(...)` writes one line per element, so a data
    file can be edited by hand without breaking the managers.

---

## 6. GUI (`ui`)

GUI classes are listed with their **methods** and relationships only. Their Swing components are
private fields created in `initComponents()` or in the `build...()` methods and are not part of the
design description.

### `UITheme` (final, utility class — never instantiated)
- **Constants (summary):** the palette `PRIMARY`, `PRIMARY_DARK`, `ACCENT`, `ACCENT_DARK`, `LIME`,
  `SKY`, `MUTED`, `TEXT`, `BORDER`, `CARD`, `DANGER`, `PAGE`, `ROW_LINE`, `SELECTION`, `TAB_FILL`,
  `TAB_LINE`; the fonts `FONT_TITLE`, `FONT_HEADING`, `FONT_SECTION`, `FONT_BOLD`, `FONT_BASE`,
  `FONT_SMALL`, `FONT_VALUE`, `FONT_TAB`.
- **Methods**
  - `private UITheme()`
  - `public static void styleTabs(JTabbedPane tabs)`
  - `public static JPanel page()`
  - `public static JPanel card()`
  - `public static JLabel label(String text, Font font, Color color)`
  - `public static JLabel hint(String text)`
  - `public static JButton button(String text, Color background, Color foreground)`
  - `public static Color shade(Color color)`
  - `public static JComboBox<String> combo(String firstItem, List<String> items)`
  - `public static JComboBox<String> options(String[] values)`
  - `public static String text(JComboBox<String> box)`
  - `public static JScrollPane borderedScroll(Component view)`
  - `public static JScrollPane detailScroll()`
  - `public static JPanel detailPane(JScrollPane scroll, JPanel actions)`
  - `public static JTable dataTable(Object[] columns)`
  - `public static JPanel fieldBox(String caption, String value)`
  - `public static JPanel valueBox(String value)`
  - `public static JPanel descriptionBox(String text)`
  - `public static JPanel twoColumns(JPanel left, JPanel right)`
  - `public static void addSection(JPanel body, String title, String[][] fields)`
  - `public static JPanel cardBody()`
  - `public static JPanel emptyDetail(String message)`
  - `public static void addFormRow(JPanel form, int row, String caption, Component field)`
  - `public static JPanel actionRow(javax.swing.JComponent... items)`
  - `public static JPasswordField passwordField(int columns)`
  - `public static void selectItem(JComboBox<String> box, String value)`
  - `public static String display(String text)`
  - `public static Color statusColor(String status)`
  - `public static String detailCaption(String reportType)`
  - `public static JTextField textField(int columns)`
  - `public static void digitsOnly(JTextField field, final int maxDigits)`
  - `public static void maxLength(JTextField field, final int maxLength)`
  - `public static JTextField hintField(final String hint, int columns)`
  - `public static JTextArea hintArea(final String hint, int rows, int columns)`
  - `public static DefaultTableCellRenderer statusRenderer()`
- **Inner classes (private, styling only)**
  - `HeaderRenderer extends DefaultTableCellRenderer` — `getTableCellRendererComponent(...)`; the dark
    green table header with white bold text.
  - `StatusRenderer extends DefaultTableCellRenderer` — `getTableCellRendererComponent(...)`; colours
    the status column through `statusColor(...)`.
  - `BodyPanel extends JPanel implements Scrollable` —
    `getPreferredScrollableViewportSize()`, `getScrollableUnitIncrement(...)`,
    `getScrollableBlockIncrement(...)`, `getScrollableTracksViewportWidth()`,
    `getScrollableTracksViewportHeight()`; the white wrapping card body returned by `cardBody()`.
  - Anonymous subclasses inside `button(...)` (minimum height), `hintField(...)` / `hintArea(...)`
    (painted placeholder text) and `digitsOnly(...)` / `maxLength(...)` (`DocumentFilter`).
- **Relationships**
  - Shared look-and-feel helper used by **every** class in `ui`.
  - The only domain reference is `ReportStatus`, reached through `statusColor(String status)`.
  - `styleTabs(...)` installs `FolderTabbedPaneUI` on the dashboards' tab strips.

### `FolderTabbedPaneUI` (extends `javax.swing.plaf.basic.BasicTabbedPaneUI`)
- **Constants (summary):** `TAB_HEIGHT = 40`, `SLANT = 12`, `EXTRA_WIDTH = 46` (private
  `static final`).
- **Methods**
  - `protected void installDefaults()`
  - `protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight)`
  - `protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics)`
  - `protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected)`
  - `protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected)`
  - `protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected)`
  - `protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected)`
  - `protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected)`
  - `protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected)`
  - `protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex)`
  - `private Polygon tabShape(int x, int y, int w, int h)`
- **Relationships**
  - Installed by `UITheme.styleTabs(...)` on `AdminDashboard`, `ResidentDashboard` and
    `AreaOfficerDashboard`.
  - Reads the palette (`UITheme.CARD`, `TAB_FILL`, `TAB_LINE`, `BORDER`, `PAGE`, `PRIMARY`, `MUTED`)
    and `UITheme.FONT_TAB`.

### `LoginFrame` (extends `javax.swing.JFrame`)
- **Methods**
  - `public LoginFrame()`
  - `public LoginFrame(UserManager userManager, ReportManager reportManager, LocationManager locationManager)`
  - `private void styleFrame()`
  - `public void handleLogin()`
  - `public void openRegistration()`
  - `public void showMessage(String message)`
  - `private void initComponents()`
  - `private void loginButtonActionPerformed(java.awt.event.ActionEvent evt)`
  - `private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt)`
  - `private void usernameFieldActionPerformed(java.awt.event.ActionEvent evt)`
- **Relationships**
  - Created by `app.WatchPoint`.
  - `handleLogin()` reads the id/username and password, refuses empty input, then calls
    `userManager.login(loginId, password)` and opens the dashboard that matches
    `user.getUserType()`: `AdminDashboard` (cast to `Administrator`), `AreaOfficerDashboard` (cast to
    `AreaOfficer`) or `ResidentDashboard` (cast to `Resident`), passing `reportManager`,
    `userManager` and `locationManager` along; the login window then disposes itself.
  - `openRegistration()` creates `RegistrationFrame(userManager, locationManager)`, centres it on the
    login window and brings the login window back to the front when sign-up closes.
  - Styled with `UITheme` (`card()`, `label(...)`, `button(...)`, `textField(...)`, `maxLength(...)`).

### `RegistrationFrame` (extends `javax.swing.JFrame`)
- **Methods**
  - `public RegistrationFrame()`
  - `public RegistrationFrame(UserManager userManager, LocationManager locationManager)`
  - `private void styleFrame()`
  - `private void updateBarangayBox()`
  - `public void register()`
  - `public void clearFields()`
  - `private void initComponents()`
  - `private void fullNameFieldActionPerformed(java.awt.event.ActionEvent evt)`
  - `private void usernameFieldActionPerformed(java.awt.event.ActionEvent evt)`
  - `private void passwordFieldActionPerformed(java.awt.event.ActionEvent evt)`
  - `private void contactNumberFieldActionPerformed(java.awt.event.ActionEvent evt)`
- **Relationships**
  - Created by `LoginFrame.openRegistration()`; its close operation only disposes itself.
  - Fills `cityMunicipalityBox` / `barangayBox` from `LocationManager` (`getMunicipalities()`,
    `getBarangays(...)`); `updateBarangayBox()` is the dependent drop-down listener.
  - `register()` checks the entered values with `UserManager.fullNameProblem(...)`,
    `userManager.checkUsername(null, ...)`, `UserManager.passwordProblem(...)`,
    `UserManager.contactNumberProblem(...)` and a password/confirm comparison, builds the address
    `barangay + ", " + cityMunicipality`, and calls
    `userManager.registerResident(fullName, username, password, address, contactNumber)`.
  - Applies `UITheme.maxLength(...)` to the name and username and `UITheme.digitsOnly(...)` to the
    contact number.

### `ResidentDashboard` (extends `javax.swing.JFrame`)
- **Methods**
  - `public ResidentDashboard()`
  - `public ResidentDashboard(Resident currentUser, ReportManager reportManager, UserManager userManager, LocationManager locationManager)`
  - `private JPanel buildAllReportsTab()`
  - `private JPanel buildFilterRow()`
  - `private void updateFilterBarangays()`
  - `public void showAllReports()`
  - `private void refreshAllReports()`
  - `private static String filterValue(JComboBox<String> box, String allOption)`
  - `private void showSelectedPublicReport()`
  - `private Report selectedPublicReport()`
  - `private JPanel buildSubmitTab()`
  - `private void updateBarangayBox()`
  - `private JPanel buildReportsTab()`
  - `private JPanel buildReportButtonRow()`
  - `private void showSelectedReport()`
  - `private Report selectedReport()`
  - `public void showOwnReports()`
  - `public void submitReport()`
  - `public void updateSelectedReport()`
  - `public void deleteSelectedReport()`
  - `public void logout()`
  - `private void showMessage(String message)`
  - `private JPanel reportForm(Report report)`
  - `public void openSettings()`
  - `private void applyTheme()`
  - `private void initComponents()`
  - `private void profileButtonActionPerformed(java.awt.event.ActionEvent evt)`
- **Relationships**
  - Created by `LoginFrame.handleLogin()` with a `Resident`.
  - Three tabs: **Reports** (public list), **Submit Report**, **My Reports**.
  - Uses `ReportManager`: `getReports(city, barangay, sort)` for the public list,
    `getReportsByReporter(currentUser.getUserId())` for My Reports, `submitReport(...)`,
    `hasPendingDuplicate(...)`, `updateResidentReport(...)`, `deleteResidentReport(...)`.
  - `refreshAllReports()` **drops `REJECTED` reports** from the public list, so invalid reports are
    only visible to their owner; `showOwnReports()` keeps them and `reportForm(...)` shows the
    `"Rejection reason"` block from `report.getRemark()`.
  - `submitReport()` validates with `ReportManager.reportProblem(...)`, normalises the text with
    `ReportManager.oneLine(...)`, replaces an empty street with `"N/A"` and creates the `Location`
    before calling `submitReport(...)`; only `PENDING` reports can be updated or deleted.
  - `openSettings()` opens `ProfileDialog` and calls `logout()` when the dialog asks for it;
    `logout()` creates a new `LoginFrame` and disposes this window.
  - Styled with `UITheme`; the tab strip is a `FolderTabbedPaneUI`.

### `AreaOfficerDashboard` (extends `javax.swing.JFrame`)
- **Methods**
  - `public AreaOfficerDashboard()`
  - `public AreaOfficerDashboard(AreaOfficer currentOfficer, ReportManager reportManager, UserManager userManager, LocationManager locationManager)`
  - `private void initComponents()`
  - `private JPanel buildReportsTab()`
  - `private JPanel buildFilterRow()`
  - `private JPanel buildReportActions()`
  - `public void showAreaReports()`
  - `private void refreshReports()`
  - `private static String filterValue(JComboBox<String> box, String allOption)`
  - `private void showSelectedReport()`
  - `private Report selectedReport()`
  - `public void updateReportStatus()`
  - `private JPanel reportForm(Report report)`
  - `public void logout()`
  - `private void showMessage(String message)`
  - `public void openSettings()`
- **Relationships**
  - Created by `LoginFrame.handleLogin()` with an `AreaOfficer`.
  - Two tabs: **Overview** (`OverviewPanel` built with `currentOfficer.getArea()`, first tab, refreshed
    whenever it is selected) and **Reports**.
  - `refreshReports()` only asks for `reportManager.getReports(currentOfficer.getArea(), barangay, sort)`,
    so the officer never sees another municipality.
  - `updateReportStatus()` opens `ReportStatusDialog(this, report, false)` (no reopen option) and
    applies the result through
    `reportManager.changeStatusInCity(report, target, reason, currentOfficer.getArea())`; on success it
    refreshes the table, the detail pane and the overview.
  - `reportForm(...)` shows the reporter's contact number (`userManager.getContactById(...)`) and the
    rejection reason when a report has one.
  - `openSettings()` opens `ProfileDialog`; `logout()` returns to `LoginFrame`.

### `AdminDashboard` (extends `javax.swing.JFrame`)
- **Methods**
  - `public AdminDashboard()`
  - `public AdminDashboard(Administrator currentUser, ReportManager reportManager, UserManager userManager, LocationManager locationManager)`
  - `private JPanel buildReportsTab()`
  - `private JPanel buildFilterRow()`
  - `private void updateFilterBarangays()`
  - `private JPanel buildReportActions()`
  - `private JPanel buildUserFilterRow()`
  - `private JPanel buildUsersTab()`
  - `private JPanel buildUserControls()`
  - `private JPanel buildUserActions()`
  - `public void showAllReports()`
  - `private void refreshReports()`
  - `private static String filterValue(JComboBox<String> box, String allOption)`
  - `private void showSelectedReport()`
  - `private Report selectedReport()`
  - `public void updateReportStatus()`
  - `public void deleteInvalidReport()`
  - `private void showSelectedUser()`
  - `private static String loginName(User user)`
  - `private User selectedUser()`
  - `public void manageUsers()`
  - `public void createUser()`
  - `public void updateUser()`
  - `public void deleteUser()`
  - `public void logout()`
  - `private void showMessage(String message)`
  - `private JPanel reportForm(Report report)`
  - `private JPanel userForm(User user)`
  - `public void openSettings()`
  - `private void applyTheme()`
  - `private void initComponents()`
  - `private void profileButtonActionPerformed(java.awt.event.ActionEvent evt)`
- **Relationships**
  - Created by `LoginFrame.handleLogin()` with an `Administrator`.
  - Three tabs: **Overview** (`OverviewPanel` built with `null`, i.e. the whole province),
    **Reports** and **Users**.
  - `refreshReports()` uses `reportManager.getReports(city, barangay, sort)` and fills the table with
    the reporter's name from `userManager.getFullNameById(...)`.
  - `updateReportStatus()` opens `ReportStatusDialog(this, report, true)`; it then calls
    `reportManager.changeStatus(report, target, reason)` or, when the dialog asked for it,
    `reportManager.reopenReport(report, true)`. Every result message is shown to the user, and on
    success the table, the detail pane and the overview are refreshed.
  - `deleteInvalidReport()` deletes a report outright (still available for spam);
    `deleteUser()` refuses to delete the signed-in account or the last administrator, and warns when a
    resident still has reports.
  - `manageUsers()` filters accounts through `userManager.searchUsers(keyword, userType)`.
    `createUser()` opens `UserCreateDialog`, `updateUser()` opens `UserUpdateDialog`.
  - `reportForm(...)` shows the reporter's contact number and the rejection reason; `userForm(...)`
    shows the account card (id, type, username, full name, creation date, area).
  - `openSettings()` opens `ProfileDialog`; `logout()` returns to `LoginFrame`.

### `OverviewPanel` (extends `javax.swing.JPanel`)
- **Methods**
  - `public OverviewPanel(ReportManager reportManager, String cityMunicipality)` — `null` means the
    whole province
  - `public void refresh()`
  - `private void addStatusSection(JPanel body)`
  - `private void addTypeSection(JPanel body)`
  - `private void addBarangaySection(JPanel body)`
  - `private void addOldestSection(JPanel body)`
  - `private String[][] asFields(List<String[]> totals)`
  - `private String count(ReportStatus status)`
  - `private String title()`
- **Relationships**
  - **Composed by** `AreaOfficerDashboard` (its own municipality) and `AdminDashboard` (`null`), always
    as the first tab.
  - Reads everything from `ReportManager`: `countReports(...)`, `countByHazardType(...)`,
    `countByBarangay(...)`, `oldestPending(...)` and `ReportManager.daysWaiting(...)`.
  - Built with `UITheme` (`cardBody()`, `addSection(...)`, `borderedScroll(...)`, `label(...)`,
    `button(...)`); the Refresh button calls `refresh()` again.

### `ProfileDialog`
- **Methods**
  - `public ProfileDialog(Window owner, User user, UserManager userManager, LocationManager locationManager)`
  - `public boolean showDialog()`
  - `public boolean isLogoutRequested()`
  - `private void buildWindow(Window owner)`
  - `private JPanel buildMenu()`
  - `private JButton menuButton(String text)`
  - `private void showCard(String name)`
  - `private static void paintMenuButton(JButton button, boolean selected)`
  - `private JPanel buildDetailsCard()`
  - `private JPanel buildPasswordCard()`
  - `private void fillBarangays()`
  - `private void requestLogout()`
  - `private void saveDetails()`
  - `private void savePassword()`
- **Relationships**
  - Opened by `openSettings()` of the three dashboards (the **Settings** button in the header).
  - Two cards: **Account Details** (full name, username for residents, contact number, city /
    barangay, and a read-only id or area) and **Change Password**.
  - `saveDetails()` collects the values and calls
    `userManager.saveDetails(user, fullName, username, contactNumber, barangay, municipality)`, which
    applies the name, username, contact and address rules.
  - `savePassword()` calls
    `userManager.changePassword(user, current, newPassword, confirmPassword)`.
  - `requestLogout()` only sets `logoutRequested` and closes the dialog; the dashboard then calls its
    own `logout()`.
  - Uses `LocationManager` for the city/barangay drop-downs and `UserManager.municipalityOf(...)` /
    `barangayOf(...)` to preselect them.

### `UserCreateDialog`
- **Methods**
  - `public UserCreateDialog(Window owner, UserManager userManager, LocationManager locationManager)`
  - `public boolean showDialog()`
  - `private void buildForm(Window owner)`
  - `private void typeChanged()`
  - `private void save()`
- **Relationships**
  - Opened by `AdminDashboard.createUser()`.
  - Offers the type (Area Officer / Administrator), full name, password + confirmation and, for an area
    officer, the area chosen from `LocationManager.getMunicipalities()`.
  - `save()` validates with `UserManager.fullNameProblem(...)`, `UserManager.passwordProblem(...)` and
    `UserManager.areaProblem(...)`, compares the two passwords, then calls
    `userManager.addAreaOfficer(fullName, password, area)` or
    `userManager.addAdministrator(fullName, password)`; the new id is shown in the confirmation.

### `UserUpdateDialog`
- **Methods**
  - `public UserUpdateDialog(Window owner, User user, UserManager userManager, LocationManager locationManager)`
  - `public boolean showDialog()`
  - `private void buildForm(Window owner)`
  - `private void fillBarangays()`
  - `private void save()`
- **Relationships**
  - Opened by `AdminDashboard.updateUser()`.
  - Shows only the fields that belong to the account type: full name, a read-only username for
    residents, an optional new password + confirmation, contact number and city/barangay for
    residents, and the area for an area officer.
  - `save()` validates with `UserManager.fullNameProblem(...)`, `UserManager.passwordProblem(...)`
    (only when a password is typed), `UserManager.contactNumberProblem(...)` and
    `UserManager.areaProblem(...)`, then calls
    `userManager.saveAccountChanges(user, fullName, password, address, contactNumber, area)`.

### `ReportUpdateDialog`
- **Methods**
  - `public ReportUpdateDialog(Window owner, Report report, String residentId, ReportManager reportManager, LocationManager locationManager)`
  - `public boolean showDialog()`
  - `private void buildForm(Window owner)`
  - `private void fillBarangays()`
  - `private void save()`
- **Relationships**
  - Opened by `ResidentDashboard.updateSelectedReport()` for a `PENDING` report of the signed-in
    resident.
  - Rebuilds the report's `Location` (city, barangay, street, specific place) and the specific detail
    and description, using `LocationManager` for the drop-downs.
  - `save()` normalises with `ReportManager.oneLine(...)`, validates with
    `ReportManager.reportProblem(...)` and calls
    `reportManager.updateResidentReport(report, residentId)`.

### `ReportStatusDialog`
- **Methods**
  - `public ReportStatusDialog(Window owner, Report report, boolean administrator)`
  - `public boolean showDialog()`
  - `public ReportStatus getSelectedStatus()`
  - `public String getReason()`
  - `public boolean isReopenRequested()`
  - `private void buildForm(Window owner)`
  - `private JPanel notePanel()`
  - `private java.awt.GridBagConstraints noteConstraints(int x, int y)`
  - `private void updateReasonState()`
  - `private void updateSaveState()`
  - `private ReportStatus getChosenStatus()`
  - `private void clearError()`
  - `private void showError(String message)`
  - `private void save()`
- **Relationships**
  - Opened by `AdminDashboard.updateReportStatus()` (`administrator = true`) and
    `AreaOfficerDashboard.updateReportStatus()` (`administrator = false`); this is the window behind
    the **Update Status** button.
  - Builds its radio buttons from `report.getStatus().nextStatuses()`, so only legal transitions can be
    chosen, and enables the reason box only for `REJECTED`.
  - `save()` asks `ReportManager.statusProblem(report, target, reason)` first and keeps the window open
    with the message shown in red when the change is refused.
  - For a `REJECTED` report it shows the final-state note plus, for an administrator only, the
    *Reopen this report* checkbox (`isReopenRequested()`); an area officer is told that only an
    administrator can reopen it.
  - The dashboards then apply the choice through `ReportManager.changeStatus(...)`,
    `changeStatusInCity(...)` or `reopenReport(report, true)` and refresh the table, the detail pane and
    the overview.

---

## 7. Cross-cutting behaviour

### 7.1 Roles and permissions

| Role | Created by | Signs in with | Can do |
| --- | --- | --- | --- |
| Resident | self sign-up (`RegistrationFrame`) | username or `U` id | Submit a report, view the public reports and their own, update or delete their own `PENDING` reports, read the rejection reason, edit name/username/contact/address and password in `ProfileDialog`. Cannot change a status. |
| Area Officer | an Administrator (`UserCreateDialog`) | `AO` id | Overview of its own municipality, move reports through the status workflow inside that municipality, reject with a reason, read the reporter's contact number, change its password. Cannot reopen a rejected report. |
| Administrator | another Administrator | `AD` id | Province-wide overview, every report, the full status workflow anywhere, reject with a reason, reopen a rejected report, delete an invalid report, create/update/delete accounts, search and filter accounts, read the reporter's contact number, change its password. |

### 7.2 Status workflow

`ReportStatus.canMoveTo(...)` is the only definition; `ReportManager.statusProblem(...)` turns a refusal
into the message the GUI shows, and `changeStatus(...)` validates before `saveReports()`.

| From | Allowed next |
| --- | --- |
| `PENDING` | `VERIFIED`, `REJECTED` |
| `VERIFIED` | `IN_PROGRESS`, `REJECTED` |
| `IN_PROGRESS` | `RESOLVED`, `REJECTED` |
| `RESOLVED` | none (final) |
| `REJECTED` | none (final) — only `ReportManager.reopenReport(report, true)` returns it to `PENDING` |

- A new report always starts as `PENDING` (`ReportManager.submitReport(...)`).
- Rejecting always needs a reason of 5–200 characters, stored in `Report.remark` and shown to the
  resident; moving the report on clears the remark, and reopening clears it as well.
- The status, the remark and the whole line are written to `reports.txt` before the GUI refreshes, so the
  table, the detail pane and the overview always show the saved value.

### 7.3 Validation rules

| Item | Rule |
| --- | --- |
| Password | 8–64 characters, no spaces, no `\|`; typed twice; a new password must differ from the old one. |
| Contact number | Exactly 11 digits, starting with `09`; the field also filters input to digits (`UITheme.digitsOnly(...)`). |
| Full name | 2–60 characters, letters plus space `.` `'` `-` `,`, at least one letter. |
| Username | 3–40 characters from letters, digits and `.` `_` `@` `+` `-`; at least one letter; unique; must not look like or equal an account id. |
| Area officer area | Required, 60 characters or fewer, taken from `locations.txt`. |
| Report | Valid hazard type, city/barangay from `locations.txt`, specific place 2–60, specific detail 2–80, description 10–500; an empty street becomes `N/A`; no duplicate `PENDING` report. |
| Free text | The `\|` character and line breaks are refused because the files are `\|`-separated; a multi-line description is collapsed by `ReportManager.oneLine(...)`. |
| Account editing | The administrator cannot delete the signed-in account or the last administrator, and is warned when a resident still has reports. |

The rules live in `UserManager` (`...Problem(...)` methods) and `ReportManager`
(`reportProblem(...)`, `remarkProblem(...)`), never in a window; every manager method re-checks them
before saving.

### 7.4 Theme and folder tabs

`UITheme` holds the whole palette: forest green `PRIMARY #1D3F22`, hill green `ACCENT #557D2C`, lime
`LIME #9CBF4A`, sky teal `SKY #2E9BA0`, palm red `DANGER #C1502B`, sage `BORDER #C6D2B8`, cream
`PAGE #F3F5EA`, white `CARD`, plus `TAB_FILL #E7EDD9` and `TAB_LINE #B7C6A5`. Fonts are Segoe UI
(`FONT_TITLE` 24, `FONT_HEADING` 18, `FONT_SECTION` 15, `FONT_TAB` 14, `FONT_BOLD` 12, `FONT_BASE` 12,
`FONT_SMALL` 11, `FONT_VALUE` 13).

`UITheme.styleTabs(...)` installs `FolderTabbedPaneUI`, which draws 40 px trapezoid tabs with 12 px
slanted top corners, the open tab in white joined to the page and the resting tabs on `TAB_FILL`.

---

## 8. Relationship map (whole system)

### 8.1 Inheritance (generalisation)
- `User` (abstract) ← `Resident`, `Administrator`, `AreaOfficer`
  - each calls `super(userId, password, fullName, dateCreated)` and overrides `getUserType()` /
    `toFileString()` (`Resident` adds username, address and contact; `AreaOfficer` adds the area).
- `Report` (abstract) ← `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport`
  - each calls `super(reportId, reporterId, location, description, status, dateSubmitted)` and
    overrides `getReportType()` / `getSpecificDetail()` / `setSpecificDetail(...)`.
- `FolderTabbedPaneUI` extends `javax.swing.plaf.basic.BasicTabbedPaneUI` (framework inheritance).
- The frames extend `javax.swing.JFrame`, `OverviewPanel` extends `javax.swing.JPanel`,
  and the renderers extend `DefaultTableCellRenderer`.

### 8.2 Composition / aggregation
- `Report` **has-a** `Location` and **has-a** `ReportStatus`; `remark` stores the rejection reason.
- `UserManager` **has** `List<User> users` and a `FileManager`.
- `ReportManager` **has** `List<Report> reports`, a `FileManager` and the `HAZARD_TYPES` constant.
- `LocationManager` **has** `List<String> lines` and a `FileManager`.
- `ResidentDashboard` **has** two lists of reports, two table/model pairs, and opens `ProfileDialog` /
  `ReportUpdateDialog` per action.
- `AreaOfficerDashboard` **has** one list of reports, one table/model pair, an `OverviewPanel`
  (composition — created in the constructor and added as the first tab) and opens `ProfileDialog` /
  `ReportStatusDialog`.
- `AdminDashboard` **has** two lists (reports and users), two table/model pairs, an `OverviewPanel`
  and opens `ProfileDialog` / `UserCreateDialog` / `UserUpdateDialog` / `ReportStatusDialog`.
- `ProfileDialog` **has** the signed-in `User`.

### 8.3 Dependency / usage (who calls whom)
- `app.WatchPoint` → `FileManager`, `UserManager`, `ReportManager`, `LocationManager`, `LoginFrame`.
- `LoginFrame` → `UserManager.login(...)`, `ResidentDashboard`, `AreaOfficerDashboard`, `AdminDashboard`,
  `RegistrationFrame`, `UITheme`.
- `RegistrationFrame` → `UserManager` (validation statics + `registerResident`), `LocationManager`,
  `UITheme`.
- `ResidentDashboard` → `ReportManager`, `UserManager` (passed on), `LocationManager`, `Location`,
  `ProfileDialog`, `ReportUpdateDialog`, `UITheme`.
- `AreaOfficerDashboard` → `ReportManager`, `UserManager` (`getContactById`), `LocationManager`,
  `OverviewPanel`, `ProfileDialog`, `ReportStatusDialog`, `UITheme`.
- `AdminDashboard` → `ReportManager`, `UserManager`, `LocationManager`, `OverviewPanel`,
  `ProfileDialog`, `UserCreateDialog`, `UserUpdateDialog`, `ReportStatusDialog`, `UITheme`.
- `OverviewPanel` → `ReportManager`, `Report`, `ReportStatus`, `UITheme`.
- `ProfileDialog` → `UserManager`, `LocationManager`, `User`, `UITheme`.
- `UserCreateDialog` / `UserUpdateDialog` → `UserManager`, `LocationManager`, `User`, `UITheme`.
- `ReportUpdateDialog` → `ReportManager`, `LocationManager`, `Report`, `Location`, `UITheme`.
- `ReportStatusDialog` → `ReportManager`, `Report`, `ReportStatus`, `UITheme`.
- `UserManager` → `FileManager`, `User`, `Resident`, `Administrator`, `AreaOfficer`.
- `ReportManager` → `FileManager`, `Report` and its four subclasses, `Location`, `ReportStatus`.
- `LocationManager` → `FileManager`.
- `UITheme` → `ReportStatus` (through `statusColor(String)`); `FolderTabbedPaneUI` → `UITheme`.
- Every `ui` class → `UITheme`; the three dashboards → `FolderTabbedPaneUI` through
  `UITheme.styleTabs(...)`.

### 8.4 Object-creation map
- `app.WatchPoint` creates the `FileManager` and the three managers.
- `UserManager` creates `Resident` (sign-up, parsing), `Administrator` and `AreaOfficer`
  (`add...`, parsing).
- `ReportManager` creates `RoadHazardReport` / `FloodReport` / `StreetlightReport` /
  `OtherHazardReport`, and the `Location` while parsing.
- `LoginFrame` creates the dashboards and `RegistrationFrame`.
- `ResidentDashboard` creates `Location`, `ProfileDialog`, `ReportUpdateDialog`.
- `AreaOfficerDashboard` creates `OverviewPanel`, `ProfileDialog`, `ReportStatusDialog`.
- `AdminDashboard` creates `OverviewPanel`, `ProfileDialog`, `UserCreateDialog`, `UserUpdateDialog`,
  `ReportStatusDialog`.
- Each dashboard creates a fresh `LoginFrame` in `logout()`.

### 8.5 High-level wiring

```
app.WatchPoint
  ├── storage.FileManager  (data/users.txt, data/reports.txt, data/locations.txt)
  ├── manager.UserManager      ─┐
  ├── manager.ReportManager    ─┼──► ui.LoginFrame
  └── manager.LocationManager  ─┘        │
                                         ├── ui.RegistrationFrame
                                         ├── ui.ResidentDashboard   → ProfileDialog, ReportUpdateDialog
                                         ├── ui.AreaOfficerDashboard → ProfileDialog, ReportStatusDialog,
                                         │                             OverviewPanel
                                         └── ui.AdminDashboard      → ProfileDialog, UserCreateDialog,
                                                                      UserUpdateDialog, ReportStatusDialog,
                                                                      OverviewPanel

  Every ui class → ui.UITheme; the three dashboards' tab strips → ui.FolderTabbedPaneUI
```

---

## 9. Data files (accessed through `FileManager`)

All three files are plain text, one record per line, with `|` as the field separator. That is why the
validators refuse a `|` inside any free-text value.

### 9.1 `data/users.txt`
Written by `User.toFileString()` and its overrides; read by `UserManager.parseUser(...)`.

| Account type | Line format |
| --- | --- |
| Resident | `Resident\|userId\|password\|fullName\|dateCreated\|username\|address\|contactNumber` |
| Administrator | `Administrator\|userId\|password\|fullName\|dateCreated` |
| AreaOfficer | `AreaOfficer\|userId\|password\|fullName\|dateCreated\|area` |

- `userId` is `U` + six digits for residents, `AD` + six digits for administrators and `AO` + six
  digits for area officers (`UserManager.formatId(...)`).
- `dateCreated` is written as `yyyy-MM-dd HH:mm` when the account is created.
- `address` is stored as `barangay + ", " + cityMunicipality`.

```
Administrator|AD000001|admin123|System Admin|2026-10-20 09:15
Resident|U000003|maria123|Maria Santos|2026-10-20 09:16|maria|Cogon, Tagbilaran City|09171234567
AreaOfficer|AO000001|officer123|Tagbilaran City Officer|2026-10-20 09:17|Tagbilaran City
```

### 9.2 `data/reports.txt`
Written by `Report.toFileString()`, read by `ReportManager.parseReport(...)`.

```
type|reportId|reporterId|cityMunicipality|barangay|street|specificPlace|description|specificDetail|status|dateSubmitted|remark
```

- `type` is `Road Hazard`, `Flood`, `Streetlight` or `Other` (from `getReportType()`).
- `reportId` is `R` + three digits (`ReportManager.generateReportId()`).
- `status` is `PENDING`, `VERIFIED`, `IN_PROGRESS`, `RESOLVED` or `REJECTED`.
- `dateSubmitted` is `yyyy-MM-dd HH:mm`.
- `remark` holds the rejection reason and is empty for every other status; a line written before the
  field existed still loads because `parseReport(...)` treats a missing last field as empty.

```
Flood|R004|U000003|Tagbilaran City|Cogon|M. Torralba Street|Near Cogon Market|Water rising after heavy rain|Knee deep|REJECTED|2026-10-16 07:20|Duplicate of R001
```

### 9.3 `data/locations.txt`
One city/municipality per line; read by `LocationManager`.

```
cityMunicipality|barangay1|barangay2|...
```

```
Tagbilaran City|Bool|Cogon|Dao|Poblacion|Tiptip
```

---

## 10. Summary of the design

- **Model** is passive: `User`/`Resident`/`Administrator`/`AreaOfficer` and
  `Report`/`RoadHazardReport`/`FloodReport`/`StreetlightReport`/`OtherHazardReport` hold data and the
  `ReportStatus` workflow; `Location` is a value object owned by a report.
- **Managers** own the collections, the validation rules and every write to disk; `FileManager` is the
  only class that touches the text files.
- **GUI** is thin: it builds forms, delegates every check and every save to a manager, and re-renders
  from the manager after each change.
- **Status changes** pass through one gate (`ReportManager.statusProblem(...)` →
  `changeStatus(...)` / `changeStatusInCity(...)` / `reopenReport(...)`), so the rules cannot be bypassed
  by a window or by any other caller.

*End of document.*
