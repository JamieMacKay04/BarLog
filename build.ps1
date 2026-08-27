param(
  [switch]$Run,
  [switch]$Package,
  [switch]$Installer,
  [string]$AppVersion = "1.2"
)

$ErrorActionPreference = "Stop"

$root = $PSScriptRoot
$srcRoot = Join-Path $root "barLog\src"
$outDir = Join-Path $root "out"
$jarPath = Join-Path $root "BarLog.jar"
$manifest = Join-Path $root "manifest.txt"
$packageInputDir = Join-Path $root "package-input"
$packageOutputDir = Join-Path $root "dist"

$javafxVersion = "25.0.1"
$depsRoot = Join-Path $root "deps"
$downloadedFxRoot = Join-Path $depsRoot "javafx-sdk-$javafxVersion"
$javafxZip = Join-Path $depsRoot "openjfx-$javafxVersion-windows-x64.zip"
$javafxUrl = "https://download2.gluonhq.com/openjfx/$javafxVersion/openjfx-$javafxVersion_windows-x64_bin-sdk.zip"
$wixVersion = "3.14.1"
$wixDir = Join-Path $depsRoot "wix-$wixVersion"
$wixZip = Join-Path $depsRoot "wix-$wixVersion-binaries.zip"
$wixUrl = "https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip"

function Ensure-Dir([string]$Path) {
  if (!(Test-Path -LiteralPath $Path)) {
    New-Item -ItemType Directory -Force -Path $Path | Out-Null
  }
}

function Resolve-JdkBin {
  if ($env:BARLOG_JDK -and (Test-Path -LiteralPath (Join-Path $env:BARLOG_JDK "bin\javac.exe"))) {
    return Join-Path $env:BARLOG_JDK "bin"
  }

  $siblingJdk = Get-ChildItem -Path (Split-Path $root -Parent) -Directory -Filter "jdk-*" -ErrorAction SilentlyContinue |
    Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName "bin\javac.exe") } |
    Select-Object -First 1
  if ($siblingJdk) {
    return Join-Path $siblingJdk.FullName "bin"
  }

  $javacCommand = Get-Command javac -ErrorAction SilentlyContinue
  if ($javacCommand) {
    return Split-Path $javacCommand.Source -Parent
  }

  throw "JDK not found. Set BARLOG_JDK to a JDK 17+ folder."
}

function Resolve-FxDir {
  if ($env:BARLOG_FX_LIB -and (Test-Path -LiteralPath (Join-Path $env:BARLOG_FX_LIB "javafx.controls.jar"))) {
    return $env:BARLOG_FX_LIB
  }

  $siblingFx = Join-Path (Split-Path $root -Parent) "javafx-sdk-$javafxVersion\lib"
  if (Test-Path -LiteralPath (Join-Path $siblingFx "javafx.controls.jar")) {
    return $siblingFx
  }

  $downloadedFxLib = Join-Path $downloadedFxRoot "lib"
  if (!(Test-Path -LiteralPath (Join-Path $downloadedFxLib "javafx.controls.jar"))) {
    Ensure-Dir $depsRoot
    if (!(Test-Path -LiteralPath $javafxZip)) {
      Write-Host "Downloading JavaFX SDK $javafxVersion..."
      Invoke-WebRequest -Uri $javafxUrl -OutFile $javafxZip
    }
    Write-Host "Extracting JavaFX SDK..."
    Expand-Archive -Path $javafxZip -DestinationPath $depsRoot -Force
  }

  return $downloadedFxLib
}

function Resolve-WixBin {
  $candleCommand = Get-Command candle.exe -ErrorAction SilentlyContinue
  $lightCommand = Get-Command light.exe -ErrorAction SilentlyContinue
  if ($candleCommand -and $lightCommand) {
    return Split-Path $candleCommand.Source -Parent
  }

  if (!(Test-Path -LiteralPath (Join-Path $wixDir "candle.exe"))) {
    Ensure-Dir $depsRoot
    if (!(Test-Path -LiteralPath $wixZip)) {
      Write-Host "Downloading WiX Toolset $wixVersion..."
      Invoke-WebRequest -Uri $wixUrl -OutFile $wixZip
    }
    Write-Host "Extracting WiX Toolset..."
    Ensure-Dir $wixDir
    Expand-Archive -Path $wixZip -DestinationPath $wixDir -Force
  }

  return $wixDir
}

$jdkBin = Resolve-JdkBin
$javac = Join-Path $jdkBin "javac.exe"
$java = Join-Path $jdkBin "java.exe"
$jar = Join-Path $jdkBin "jar.exe"
$jlink = Join-Path $jdkBin "jlink.exe"
$jpackage = Join-Path $jdkBin "jpackage.exe"
$jdkRoot = Split-Path $jdkBin -Parent
$fxDir = Resolve-FxDir
$fxBinDir = Join-Path (Split-Path $fxDir -Parent) "bin"
$modulePath = "$(Join-Path $jdkRoot 'jmods');$fxDir"
$runtimeImageDir = Join-Path $root "runtime-image"

