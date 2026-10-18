# WatchPoint — Class Reference (Explained)

This file lists **everything that exists in the system**: every class, every attribute and every method, with a short note on what each one is for. Classes or members that no longer exist in the code have been removed, so this document matches `src\` exactly.

The system is built from **19 classes**:

- 1 entry point (`Main`)
- 14 model / manager classes (no GUI code)
- **4 GUI classes only** — `LoginFrame`, `RegistrationFrame`, `ResidentDashboard`, `AdminDashboard`

All of the interface styling lives **inside those four GUI classes** (colours, fonts, the header bar, table styling, the detail cards and the submit/update forms). There are no shared theme, filter-bar or detail-panel classes: every one of those members is a private helper inside one of the four windows.

> The NetBeans copy of the project (`Watchpoint NetBeans\WatchPoint`) contains the same classes with the same members; the only differences are the `package com.mycompany.watchpoint;` line and that its entry point is called `WatchPoint` instead of `Main`.

## Index

1. **Entry point** — `Main`
2. **Domain model** — `User` (abstract), `Resident`, `Administrator`, `Location`, `ReportStatus` (enum), `Report` (abstract), `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport`
3. **Managers** — `FileManager`, `UserManager`, `ReportManager`, `LocationManager`
4. **GUI windows** — `LoginFrame`, `RegistrationFrame`, `ResidentDashboard`, `AdminDashboard`
5. **Inner (nested) classes** — `HeaderRenderer`, `StatusRenderer`, `BodyPanel` (inside both dashboards)
6. **Anonymous classes** — hint-painting fields, comparators, listener lambdas
7. **How it all fits together**

---

## 1. Entry point

### `Main`
**What it is:** Starts the program. Creates one `FileManager` for the `data` folder, builds the three managers from it, and opens the login window on the Swing event thread.

**Methods**
| Method | What it does |
| --- | --- |
| `public static void main(String[] args)` | Creates `FileManager("data")`, then `UserManager`, `ReportManager` and `LocationManager`; inside `SwingUtilities.invokeLater(...)` it creates `LoginFrame(userManager, reportManager, locationManager)` and makes it visible. No Look & Feel is set, so the windows use Swing's default (Metal) theme and get their colours from the explicit styling inside the GUI classes. |

**Connects to:** `FileManager`, `UserManager`, `ReportManager`, `LocationManager`, `LoginFrame`.

---

## 2. Domain model

### `User` (abstract)
**What it is:** The parent of every account. Holds the login details shared by residents and administrators.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private String userId` | Account id, `U<number>` (e.g. `U1003`). |
| `private String username` | Login name, unique across all accounts. |
| `private String password` | Plain-text password used by `verifyPassword(...)`. |
| `private String fullName` | Display name shown in the header and in the report details. |

**Methods**
| Method | What it does |
| --- | --- |
| `protected User(String userId, String username, String password, String fullName)` | Stores the four values. Called by `Resident` and `Administrator` through `super(...)`. |
| `public String getUserId()` | Returns the account id. |
| `public String getUsername()` | Returns the login name. |
| `public String getFullName()` | Returns the display name. |
| `public boolean verifyPassword(String input)` | Compares `input` with the stored password (`false` when `input` is `null`). |
| `public abstract String getUserType()` | Each subclass returns `"Resident"` or `"Administrator"`. |
| `public String toFileString()` | Joins type, id, username, password and full name with `|` for `users.txt`; subclasses append their own fields. |

**Connects to:** extended by `Resident` and `Administrator`; stored by `UserManager`; shown by the dashboards' Users tab.

### `Resident` (extends `User`)
**What it is:** A community member who reports hazards.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private String address` | `barangay + ", " + cityMunicipality`, built by `RegistrationFrame.register()`. |
| `private String contactNumber` | Phone number typed in the registration form. |

**Methods**
| Method | What it does |
| --- | --- |
| `public Resident(String userId, String username, String password, String fullName, String address, String contactNumber)` | Calls `super(...)` and stores the address and contact number. |
| `public String getAddress()` | Returns the address line. |
| `public String getContactNumber()` | Returns the contact number. |
| `public String getUserType()` | Returns `"Resident"` (overrides `User`). |
| `public String toFileString()` | Appends the address and contact number to the parent line for `users.txt`. |

**Connects to:** created by `UserManager.registerResident(...)` and by `AdminDashboard.createUser()`; the logged-in resident is held by `ResidentDashboard.currentUser`.

### `Administrator` (extends `User`)
**What it is:** A city or barangay staff member who reviews reports and manages accounts.

**Methods**
| Method | What it does |
| --- | --- |
| `public Administrator(String userId, String username, String password, String fullName)` | Calls `super(...)`. |
| `public String getUserType()` | Returns `"Administrator"` (overrides `User`). |
| `public String toFileString()` | Uses the parent line unchanged for `users.txt`. |

**Connects to:** created by `AdminDashboard.createUser()`; the logged-in administrator is held by `AdminDashboard.currentUser`.

### `Location`
**What it is:** Where a hazard was seen. Every `Report` holds one.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private String cityMunicipality` | City or municipality (from the Bohol list). |
| `private String barangay` | Barangay inside that city/municipality. |
| `private String street` | Street name; `"N/A"` when the reporter leaves it blank. |
| `private String specificPlace` | Landmark, e.g. “Near Water Refiller”. |

**Methods**
| Method | What it does |
| --- | --- |
| `public Location(String cityMunicipality, String barangay, String street, String specificPlace)` | Stores the four parts of the address. |
| `public String getCityMunicipality()` | Returns the city/municipality. |
| `public String getBarangay()` | Returns the barangay. |
| `public String getStreet()` | Returns the street. |
| `public String getSpecificPlace()` | Returns the landmark. |
| `public String getFullLocation()` | Builds `"street, barangay, cityMunicipality"` and adds `" (specificPlace)"` when a landmark was given. |

