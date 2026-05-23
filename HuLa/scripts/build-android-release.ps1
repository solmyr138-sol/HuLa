# P0: Production Android APK (use this for perf comparison, not `tauri android dev`)
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "setup-android-env.ps1")

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

$env:TAURI_ENV_PLATFORM = "android"
Write-Host ">>> Vite production build (android mode)..."
pnpm exec vite build --mode android

& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")

$env:CARGO_BUILD_JOBS = "1"
$env:CARGO_INCREMENTAL = "0"

Write-Host ">>> Tauri Android release (arm64, low-memory Rust)..."
Write-Host "    (若真机曾装过 release 签名包，先执行: .\scripts\adb-uninstall-hula.ps1)"
pnpm tauri android build --apk true --target aarch64

& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")

$apkDir = Join-Path $root "src-tauri\gen\android\app\build\outputs\apk\arm64\release"
$signed = Join-Path $apkDir "app-arm64-release.apk"
$unsigned = Join-Path $apkDir "app-arm64-release-unsigned.apk"
Write-Host ""
if (Test-Path $signed) {
    Write-Host "Install signed APK: $signed"
} elseif (Test-Path $unsigned) {
    Write-Host "WARNING: Only unsigned APK found. Real devices reject it (软件包似乎无效)." -ForegroundColor Yellow
    Write-Host "  $unsigned"
    Write-Host "  Create src-tauri/gen/android/keystore.properties from keystore.properties.example, then rebuild."
} else {
    Write-Host "APK output: src-tauri/gen/android/app/build/outputs/apk/"
}
Write-Host "Dev with hot reload: .\scripts\android-dev.ps1"
