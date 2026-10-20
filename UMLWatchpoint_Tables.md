# WatchPoint — UML Class Tables

Community Safety Reporting System (Java + Java Swing) for the province of **Bohol, Philippines**.

This file presents the UML design in **table form**: one *Attributes and methods* table per class,
followed by a *Relationships* table that states which classes it is connected to and how.
Names are written exactly as they appear in the source code.

**Scope:** the **15 classes** of the `model`, `manager` and `storage` packages.
The entry point (`app.WatchPoint`) and every class in the `ui` package (the Swing windows) are
**excluded** — where a class is used by a window, the window itself is out of scope here.

---

## 1. System Overview

| Layer | Classes |
| --- | --- |
| Domain model (`model`) | `User`, `Resident`, `Administrator`, `AreaOfficer`, `Location`, `ReportStatus`, `Report`, `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport` |
| Managers (`manager`) | `UserManager`, `ReportManager`, `LocationManager` |
| Storage (`storage`) | `FileManager` |
| Out of scope | `app.WatchPoint` (entry point) and the whole `ui` package |

**Inheritance at a glance**

| Superclass | Subclasses |
| --- | --- |
| `User` (abstract) | `Resident`, `Administrator`, `AreaOfficer` |
| `Report` (abstract) | `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport` |

**Layers at a glance**

| Layer | Knows about | Never touches |
| --- | --- | --- |
| `model` | other model classes (`Location`, `ReportStatus`) | files, Swing, managers |
| `manager` | `model`, `storage`, its own list | Swing |
| `storage` | the plain-text data files only | model, managers, Swing |

---

## 2. Domain Model (`model`)

### 2.1 `User` (abstract)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String userId` | `U` / `AD` / `AO` + six digits |
| Attribute | `private String password` | Stored as plain text in `users.txt` |
| Attribute | `private String fullName` | 2–60 characters, letters only (plus space `.` `'` `-` `,`) |
| Attribute | `private String dateCreated` | `yyyy-MM-dd HH:mm`, written when the account is made |
| Constructor | `protected User(String userId, String password, String fullName, String dateCreated)` | Called by every subclass |
| Method | `public String getUserId()` | Account id |
| Method | `public String getFullName()` | |
| Method | `public String getDateCreated()` | Shown as **ACCOUNT CREATED** |
| Method | `public void setDateCreated(String dateCreated)` | |
| Method | `public void setFullName(String fullName)` | |
| Method | `public void setPassword(String password)` | |
| Method | `public boolean verifyPassword(String input)` | Used by `UserManager.login(...)` |
| Method | `public abstract String getUserType()` | `"Resident"`, `"Administrator"` or `"Area Officer"` |
| Method | `public String toFileString()` | `type\|userId\|password\|fullName\|dateCreated` |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `Resident`, `Administrator`, `AreaOfficer` | Inheritance | `User` is their superclass; each one calls `super(...)` |
| `UserManager` | Used by | Keeps `List<User>` and calls `verifyPassword(...)`, `getUserId()`, `getFullName()`, `getDateCreated()` and `toFileString()` |

### 2.2 `Resident` (extends `User`)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String username` | Sign-in name; residents are the only account type with one |
| Attribute | `private String address` | `barangay + ", " + cityMunicipality` |
| Attribute | `private String contactNumber` | Exactly 11 digits, starting with `09` |
| Constructor | `public Resident(String userId, String username, String password, String fullName, String dateCreated, String address, String contactNumber)` | Calls `super(userId, password, fullName, dateCreated)` |
| Method | `public String getUsername()` | |
| Method | `public void setUsername(String username)` | 3–40 characters, unique, must contain a letter |
| Method | `public String getAddress()` | Split again by `UserManager.municipalityOf(...)` / `barangayOf(...)` |
| Method | `public void setAddress(String address)` | |
| Method | `public String getContactNumber()` | Returned by `UserManager.getContactById(...)` |
| Method | `public void setContactNumber(String contactNumber)` | |
| Method | `@Override public String getUserType()` | Returns `"Resident"` |
| Method | `@Override public String toFileString()` | Appends `username`, `address`, `contactNumber` (8 fields in total) |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `User` | Inheritance | Extends the abstract superclass |
| `UserManager` | Created by | `registerResident(...)` (sign-up) and `parseUser(...)` (loading `users.txt`) |
| `UserManager` | Used by | `login(...)` also matches the username; `saveDetails(...)` edits name, username, contact and address; `getContactById(...)` reads the contact number; `checkUsername(...)` validates the username |

