# PnC-mods

A dual-purpose Android utility app for "Puzzle & Conquest" game modifications.

## Features

### 📁 File Swapper
Replace game asset files with bundled versions while keeping backups for easy restoration.
- Uses Storage Access Framework (SAF) for safe file operations
- Automatic backup creation
- One-click restore functionality

### 🖱️ Auto-Clicker
Automated clicking tool with customizable presets and draggable floating window.
- **Preset Management**: Add, edit, and delete click positions in-app
- **Floating GUI**: Draggable overlay window that doesn't block other apps
- **Adjustable Speed**: Default 100ms (10 clicks/second), customizable down to 50ms
- **Collapsible Interface**: Minimize to stay out of the way
- **Persistent Storage**: Presets saved to `/sdcard/PnCmod/click_presets.json` for easy manual editing

#### Default Presets:
- Helps - 1st slot (920, 410)
- Helps - 2nd slot (920, 620)
- Helps - 3rd slot (920, 830)
- Heal (910, 2145)
- Train+speedup (700, 2215)

## Requirements
- **Android 7.0+** (API 24+)
- **Accessibility Service** permission (for auto-clicker)
- **Overlay Permission** (for floating window)
- **Storage Permissions** (for file operations)

## Installation
1. Download the latest `PnCmod-debug.apk` from releases
2. Install on your device
3. Grant required permissions when prompted
4. Enable accessibility service for auto-clicker

## Build from Source
```bash
cd "PnCmod - source"
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
.\gradlew.bat assembleDebug
```

**Requirements:**
- JDK 17 (Eclipse Adoptium recommended)
- Android SDK with Build Tools 34.0.0

## Usage

### File Swapper
1. Tap "Pick File (SAF)" to select the target game file
2. Tap "Replace File" to swap with the bundled version
3. Tap "Restore Original" to revert changes

### Auto-Clicker
1. Tap "Launch Auto-Clicker" from main menu
2. Enable accessibility service if prompted
3. Floating window appears with preset selector
4. Select a preset and tap "Start" to begin clicking
5. Tap "+" to expand preset editing options
6. Drag the window by touching and moving the header

## File Locations
- **Presets**: `/sdcard/PnCmod/click_presets.json`
- **Backups**: `/sdcard/PnCmod/original_backup.bin`

You can manually edit `click_presets.json` to add/modify presets.

## Technical Details
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: Services-based with floating overlay UI
- **Click Method**: Accessibility Service gesture dispatch API

## License
See [LICENSE](LICENSE) file for details.

## Contributing
Feel free to submit issues or pull requests for improvements!
