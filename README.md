# Interview Simulator System (Java Swing + MySQL)

## What it does
Desktop placement-prep app with:
* User registration + login (SHA-256 hashed passwords stored in MySQL)
* Dashboard with topic selection (Java, DSA, DBMS, OS)
* 30-second timer per MCQ question
* Result screen with score %, correct/wrong breakdown, and weak-topic suggestion
* Previous results history
* Optional Admin screen (for `admin` username) to add/update/delete questions

## 1) Create the database
1. Open MySQL client (MySQL 8+ recommended)
2. Run `database_setup.sql`

This will create:
* Database: `interview_simulator`
* Tables: `users`, `questions`, `results`
* Seed users:
  * `admin` / `password123`
  * `user1` / `password123`

## 2) Configure MySQL connection
The app reads these environment variables (fallbacks are for local dev only):
* `DB_URL` (default: `jdbc:mysql://localhost:3306/interview_simulator?useSSL=false&serverTimezone=UTC`)
* `DB_USER` (default: `root`)
* `DB_PASSWORD` (default: `Vaishnavi@18`)

Example (Windows PowerShell):
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/interview_simulator?useSSL=false&serverTimezone=UTC"
$env:DB_USER="root"
$env:DB_PASSWORD="Vaishnavi@18"
```

## 3) Add MySQL JDBC driver
Connector/J is now configured under:
* `lib/mysql-connector-j-9.7.0.jar`

## 4) Compile and run
From the project root (`InterviewSimulator/`):

### Compile
```powershell
cmd /c "if not exist out mkdir out & dir /b /s src\\*.java > sources.txt & javac -encoding UTF-8 -d out @sources.txt & del sources.txt"
```

### Run
```powershell
java -cp "out;lib/*" Main
```

## Notes
* Correct answers in `questions.correct_answer` must exactly match one of `option1..option4`.
* The quiz uses **all** questions for the selected topic.

