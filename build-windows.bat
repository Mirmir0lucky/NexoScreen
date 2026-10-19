@echo off
setlocal
java -version
gradle -version
if errorlevel 1 exit /b 1
gradle clean build
pause
