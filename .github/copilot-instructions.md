# Copilot Instructions for PnC-mods

## Overview
This project is a utility app designed to replace a specific file with a bundled version while keeping a local backup for restoration. It primarily interacts with external storage and requires specific permissions to function correctly.

## Architecture
- **Main Components**: The main component is `MainActivity`, which handles user interactions and file operations.
- **Service Boundaries**: The app interacts with the Android file system and requires permissions for reading and writing files.
- **Data Flow**: The app reads a target file, backs it up, and replaces it with a new asset file. It also allows restoring the original file from the backup.

## Developer Workflows
- **Building the Project**: Use the Gradle wrapper scripts (`gradlew` or `gradlew.bat`) to build the project. Run `./gradlew build` in the terminal.
- **Testing**: Ensure to test on devices with different Android versions to verify permission handling.
- **Debugging**: Use Android Studio's debugging tools to set breakpoints and inspect the flow, especially around permission requests and file operations.

## Project-Specific Conventions
- **Permissions Handling**: The app checks for permissions at runtime, especially for Android 6.0 (API level 23) and above. Ensure to handle `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` permissions properly.
- **File Operations**: Use `Uri` for file access, especially when dealing with content providers. Always check for null and handle exceptions gracefully.

## Integration Points
- **External Dependencies**: The app relies on AndroidX libraries for activity results and permissions. Ensure these are included in your `build.gradle.kts` file.
- **Cross-Component Communication**: Use `SharedPreferences` to store and retrieve the target URI across app sessions.

## Key Files/Directories
- **MainActivity.java**: The main entry point for the app, handling UI and file operations.
- **build.gradle.kts**: Configuration for project dependencies and build settings.
- **AndroidManifest.xml**: Ensure all required permissions are declared here.

## Examples
- **File Replacement**: The method `doReplace()` handles the logic for replacing the target file with a new asset. Ensure to back up the original file first.
- **Permission Request**: The `checkAndRequestPermissions()` method demonstrates how to request necessary permissions at runtime.

## Conclusion
This document serves as a guide for AI coding agents to understand the structure and workflows of the PnC-mods project. Follow the conventions and patterns outlined to ensure consistency and functionality in development.