**Connects to:** held by `Report`; created by `ResidentDashboard.submitReport()` / `updateSelectedReport()` and rebuilt by `ReportManager.parseReport(...)`.

### `ReportStatus` (enum)
**What it is:** The four stages a report moves through.

**Constants**
| Constant | Meaning |
| --- | --- |
| `PENDING` | Submitted, waiting for an administrator. Only these can be edited or deleted by the resident. |
| `VERIFIED` | An administrator confirmed it. |
| `IN_PROGRESS` | Someone is working on it. |
| `RESOLVED` | The hazard has been fixed. |

**Methods:** the implicit `values()` and `valueOf(String)`, plus `name()` used when writing `reports.txt`, filtering and colouring the Status column.

**Connects to:** stored by `Report`; chosen in `AdminDashboard.statusBox`; re-read by `ReportManager.parseReport(...)`.

### `Report` (abstract)
**What it is:** The parent of every hazard report. Holds the information that all four report types share.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private String reportId` | `R<number>` id (e.g. `R019`). |
| `private String reporterId` | `userId` of the resident who submitted it. |
| `private Location location` | Where the hazard is. |
| `private String description` | Free text typed by the reporter. |
| `private ReportStatus status` | Current stage (starts as `PENDING`). |
| `private String dateSubmitted` | `yyyy-MM-dd HH:mm`, created by `ReportManager`. |

**Methods**
| Method | What it does |
| --- | --- |
| `protected Report(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted)` | Stores the shared values. Called by the four subclasses through `super(...)`. |
| `public String getReportId()` | Returns the report id. |
| `public String getReporterId()` | Returns the reporter’s user id. |
| `public Location getLocation()` | Returns the location object. |
| `public String getDescription()` | Returns the description. |
| `public ReportStatus getStatus()` | Returns the current status. |
| `public String getDateSubmitted()` | Returns the date/time text. |
| `public void setLocation(Location location)` | Replaces the location (used by the Update Report form). |
| `public void setDescription(String description)` | Replaces the description (Update Report form). |
| `public void setStatus(ReportStatus status)` | Changes the stage (used by `ReportManager.updateReportStatus`). |
| `public abstract String getReportType()` | `"Road Hazard"`, `"Flood"`, `"Streetlight"` or `"Other"`. |
| `public abstract String getSpecificDetail()` | The type-specific value (see the subclasses). |
| `public abstract void setSpecificDetail(String specificDetail)` | Replaces that value (needed so the Update Report form can edit it). |
| `public String toFileString()` | Joins type, id, reporter, the four location parts, description, specific detail, status and date with `|` for `reports.txt`. |

**Connects to:** extended by the four report classes; created and stored by `ReportManager`; displayed by both dashboards.

### `RoadHazardReport` (extends `Report`)
**What it is:** A road problem such as a pothole or a broken road.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private String roadHazardType` | The detail, e.g. `Guba na Karsada`. |

**Methods**
| Method | What it does |
| --- | --- |
| `public RoadHazardReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String roadHazardType)` | Calls `super(...)` and stores the road-hazard type. |
| `public String getReportType()` | Returns `"Road Hazard"`. |
| `public String getSpecificDetail()` | Returns `roadHazardType`. |
| `public void setSpecificDetail(String specificDetail)` | Replaces `roadHazardType`. |

**Connects to:** created by `ReportManager.submitReport(...)` when the type is `Road Hazard`, and rebuilt by `parseReport(...)`.

### `FloodReport` (extends `Report`)
**What it is:** A report about flooding.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private String waterLevel` | The detail, e.g. `Knee deep`. |

**Methods**
| Method | What it does |
| --- | --- |
| `public FloodReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String waterLevel)` | Calls `super(...)` and stores the water level. |
| `public String getReportType()` | Returns `"Flood"`. |
| `public String getSpecificDetail()` | Returns `waterLevel`. |
| `public void setSpecificDetail(String specificDetail)` | Replaces `waterLevel`. |

**Connects to:** created by `ReportManager.submitReport(...)` when the type is `Flood`.

### `StreetlightReport` (extends `Report`)
**What it is:** A report about a broken or unlit streetlight.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private String poleNumber` | The detail, e.g. `Pole 12`. |

**Methods**
| Method | What it does |
| --- | --- |
| `public StreetlightReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String poleNumber)` | Calls `super(...)` and stores the pole number. |
| `public String getReportType()` | Returns `"Streetlight"`. |
| `public String getSpecificDetail()` | Returns `poleNumber`. |
| `public void setSpecificDetail(String specificDetail)` | Replaces `poleNumber`. |

**Connects to:** created by `ReportManager.submitReport(...)` when the type is `Streetlight`.

### `OtherHazardReport` (extends `Report`)
**What it is:** Any hazard that does not fit the other three types.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private String hazardCategory` | The detail, e.g. `Stray animals`. |

**Methods**
| Method | What it does |
| --- | --- |
| `public OtherHazardReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String hazardCategory)` | Calls `super(...)` and stores the category. |
| `public String getReportType()` | Returns `"Other"`. |
| `public String getSpecificDetail()` | Returns `hazardCategory`. |
| `public void setSpecificDetail(String specificDetail)` | Replaces `hazardCategory`. |

**Connects to:** created by `ReportManager.submitReport(...)` for anything else, and by `parseReport(...)` as the fallback.

---

## 3. Managers

### `FileManager`
**What it is:** Reads and writes the three text files in the `data` folder. Nothing else in the system touches the disk.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private String dataFolder` | Folder name, `"data"` from `Main`. |

