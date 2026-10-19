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
│       ├── ReportUpdateDialog.java Modal Update Report form
│       └── UITheme.java            Shared colours, fonts, and reusable widgets
├── data/                       Plain text storage
│   ├── users.txt               One user per line
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

`users.txt` line format:

```
type|userId|username|password|fullName[|address|contactNumber]
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

