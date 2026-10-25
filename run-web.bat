@echo off
title WearNet - Digital Wardrobe Platform Web Server
echo ========================================================
echo   👗 WearNet - Digital Wardrobe & Peer Sharing Platform
echo ========================================================
echo Starting Embedded Java Web Application...
echo.

set JAVA_CMD="C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
if not exist %JAVA_CMD% (
    set JAVA_CMD=java
)

%JAVA_CMD% -cp "target\classes;C:\Users\aryar\.m2\repository\org\xerial\sqlite-jdbc\3.46.1.1\sqlite-jdbc-3.46.1.1.jar" com.mycompany.wardrobeapp.Wardrobeapp

pause
