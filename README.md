# WatchPoint

A community safety reporting system for **Bohol, Philippines**. A Java Swing desktop app where
residents report hazards and staff track them until they are resolved.

Reports are stored in plain text files, so there is no database to set up.

## Features

- Three roles: **Resident**, **Area Officer** (one city / municipality) and **Administrator** (whole province).
- Report types: road hazard, flood, streetlight and other.
- Status flow: `PENDING` → `VERIFIED` → `IN PROGRESS` → `RESOLVED`, or `REJECTED` with a reason.
- A resident can edit or delete a report only while it is still pending.
- Search, filter, sort, per-barangay and per-hazard counts, and the oldest pending report.
- Every field is validated before it is saved.

## Requirements

- JDK 8 or newer. No libraries, no build tool.

## Run

From the project root in PowerShell:

```
javac -d bin (Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp bin app.WatchPoint
```

In VS Code: open the **WatchPoint** folder and press `Ctrl`+`Alt`+`N` (Code Runner).

The `data` folder and its four files are created automatically when they are missing.

## Demo accounts

| Role | Log in with | Password |
| --- | --- | --- |
| Administrator | `AD000001` | `admin123` |
| Area Officer (Tagbilaran City) | `AO000001` | `officer123` |
| Resident | `maria` or `U000003` | `maria123` |

Residents may log in with a username or an id; administrators and area officers use their id.
All seeded accounts are listed in `data/users.txt`.

## Project structure

```
src/
  app/         entry point (app.WatchPoint)
  model/       User, Resident, Administrator, AreaOfficer, Location, ReportStatus, Report + 4 report types
  manager/     UserManager, ReportManager, LocationManager
  storage/     FileManager - the only class that reads and writes files
  validation/  TextRules, UserRules, ReportRules, LocationRules
  ui/          Swing windows
data/          users.txt, reports.txt, locations.txt, ids.txt
bin/           compiled classes
```

## Data files

One record per line, fields separated by `|`:

- `users.txt` - one account per line
- `reports.txt` - one report per line
- `locations.txt` - one city / municipality per line, then its barangays
- `ids.txt` - the last id number issued for each prefix