### 2.3 `Administrator` (extends `User`)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | — | No attributes of its own; it inherits all four `User` fields |
| Constructor | `public Administrator(String userId, String password, String fullName, String dateCreated)` | Calls `super(...)` |
| Method | `@Override public String getUserType()` | Returns `"Administrator"` |
| Method | `@Override public String toFileString()` | Inherits the base format: 5 fields, nothing appended |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `User` | Inheritance | Extends the abstract superclass |
| `UserManager` | Created by | `addAdministrator(...)` and `parseUser(...)` |
| `UserManager` | Used by | `countAdministrators()` counts them so the last administrator cannot be deleted |

### 2.4 `AreaOfficer` (extends `User`)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String area` | One city / municipality; the officer only works inside it |
| Constructor | `public AreaOfficer(String userId, String password, String fullName, String dateCreated, String area)` | Calls `super(...)` |
| Method | `public String getArea()` | Passed to every area-limited query |
| Method | `public void setArea(String area)` | Required, 60 characters or fewer |
| Method | `@Override public String getUserType()` | Returns `"Area Officer"` |
| Method | `@Override public String toFileString()` | Appends `area` (6 fields in total) |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `User` | Inheritance | Extends the abstract superclass |
| `UserManager` | Created by | `addAreaOfficer(...)` and `parseUser(...)`; `areaProblem(...)` validates the area |
| `ReportManager` | Uses | Its `area` is the `cityMunicipality` argument of `getReports(city, barangay, sort)`, `getReportsInCity(...)`, `countReports(...)` and `changeStatusInCity(...)` |