$fxJars = @(
  "javafx.base.jar",
  "javafx.graphics.jar",
  "javafx.controls.jar",
  "javafx.fxml.jar",
  "jdk.jsobject.jar"
) | ForEach-Object { Join-Path $fxDir $_ }

foreach ($fxJar in $fxJars) {
  if (!(Test-Path -LiteralPath $fxJar)) {
    throw "Missing JavaFX jar: $fxJar"
  }
}

if (Test-Path -LiteralPath $outDir) {
  Remove-Item -LiteralPath $outDir -Recurse -Force
}
Ensure-Dir $outDir
$sourceFiles = Get-ChildItem -Path $srcRoot -Recurse -File -Filter "*.java"
$classpath = $fxJars -join ";"
& $javac -d $outDir -cp $classpath $sourceFiles.FullName
if ($LASTEXITCODE -ne 0) { throw "Compilation failed." }

Get-ChildItem -Path $srcRoot -Recurse -File |
  Where-Object { $_.Extension -ne ".java" } |
  ForEach-Object {
    $relativePath = $_.FullName.Substring($srcRoot.Length).TrimStart('\')
    $destination = Join-Path $outDir "barLog\src\$relativePath"
    Ensure-Dir (Split-Path $destination -Parent)
    Copy-Item -Force -LiteralPath $_.FullName -Destination $destination
  }

& $jar cfm $jarPath $manifest -C $outDir .
if ($LASTEXITCODE -ne 0) { throw "JAR creation failed." }

if (Test-Path -LiteralPath $packageInputDir) {
  Remove-Item -LiteralPath $packageInputDir -Recurse -Force
}
Ensure-Dir $packageInputDir
Copy-Item -Force -LiteralPath $jarPath -Destination (Join-Path $packageInputDir "BarLog.jar")

function Build-RuntimeImage {
  if (!(Test-Path -LiteralPath (Join-Path $fxBinDir "glass.dll"))) {
    throw "JavaFX native libraries not found in: $fxBinDir"
  }

  if (Test-Path -LiteralPath $runtimeImageDir) {
    Remove-Item -LiteralPath $runtimeImageDir -Recurse -Force
  }

  & $jlink `
    --module-path $modulePath `
    --add-modules javafx.controls,javafx.fxml,java.net.http `
    --strip-debug `
    --no-header-files `
    --no-man-pages `
    --output $runtimeImageDir
  if ($LASTEXITCODE -ne 0) { throw "Runtime image creation failed." }

  Get-ChildItem -Path $fxBinDir -File -Filter "*.dll" |
    ForEach-Object {
      Copy-Item -Force -LiteralPath $_.FullName -Destination (Join-Path $runtimeImageDir "bin")
    }
}

if ($Run) {
  & $java "-Dprism.order=sw" "--enable-native-access=javafx.graphics" --module-path $fxDir --add-modules javafx.controls,javafx.fxml -cp $jarPath barLog.src.Main
}

if ($Package -or $Installer) {
  Build-RuntimeImage
}

if ($Package) {
  Ensure-Dir $packageOutputDir
  $appImageDir = Join-Path $packageOutputDir "BarLog"
  $zipPath = Join-Path $packageOutputDir "BarLog-Windows-x64.zip"

  if (Test-Path -LiteralPath $appImageDir) {
    Remove-Item -LiteralPath $appImageDir -Recurse -Force
  }
  if (Test-Path -LiteralPath $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
  }

  & $jpackage `
    --type app-image `
    --dest $packageOutputDir `
    --name BarLog `
    --app-version $AppVersion `
    --input $packageInputDir `
    --main-jar BarLog.jar `
    --main-class barLog.src.Main `
    --runtime-image $runtimeImageDir `
    --java-options "-Dprism.order=sw" `
    --java-options "--enable-native-access=javafx.graphics"
  if ($LASTEXITCODE -ne 0) { throw "Portable package creation failed." }

  Compress-Archive -Path (Join-Path $appImageDir "*") -DestinationPath $zipPath -CompressionLevel Optimal
  Write-Host "Portable download created: $zipPath"
}

if ($Installer) {
  Ensure-Dir $packageOutputDir
  $installerPath = Join-Path $packageOutputDir "BarLog-$AppVersion.exe"
  if (Test-Path -LiteralPath $installerPath) {
    Remove-Item -LiteralPath $installerPath -Force
  }

  $wixBin = Resolve-WixBin
  $env:PATH = "$wixBin;$env:PATH"
  & $jpackage `
    --type exe `
    --dest $packageOutputDir `
    --name BarLog `
    --app-version $AppVersion `
    --input $packageInputDir `
    --main-jar BarLog.jar `
    --main-class barLog.src.Main `
    --runtime-image $runtimeImageDir `
    --win-menu `
    --win-shortcut `
    --win-per-user-install `
    --java-options "-Dprism.order=sw" `
    --java-options "--enable-native-access=javafx.graphics"
  if ($LASTEXITCODE -ne 0) {
    throw "Installer creation failed. Install WiX Toolset and run .\build.ps1 -Installer again."
  }
}
