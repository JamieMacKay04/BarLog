# BarLog
Bar logging system for efficient cellar management.

## Build and package (Windows)

Build prerequisites (only needed on the development computer):

- JDK 17+ with `jpackage`
- PowerShell 5+

Build, run, and create a self-contained portable download:

```powershell
.\build.ps1 -Run
.\build.ps1 -Package
```

Give `dist/BarLog-Windows-x64.zip` to the user. On any Windows x64 computer they can extract it and double-click `BarLog.exe`; Java and JavaFX do not need to be installed.

If Windows marks the downloaded ZIP as blocked, right-click the ZIP, choose **Properties**, enable **Unblock**, and then extract it again. Prefer the installer below for ordinary distribution.

To create a traditional installer (downloads a portable copy of WiX Toolset on the first build):

```powershell
.\build.ps1 -Installer
```

Installer output: `dist/BarLog-1.2.exe`. This is the recommended file to distribute because it installs the runtime and creates application shortcuts. Increase `-AppVersion` for each release so Windows Installer can upgrade an older copy, for example `./build.ps1 -Installer -AppVersion 1.3`.

The packaging build creates a custom Java runtime containing `java.net.http`, the JavaFX modules, and the JavaFX native Windows DLLs. All three are required; omitting the native DLLs causes the JavaFX renderer to fail before the application window opens.

Application data is stored per user in `%LOCALAPPDATA%\BarLog\data`, so updates or moving the portable app do not erase it.

## Notes
- If you already have a JavaFX SDK, set `BARLOG_FX_LIB` to its `lib` folder to skip the download.
- Set `BARLOG_JDK` to a JDK folder if the build script cannot locate one automatically.