### 2.5 `Location`

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String cityMunicipality` | Must exist in `locations.txt` |
| Attribute | `private String barangay` | Must belong to that city / municipality |
| Attribute | `private String street` | Optional; stored as `N/A` when left empty |
| Attribute | `private String specificPlace` | Required, 2–60 characters |
| Constructor | `public Location(String cityMunicipality, String barangay, String street, String specificPlace)` | Value object — no validation here |
| Method | `public String getCityMunicipality()` | Filtering and the Area Officer's area check |
| Method | `public String getBarangay()` | Filtering, duplicate detection, statistics |
| Method | `public String getStreet()` | Displayed in the report detail |
| Method | `public String getSpecificPlace()` | Displayed in the report detail |
| Method | `public String getFullLocation()` | `street, barangay, city (specificPlace)` |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `Report` | Composition | Every report owns exactly one `Location` (`Report.location`) |
| `ReportManager` | Created by | `parseReport(...)` rebuilds it from a line; `submitReport(...)` rebuilds it with trimmed fields before saving |

### 2.6 `ReportStatus` (enum)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Constant | `PENDING` | New report, waiting for review |
| Constant | `VERIFIED` | Reviewed and confirmed as a real hazard |
| Constant | `IN_PROGRESS` | The responsible department has started the work |
| Constant | `RESOLVED` | The issue was fixed; final |
| Constant | `REJECTED` | Invalid, duplicate, unsupported or out of scope; final |
| Method | `public boolean isFinal()` | `true` for `RESOLVED` and `REJECTED` |
| Method | `public boolean canMoveTo(ReportStatus target)` | The workflow rule (table below) |
| Method | `public List<ReportStatus> nextStatuses()` | The allowed targets, in workflow order |
| Method | `public String displayName()` | `name()` with a space instead of `_` (`IN PROGRESS`) |

**Allowed transitions** — the state-machine part of the UML

| From | Allowed next | Why |
| --- | --- | --- |
| `PENDING` | `VERIFIED`, `REJECTED` | A new report must be reviewed before work starts |
| `VERIFIED` | `IN_PROGRESS`, `REJECTED` | Only a verified report can be worked on |
| `IN_PROGRESS` | `RESOLVED`, `REJECTED` | Only work that started can be finished |
| `RESOLVED` | none | Final; no normal status change |
| `REJECTED` | none | Final; only an administrator may reopen it (`ReportManager.reopenReport(...)` → `PENDING`) |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `Report` | Composition | `Report.status` is one of these constants |
| `Report` | Used by | `toFileString()` writes `name()`; `parseReport(...)` reads it back through `parseStatus(...)` |
| `ReportManager` | Used by | `statusProblem(...)` and `changeStatus(...)` enforce the table, `nextStatusesText(...)` builds the refusal message, and `countReports(...)` counts reports per status |

### 2.7 `Report` (abstract)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String reportId` | `R` + three digits |
| Attribute | `private String reporterId` | The `userId` of the resident who filed it |
| Attribute | `private Location location` | Composition; city, barangay, street, specific place |
| Attribute | `private String description` | 10–500 characters, one line |
| Attribute | `private ReportStatus status` | Starts as `PENDING` |
| Attribute | `private String dateSubmitted` | `yyyy-MM-dd HH:mm` |
| Attribute | `private String remark` | Rejection reason; empty for every other status |
| Constructor | `protected Report(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted)` | Sets `remark` to `""` |
| Method | `public String getReportId()` | |
| Method | `public String getReporterId()` | Resolved to a name and contact by `UserManager` |
| Method | `public Location getLocation()` | |
| Method | `public String getDescription()` | |
| Method | `public ReportStatus getStatus()` | |
| Method | `public String getDateSubmitted()` | |
| Method | `public void setLocation(Location location)` | Used when a resident edits a pending report |
| Method | `public void setDescription(String description)` | |
| Method | `public void setStatus(ReportStatus status)` | Only called by `ReportManager.changeStatus(...)` |
| Method | `public boolean canChangeStatusTo(ReportStatus target)` | Delegates to `status.canMoveTo(target)` |
| Method | `public String getRemark()` | Never `null` (returns `""`) |
| Method | `public void setRemark(String remark)` | Cleared when the report leaves `REJECTED` |
| Method | `public abstract String getReportType()` | `"Road Hazard"`, `"Flood"`, `"Streetlight"`, `"Other"` |
| Method | `public abstract String getSpecificDetail()` | The hazard-specific detail |
| Method | `public abstract void setSpecificDetail(String specificDetail)` | |
| Method | `public String toFileString()` | The 12 fields of `reports.txt` |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport` | Inheritance | The four concrete report types |
| `Location` | Composition | Every report has exactly one location |
| `ReportStatus` | Composition | Every report has exactly one status (and `canChangeStatusTo` asks it) |
| `ReportManager` | Managed by | Creates, stores, parses, saves, updates the status and deletes reports |
| `UserManager` | Uses | The reporter is resolved with `getFullNameById(...)` / `getContactById(...)` |

### 2.8 `RoadHazardReport` (extends `Report`)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String roadHazardType` | Free text such as `Pothole` or `Broken road` |
| Constructor | `public RoadHazardReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String roadHazardType)` | Calls `super(...)` |
| Method | `@Override public String getReportType()` | Returns `"Road Hazard"` |
| Method | `@Override public String getSpecificDetail()` | Returns `roadHazardType` |
| Method | `@Override public void setSpecificDetail(String specificDetail)` | Replaces `roadHazardType` |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `Report` | Inheritance | Concrete report type |
| `ReportManager` | Created by | `submitReport(...)` when the hazard type is `"Road Hazard"`, and `parseReport(...)` |

### 2.9 `FloodReport` (extends `Report`)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String waterLevel` | Free text such as `Knee deep` |
| Constructor | `public FloodReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String waterLevel)` | Calls `super(...)` |
| Method | `@Override public String getReportType()` | Returns `"Flood"` |
| Method | `@Override public String getSpecificDetail()` | Returns `waterLevel` |
| Method | `@Override public void setSpecificDetail(String specificDetail)` | Replaces `waterLevel` |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `Report` | Inheritance | Concrete report type |
| `ReportManager` | Created by | `submitReport(...)` / `parseReport(...)` for `"Flood"` |

