# CodeVault IDE

CodeVault IDE is an Android-based lightweight Integrated Development
Environment (IDE) designed for writing, executing, previewing, and
managing source code directly from mobile devices. The application
provides a code editor, syntax highlighting, online compilation support,
version control, snapshots, backups, markdown preview, and file
management features.

## Features

### 1. Code Editor

-   Mobile-friendly source code editor.
-   Syntax highlighting support for:
    -   C
    -   C++
    -   Kotlin
    -   Python
-   Custom syntax rules loaded from JSON assets.
-   Snapshot support for saving editor states.

### 2. Online Compiler

CodeVault IDE uses an online compiler architecture.

-   Compiler entry point:
    -   `CompilerManager`
-   Compiler communication:
    -   Online Judge0-compatible API
-   Default compiler endpoint:
    -   `https://ce.judge0.com`
-   Supported language IDs are dynamically resolved from the compiler
    server.

Supported workflow: 1. User writes source code. 2. Source code is sent
to compiler API. 3. Compilation and runtime output are returned. 4.
Result is displayed inside the application terminal.

## 3. Version Control System

The application includes a local version control mechanism similar to
Git.

Capabilities: - Automatic version creation. - Manual save versions. -
Auto-save snapshots. - Parent-child version relationships. - Branch
creation. - Branch checkout. - Version rollback. - Difference comparison
between versions. - Global history view.

Technology: - Android Room Database - Delta-based version comparison

## 4. File Management

The application provides: - File creation. - File storage management. -
Recent files panel. - File history tracking. - Persistent local storage.

## 5. Backup and Recovery

Features: - Automatic backup manager. - Snapshot recovery. - Restore
previous versions without deleting history.

## 6. Markdown and Preview Support

The application supports: - Markdown rendering. - HTML preview. - Rich
content display.

Libraries: - Multiplatform Markdown Renderer - Coil image loading
support

------------------------------------------------------------------------

# Project Architecture

The application follows a modular Android architecture.

    CodeVaultIDE/
    │
    ├── app/
    │   ├── src/
    │   │   ├── main/
    │   │   │   ├── java/
    │   │   │   ├── res/
    │   │   │   ├── assets/
    │   │   │   └── AndroidManifest.xml
    │   │
    │   └── build.gradle.kts
    │
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── README.md

------------------------------------------------------------------------

# Source Code Structure

## Main Application

    com.example.codevaultide
    │
    ├── MainActivity.kt

Responsible for: - Application startup. - Navigation initialization. -
Main UI hosting.

------------------------------------------------------------------------

# UI Layer

Location:

    ui/

## Navigation

    ui/navigation/
    └── Navigation.kt

Handles: - Screen routing. - Navigation graph.

## Screens

    ui/screens/
    │
    ├── HomeScreen.kt
    ├── EditorScreen.kt
    ├── CompilerScreen.kt
    ├── FilesScreen.kt
    ├── HistoryScreen.kt
    ├── DiffScreen.kt
    ├── SettingsScreen.kt
    └── VersionHistoryScreen.kt

Responsibilities:

### HomeScreen

Application dashboard.

### EditorScreen

Code editing interface.

### CompilerScreen

Compilation and output display.

### FilesScreen

File browsing and management.

### HistoryScreen

Global version history.

### DiffScreen

Version comparison.

### SettingsScreen

Application configuration.

### VersionHistoryScreen

Detailed file version tracking.

------------------------------------------------------------------------

# Editor Module

Location:

    editor/

Files:

    CodeEditor.kt
    EditorViewModel.kt
    FileViewModel.kt
    Snapshot.kt
    SnapshotManager.kt
    SyntaxHighlighter.kt
    SyntaxRules.kt
    MarkdownPreview.kt
    HtmlPreview.kt

Responsibilities:

-   Code editing engine.
-   Syntax highlighting.
-   Editor state management.
-   Snapshot creation.
-   Preview rendering.

------------------------------------------------------------------------

# Compiler Module

Location:

    compiler/

File:

    CompilerManager.kt

