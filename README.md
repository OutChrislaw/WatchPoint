# WatchPoint: A Community Safety Reporting System

Desktop application (Java + Java Swing) for reporting and tracking community safety
hazards across the province of **Bohol**. Reports are stored in plain `.txt` files
(no real database).

## Project layout

```
WatchPoint/
├── src/                     Java source files (no packages)
│   ├── Main.java            Entry point, creates the managers and opens LoginFrame
│   ├── User.java            Abstract parent of Resident and Administrator
│   ├── Resident.java        A community member who files reports
│   ├── Administrator.java   City/barangay staff who manages reports and users
│   ├── Location.java        City/municipality, barangay, street, specific place
│   ├── Report.java          Abstract parent of the four report types
│   ├── ReportStatus.java    PENDING, VERIFIED, IN_PROGRESS, RESOLVED
│   ├── RoadHazardReport.java
│   ├── FloodReport.java
│   ├── StreetlightReport.java
│   ├── OtherHazardReport.java
│   ├── UserManager.java     Registration, login, and user records
│   ├── ReportManager.java   Submit, search, update, and delete reports
│   ├── FileManager.java     Reads and writes users.txt and reports.txt
│   ├── LoginFrame.java      Login window
│   ├── RegistrationFrame.java  New resident sign-up window
│   ├── ResidentDashboard.java  Window for residents
│   └── AdminDashboard.java     Window for administrators
├── data/                    Plain text storage
│   ├── users.txt            One user per line
│   └── reports.txt          One report per line
└── bin/                     Compiled .class files
```

`users.txt` line format:

```
type|userId|username|password|fullName[|address|contactNumber]
```

`reports.txt` line format:

```
type|reportId|reporterId|cityMunicipality|barangay|street|specificPlace|description|specificDetail|status|dateSubmitted
```

## Build and run

From the project root:

```
javac -d bin src\*.java
java -cp bin Main
```

Run the app from the project root so the `data` folder is found. The `data` folder
and both text files are created automatically when they are missing.