**Methods**
| Method | What it does |
| --- | --- |
| `public FileManager(String dataFolder)` | Stores the folder and makes sure `users.txt`, `reports.txt` and `locations.txt` exist (creating the folder and files when missing). |
| `public List<String> readLines(String fileName)` | Returns every non-blank line of the file. |
| `public void writeLines(String fileName, List<String> lines)` | Overwrites the file with the given lines. |
| `private void ensureFileExists(String fileName)` | Creates the folder/file when they are missing. |

**Connects to:** used by `UserManager`, `ReportManager` and `LocationManager`; created by `Main`.

### `UserManager`
**What it is:** Keeps the list of accounts, checks logins, and saves `users.txt`.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private List<User> users` | Every account loaded from `users.txt`. |
| `private FileManager fileManager` | Used to read and save the file. |

**Methods**
| Method | What it does |
| --- | --- |
| `public UserManager(FileManager fileManager)` | Stores the file manager and calls `loadUsers()`. |
| `public boolean registerResident(String fullName, String username, String password, String address, String contactNumber)` | Validates the inputs, rejects a duplicate username, creates a `Resident` with a new id and saves. Returns `false` when invalid or taken. |
| `public User login(String username, String password)` | Returns the matching `User`, or `null` when the username/password pair is wrong. |
| `public User getUserById(String userId)` | Returns the account with that id, or `null`. |
| `public List<User> getAllUsers()` | Returns a copy of the account list (used by the Users tab). |
| `public boolean addUser(User user)` | Adds an account unless the username exists, then saves. |
| `public boolean updateUser(User user)` | Replaces the account with the same id and saves. |
| `public boolean deleteUser(String userId)` | Removes the account with that id and saves. |
| `private boolean isUsernameTaken(String username)` | Case-insensitive duplicate check. |
| `private void loadUsers()` | Reads `users.txt` and calls `parseUser(...)` per line. |
| `private void saveUsers()` | Writes `toFileString()` for every account. |
| `private User parseUser(String line)` | Splits one `users.txt` line into a `Resident` or `Administrator` (returns `null` for unknown types). |
| `private String generateUserId()` | Returns `U` + (highest existing number + 1). |

**Connects to:** used by `LoginFrame` (`login`), `RegistrationFrame` (`registerResident`), both dashboards and `Main`; creates `Resident` / `Administrator`.

### `ReportManager`
**What it is:** Keeps the list of reports, creates new ones, changes their status, and saves `reports.txt`.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private List<Report> reports` | Every report loaded from `reports.txt`. |
| `private FileManager fileManager` | Used to read and save the file. |

**Methods**
| Method | What it does |
| --- | --- |
| `public ReportManager(FileManager fileManager)` | Stores the file manager and calls `loadReports()`. |
| `public Report submitReport(String reporterId, String hazardType, Location location, String description, String specificDetail)` | Builds the matching `Report` subclass with status `PENDING` and a generated id/timestamp, saves it, and returns it. |
| `public Report getReportById(String reportId)` | Returns that report, or `null`. |
| `public List<Report> getReportsByReporter(String reporterId)` | Returns one resident’s reports (the My Reports tab). |
| `public List<Report> getAllReports()` | Returns a copy of every report (the Reports tabs). |
| `public boolean updateResidentReport(Report report, String residentId)` | Saves an edited report, but only when it belongs to that resident and is still `PENDING`. |
| `public boolean deleteResidentReport(String reportId, String residentId)` | Deletes one of the resident’s `PENDING` reports. |
| `public boolean updateReportStatus(String reportId, ReportStatus status)` | Sets a new status (administrator). |
| `public boolean deleteInvalidReport(String reportId)` | Deletes any report (administrator). |
| `private void loadReports()` | Reads `reports.txt` and calls `parseReport(...)` per line. |
| `private void saveReports()` | Writes `toFileString()` for every report. |
| `private Report parseReport(String line)` | Rebuilds the correct subclass from one `reports.txt` line (11 fields). |
| `private String currentTimestamp()` | Returns `yyyy-MM-dd HH:mm` for a new report. |
| `private String generateReportId()` | Returns `R` + a zero-padded (3 digits) counter, e.g. `R024`. |

**Connects to:** used by both dashboards and `Main`; creates the four report subclasses.

### `LocationManager`
**What it is:** Supplies the Bohol city/municipality and barangay lists used by every drop-down.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private List<String> lines` | The raw lines of `locations.txt`. |
| `private FileManager fileManager` | Used to read the file. |

**Methods**
| Method | What it does |
| --- | --- |
| `public LocationManager(FileManager fileManager)` | Stores the file manager and loads `locations.txt`. |
| `public List<String> getMunicipalities()` | Returns the first token of every line (48 Bohol municipalities). |
| `public List<String> getBarangays(String municipality)` | Returns the barangays of that municipality (the rest of its line). |
| `private String municipalityOf(String line)` | Returns the part of a line before the first `|`. |

**Connects to:** used by `RegistrationFrame` (address drop-downs), by both dashboards (filters, submit form, update form) and by `Main`. The data lives in `data\locations.txt`, one line per municipality: `Municipality|Barangay|Barangay|...`.

---

## 4. GUI windows

### `LoginFrame` (extends `JFrame`)
**What it is:** The first window. It asks for a username and password, then opens the right dashboard.

The look (light page, bevel-bordered card, navy `#003366` title, grey captions, blue `#0099FF` buttons) is set directly in `initComponents()` — there is no theme class any more.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private UserManager userManager` | Used to check the login. |
| `private ReportManager reportManager` | Passed on to the dashboard that opens. |
| `private LocationManager locationManager` | Passed on to the dashboard and to `RegistrationFrame`. |
| `private JTextField usernameField` | Where the username is typed. |
| `private JPasswordField passwordField` | Where the password is typed (masked). |
| `private JButton loginButton` | The **Log In** button. |
| `private JButton registerButton` | The **Register as Resident** button. |
| `private JLabel jLabel1 … jLabel4` | Title “WatchPoint”, subtitle, and the Username / Password captions. |
| `private JLayeredPane jLayeredPane1` | The bevel-bordered card that holds the controls. |
| `private JPanel jPanel1` | The page that holds the card (`BorderLayout.CENTER`). |

