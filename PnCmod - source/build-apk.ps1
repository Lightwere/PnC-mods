# Quick build and show APK location
$env:Path += ";$env:LOCALAPPDATA\Android\Sdk\platform-tools"
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"

Write-Host "Building PnCmod..." -ForegroundColor Cyan
.\gradlew.bat assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "=== Build Successful ===" -ForegroundColor Green
    $apk = Get-Item "app\build\outputs\apk\debug\PnCmod-debug.apk"
    Write-Host "APK Location: $($apk.FullName)" -ForegroundColor Cyan
    Write-Host "APK Size: $([math]::Round($apk.Length / 1MB, 2)) MB" -ForegroundColor White
    Write-Host ""
    Write-Host "To install via ADB:" -ForegroundColor Yellow
    Write-Host "  adb install -r `"$($apk.FullName)`"" -ForegroundColor Gray
}
