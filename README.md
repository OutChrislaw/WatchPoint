# WatchPoint: A Community Safety Reporting System

Desktop application (Java + Java Swing) for reporting and tracking community safety
hazards across the province of **Bohol**. Reports are stored in plain `.txt` files
(no real database).

## Project layout

```
WatchPoint/
├── src/
│   ├── app/                    Where the program starts
│   │   └── WatchPoint.java         Creates the managers and opens LoginFrame
│   ├── model/                  Data classes (no GUI, no file access)
│   │   ├── User.java               Abstract parent of Resident and Administrator
│   │   ├── Resident.java           A community member who files reports
│   │   ├── Administrator.java      Staff who manages reports and users (AD ids)
│   │   ├── AreaOfficer.java        Staff for one city/municipality (AO ids)
│   │   ├── Location.java           City/municipality, barangay, street, specific place
│   │   ├── ReportStatus.java       PENDING, VERIFIED, IN_PROGRESS, RESOLVED
│   │   ├── Report.java             Abstract parent of the four report types
│   │   ├── RoadHazardReport.java
│   │   ├── FloodReport.java
│   │   ├── StreetlightReport.java
│   │   └── OtherHazardReport.java
│   ├── storage/                The file layer
│   │   └── FileManager.java        Reads and writes the data files
│   ├── manager/                The rules (a GUI class never touches a file)
│   │   ├── UserManager.java        Registration, login, and user records
│   │   ├── ReportManager.java      Submit, filter, sort, update, and delete reports
│   │   └── LocationManager.java    Bohol cities/municipalities and their barangays
│   └── ui/                     Every window and the shared look
│       ├── LoginFrame.java         Login window
│       ├── RegistrationFrame.java  New resident sign-up window
│       ├── ResidentDashboard.java  Window for residents
│       ├── AdminDashboard.java     Window for administrators
│       ├── AreaOfficerDashboard.java Window for area officers
│       ├── UserUpdateDialog.java   Form an admin uses to edit an account
│       ├── ProfileDialog.java      Settings window of the signed-in user
│       ├── ReportUpdateDialog.java Modal Update Report form
│       └── UITheme.java            Shared colours, fonts, and reusable widgets
├── data/                       Plain text storage
│   ├── users.txt               One user or area officer per line
│   ├── reports.txt             One report per line
│   └── locations.txt           Bohol cities/municipalities and barangays
└── bin/                        Compiled .class files (same package folders)
```

Dependencies only point one way — no class reaches back up:

```
app  ──►  ui  ──►  manager  ──►  model
                     │
                     └──►  storage
```

**Roles**

| Role | Created by | Sees | Can do |
|---|---|---|---|
| Resident | self-registers and can log in right away | own reports | Submit a report, view reports, update or delete own PENDING reports, edit own details, username and password from the Settings button |
| Area Officer | an Administrator | only its own city/municipality | Change report status inside its area, change own password from the Settings button |
| Administrator | an Administrator | the whole province | All reports, change status, delete invalid reports, edit any account in a form, filter accounts by type and search by id, username or full name, change own password from the Settings button |

**Creating accounts.** Residents sign up from the login window and can log in straight away.
Only an administrator creates the other accounts, from the **Create User** form in the Users tab:
pick the user type, type the full name and password, and choose the area for an area officer. The
new id is shown after saving, because administrators and area officers log in with it.

`users.txt` line format (one line per account type):

```
Resident|userId|password|fullName|dateCreated|username|address|contactNumber
Administrator|userId|password|fullName|dateCreated
AreaOfficer|userId|password|fullName|dateCreated|area
```

`dateCreated` is the moment the account was made, stored as `yyyy-MM-dd HH:mm`. It is written
automatically when a resident signs up or when an administrator creates an account, and it shows as
**ACCOUNT CREATED** on the account card. The field is empty on accounts that were added before it
existed, and the card shows `-` for those.

Only a **Resident** has a username, because residents sign in with it (their id works too).
Administrators and area officers have **no username** and sign in with their **id**.
Every id ends with six digits: `U000003` for a resident, `AD000001` for an administrator and
`AO000001` for an area officer. Example lines:

```
Administrator|AD000001|admin123|System Admin|2026-10-20 09:15
Resident|U000003|maria123|Maria Santos|2026-10-20 09:16|maria|Cogon, Tagbilaran City|09171234567
AreaOfficer|AO000001|officer123|Tagbilaran City Officer|2026-10-20 09:17|Tagbilaran City
```


`reports.txt` line format:

```
type|reportId|reporterId|cityMunicipality|barangay|street|specificPlace|description|specificDetail|status|dateSubmitted
```

`dateSubmitted` is stored as `yyyy-MM-dd HH:mm` (date and time).

`locations.txt` line format (one line per city/municipality):

```
cityMunicipality|barangay1|barangay2|...
```

**Settings window.** The **Settings** button in the header (every role) opens a window with a menu on the left:

| Item | What it does |
|---|---|
| Account Details | Full name, username (residents only), contact and address. Administrators and area officers also see the user id they log in with. A resident may change the username (3-40 characters; letters, numbers, `.` `_` `@` `+` `-`; it must contain a letter and must not be used by another resident or look like an account id). |
| Change Password | Current password, new password (at least 8 characters, no spaces, different from the current one) and confirmation. |
| Logout | Signs out and returns to the login window. |

**Validation rules.** Every text field is checked before it is saved, and the managers repeat the
same checks so nothing can slip past the forms.

| Field | Rule |
|---|---|
| Password | 8 to 64 characters, no spaces, and it is typed twice on every form that sets a password. A new password must be different from the old one. |
| Contact Number | Exactly 11 digits and it must start with `09` (for example `09171234567`). The field only accepts digits, so letters, spaces and dashes cannot be typed or pasted. |
| Full Name | 2 to 60 characters, letters and space `.` `'` `-` `,` only, and it must contain a letter. |
| Username | 3 to 40 characters from letters, numbers and `.` `_` `@` `+` `-`; it must contain a letter, must be unique, must not be only digits, must not look like an account id (`U000001`, `AD000001`, `AO000001`) and must not match another account's id. |
| Report | Hazard type from the fixed list; city / municipality and barangay chosen from `locations.txt`; specific place (2-60 characters); specific detail (2-80 characters); description (10-500 characters, line breaks are collapsed into spaces); street is optional and saved as `N/A`; a resident cannot file the same PENDING hazard twice. |
| Status change | The chosen status must differ from the report's current status. |
| Administrator actions | An administrator cannot delete the account they are signed in with or the last remaining administrator, and deleting a resident who filed reports asks for confirmation first. |
| Any text | The `\|` character and line breaks are refused, because the data files use `\|` as the separator between fields. |

Rules are only applied to new input. Accounts created before a rule existed keep working, and their
stored values are checked again the next time somebody edits them.
## Build and run

From the project root (PowerShell):

```
javac -d bin (Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp bin app.WatchPoint
```

The same compile from `cmd.exe`:

```
javac -d bin src\app\*.java src\model\*.java src\storage\*.java src\manager\*.java src\ui\*.java
java -cp bin app.WatchPoint
```

`app.WatchPoint` is the class name because `WatchPoint` now lives in the `app` package.

### In VS Code

| Button / command | What it does |
|---|---|
| **Run Code** (Code Runner, `Ctrl`+`Alt`+`N`) | Builds the whole project and starts `app.WatchPoint`, using the command in `.vscode\settings.json` — no batch file. |
| ▷ **Run** in the editor gutter (Java extension) | Works too — open the `WatchPoint` folder itself as the workspace, not its parent folder. |

Code Runner's default *"javac this one file, then java this one class"* cannot work here,
because the classes live in packages (`app`, `model`, `manager`, `storage`, `ui`). The
override in `.vscode\settings.json` uses `javac -sourcepath src` on
`src\app\WatchPoint.java`, so only the entry point has to be named:

```
javac -d "...\bin" -sourcepath "...\src" "...\src\app\WatchPoint.java" && java -cp "...\bin" app.WatchPoint
```

Run the app from the project root so the `data` folder is found. The `data` folder
and the text files are created automatically when they are missing. `locations.txt`
ships with the full list of Bohol cities/municipalities and their barangays.