**Methods**
| Method | What it does |
| --- | --- |
| `public LoginFrame()` | No-argument constructor required by the NetBeans form editor; only calls `initComponents()`. |
| `public LoginFrame(UserManager userManager, ReportManager reportManager, LocationManager locationManager)` | Stores the managers, calls `initComponents()`, and wires the **Register as Resident** button to `openRegistration()`. |
| `public void handleLogin()` | Reads the fields, calls `userManager.login(...)`; on success opens `AdminDashboard` (casting to `Administrator`) or `ResidentDashboard` (casting to `Resident`) and disposes the login window; on failure shows “Invalid username or password.”. |
| `public void openRegistration()` | Opens `RegistrationFrame(userManager, locationManager)`. |
| `public void showMessage(String message)` | Small helper around `JOptionPane.showMessageDialog(...)`. |
| `private void initComponents()` | Builds the whole window (generated block; the matching `LoginFrame.form` describes it for the NetBeans editor). |
| `private void loginButtonActionPerformed(ActionEvent evt)` | Calls `handleLogin()`. |
| `private void passwordFieldActionPerformed(ActionEvent evt)` | Enter inside the password field also calls `handleLogin()`. |
| `private void usernameFieldActionPerformed(ActionEvent evt)` | Enter inside the username field moves the caret to the password field. |

**Connects to:** created by `Main`; uses `UserManager`; creates `AdminDashboard`, `ResidentDashboard` and `RegistrationFrame`.

### `RegistrationFrame` (extends `JFrame`)
**What it is:** The “Register as Resident” window where a new account is created.

**Attributes**
| Attribute | Meaning |
| --- | --- |
| `private UserManager userManager` | Used to create and save the account. |
| `private LocationManager locationManager` | Supplies the city/municipality and barangay lists. |
| `private JTextField fullNameField` | Full name input. |
| `private JTextField usernameField` | Username input. |
| `private JPasswordField passwordField` | Password input (masked with `'\u2022'`). |
| `private JComboBox<String> cityMunicipalityBox` | City/municipality drop-down (Bohol list). |
| `private JComboBox<String> barangayBox` | Barangay drop-down; refilled when the city changes. |
| `private JTextField contactNumberField` | Contact-number input (shows `09XXXXXXXXX`). |
| `private JButton registerButton` | The **Register as Resident** button. |
| `private JLabel jLabel1 … jLabel8` | Title, subtitle and the six field captions. |
| `private JLayeredPane jLayeredPane2` | The bevel-bordered card holding the form. |
| `private JPanel jPanel1` | The page holding the card. |

**Methods**
| Method | What it does |
| --- | --- |
| `public RegistrationFrame()` | No-argument constructor for the form editor; only `initComponents()`. |
| `public RegistrationFrame(UserManager userManager, LocationManager locationManager)` | Stores the managers, calls `initComponents()`, fills `cityMunicipalityBox` from `locationManager.getMunicipalities()`, then wires the city drop-down and the Register button. |
| `private void updateBarangayBox()` | Refills `barangayBox` for the selected city/municipality. |
| `public void register()` | Reads the inputs, requires a city and barangay, builds the address `barangay + ", " + cityMunicipality`, calls `userManager.registerResident(...)` and shows a success/failure message. |
| `public void clearFields()` | Clears the text inputs, puts the `09XXXXXXXXX` hint back and resets the city drop-down (which refreshes the barangays). |
| `private void initComponents()` | Builds the window (generated block; paired with `RegistrationFrame.form`). |
| `private void fullNameFieldActionPerformed(ActionEvent evt)` | Enter moves the caret to the username field. |
| `private void usernameFieldActionPerformed(ActionEvent evt)` | Enter moves the caret to the password field. |
| `private void passwordFieldActionPerformed(ActionEvent evt)` | Enter moves the caret to the city drop-down. |
| `private void contactNumberFieldActionPerformed(ActionEvent evt)` | Enter in the last field calls `register()`. |

**Connects to:** created by `LoginFrame.openRegistration()`; uses `LocationManager` and `UserManager`.

### `ResidentDashboard` (extends `JFrame`)
**What it is:** The window a resident sees after logging in, with three tabs — **Reports** (all reports), **Submit Report** and **My Reports**. All of its styling is private to the class.

