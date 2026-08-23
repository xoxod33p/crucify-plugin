@echo off
title Building Crucify Plugin
cd /d "%~dp0"

echo ========================================================
echo   Building CrucifyPlugin JAR...
echo ========================================================
echo.

call mvn clean package -DskipTests

if %ERRORLEVEL% equ 0 (
    echo.
    echo ========================================================
    echo   BUILD SUCCESS!
    echo   Jar location: "%~dp0target\crucify-plugin.jar"
    echo ========================================================
) else (
    echo.
    echo ========================================================
    echo   BUILD FAILED! Please check the errors above.
    echo ========================================================
)

echo.
pause

