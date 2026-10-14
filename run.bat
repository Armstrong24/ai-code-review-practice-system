@echo off
rem Build and run. Usage: run.bat web (website)  |  run.bat (desktop)  |  run.bat admin  |  run.bat test
cd /d "%~dp0"
if exist bin rmdir /s /q bin
mkdir bin
dir /s /b src\*.java > sources.txt
javac --release 17 -encoding UTF-8 -d bin -cp "lib\*" @sources.txt
if errorlevel 1 (del sources.txt & pause & exit /b 1)
del sources.txt
java -cp "bin;lib\*" com.bcorps.app.Main %*