### 2.10 `StreetlightReport` (extends `Report`)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String poleNumber` | Free text such as `Pole 12` |
| Constructor | `public StreetlightReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String poleNumber)` | Calls `super(...)` |
| Method | `@Override public String getReportType()` | Returns `"Streetlight"` |
| Method | `@Override public String getSpecificDetail()` | Returns `poleNumber` |
| Method | `@Override public void setSpecificDetail(String specificDetail)` | Replaces `poleNumber` |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `Report` | Inheritance | Concrete report type |
| `ReportManager` | Created by | `submitReport(...)` / `parseReport(...)` for `"Streetlight"` |

### 2.11 `OtherHazardReport` (extends `Report`)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String hazardCategory` | Free text such as `Fallen tree` |
| Constructor | `public OtherHazardReport(String reportId, String reporterId, Location location, String description, ReportStatus status, String dateSubmitted, String hazardCategory)` | Calls `super(...)` |
| Method | `@Override public String getReportType()` | Returns `"Other"` |
| Method | `@Override public String getSpecificDetail()` | Returns `hazardCategory` |
| Method | `@Override public void setSpecificDetail(String specificDetail)` | Replaces `hazardCategory` |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `Report` | Inheritance | Concrete report type |
| `ReportManager` | Created by | `submitReport(...)` / `parseReport(...)` for every hazard type that is not `"Road Hazard"`, `"Flood"` or `"Streetlight"` (the list is `ReportManager.HAZARD_TYPES`) |

---

## 3. Managers and Storage (`manager`, `storage`)

### 3.1 `FileManager` (storage)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private String dataFolder` | Folder that holds the three text files |
| Constructor | `public FileManager(String dataFolder)` | Also creates `users.txt`, `reports.txt` and `locations.txt` if they are missing |
| Method | `public List<String> readLines(String fileName)` | Reads one record per line, skipping blank lines |
| Method | `public void writeLines(String fileName, List<String> lines)` | Rewrites the whole file, one line per element |
| Method | `private void ensureFileExists(String fileName)` | Creates the folder and the file when needed |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `UserManager` | Used by | Reads and writes `users.txt` |
| `ReportManager` | Used by | Reads and writes `reports.txt` |
| `LocationManager` | Used by | Reads `locations.txt` |
| `data/users.txt`, `data/reports.txt`, `data/locations.txt` | Reads / writes | The only class in the system that touches the files |

