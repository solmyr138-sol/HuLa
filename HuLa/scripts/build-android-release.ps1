# P0: Production Android APK (use this for perf comparison, not `tauri android dev`)
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "setup-android-env.ps1")

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

$env:TAURI_ENV_PLATFORM = "android"
Write-Host ">>> Vite production build (android mode)..."
pnpm exec vite build --mode android

Write-Host ">>> Tauri Android release..."
pnpm tauri android build --apk

Write-Host ""
Write-Host "Install APK from src-tauri/gen/android/app/build/outputs/apk/"
Write-Host "Dev with hot reload: .\scripts\android-dev.ps1"