**Attributes — palette and fonts (private constants)**
| Attribute | Meaning |
| --- | --- |
| `private static final Color PRIMARY = (0, 51, 102)` | Navy used for titles, section headings and the table header. |
| `private static final Color MUTED = (102, 102, 102)` | Grey captions and hints. |
| `private static final Color TEXT = (51, 51, 51)` | Normal text. |
| `private static final Color ACCENT = (0, 153, 255)` | Blue primary buttons. |
| `private static final Color BORDER = (204, 204, 204)` | Field and card borders (also the grey secondary button). |
| `private static final Color DANGER = (198, 40, 40)` | Red delete button. |
| `private static final Color PAGE = (238, 238, 238)` | Tab background. |
| `private static final Color ROW_LINE = (226, 226, 226)` | Table row separator. |
| `private static final Color SELECTION = (204, 229, 255)` | Selected table row. |
| `private static final Font FONT_TITLE / FONT_HEADING / FONT_SECTION / FONT_BOLD / FONT_BASE / FONT_SMALL / FONT_VALUE` | Segoe UI at 24 / 18 / 15 / 12 bold / 12 / 11 bold / 13. |
| `private static final String ALL_MUNICIPALITIES = "All Municipalities"` | First item of the city filter. |
| `private static final String ALL_BARANGAYS = "All Barangays"` | First item of the barangay filter. |
| `private static final String[] SORT_OPTIONS` | The six Sort by choices (Report ID oldest/newest, Date newest/oldest, Status, Hazard Type). |

**Attributes — data**
| Attribute | Meaning |
| --- | --- |
| `private Resident currentUser` | The resident who is logged in. |
| `private ReportManager reportManager` | Reads and saves reports. |
| `private UserManager userManager` | Used to resolve the reporter’s full name and to reopen the login window. |
| `private LocationManager locationManager` | Supplies the drop-downs. |

**Attributes — Submit Report tab**
| Attribute | Meaning |
| --- | --- |
| `private JComboBox<String> hazardTypeBox` | Road Hazard / Flood / Streetlight / Other. |
| `private JComboBox<String> cityMunicipalityBox` | City/municipality of the hazard. |
| `private JComboBox<String> barangayBox` | Barangay; refilled when the city changes. |
| `private JTextField streetField` | Street (hint `Optional (e.g. Casa Road)`). |
| `private JTextField specificPlaceField` | Landmark (hint `(e.g. Near Water Refiller)`). |
| `private JTextField specificDetailField` | Type-specific detail (hint `(e.g. Pothole, Knee deep, Pole 12)`). |
| `private JTextArea descriptionArea` | What was observed (hint `(e.g. Road blocked due to accident)`). |

**Attributes — Reports tab**
| Attribute | Meaning |
| --- | --- |
| `private JTable publicReportTable` | Table of all reports (Report ID, Type, Reporter, Status, Date & Time). |
| `private DefaultTableModel publicReportTableModel` | The table’s data. |
| `private JScrollPane publicDetailScroll` | The report card on the right. |
| `private JComboBox<String> publicFilterCityBox` | City/municipality filter. |
| `private JComboBox<String> publicFilterBarangayBox` | Barangay filter (depends on the city). |
| `private JComboBox<String> publicFilterSortBox` | Sort by. |
| `private List<Report> publicReports` | The reports currently shown. |

**Attributes — My Reports tab**
| Attribute | Meaning |
| --- | --- |
| `private JTable reportTable` | Table of this resident’s reports (Report ID, Type, Status, Date & Time). |
| `private DefaultTableModel reportTableModel` | The table’s data. |
| `private JScrollPane detailScroll` | The report card on the right. |
| `private List<Report> currentReports` | This resident’s reports. |

**Methods — constructors and tabs**
| Method | What it does |
| --- | --- |
| `public ResidentDashboard()` | No-argument constructor for the form editor; only `initComponents()`. |
| `public ResidentDashboard(Resident currentUser, ReportManager reportManager, UserManager userManager, LocationManager locationManager)` | Stores everything, sets the title and the “Signed in as …” subtitle, adds the three tabs, sizes the window, then loads both lists. |
| `private JPanel buildAllReportsTab()` | Builds the **Reports** tab: filter row, table, report card (50/50 split) and hint line. |
| `private JPanel buildFilterRow()` | Builds the City / Municipality, Barangay and Sort by drop-downs. |
| `private JPanel buildSubmitTab()` | Builds the **Submit Report** tab: a centred white card with captions above full-width fields and a **Submit Report** button. |
| `private JPanel buildReportsTab()` | Builds the **My Reports** tab: table, report card and the Update/Delete row. |
| `private JPanel buildReportButtonRow()` | Builds **Update Selected Report** (grey) and **Delete Selected Report** (red). |
| `private void addFormRow(JPanel form, int row, String caption, Component field)` | Adds one caption + field row to a GridBag form. |
| `private void updateBarangayBox()` | Refills `barangayBox` for the selected city. |

**Methods — data and actions**
| Method | What it does |
| --- | --- |
| `public void showAllReports()` | Calls `refreshAllReports()`. |
| `private void refreshAllReports()` | Re-reads every report, applies the filters/sort, refills the table and selects the first row. |
| `private static List<Report> selectedReports(List<Report> reports, JComboBox<String> cityBox, JComboBox<String> barangayBox, JComboBox<String> sortBox)` | Returns the reports that match the filters, sorted. |
| `private static String selected(JComboBox<String> box, String allOption)` | Returns the selected text, or `null` when the “All …” item is chosen. |
| `private static void sortReports(List<Report> reports, String sortMode)` | Sorts by id, date, status or hazard type. |
| `private void showSelectedPublicReport()` | Shows the selected report in `publicDetailScroll`. |
| `private Report selectedPublicReport()` | Returns the selected report, or `null`. |
| `public void showOwnReports()` | Loads this resident’s reports and refills the My Reports table. |
| `private void showSelectedReport()` | Shows the selected report in `detailScroll`. |
| `private Report selectedReport()` | Returns the selected own-report, or `null`. |
| `public void submitReport()` | Validates the submit form, creates the `Location`, calls `reportManager.submitReport(...)`, clears the fields, refreshes both lists and switches to My Reports. |
| `public void updateSelectedReport()` | Opens the **Update Report** dialog (same card design as the submit form, pre-filled) and saves through `reportManager.updateResidentReport(...)`; only `PENDING` reports. |
| `public void deleteSelectedReport()` | Asks for confirmation, then deletes the selected `PENDING` report. |
| `public void logout()` | Opens a new `LoginFrame(...)` and disposes this window. |
| `private void showMessage(String message)` | Small helper around `JOptionPane.showMessageDialog(...)`. |

