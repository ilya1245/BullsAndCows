# Build script for Bulls and Cows JAR
# Run from java/ directory

param(
    [switch]$clean = $false
)

$srcDir = Join-Path (Get-Location) "src"
Set-Location $srcDir
$outDir = Join-Path $srcDir "out"

Write-Host "Building Bulls and Cows JAR..." -ForegroundColor Green

# Clean previous build if requested
if ($clean -and (Test-Path $outDir)) {
    Write-Host "Cleaning previous build..."
    Remove-Item $outDir -Recurse -Force
}

# Create output directory if it doesn't exist
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

# Compile Java files
Write-Host "Compiling Java files..." -ForegroundColor Yellow
$files = @(
    "start\BoolsAndCows.java",
    "exception\ContradictoryAnswersException.java",
    "logic\GameNumber.java",
    "logic\ComputerPlayer.java",
    "view\IntFilter.java",
    "view\AnswerVerifier.java",
    "view\GameNumberVerifier.java",
    "view\MoveTableModel.java",
    "view\GameTable.java",
    "view\CompGuessPanel.java",
    "view\HumanGuessPanel.java",
    "view\BoolsAndCowsFrame.java",
    "view\HelpFrame.java",
    "view\AboutFrame.java"
)

javac -encoding UTF-8 -d $outDir $files

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Compilation successful!" -ForegroundColor Green

# Create JAR file
Write-Host "Creating JAR file..." -ForegroundColor Yellow
$jarDir = Split-Path $srcDir -Parent
$jarPath = Join-Path $jarDir "BullsAndCows.jar"
jar cfe $jarPath start.BoolsAndCows -C $outDir .

if ($LASTEXITCODE -eq 0) {
    Write-Host "JAR created successfully: $jarPath" -ForegroundColor Green
    Write-Host "Run with: java -jar BullsAndCows.jar" -ForegroundColor Cyan
} else {
    Write-Host "JAR creation failed!" -ForegroundColor Red
    exit 1
}
