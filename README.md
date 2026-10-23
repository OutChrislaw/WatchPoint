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
| Resident | self-registers and can log in right away | own reports and the public reports list | Submit a report, view reports, update or delete own PENDING reports, see why a report was rejected, edit own details, username and password from the Settings button |
| Area Officer | an Administrator | only its own city/municipality | **Area overview** (counts per status, hazard type and barangay, oldest pending report), move a report through the status workflow inside its area, reject a report with a reason, see the reporter details saved with each report (full name, address, contact number), change own password from the Settings button |
| Administrator | an Administrator | the whole province | **Province-wide overview**, all reports, change status, reject with a reason, **reopen a rejected report**, delete invalid reports, see the reporter details saved with each report, see how many reports an account filed, edit any account in a form, filter accounts by type and search by id, username or full name, change own password from the Settings button |

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
type|reportId|reporterId|cityMunicipality|barangay|street|specificPlace|description|specificDetail|status|dateSubmitted|remark|reporterName|reporterAddress|reporterContact
```

`dateSubmitted` is stored as `yyyy-MM-dd HH:mm` (date and time).

`status` is one of **PENDING**, **VERIFIED**, **IN_PROGRESS**, **RESOLVED** or **REJECTED**.
`remark` holds the reason written when a report is rejected and is empty for every other status, so
lines written before this field existed still load (the missing field is read as empty).

The last three fields are the reporter's **full name**, **address** and **contact number** as they
were when the report was filed. They are copied from the account when the report is submitted and
are never changed afterwards, so a report still shows who filed it even after that account is
deleted. Lines written before these fields existed still load: their details are read from the
account instead, and only an account that no longer exists leaves them empty (shown as `-`).

**Status workflow.** A new report starts as `PENDING`. The **Update Status** button in the Reports tab
opens a window that lists only the statuses the workflow allows next, so a report cannot skip a step:

| From | Allowed next status |
|---|---|
| PENDING | VERIFIED or REJECTED |
| VERIFIED | IN PROGRESS or REJECTED |
| IN PROGRESS | RESOLVED or REJECTED |
| RESOLVED | none, the report is final |
| REJECTED | none, but an administrator can reopen it back to PENDING |

An Area Officer may move reports inside its own area, and an Administrator may move any report. Only
the Administrator can reopen a rejected report, and reopening clears the rejection reason. Rejecting
always needs a reason (5-200 characters), which the resident then sees on the report.

The rules are not in the windows: `model/ReportStatus` holds `isFinal()`, `canMoveTo()` and
`nextStatuses()`, and `manager/ReportManager` holds `statusProblem()` (the message to show),
`changeStatus()`/`changeStatusInCity()` (validate, apply, save) and `reopenReport()`. Every status
change, from the GUI or from anything else, goes through those methods before `reports.txt` is written.

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
| Status change | It must follow the workflow table above: no skipping, and a report that is RESOLVED or REJECTED cannot be changed through a normal update. |
| Rejection reason | 5-200 characters, one line, and the `\|` character is refused. The reason is cleared if the report is reopened or moved on. |
| Administrator actions | An administrator cannot delete the account they are signed in with or the last remaining administrator, and deleting a resident who filed reports asks for confirmation first; the reports keep the reporter details that were saved with them. |
| Any text | The `\|` character and line breaks are refused, because the data files use `\|` as the separator between fields. |

Rules are only applied to new input. Accounts created before a rule existed keep working, and their
stored values are checked again the next time somebody edits them.

**Theme.** The look follows the Chocolate Hills poster: cream pages, white cards, forest-green text
and hill-green buttons. Every colour and font lives in `ui/UITheme.java`, so the whole application
changes from that one file.

| Colour | Value | Used for |
|---|---|---|
| Forest green | `#1D3F22` | window titles, section headings, table headers, tab labels |
| Hill green | `#557D2C` | primary buttons (Log In, Save, Submit, Create, Update Status) |
| Hill lime | `#9CBF4A` | highlights and accents |
| Sky teal | `#2E9BA0` | the VERIFIED status |
| Palm red | `#C1502B` | Delete buttons |
| Sage | `#C6D2B8` | borders and lines |
| Cream | `#F3F5EA` | page background behind the cards |
| White | `#FFFFFF` | cards, fields and the open tab |

The dashboards use a **folder tab** strip drawn by `ui/FolderTabbedPaneUI.java`: a 40 pixel tall
trapezoid tab with slanted top corners, Segoe UI bold 14, the open tab white with no bottom line so it
joins the page, and the other tabs on the `#E7EDD9` tint. Tab height, width, slant and colours are the
four constants at the top of that file.
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

