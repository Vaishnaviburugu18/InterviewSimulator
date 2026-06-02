@echo off
setlocal
if not exist out mkdir out
dir /b /s src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
  del sources.txt
  exit /b 1
)
del sources.txt
java -cp "out;lib/*" Main

