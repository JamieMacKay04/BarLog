param(
  [switch]$Run,
  [switch]$Package
)

$ErrorActionPreference = "Stop"

$root = $PSScriptRoot
$srcRoot = Join-Path $root "barLog\\src"
$sourcesFile = Join-Path $root "sources.txt"
$outDir = Join-Path $root "out"
$jarPath = Join-Path $root "BarLog.jar"
$manifest = Join-Path $root "manifest.txt"

$depsRoot = Join-Path $root "deps"
$javafxVersion = "25.0.1"
$javafxSdkRoot = Join-Path $depsRoot "javafx-sdk-$javafxVersion"
$javafxLibDir = Join-Path $javafxSdkRoot "lib"
$javafxZip = Join-Path $depsRoot "openjfx-$javafxVersion-windows-x64.zip"
$javafxUrl = "https://download2.gluonhq.com/openjfx/$javafxVersion/openjfx-$javafxVersion_windows-x64_bin-sdk.zip"

$jacksonVersion = "2.17.2"
$jacksonDir = Join-Path $depsRoot "jackson-$jacksonVersion"
$jacksonBaseUrl = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core"

$packageInputDir = Join-Path $root "package-input"
$packageOutputDir = Join-Path $root "dist"

function Resolve-FxDir {
  if (Test-Path $javafxLibDir) {
    return $javafxLibDir
  }
  if ($env:BARLOG_FX_LIB -and (Test-Path $env:BARLOG_FX_LIB)) {
    return $env:BARLOG_FX_LIB
  }
  throw "JavaFX jars not found. Run this script once online to download them or set BARLOG_FX_LIB to a folder containing the JavaFX jars."
}

function Ensure-Dir($path) {
  if (!(Test-Path $path)) {
    New-Item -ItemType Directory -Force -Path $path | Out-Null
  }
}

function Ensure-JavaFx {
  if ($env:BARLOG_FX_LIB -and (Test-Path $env:BARLOG_FX_LIB)) {
    return
  }
  if (Test-Path $javafxLibDir) {
    return
  }
  Ensure-Dir $depsRoot
  if (!(Test-Path $javafxZip)) {
    Write-Host "Downloading JavaFX SDK $javafxVersion..."
    Invoke-WebRequest -Uri $javafxUrl -OutFile $javafxZip
  }
  Write-Host "Extracting JavaFX SDK..."
  Expand-Archive -Path $javafxZip -DestinationPath $depsRoot -Force
}

function Ensure-Jackson {
  Ensure-Dir $jacksonDir
  $jacksonArtifacts = @(
    "jackson-annotations-$jacksonVersion.jar",
    "jackson-core-$jacksonVersion.jar",
    "jackson-databind-$jacksonVersion.jar"
  )
  foreach ($artifact in $jacksonArtifacts) {
    $target = Join-Path $jacksonDir $artifact
    if (!(Test-Path $target)) {
      $group = $artifact.Split("-")[0] + "-" + $artifact.Split("-")[1]
      $url = "$jacksonBaseUrl/$group/$jacksonVersion/$artifact"
      Write-Host "Downloading $artifact..."
      Invoke-WebRequest -Uri $url -OutFile $target
    }
  }
}

Ensure-JavaFx
Ensure-Jackson

$fxDir = Resolve-FxDir
$fxJars = @(
  "javafx.base.jar",
  "javafx.graphics.jar",
  "javafx.controls.jar",
  "javafx.fxml.jar",
  "jdk.jsobject.jar"
) | ForEach-Object { Join-Path $fxDir $_ }

foreach ($jar in $fxJars) {
  if (!(Test-Path $jar)) {
    throw "Missing JavaFX jar: $jar"
  }
}

$jacksonJars = @(
  "jackson-annotations-$jacksonVersion.jar",
  "jackson-core-$jacksonVersion.jar",
  "jackson-databind-$jacksonVersion.jar"
) | ForEach-Object { Join-Path $jacksonDir $_ }

$cp = ($fxJars + $jacksonJars) -join ";"

javac -d $outDir -cp $cp "@$sourcesFile"

Get-ChildItem -Path $srcRoot -Recurse -File |
  Where-Object { $_.Extension -ne ".java" } |
  ForEach-Object {
    $rel = $_.FullName.Substring($srcRoot.Length).TrimStart('\')
    $dest = Join-Path $root "out\\barLog\\src\\$rel"
    New-Item -ItemType Directory -Force -Path (Split-Path $dest) | Out-Null
    Copy-Item -Force $_.FullName $dest
  }

jar cfm $jarPath $manifest -C $outDir .

function Sync-PackageInput {
  Ensure-Dir $packageInputDir
  Copy-Item -Force $jarPath (Join-Path $packageInputDir "BarLog.jar")
  foreach ($jar in $jacksonJars) {
    Copy-Item -Force $jar (Join-Path $packageInputDir (Split-Path $jar -Leaf))
  }
}

Sync-PackageInput

if ($Run) {
  $modulePath = $fxDir
  $runCp = ($jarPath + $jacksonJars) -join ";"
  java -Dprism.order=sw --enable-native-access=javafx.graphics --module-path $modulePath --add-modules javafx.controls,javafx.fxml -cp $runCp barLog.src.Main
}

if ($Package) {
  Ensure-Dir $packageOutputDir
  jpackage `
    --type exe `
    --dest $packageOutputDir `
    --name BarLog `
    --app-version 1.0 `
    --input $packageInputDir `
    --main-jar BarLog.jar `
    --main-class barLog.src.Main `
    --module-path $fxDir `
    --add-modules javafx.controls,javafx.fxml `
    --java-options "-Dprism.order=sw" `
    --java-options "--enable-native-access=javafx.graphics"
}
