# PnCmod ADB WiFi Deployment Script
# Usage: .\deploy-wireless.ps1 [device-ip]

param(
    [string]$DeviceIP = ""
)

# Add ADB to PATH
$env:Path += ";$env:LOCALAPPDATA\Android\Sdk\platform-tools"

Write-Host "=== PnCmod Wireless Deployment ===" -ForegroundColor Cyan
Write-Host ""

# Check if device IP provided
if ($DeviceIP -eq "") {
    Write-Host "Checking for USB connected devices..." -ForegroundColor Yellow
    adb devices
    Write-Host ""
    Write-Host "To enable wireless ADB:" -ForegroundColor Green
    Write-Host "1. Connect device via USB"
    Write-Host "2. Run: adb tcpip 5555"
    Write-Host "3. Find device IP in WiFi settings"
    Write-Host "4. Run: .\deploy-wireless.ps1 <device-ip>"
    Write-Host ""
    Write-Host "Example: .\deploy-wireless.ps1 192.168.1.100" -ForegroundColor Cyan
    exit
}

Write-Host "Connecting to $DeviceIP..." -ForegroundColor Yellow
adb connect "${DeviceIP}:5555"

Write-Host ""
Write-Host "Building app..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
.\gradlew.bat assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Installing app..." -ForegroundColor Yellow
    adb -s "${DeviceIP}:5555" install -r "app\build\outputs\apk\debug\PnCmod-debug.apk"
    
    Write-Host ""
    Write-Host "=== Deployment Complete ===" -ForegroundColor Green
    Write-Host ""
    Write-Host "PnCmod files will be created at:" -ForegroundColor Cyan
    Write-Host "/sdcard/PnCmod/" -ForegroundColor White
    Write-Host "  - click_presets.json (edit this for custom click locations)" -ForegroundColor Gray
    Write-Host "  - 1b98f4343ed035646b53cccdb7bd3811.assetbundles (replacement file)" -ForegroundColor Gray
    Write-Host "  - original_backup.bin (backup of original file)" -ForegroundColor Gray
} else {
    Write-Host ""
    Write-Host "Build failed!" -ForegroundColor Red
}
