 # Interview Simulator System (Java Swing + MySQL)

## What it does

Desktop placement-prep application with:

* User registration and login (SHA-256 hashed passwords stored in MySQL)
* Dashboard with topic selection (Java, DSA, DBMS, OS)
* 30-second timer for each MCQ
* Result screen with score percentage, correct/wrong breakdown, and weak-topic suggestions
* Previous results history
* Admin panel (for admin users) to add, update, and delete questions

## 1. Create the Database

1. Open MySQL Client (MySQL 8+ recommended)
2. Run `database_setup.sql`

This creates:

* Database: `interview_simulator`
* Tables: `users`, `questions`, `results`

## 2. Configure MySQL Connection

The application reads these environment variables:

* `DB_URL`
* `DB_USER`
* `DB_PASSWORD`

Example:

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/interview_simulator?useSSL=false&serverTimezone=UTC"
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

## 3. Add MySQL JDBC Driver

MySQL Connector/J is configured in:

```text
lib/mysql-connector-j-9.7.0.jar
```

## 4. Compile and Run

### Compile

```powershell
cmd /c "if not exist out mkdir out & dir /b /s src\*.java > sources.txt & javac -encoding UTF-8 -d out @sources.txt & del sources.txt"
```

### Run

```powershell
java -cp "out;lib/*" Main
```

## Features

* Java Swing GUI
* MySQL Database Integration
* Secure Password Storage
* Topic-wise Quiz System
* Result Analytics
* Admin Question Management
* Placement Preparation Support

## Technologies Used

* Java
* Java Swing
* JDBC
* MySQL
* OOP Concepts
* SHA-256 Password Hashing

## Author

Vaishnavi Burugu