### 3.2 `UserManager` (manager)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private List<User> users` | Every account, loaded once in the constructor |
| Attribute | `private FileManager fileManager` | Used to read and save `users.txt` |
| Constructor | `public UserManager(FileManager fileManager)` | Calls `loadUsers()` |
| Method | `public boolean registerResident(String fullName, String username, String password, String address, String contactNumber)` | Validates everything, then creates a `Resident` and saves |
| Method | `public User login(String loginId, String password)` | Matches the id of any account, or the username of a resident, case-insensitively |
| Method | `public User getUserById(String userId)` | Linear search |
| Method | `public List<User> getAllUsers()` | Copy of the list |
| Method | `public boolean updateUser(User user)` | Replaces the stored object with the same id and saves |
| Method | `public boolean deleteUser(String userId)` | Removes and saves |
| Method | `public String getFullNameById(String userId)` | `"-"` when the account is gone |
| Method | `public String getContactById(String userId)` | `"-"` when there is no resident or no number |
| Method | `public Administrator addAdministrator(String fullName, String password)` | Validates, generates the `AD` id, saves |
| Method | `public AreaOfficer addAreaOfficer(String fullName, String password, String area)` | Validates, generates the `AO` id, saves |
| Method | `public String checkUsername(User user, String username)` | 3–40 characters, allowed characters, contains a letter, not an id, unique |
| Method | `public String saveDetails(User user, String fullName, String username, String contactNumber, String barangay, String municipality)` | Name, username, contact and address; returns the problem or `null` |
| Method | `public String changePassword(User user, String currentPassword, String newPassword, String confirmPassword)` | Current must match, new must be strong and different, confirm must match |
| Method | `public boolean saveAccountChanges(User user, String fullName, String password, String address, String contactNumber, String area)` | The administrator's edit form; re-validates everything |
| Method | `public int countAdministrators()` | Used to protect the last administrator |
| Method | `public List<User> searchUsers(String keyword)` | Search by id, username or full name |
| Method | `public List<User> searchUsers(String keyword, String userType)` | Same, filtered by `"All Types"`, `"Resident"`, `"Administrator"` or `"Area Officer"` |
| Method | `public static String fullNameProblem(String fullName)` | The full-name rule; returns the message or `null` |
| Method | `public static String passwordProblem(String password)` | 8–64 characters, no spaces, no `\|` |
| Method | `public static String contactNumberProblem(String contactNumber)` | 11 digits starting with `09` |
| Method | `public static String areaProblem(String area)` | Required and 60 characters or fewer |
| Method | `public static String municipalityOf(String address)` | Text after the last comma of an address |
| Method | `public static String barangayOf(String address)` | Text before the last comma of an address |
| Method | `private static String forbiddenCharacterProblem(String value)` | Refuses `\|`, `\n` and `\r` |
| Method | `private static boolean looksLikeAccountId(String text)` | `true` for `U` / `AD` / `AO` + six digits |
| Method | `private boolean isUsernameTaken(String username)` | Case-insensitive duplicate check |
| Method | `private void loadUsers()` | Reads `users.txt` through `parseUser(...)` |
| Method | `private void saveUsers()` | Writes every account through `toFileString()` |
| Method | `private static String formatId(String prefix, int number)` | Prefix + six digits |
| Method | `private String generateUserId()` | Highest `U` number + 1 |
| Method | `private String generateAreaOfficerId()` | Highest `AO` number + 1 |
| Method | `private String generateAdministratorId()` | Highest `AD` number + 1 |
| Method | `private String currentTimestamp()` | `yyyy-MM-dd HH:mm` for `dateCreated` |
| Method | `private User parseUser(String line)` | Rebuilds the right subclass from one line |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `FileManager` | Uses | `readLines("users.txt")` and `writeLines("users.txt", ...)` |
| `Resident` | Creates | `registerResident(...)` and `parseUser(...)` |
| `Administrator` | Creates | `addAdministrator(...)` and `parseUser(...)` |
| `AreaOfficer` | Creates | `addAreaOfficer(...)` and `parseUser(...)` |
| `User` | Aggregation | Keeps `List<User> users` and calls `toFileString()`, `verifyPassword(...)` and the getters |
| `Report` | Does not see | A report stores only the reporter's `userId`; the name and contact are looked up here on demand |

