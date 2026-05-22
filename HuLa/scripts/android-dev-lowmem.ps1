# 仅当 android-dev.ps1 编译 OOM / Gradle daemon 崩溃时使用
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "setup-android-env.ps1")

Set-Location (Join-Path $PSScriptRoot "..")

$env:TAURI_ENV_PLATFORM = "android"
$env:TAURI_ANDROID_ARCHS = "x86_64"
$env:CARGO_BUILD_JOBS = "1"
$env:CARGO_INCREMENTAL = "0"
$env:RUSTFLAGS = "-C debuginfo=1"
$env:GRADLE_OPTS = "-Dorg.gradle.daemon=false -Dorg.gradle.workers.max=1"

Write-Host "LOW-MEM mode: cleaning x86_64-linux-android target..."
Push-Location "src-tauri"
$androidTarget = "target\x86_64-linux-android"
if (Test-Path $androidTarget) {
    foreach ($attempt in 1..3) {
        try {
            Remove-Item -Recurse -Force $androidTarget -ErrorAction Stop
            break
        } catch {
            Write-Host "Remove attempt $attempt failed, retry..."
            Start-Sleep -Seconds 2
        }
    }
}
Pop-Location

& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")
& (Join-Path $PSScriptRoot "sync-android-devurl.ps1")

$gradlew = Join-Path (Get-Location) "src-tauri\gen\android\gradlew.bat"
if (Test-Path $gradlew) { & $gradlew --stop 2>&1 | Out-Null }

pnpm tauri android dev @args
& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")
