# Copilot Instructions for PnC-mods

## Overview
PnC-mods is a dual-purpose Android utility app featuring:
1. **File Swapper**: Replace game asset files with bundled versions while keeping backups
2. **Auto-Clicker**: Automated clicking tool with customizable presets, draggable floating window, and preset management

## Architecture

### Main Components
- **MainActivity.java**: Entry point with two-section UI (File Swapper & Auto-Clicker)
- **FloatingWindowService.java**: Manages draggable floating overlay for auto-clicker with preset editing
- **AutoClickerAccessibilityService.java**: Accessibility service that performs automated clicks using gesture dispatch API
- **activity_main.xml**: Card-based layout with separate sections for each feature

### Data Flow
- **File Operations**: Uses Storage Access Framework (SAF) for safe file access; stores target URI in SharedPreferences
- **Auto-Clicker**: Loads presets from `/sdcard/PnCmod/click_presets.json` (device storage) with fallback to embedded assets
- **Floating Window**: Maintains state across minimize/expand and dynamically toggles `FLAG_NOT_FOCUSABLE` for focus management

## Developer Workflows

### Building
```bash
cd "PnCmod - source"
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot"
.\gradlew.bat assembleDebug
```

### Wireless ADB Deployment
```bash
adb connect 192.168.0.136:5555  # Device IP
adb -s 192.168.0.136:5555 install -r app/build/outputs/apk/debug/PnCmod-debug.apk
```

### Testing
- Test permission flows on Android 6.0+ (runtime permissions)
- Verify accessibility service registration and gesture dispatch
- Test floating window focus handling on different Android versions
- Validate preset JSON parsing and file I/O

## Project-Specific Conventions

### Permissions
- `READ_EXTERNAL_STORAGE` & `WRITE_EXTERNAL_STORAGE`: For file operations (Android 6+)
- `MANAGE_EXTERNAL_STORAGE`: For broad file access (fallback for older APIs)
- `SYSTEM_ALERT_WINDOW`: For floating window overlay
- `BIND_ACCESSIBILITY_SERVICE`: For accessibility service (declared in manifest, user-enabled at runtime)

### File Organization
- **Assets**: `app/src/main/assets/click_presets.json` (embedded defaults)
- **External Storage**: `/sdcard/PnCmod/click_presets.json` (user editable, persists across app updates)
- **Backup**: `/sdcard/PnCmod/original_backup.bin` (file swapper backup)

### Focus Management (Critical for Floating Window)
- Window starts with `FLAG_NOT_FOCUSABLE` to allow touches through
- When user taps EditText field: Remove `FLAG_NOT_FOCUSABLE`, show keyboard
- When user finishes editing or taps button: Restore `FLAG_NOT_FOCUSABLE`, hide keyboard
- Implementation: `setupEditTextFocusListeners()` handles focus state changes

### UI State Management
- **Minimize/Expand**: Toggles visibility of content container and edit toggle section
- **Edit Section**: Collapsible with `-` / `+` button; auto-collapses when button pressed
- **Dragging**: Implemented via `OnTouchListener` tracking ACTION_DOWN and ACTION_MOVE

## Key Files/Directories

### Source Code
- `MainActivity.java`: Two-section UI, file picker, permissions, service launching
- `FloatingWindowService.java`: Floating overlay, preset CRUD, delay control, focus management
- `AutoClickerAccessibilityService.java`: Click gesture dispatch using API 24+ GestureDescription
- `activity_main.xml`: CardView-based layout with File Swapper and Auto-Clicker sections
- `floating_window.xml`: Collapsible edit section with EditText fields
- `click_presets.json`: Default presets with coordinates and click delay (100ms default)

### Build & Config
- `build.gradle.kts`: Dependencies (AndroidX, CardView, Material), APK naming customization
- `AndroidManifest.xml`: Service registration, permissions, accessibility service config
- `accessibility_service_config.xml`: Accessibility service capabilities

### Resources
- `mipmap-*`: Custom wrench icon (192px original, resized to multiple densities)
- `drawable/`: Launcher icon drawables and backgrounds

## Examples

### Adding a New Preset
1. Open floating window auto-clicker
2. Toggle edit section with `+`
3. Fill Name, X, Y coordinates
4. Tap "Add or Update Preset"
5. Preset saved to `/sdcard/PnCmod/click_presets.json`

### Implementing a New Feature
- Register in AndroidManifest if it's a Service/Receiver
- Add UI components to activity_main.xml if needed
- Follow existing async patterns for file I/O (use handlers, avoid blocking main thread)
- Test permission flows on target device

### Window Focus Workflow
```
User touches EditText 
  → OnFocusChangeListener fires hasFocus=true
  → Remove FLAG_NOT_FOCUSABLE
  → Keyboard appears
  → User taps button or EditText loses focus
  → OnFocusChangeListener fires hasFocus=false
  → Restore FLAG_NOT_FOCUSABLE
  → Window becomes transparent to touches again
```

## Important Notes
- **External Storage Path**: All user files go to `/sdcard/PnCmod/` for easy manual editing
- **Default Click Speed**: 100ms (10 clicks/second)
- **Min SDK**: 24, Target SDK: 34
- **APK Output**: `PnCmod-{variant}.apk` (e.g., PnCmod-debug.apk)
- **Icon**: Custom wrench PNG in all density folders

## Troubleshooting
- **Icon not updating**: Delete `mipmap-anydpi-v26` folder if it causes adaptive icon issues
- **Presets not loading**: Check `/sdcard/PnCmod/` for stale files; app will copy from assets if missing
- **Floating window unresponsive**: Verify `FLAG_NOT_FOCUSABLE` is being restored after editing
- **Build failures**: Ensure `JAVA_HOME` points to Eclipse Adoptium JDK 17