### 3.3 `ReportManager` (manager)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private List<Report> reports` | Every report, loaded once in the constructor |
| Attribute | `private FileManager fileManager` | Used to read and save `reports.txt` |
| Constant | `public static final String[] HAZARD_TYPES` | `Road Hazard`, `Flood`, `Streetlight`, `Other` |
| Constructor | `public ReportManager(FileManager fileManager)` | Calls `loadReports()` |
| Method | `public Report submitReport(String reporterId, String hazardType, Location location, String description, String specificDetail)` | Validates, creates the concrete report as `PENDING`, saves |
| Method | `public Report getReportById(String reportId)` | Linear search |
| Method | `public List<Report> getReportsByReporter(String reporterId)` | A resident's own reports |
| Method | `public List<Report> getAllReports()` | Copy of the list |
| Method | `public boolean updateResidentReport(Report report, String residentId)` | Only the owner, and only while `PENDING` |
| Method | `public boolean deleteResidentReport(String reportId, String residentId)` | Only the owner, and only while `PENDING` |
| Method | `public boolean deleteInvalidReport(String reportId)` | Administrator deletion of spam |
| Method | `public boolean updateReportStatus(String reportId, ReportStatus status)` | Legacy wrapper around `changeStatus(...)` |
| Method | `public boolean updateReportStatus(String reportId, ReportStatus status, String remark)` | Legacy wrapper, with a reason |
| Method | `public boolean updateReportStatusInCity(String reportId, ReportStatus status, String cityMunicipality)` | Legacy wrapper with the area check |
| Method | `public boolean updateReportStatusInCity(String reportId, ReportStatus status, String cityMunicipality, String remark)` | Legacy wrapper with area and reason |
| Method | `public String changeStatus(Report report, ReportStatus target, String reason)` | Validate → set status and remark → save; returns the message or `null` |
| Method | `public String changeStatusInCity(Report report, ReportStatus target, String reason, String cityMunicipality)` | Same, but refuses a report outside the area |
| Method | `public String reopenReport(Report report, boolean administrator)` | `REJECTED` → `PENDING`, clears the reason, administrator only |
| Method | `public static String statusProblem(Report report, ReportStatus target, String reason)` | The workflow rule plus the rejection-reason rule |
| Method | `public static ReportStatus parseStatus(String text)` | Reads a status name, falls back to `PENDING` |
| Method | `public static String remarkProblem(String remark)` | 5–200 characters, one line, no `\|` |
| Method | `public List<Report> getReports(String cityMunicipality, String barangay, String sortMode)` | Filtering and sorting for the tables |
| Method | `public List<Report> getReportsInCity(String cityMunicipality)` | Shortcut for one area |
| Method | `public int countReports(String cityMunicipality, ReportStatus status)` | A `null` status counts every report |
| Method | `public List<String[]> countByBarangay(String cityMunicipality)` | `{ barangay, count }`, busiest first |
| Method | `public List<String[]> countByHazardType(String cityMunicipality)` | `{ type, count }`, most common first |
| Method | `public Report oldestPending(String cityMunicipality)` | The `PENDING` report that waited longest |
| Method | `public static int daysWaiting(String dateSubmitted)` | Whole days since submission, `-1` when unreadable |
| Method | `public boolean hasPendingDuplicate(String reporterId, String hazardType, String barangay, String specificDetail)` | Blocks the same pending hazard twice |
| Method | `public static String oneLine(String text)` | Collapses spaces, tabs and line breaks into one line |
| Method | `public static String reportProblem(String hazardType, Location location, String description, String specificDetail)` | All report field rules; returns the message or `null` |
| Method | `private static String forbiddenCharacterProblem(String street, String place, String detail, String description)` | Refuses `\|` in report text |
| Method | `private static String nextStatusesText(ReportStatus status)` | `"VERIFIED or REJECTED"` for the refusal messages |
| Method | `private List<String[]> countBy(String cityMunicipality, boolean byBarangay)` | Shared counting for both breakdowns |
| Method | `private static void addTotal(List<String> names, List<Integer> totals, String name)` | Increments a name/count pair |
| Method | `private static void sort(List<Report> list, String sortMode)` | By id, date, status or type |
| Method | `private void loadReports()` | Reads `reports.txt` through `parseReport(...)` |
| Method | `private void saveReports()` | Writes every report through `toFileString()` |
| Method | `private Report parseReport(String line)` | Rebuilds the subclass, the status and the remark |
| Method | `private String currentTimestamp()` | `yyyy-MM-dd HH:mm` for `dateSubmitted` |
| Method | `private String generateReportId()` | `R` + three digits |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `FileManager` | Uses | `readLines("reports.txt")` and `writeLines("reports.txt", ...)` |
| `Report` | Aggregation | Keeps `List<Report> reports`; calls `toFileString()`, `getStatus()`, `getLocation()` and the setters |
| `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport` | Creates | `submitReport(...)` picks the class from `HAZARD_TYPES`, `parseReport(...)` rebuilds it from the line |
| `Location` | Creates | `parseReport(...)` and `submitReport(...)` (trimmed copy) |
| `ReportStatus` | Uses | `parseStatus(...)`, `statusProblem(...)`, `changeStatus(...)`, `countReports(...)` and the workflow messages |
| `UserManager` | Does not use | The reporter is only stored as an id; names are resolved by the caller |

### 3.4 `LocationManager` (manager)

**Attributes and methods**

