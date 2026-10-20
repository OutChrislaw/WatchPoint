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
│   │   ├── Administrator.java      Staff who manages reports and users
│   │   ├── AreaOfficer.java        Staff for one city/municipality (AO ids)
│   │   ├── RegistrationRequest.java A resident sign-up waiting for approval
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
│       ├── ProfileDialog.java      Update Profile window of the signed-in user
│       ├── ReportUpdateDialog.java Modal Update Report form
│       └── UITheme.java            Shared colours, fonts, and reusable widgets
├── data/                       Plain text storage
│   ├── users.txt               One user or area officer per line
│   ├── registrations.txt       Resident sign-ups waiting for approval
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
| Resident | self-registers, then accepted by the Area Officer of that area | own reports | Submit a report, view reports, update or delete own PENDING reports, edit own details, username and password from the Settings button |
| Area Officer | an Administrator | only its own city/municipality | Change report status inside its area, accept or reject resident registrations, change own password from the Settings button |
| Administrator | an Administrator | the whole province | All reports, change status, delete invalid reports, edit any account in a form, change own password from the Settings button |

`users.txt` line format (one line per account type):

```
Resident|userId|password|fullName|username|address|contactNumber
Administrator|userId|password|fullName
AreaOfficer|userId|password|fullName|area
```

Only a **Resident** has a username, because residents sign in with it (their id works too).
Administrators and area officers have **no username** and sign in with their **id**
(`U…` and `AO…`). For example:

```
Administrator|U999|admin123|System Admin
Resident|U1003|maria123|Maria Santos|maria|Cogon, Tagbilaran City|09171234567
AreaOfficer|AO001|officer123|Tagbilaran City Officer|Tagbilaran City
```

`registrations.txt` line format (a resident sign-up that is still waiting):

```
fullName|username|password|contactNumber|barangay|municipality|dateRequested
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
| Account Details | Full name, user id, username (residents only), contact and address. A resident may change the username (3-40 characters; letters, numbers, `.` `_` `@` `+` `-`; it must not be taken by another account or already waiting for approval). |
| Change Password | Current password, new password (at least 6 characters) and confirmation. |
| Logout | Signs out and returns to the login window. |
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

