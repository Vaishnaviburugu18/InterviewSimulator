 # Interview Simulator System (Java Swing + SQLite)

## What it does

Desktop placement-prep application with:

* User registration and login (SHA-256 hashed passwords stored in SQLite)
* Dashboard with topic selection (30 domains including Java, DSA, DBMS, OS, React, SQL, and DevOps)
* 30-second timer for each MCQ
* Result screen with score percentage, correct/wrong breakdown, and weak-topic suggestions
* Previous results history
* Admin panel (for admin users) to add, update, and delete questions
* Gamified experience with XP levels, streaks, and milestone achievements
* Resume keyword scanning for topic recommendations

## 1. Prerequisites

No database server installation (like MySQL, Workbench, or XAMPP) is needed! The project runs completely out-of-the-box using a local self-contained SQLite database.

* Java Development Kit (JDK 17 or higher recommended)

## 2. Compile and Run

The easiest way to run the application is by double-clicking the `run.bat` file in the root directory.

Alternatively, you can compile and run using terminal commands:

### Compile

```powershell
if (!(Test-Path out)) { New-Item -ItemType Directory -Path out }
Get-ChildItem -Recurse -Filter "*.java" src | Select-Object -ExpandProperty FullName | Out-File -Encoding UTF8 sources.txt
javac -encoding UTF-8 -cp "lib/*" -d out @sources.txt
Remove-Item sources.txt -ErrorAction SilentlyContinue
```

### Run

```powershell
java -cp "out;lib/*" Main
```

## How It Works

* **Auto-creation:** The application automatically creates `InterviewSimulator.db` in the root folder on the first run.
* **Auto-initialization:** All database tables and columns are automatically created by `DatabaseInitializer` if they don't exist.
* **Auto-seeding:** 900 high-quality MCQ questions across 30 domains are seeded automatically on first launch by `DatabaseSeeder`.

## Technologies Used

* Java
* Java Swing
* JDBC (SQLite)
* SQLite (org.xerial:sqlite-jdbc:3.36.0.3)
* SHA-256 Password Hashing

## Author

Vaishnavi Burugu