| Kind | Member | Notes |
| --- | --- | --- |
| Attribute | `private List<String> lines` | The raw lines of `locations.txt` |
| Attribute | `private FileManager fileManager` | Used to read `locations.txt` |
| Constructor | `public LocationManager(FileManager fileManager)` | Loads the file immediately |
| Method | `public List<String> getMunicipalities()` | The city / municipality names, in file order |
| Method | `public List<String> getBarangays(String municipality)` | The barangays of one city / municipality |
| Method | `private String municipalityOf(String line)` | Text before the first `\|` |

**Relationships**

| Related class | Relationship | Details |
| --- | --- | --- |
| `FileManager` | Uses | `readLines("locations.txt")` |
| `data/locations.txt` | Reads | The only source of valid city and barangay names |
| `ReportManager` | Supplies the names that | `reportProblem(...)` expects for the city and barangay of a report |
| `UserManager` | Supplies the names that | `areaProblem(...)` expects for an area officer's area |

---

## 4. Relationship Map (whole system)

### 4.1 Inheritance (generalisation)

| Superclass | Subclasses | What differs |
| --- | --- | --- |
| `User` (abstract) | `Resident`, `Administrator`, `AreaOfficer` | `getUserType()` and `toFileString()`; `Resident` adds username, address and contact, `AreaOfficer` adds the area |
| `Report` (abstract) | `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport` | `getReportType()`, `getSpecificDetail()` and `setSpecificDetail(...)` |

### 4.2 Composition / aggregation

| Whole | Part | Kind | Notes |
| --- | --- | --- | --- |
| `Report` | `Location` | Composition | One report owns one location; it is recreated when the report is edited |
| `Report` | `ReportStatus` | Composition | One report owns one status at a time |
| `UserManager` | `List<User>` | Aggregation | All accounts, in file order |
| `ReportManager` | `List<Report>` | Aggregation | All reports, in file order |
| `LocationManager` | `List<String>` | Aggregation | The raw lines of `locations.txt` |
| `UserManager`, `ReportManager`, `LocationManager` | `FileManager` | Association | Each manager keeps one reference and uses it for its own file |

### 4.3 Dependency / usage (who calls whom)

| Class | Depends on | Details |
| --- | --- | --- |
| `UserManager` | `FileManager`, `User`, `Resident`, `Administrator`, `AreaOfficer` | Creates the subclasses, asks `User` for its data, saves through `FileManager` |
| `ReportManager` | `FileManager`, `Report`, the four report subclasses, `Location`, `ReportStatus` | Creates and stores reports, enforces the workflow, saves through `FileManager` |
| `LocationManager` | `FileManager` | Only reads `locations.txt` |
| `FileManager` | `java.io` | Reads and writes the three text files |
| `Resident` | `User` | Inheritance |
| `Administrator` | `User` | Inheritance |
| `AreaOfficer` | `User` | Inheritance |
| `RoadHazardReport`, `FloodReport`, `StreetlightReport`, `OtherHazardReport` | `Report` | Inheritance |
| `Report` | `Location`, `ReportStatus` | Composition |

### 4.4 Object-creation map

| Creator | Creates | When |
| --- | --- | --- |
| `UserManager` | `Resident` | `registerResident(...)` and `parseUser(...)` |
| `UserManager` | `Administrator` | `addAdministrator(...)` and `parseUser(...)` |
| `UserManager` | `AreaOfficer` | `addAreaOfficer(...)` and `parseUser(...)` |
| `ReportManager` | `RoadHazardReport` / `FloodReport` / `StreetlightReport` / `OtherHazardReport` | `submitReport(...)` (new report) and `parseReport(...)` (loading) |
| `ReportManager` | `Location` | `parseReport(...)` and `submitReport(...)` |

### 4.5 Status flow at a glance

```
                 submitReport()
        [new] ───────────────────► PENDING
                                     │  │
                        VERIFIED     │  │  REJECTED (reason required)
                    ┌────────────────┘  └────────────────┐
                    ▼                                    ▼
                VERIFIED                              REJECTED
                    │  │                                  │
       IN_PROGRESS  │  │  REJECTED (reason required)       │ reopenReport(report, true)
            ┌───────┘  └──────────────┐                   │   (administrator only)
            ▼                         ▼                   │
        IN_PROGRESS ──────REJECTED──► REJECTED ◄──────────┘
            │                         ▲
   RESOLVED │                         │
            ▼                         │
        RESOLVED  (final)             │
```

