# BarLog
Bar logging system for efficient cellar management.

## Quick start (Windows)
Prereqs:
- JDK 17+ (includes `jpackage`)
- PowerShell 5+

Build, run, and package (downloads JavaFX + Jackson on first run):
```powershell
.\build.ps1 -Run
.\build.ps1 -Package
```

Installer output:
- `dist/BarLog-1.0.exe`

## Notes
- If you already have a JavaFX SDK, set `BARLOG_FX_LIB` to its `lib` folder to skip the download.
