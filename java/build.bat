@echo off
REM Build script for Bulls and Cows JAR
REM Run from java\ directory

setlocal enabledelayedexpansion

cd /d "%~dp0src" || exit /b 1

echo.
echo Building Bulls and Cows JAR...
echo.

REM Clean previous build if requested
if "%1"=="clean" (
    echo Cleaning previous build...
    if exist out (
        rmdir /s /q out
    )
)

REM Create output directory
if not exist out (
    mkdir out
)

REM Compile Java files
echo Compiling Java files...
javac -encoding UTF-8 -d out ^
    start\BoolsAndCows.java ^
    exception\ContradictoryAnswersException.java ^
    logic\GameNumber.java ^
    logic\ComputerPlayer.java ^
    view\IntFilter.java ^
    view\AnswerVerifier.java ^
    view\GameNumberVerifier.java ^
    view\MoveTableModel.java ^
    view\GameTable.java ^
    view\CompGuessPanel.java ^
    view\HumanGuessPanel.java ^
    view\BoolsAndCowsFrame.java ^
    view\HelpFrame.java ^
    view\AboutFrame.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b 1
)

echo Compilation successful!
echo.

REM Create JAR file
echo Creating JAR file...
jar cfe ..\BullsAndCows.jar start.BoolsAndCows -C out .

if %errorlevel% neq 0 (
    echo JAR creation failed!
    exit /b 1
)

echo.
echo JAR created successfully: ..\BullsAndCows.jar
echo Run with: java -jar BullsAndCows.jar
echo.