**Methods — look and layout helpers**
| Method | What it does |
| --- | --- |
| `private static JPanel page()` | Grey tab page with padding and a 12 px vertical gap. |
| `private static JPanel card()` | White panel with a 1 px border and 20/22 px padding. |
| `private static JLabel label(String text, Font font, Color color)` | Left-aligned label. |
| `private static JLabel hint(String text)` | Grey 12 pt hint label. |
| `private static JButton button(String text, Color background, Color foreground)` | Flat button with the given colours and bold 12 pt text. |
| `private static JComboBox<String> combo(String firstItem, List<String> items)` | Drop-down filled from a list (optional first item). |
| `private static JComboBox<String> options(String[] values)` | Drop-down filled from an array. |
| `private static String text(JComboBox<String> box)` | Selected text, or `""`. |
| `private static JScrollPane borderedScroll(Component view)` | Scroll pane with a 1 px border and white viewport. |
| `private static JScrollPane detailScroll()` | Empty bordered scroll pane used for the report card. |
| `private static JTable dataTable(Object[] columns)` | Read-only table with the navy header, 28 px rows and light grid. |
| `private static JPanel fieldBox(String caption, String value)` | Bordered box: small grey caption above the value. |
| `private static JPanel valueBox(String value)` | Bordered box with a single value (used for the type-specific detail). |
| `private static JPanel descriptionBox(String text)` | Bordered box holding a read-only, word-wrapped text area that grows with the text (3–14 lines). |
| `private static JPanel twoColumns(JPanel left, JPanel right)` | Two equal boxes side by side. |
| `private static void addSection(JPanel body, String title, String[][] fields)` | Adds a section heading plus its 2-column field boxes. |
| `private static String display(String text)` | Returns `"-"` for empty text. |
| `private static Color statusColor(String status)` | Orange / blue / purple / green per status. |
| `private static String detailCaption(String reportType)` | “Road Hazard Type”, “Water Level”, “Pole Number” or “Hazard Category”. |
| `private String reporterName(Report report)` | Reporter’s **full name** from `userManager.getUserById(...)`, falling back to the id. |
| `private JPanel reportForm(Report report)` | Builds the whole report card (heading, status, Report Information, Location, type detail, Description). |
| `private static JPanel emptyDetail(String message)` | Card shown when nothing is selected. |
| `private static JTextField hintField(String hint, int columns)` | Text field that paints a grey hint while empty. |
| `private static JTextArea hintArea(String hint, int rows, int columns)` | Text area that paints a grey hint while empty. |
| `private void initComponents()` | Builds the window shell: `jPanel1` → `jLayeredPane1` (bevel border) with `titleLabel`, `subtitleLabel`, `tabs` and the **Log Out** button, plus the tabbed pane. |
| `private void logoutButtonActionPerformed(ActionEvent evt)` | Calls `logout()`. |

**Connects to:** created by `LoginFrame.handleLogin()`; uses `ReportManager`, `UserManager` and `LocationManager`; creates `Location`, `Report` (through the manager) and a new `LoginFrame` on logout.

### `AdminDashboard` (extends `JFrame`)
**What it is:** The window an administrator sees after logging in, with two tabs — **Reports** (review reports, change status, delete invalid ones) and **Users** (create / update / delete accounts). It carries the same palette and helper methods as `ResidentDashboard`.

**Attributes — palette, fonts and constants**
| Attribute | Meaning |
| --- | --- |
| `PRIMARY`, `MUTED`, `TEXT`, `ACCENT`, `BORDER`, `DANGER`, `PAGE`, `ROW_LINE`, `SELECTION` | Same colours as `ResidentDashboard` (navy, greys, blue, red, page, grid, selection). |
| `FONT_TITLE`, `FONT_HEADING`, `FONT_SECTION`, `FONT_BOLD`, `FONT_BASE`, `FONT_SMALL`, `FONT_VALUE` | Same Segoe UI sizes as `ResidentDashboard`. |
| `private static final String ALL_MUNICIPALITIES` / `ALL_BARANGAYS` | First items of the two filters. |
| `private static final String[] SORT_OPTIONS` | The six Sort by choices. |

**Attributes — data**
| Attribute | Meaning |
| --- | --- |
| `private Administrator currentUser` | The administrator who is logged in. |
| `private ReportManager reportManager` | Reads and saves reports. |
| `private UserManager userManager` | Loads and saves accounts. |
| `private LocationManager locationManager` | Supplies the filters. |

**Attributes — Reports tab**
| Attribute | Meaning |
| --- | --- |
| `private JTable reportTable` | Table of all reports (Report ID, Type, Reporter, Status, Date & Time). |
| `private DefaultTableModel reportTableModel` | The table’s data. |
| `private JScrollPane reportDetailScroll` | The report card on the right. |
| `private JComboBox<String> reportFilterCityBox` | City/municipality filter. |
| `private JComboBox<String> reportFilterBarangayBox` | Barangay filter. |
| `private JComboBox<String> reportFilterSortBox` | Sort by. |
| `private List<Report> allReports` | Every report, before filtering. |
| `private List<Report> currentReports` | The reports currently shown. |
| `private JComboBox<String> statusBox` | Status to apply (PENDING / VERIFIED / IN_PROGRESS / RESOLVED). |

