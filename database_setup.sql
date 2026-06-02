-- Interview Simulator System - MySQL Schema + Sample Data
-- Run this script in your MySQL client (ensure MySQL 8+ recommended).

DROP DATABASE IF EXISTS interview_simulator;
CREATE DATABASE interview_simulator CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE interview_simulator;

-- =========================
-- Tables
-- =========================

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL
);

CREATE TABLE questions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  topic VARCHAR(50) NOT NULL,
  question TEXT NOT NULL,
  option1 VARCHAR(100) NOT NULL,
  option2 VARCHAR(100) NOT NULL,
  option3 VARCHAR(100) NOT NULL,
  option4 VARCHAR(100) NOT NULL,
  correct_answer VARCHAR(100) NOT NULL,
  INDEX(topic)
);

CREATE TABLE results (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  topic VARCHAR(50) NOT NULL,
  score INT NOT NULL,
  test_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- Seed Users
-- Password hashing in Java uses SHA-256 hex.
-- SHA-256("password123") = ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
-- =========================

INSERT INTO users (username, email, password) VALUES
('admin', 'admin@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f'),
('user1', 'user1@example.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f');

-- =========================
-- Seed Questions
-- NOTE: correct_answer must match one of option1..option4 exactly.
-- =========================

-- Java
INSERT INTO questions (topic, question, option1, option2, option3, option4, correct_answer) VALUES
('Java', 'A key OOP feature where a class can inherit properties from another class is called:', 'Method overloading', 'Inheritance', 'Polymorphism', 'Encapsulation', 'Inheritance'),
('Java', 'Which keyword is used to inherit from a class in Java?', 'extends', 'implements', 'instanceof', 'this', 'extends'),
('Java', 'Polymorphism means:', 'One method many forms', 'Multiple constructors with same name', 'Variables declared once', 'Class cannot be inherited', 'One method many forms'),
('Java', 'Which collection is synchronized by default in Java?', 'ArrayList', 'Vector', 'HashMap', 'LinkedList', 'Vector'),
('Java', 'JVM stands for:', 'Java Virtual Machine', 'Java Virtual Memory', 'Java Virtual Mode', 'Java Virtual Method', 'Java Virtual Machine');

-- DSA
INSERT INTO questions (topic, question, option1, option2, option3, option4, correct_answer) VALUES
('DSA', 'Time complexity of binary search is:', 'O(n)', 'O(log n)', 'O(n log n)', 'O(1)', 'O(log n)'),
('DSA', 'Which data structure follows FIFO order?', 'Stack', 'Queue', 'Priority Queue', 'Tree', 'Queue'),
('DSA', 'Insertion at the beginning of an array requires shifting elements. Time complexity is:', 'O(1)', 'O(log n)', 'O(n)', 'O(n^2)', 'O(n)'),
('DSA', 'Worst-case time complexity of bubble sort is:', 'O(n)', 'O(n log n)', 'O(n^2)', 'O(log n)', 'O(n^2)'),
('DSA', 'Preorder traversal visits nodes in the order:', 'Left - Node - Right (Inorder)', 'Node - Right - Left', 'Node - Left - Right (Preorder)', 'Left - Right - Node (Postorder)', 'Node - Left - Right (Preorder)');

-- DBMS
INSERT INTO questions (topic, question, option1, option2, option3, option4, correct_answer) VALUES
('DBMS', 'ACID stands for:', 'Atomicity, Consistency, Isolation, Durability', 'Availability, Consistency, Integrity, Durability', 'Atomicity, Control, Isolation, Data', 'Consistency, Isolation, Independence, Durability', 'Atomicity, Consistency, Isolation, Durability'),
('DBMS', 'SQL command used to delete rows is:', 'SELECT', 'UPDATE', 'DELETE', 'REMOVE', 'DELETE'),
('DBMS', 'Which normal form removes partial dependency?', '1NF', '2NF', '3NF', 'BCNF', '2NF'),
('DBMS', 'Primary key in a table:', 'Can contain NULL values freely', 'Uniquely identifies each row', 'Can have duplicates', 'Is optional for indexing', 'Uniquely identifies each row'),
('DBMS', 'Join type that returns all matching rows from both tables and also returns unmatched rows:', 'INNER JOIN', 'LEFT JOIN', 'RIGHT JOIN', 'FULL OUTER JOIN', 'FULL OUTER JOIN');

-- OS
INSERT INTO questions (topic, question, option1, option2, option3, option4, correct_answer) VALUES
('OS', 'CPU scheduling algorithm that selects the process with the shortest next CPU burst is:', 'FCFS', 'SJF', 'Round Robin', 'Priority Scheduling', 'SJF'),
('OS', 'In Round Robin scheduling, the concept that controls how long each process runs is called:', 'Starvation', 'Time quantum', 'Context switching', 'Interrupt vector', 'Time quantum'),
('OS', 'Deadlock occurs when there is:', 'Mutual exclusion, hold and wait, no preemption, circular wait', 'Only mutual exclusion', 'Only circular wait', 'Only hold and wait', 'Mutual exclusion, hold and wait, no preemption, circular wait'),
('OS', 'Paging is mainly used for:', 'Virtual memory management', 'CPU arithmetic', 'Disk defragmentation', 'Process creation', 'Virtual memory management'),
('OS', 'Which process state means the process is currently executing on the CPU?', 'Ready', 'Running', 'Waiting', 'Terminated', 'Running');