| Actor | May change the status | May reopen a `REJECTED` report |
| --- | --- | --- |
| Resident | No — view only | No |
| Area Officer | Yes, inside its own city / municipality | No |
| Administrator | Yes, anywhere in the province | Yes |

Every change goes through `ReportManager.changeStatus(...)` (or `changeStatusInCity(...)`), which calls
`ReportStatus.canMoveTo(...)` and refuses anything the table in section 2.6 does not allow; only
`ReportManager.reopenReport(report, true)` can leave `REJECTED`, and nothing can leave `RESOLVED`.

---

## 5. Data Files (accessed through `FileManager`)

All three files are plain text, one record per line, with `|` as the field separator — which is why the
validators refuse a `|` inside any free-text value.

### 5.1 `data/users.txt`

| Account type | Line format |
| --- | --- |
| Resident | `Resident\|userId\|password\|fullName\|dateCreated\|username\|address\|contactNumber` |
| Administrator | `Administrator\|userId\|password\|fullName\|dateCreated` |
| AreaOfficer | `AreaOfficer\|userId\|password\|fullName\|dateCreated\|area` |

**Written by** `User.toFileString()` and its overrides (`Resident`, `AreaOfficer`);
**read by** `UserManager.parseUser(...)`.

| Field | Meaning |
| --- | --- |
| `type` | `Resident`, `Administrator` or `AreaOfficer` (from `getUserType()`) |
| `userId` | `U`, `AD` or `AO` + six digits (`UserManager.formatId(...)`) |
| `password` | Stored as plain text |
| `fullName` | 2–60 characters |
| `dateCreated` | `yyyy-MM-dd HH:mm`, written when the account is made |
| `username` | Residents only; the sign-in name |
| `address` | `barangay + ", " + cityMunicipality` |
| `contactNumber` | 11 digits starting with `09` |
| `area` | Area officers only; one city / municipality |

```
Administrator|AD000001|admin123|System Admin|2026-10-20 09:15
Resident|U000003|maria123|Maria Santos|2026-10-20 09:16|maria|Cogon, Tagbilaran City|09171234567
AreaOfficer|AO000001|officer123|Tagbilaran City Officer|2026-10-20 09:17|Tagbilaran City
```

### 5.2 `data/reports.txt`

```
type|reportId|reporterId|cityMunicipality|barangay|street|specificPlace|description|specificDetail|status|dateSubmitted|remark
```

**Written by** `Report.toFileString()`; **read by** `ReportManager.parseReport(...)`.

| Field | Meaning |
| --- | --- |
| `type` | `Road Hazard`, `Flood`, `Streetlight` or `Other` (from `getReportType()`) |
| `reportId` | `R` + three digits (`ReportManager.generateReportId()`) |
| `reporterId` | The resident's `userId` |
| `cityMunicipality` | Must exist in `locations.txt` |
| `barangay` | Must belong to that city / municipality |
| `street` | `N/A` when the resident leaves it empty |
| `specificPlace` | Required, 2–60 characters |
| `description` | 10–500 characters, one line |
| `specificDetail` | The hazard-specific detail (road type, water level, pole number, category) |
| `status` | `PENDING`, `VERIFIED`, `IN_PROGRESS`, `RESOLVED` or `REJECTED` |
| `dateSubmitted` | `yyyy-MM-dd HH:mm` |
| `remark` | The rejection reason; empty for every other status |

```
Flood|R004|U000003|Tagbilaran City|Cogon|M. Torralba Street|Near Cogon Market|Water rising after heavy rain|Knee deep|REJECTED|2026-10-16 07:20|Duplicate of R001
```

A line written before the `remark` field existed still loads, because `parseReport(...)` treats a
missing last field as empty.

### 5.3 `data/locations.txt`

```
cityMunicipality|barangay1|barangay2|...
```

One city / municipality per line; **read by** `LocationManager` (`getMunicipalities()` and
`getBarangays(...)`).

```
Tagbilaran City|Bool|Cogon|Dao|Poblacion|Tiptip
```

---

*End of document.*