Responsibilities:

-   API communication.
-   Language detection.
-   Source code submission.
-   Compilation response handling.

------------------------------------------------------------------------

# Version Control Module

Location:

    versioncontrol/

Files:

    VersionRepository.kt
    VersionHistoryViewModel.kt
    DiffManager.kt
    DeltaManager.kt
    RollbackManager.kt

Responsibilities:

## VersionRepository

Database access layer for versions.

## DiffManager

Calculates changes between versions.

## DeltaManager

Stores optimized changes.

## RollbackManager

Restores previous states.

------------------------------------------------------------------------

# Database Layer

Location:

    database/

Files:

    AppDatabase.kt
    FileEntity.kt
    FileDao.kt
    VersionEntity.kt
    VersionDao.kt
    VersionWithFile.kt

Technology: - Room Database

Stores: - Files. - Versions. - Version relationships.

------------------------------------------------------------------------

# Storage Layer

Location:

    storage/

File:

    FileManager.kt

Responsibilities: - Local file operations. - File persistence. - File
retrieval.

------------------------------------------------------------------------

# Recovery Module

Location:

    recovery/

File:

    AutoBackupManager.kt

Handles: - Automatic backups. - Recovery operations.

------------------------------------------------------------------------

# Utility Module

Location:

    util/

File:

    CodeTemplates.kt

Contains: - Default code templates. - Starter snippets.

------------------------------------------------------------------------

# Assets

Location:

    assets/
    └── syntax/
        ├── c.json
        ├── cpp.json
        ├── kotlin.json
        └── python.json

Contains syntax highlighting definitions.

------------------------------------------------------------------------

# Android Resources

    res/
    │
    ├── layout/
    ├── drawable/
    ├── mipmap/
    ├── values/
    └── xml/

Includes:

## Layouts

-   Main screen.
-   Markdown preview.
-   Version history.
-   Snapshot manager.
-   Theme settings.

## Values

-   Colors.
-   Themes.
-   Strings.

## XML

-   Backup configuration.
-   Data extraction rules.

------------------------------------------------------------------------

# Technology Stack

  Component         Technology
  ----------------- ---------------------------------
  Platform          Android
  Language          Kotlin
  UI                Jetpack Compose + Material 3
  Database          Room
  Networking        OkHttp
  Compiler          Judge0-compatible API
  Version Control   Custom Delta Versioning
  Markdown          Multiplatform Markdown Renderer
  Image Loading     Coil 3
  Build System      Gradle Kotlin DSL

------------------------------------------------------------------------

# Minimum Requirements

-   Android SDK: 26+
-   Compile SDK: 36
-   Java Version: 17
-   Kotlin JVM Toolchain: 17
-   Internet connection required for online compilation.

------------------------------------------------------------------------

# Build Instructions

1.  Open the project in Android Studio.

2.  Sync Gradle dependencies.

3.  Select an Android device/emulator.

4.  Build the application:

```{=html}
<!-- -->
```
    Build > Make Project

5.  Run:

```{=html}
<!-- -->
```
    Run > Run 'app'

------------------------------------------------------------------------

# Testing Workflow

1.  Create a source file.
2.  Write code using the editor.
3.  Save the file.
4.  Check version history.
5.  Press Run.
6.  View compiler output.
7.  Create snapshots and restore previous versions.

------------------------------------------------------------------------

# Application Flow

    User
     |
     v
    Code Editor
     |
     v
    Save File
     |
     +----------------+
     |                |
     v                v
    Room Database     Compiler API
     |                |
     v                v
    Version History   Execution Result
     |
     v
    Snapshots / Rollback

------------------------------------------------------------------------

# Security Notes

-   The application does not execute code locally.
-   User programs are sent to the configured online compiler service.
-   Internet permission is required only for compiler API communication.

------------------------------------------------------------------------

# Future Improvements

Possible enhancements:

-   GitHub integration.
-   Offline compiler support.
-   More programming languages.
-   Cloud synchronization.
-   User authentication.
-   Collaborative editing.
