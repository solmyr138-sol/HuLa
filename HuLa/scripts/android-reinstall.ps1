# Reinstall APK when Rust is already built. Start Vite in another terminal: pnpm dev
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "ensure-console-encoding.ps1")
. (Join-Path $PSScriptRoot "setup-android-env.ps1")

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$so = Join-Path $root "src-tauri\target\x86_64-linux-android\debug\libhula_app_lib.so"
if (-not (Test-Path $so)) {
    throw "Run .\scripts\android-dev.ps1 first to build libhula_app_lib.so"
}

$env:GRADLE_OPTS = "-Dorg.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=2048m -Dorg.gradle.daemon=true"
$env:KOTLIN_DAEMON_JVM_OPTIONS = "-Xmx3072m -XX:MaxMetaspaceSize=1536m"
$env:TAURI_ANDROID_ARCHS = "x86_64"

& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")
& (Join-Path $PSScriptRoot "sync-android-devurl.ps1")

. (Join-Path $PSScriptRoot "select-adb-device.ps1")
Select-HuLaAdbDevice -PreferEmulator | Out-Null

Set-Location (Join-Path $root "src-tauri\gen\android")
Write-Host "Gradle installX86_64Debug..."
$prevEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
.\gradlew.bat :app:installX86_64Debug
if ($LASTEXITCODE -ne 0) { $ErrorActionPreference = $prevEap; exit $LASTEXITCODE }
$ErrorActionPreference = $prevEap

& (Join-Path $PSScriptRoot "android-grant-permissions.ps1")
Write-Host "Start: adb shell am start -n com.hula.app/.MainActivity"