**Attributes — Users tab**
| Attribute | Meaning |
| --- | --- |
| `private JTable userTable` | Table of accounts (User ID, Type, Username, Full Name). |
| `private DefaultTableModel userTableModel` | The table’s data. |
| `private JScrollPane userDetailScroll` | The account card on the right. |
| `private List<User> currentUsers` | The accounts currently shown. |

**Methods — constructors and tabs**
| Method | What it does |
| --- | --- |
| `public AdminDashboard()` | No-argument constructor for the form editor; only `initComponents()`. |
| `public AdminDashboard(Administrator currentUser, ReportManager reportManager, UserManager userManager, LocationManager locationManager)` | Stores everything, sets the title and “Signed in as … - Administrator”, adds the two tabs, sizes the window, then loads reports and accounts. |
| `private JPanel buildReportsTab()` | Builds the **Reports** tab: filter row, table, report card (50/50 split) and the status controls. |
| `private JPanel buildFilterRow()` | Builds the City / Municipality, Barangay and Sort by drop-downs. |
| `private void updateFilterBarangays()` | Refills the barangay filter for the selected city. |
| `private JPanel buildReportControls()` | Builds “Set status to:”, the status drop-down, **Update Status** (blue) and **Delete Invalid Report** (red). |
| `private JPanel buildUsersTab()` | Builds the **Users** tab: table, account card (50/50 split) and the account buttons. |
| `private JPanel buildUserControls()` | Builds **Create User** (blue), **Update User** (grey) and **Delete User** (red). |

**Methods — reports**
| Method | What it does |
| --- | --- |
| `public void showAllReports()` | Re-reads every report from `ReportManager` and refreshes the table. |
| `private void refreshReports()` | Applies the filters/sort to `allReports`, refills the table and selects the first row. |
| `private static List<Report> selectedReports(List<Report> reports, JComboBox<String> cityBox, JComboBox<String> barangayBox, JComboBox<String> sortBox)` | Returns the matching, sorted reports. |
| `private static String selected(JComboBox<String> box, String allOption)` | Selected text, or `null` for the “All …” item. |
| `private static void sortReports(List<Report> reports, String sortMode)` | Sorts by id, date, status or hazard type. |
| `private void showSelectedReport()` | Shows the selected report in `reportDetailScroll`. |
| `private Report selectedReport()` | Returns the selected report, or `null`. |
| `public void updateReportStatus()` | Sets the selected report’s status to the value in `statusBox`. |
| `public void deleteInvalidReport()` | Deletes the selected report after confirmation. |

**Methods — accounts**
| Method | What it does |
| --- | --- |
| `private void showSelectedUser()` | Shows the selected account in `userDetailScroll`. |
| `private User selectedUser()` | Returns the selected account, or `null`. |
| `public void manageUsers()` | Reloads every account and refills the Users table. |
| `public void createUser()` | Asks for type, full name, username and password (plus address/contact for a resident), generates the next `U<number>` id and calls `userManager.addUser(...)`. |
| `public void updateUser()` | Edits the selected account’s full name, password (and address/contact for a resident) and calls `userManager.updateUser(...)`. |
| `public void deleteUser()` | Deletes the selected account after confirmation. |
| `public void logout()` | Opens a new `LoginFrame(...)` and disposes this window. |
| `private void showMessage(String message)` | Small helper around `JOptionPane.showMessageDialog(...)`. |

**Methods — look and layout helpers** (same set as `ResidentDashboard`)
| Method | What it does |
| --- | --- |
| `private static JPanel page()` | Grey tab page with padding. |
| `private static JPanel card(int width, int height)` | White bordered panel with a fixed size (kept for parity; not used by this class). |
| `private static JLabel label(String text, Font font, Color color)` | Left-aligned label. |
| `private static JLabel hint(String text)` | Grey 12 pt hint label. |
| `private static JButton button(String text, Color background, Color foreground)` | Flat button with the given colours. |
| `private static JComboBox<String> combo(String firstItem, List<String> items)` / `options(String[] values)` | Drop-downs filled from a list or an array. |
| `private static String text(JComboBox<String> box)` | Selected text, or `""`. |
| `private static JScrollPane borderedScroll(Component view)` / `detailScroll()` | Bordered scroll panes. |
| `private static JTable dataTable(Object[] columns)` | Read-only table with the navy header and light grid. |
| `private static JPanel fieldBox(String caption, String value)` / `valueBox(String value)` | Bordered caption+value boxes. |
| `private static JPanel descriptionBox(String text)` | Word-wrapped, growing description box. |
| `private static JPanel twoColumns(JPanel left, JPanel right)` | Two equal boxes side by side. |
| `private static void addSection(JPanel body, String title, String[][] fields)` | Section heading plus its 2-column field boxes. |
| `private static String display(String text)` | `"-"` for empty text. |
| `private static Color statusColor(String status)` | Orange / blue / purple / green per status. |
| `private static String detailCaption(String reportType)` | Caption for the type-specific value. |
| `private String reporterName(Report report)` | Reporter’s **full name** (falls back to the id). |
| `private JPanel reportForm(Report report)` | Builds the report card. |
| `private static JPanel userForm(User user)` | Builds the account card (Account Information, plus Resident Details for a resident). |
| `private static JPanel emptyDetail(String message)` | Card shown when nothing is selected. |
| `private void initComponents()` | Builds the window shell: `jPanel1` → `jLayeredPane1` with `titleLabel`, `subtitleLabel`, `tabs` and the **Log Out** button. |
| `private void logoutButtonActionPerformed(ActionEvent evt)` | Calls `logout()`. |

