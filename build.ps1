param(
  [switch]$Run
)

$ErrorActionPreference = "Stop"

$root = $PSScriptRoot
$srcRoot = Join-Path $root "barLog\\src"
$sourcesFile = Join-Path $root "sources.txt"
$outDir = Join-Path $root "out"
$jarPath = Join-Path $root "BarLog.jar"
$manifest = Join-Path $root "manifest.txt"
$appDir = Join-Path $root "dist\\BarLogFx\\app"

function Resolve-FxDir {
  if (Test-Path $appDir) {
    return $appDir
  }
  if ($env:BARLOG_FX_LIB -and (Test-Path $env:BARLOG_FX_LIB)) {
    return $env:BARLOG_FX_LIB
  }
  throw "JavaFX jars not found. Expected $appDir or set BARLOG_FX_LIB to a folder containing the JavaFX jars."
}

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
  "jackson-annotations-2.17.2.jar",
  "jackson-core-2.17.2.jar",
  "jackson-databind-2.17.2.jar"
) | ForEach-Object {
  $p = Join-Path $fxDir $_
  if (Test-Path $p) { $p }
}

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

if (Test-Path $appDir) {
  Copy-Item -Force $jarPath (Join-Path $appDir "BarLog.jar")
}

if ($Run) {
  $modulePath = $fxJars -join ";"
  $runCp = ($jarPath + $jacksonJars) -join ";"
  java -Dprism.order=sw --enable-native-access=javafx.graphics --module-path $modulePath --add-modules javafx.controls,javafx.fxml -cp $runCp barLog.src.Main
}