**Connects to:** created by `LoginFrame.handleLogin()`; uses `ReportManager`, `UserManager`, `LocationManager`; creates `Resident` / `Administrator` in `createUser()` / `updateUser()` and a new `LoginFrame` on logout.

---

## 5. Inner (nested) classes

Both dashboards declare three small private nested types. They are duplicated on purpose: the system is limited to four GUI classes, so no shared widget class is allowed.

### `ResidentDashboard.HeaderRenderer` (extends `DefaultTableCellRenderer`)
Paints the table header: navy `PRIMARY` background, white bold 12 pt text and 10 px left padding. Configured once in `dataTable(...)`.

### `ResidentDashboard.StatusRenderer` (extends `DefaultTableCellRenderer`)
Paints the Status column: bold 11 pt. When the row is selected it uses normal text colour, otherwise the status colour from `statusColor(...)` — PENDING orange, VERIFIED blue, IN_PROGRESS purple, RESOLVED green.

### `ResidentDashboard.BodyPanel` (extends `JPanel` implements `Scrollable`)
The white card body used by `reportForm(...)` and `emptyDetail(...)`. A vertical `BoxLayout` with 20/22 px padding, and `getScrollableTracksViewportWidth()` returns `true` so the card always fills the width of its scroll pane.

### `AdminDashboard.HeaderRenderer` / `AdminDashboard.StatusRenderer` / `AdminDashboard.BodyPanel`
Identical to the three above (same painting rules, same scrolling behaviour). `AdminDashboard.BodyPanel` is used by both `reportForm(...)` and `userForm(...)`.

---

## 6. Anonymous classes

| Where | What it is | Why |
| --- | --- | --- |
| `ResidentDashboard.hintField(...)` | anonymous `JTextField` overriding `paintComponent(...)` | draws the grey hint text while the field is empty (submit form and update dialog). |
| `ResidentDashboard.hintArea(...)` | anonymous `JTextArea` overriding `paintComponent(...)` | same, for the description box. |
| `ResidentDashboard.sortReports(...)` | anonymous `Comparator<Report>` | compares by report id, date, status or hazard type. |
| `AdminDashboard.sortReports(...)` | anonymous `Comparator<Report>` | the same comparison, for the admin Reports tab. |

**Listener lambdas** (`ActionListener` / `ListSelectionListener`) written inline with `e -> ...`:

- `ResidentDashboard` — the three filter drop-downs, the submit form's city drop-down, the **Submit Report** button, both tables' selection listeners, **Update Selected Report**, **Delete Selected Report**, and inside the Update Report dialog its city drop-down, **Cancel** and **Update Report** buttons.
- `AdminDashboard` — the three filter drop-downs, **Update Status**, **Delete Invalid Report**, both tables' selection listeners, **Create User**, **Update User**, **Delete User**.

The only listener written as a method reference is the **Log Out** button in both dashboards (`this::logoutButtonActionPerformed`).

---

## 7. How it all fits together

**Start-up**

1. `Main` creates `FileManager("data")`, which makes sure `data\users.txt`, `data\reports.txt` and `data\locations.txt` exist.
2. `UserManager`, `ReportManager` and `LocationManager` load their files through that `FileManager`.
3. `LoginFrame(userManager, reportManager, locationManager)` is created on the Swing event thread.

**Logging in**

4. `LoginFrame.handleLogin()` calls `userManager.login(username, password)`.
5. On success it opens `AdminDashboard` (cast to `Administrator`) or `ResidentDashboard` (cast to `Resident`), passing all three managers, and disposes the login window.
6. **Register as Resident** opens `RegistrationFrame`, whose `register()` builds the address and calls `userManager.registerResident(...)`.

**Resident work**

7. `ResidentDashboard` shows all reports (filtered by city/barangay and sorted), submits new ones through `reportManager.submitReport(...)`, and edits or deletes its own `PENDING` reports through the Update Report dialog and `reportManager.updateResidentReport(...)` / `deleteResidentReport(...)`.
8. The Reporter column and the REPORTER field show the reporter’s **full name**, resolved with `userManager.getUserId(...)`-style lookup (`getUserById`).

**Administrator work**

9. `AdminDashboard` reviews every report, changes a status with `reportManager.updateReportStatus(...)` or removes an invalid one with `deleteInvalidReport(...)`, and manages accounts with `userManager.addUser(...)`, `updateUser(...)` and `deleteUser(...)`.

**Storage**

- `data\users.txt` — one line per account: `type|userId|username|password|fullName` (+ `address|contactNumber` for residents).
- `data\reports.txt` — one line per report: `type|reportId|reporterId|city|barangay|street|specificPlace|description|specificDetail|status|dateSubmitted`.
- `data\locations.txt` — one line per municipality: `Municipality|Barangay|Barangay|...`.

**Where the look lives**

- There is no theme class. Each of the four GUI classes owns its colours (`PRIMARY`, `MUTED`, `TEXT`, `ACCENT`, `BORDER`, `DANGER`), its Segoe UI fonts and its small helpers (`page`, `card`, `label`, `button`, `dataTable`, `fieldBox`, `descriptionBox`, …).
- Both dashboards draw the same shell: `jPanel1` → bevel-bordered `jLayeredPane1` holding the *WatchPoint* title, the “Signed in as …” subtitle, the tabbed pane and the **Log Out** button at the top right.
- Table and report card always share the width 50/50 (`new GridLayout(1, 2, 14, 0)`), so the layout stays balanced when the window is resized.
