# HuLa Android dev (emulator-first). Run android-setup-once.ps1 only if Gradle OOM.
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "setup-android-env.ps1")

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

& (Join-Path $PSScriptRoot "free-dev-port.ps1")

. (Join-Path $PSScriptRoot "select-adb-device.ps1")
$serial = Select-HuLaAdbDevice -PreferEmulator
if ($serial -notmatch '^emulator-') {
    Write-Host "ERROR: This script is for the x86_64 emulator only. Unplug the phone or use: pnpm tauri android dev --host <LAN-IP> <phone-serial>"
    exit 1
}
$isEmulator = $true

$prevEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
if ($isEmulator) {
    adb -s $serial reverse --remove-all 2>&1 | Out-Null
    adb -s $serial reverse tcp:5210 tcp:5210 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Emulator: adb reverse tcp:5210 + tauri --host 127.0.0.1 (avoids broken 192.168.x.x on Windows)"
    } else {
        Write-Host "WARN: adb reverse failed — install platform-tools / restart emulator"
    }
}
$ErrorActionPreference = $prevEap

$env:GRADLE_OPTS = "-Dorg.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=2048m -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dorg.gradle.daemon=true"
$env:KOTLIN_DAEMON_JVM_OPTIONS = "-Xmx3072m -XX:MaxMetaspaceSize=1536m"
$env:TAURI_ENV_PLATFORM = "android"
$env:CARGO_BUILD_JOBS = "6"
$env:CARGO_INCREMENTAL = "1"

# Emulator-only Gradle/Rust targets (faster; phone builds need universal flavors — do not use this script)
$gradleProps = Join-Path $root "src-tauri\gen\android\gradle.properties"
if (Test-Path $gradleProps) {
    $gpLines = @(Get-Content $gradleProps | Where-Object { $_ -notmatch '^(abiList|archList|targetList)=' })
    $gpLines += "abiList=x86_64", "archList=x86_64", "targetList=x86_64"
    Set-Content -Path $gradleProps -Value $gpLines -Encoding UTF8
}

$abi = (Invoke-HuLaAdb -s $serial shell getprop ro.product.cpu.abi | Select-Object -Last 1).ToString().Trim()
if ($abi -notmatch 'x86') {
    Write-Host "ERROR: $serial ABI is '$abi' (expected x86*). Unplug the phone or start the Pixel x86_64 AVD."
    exit 1
}
Write-Host "Device ABI: $abi (expect Rust log: x86_64-linux-android -> jniLibs/x86_64)"

& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")
& (Join-Path $PSScriptRoot "patch-android-mainactivity.ps1")

$so = Join-Path $root "src-tauri\target\x86_64-linux-android\debug\libhula_app_lib.so"
if (Test-Path $so) {
    $mb = [math]::Round((Get-Item $so).Length / 1MB, 1)
    Write-Host "Rust Android cache OK (${mb} MB .so)"
} else {
    Write-Host "First Android Rust build: ~30-60 min."
}

Write-Host ""
if ($isEmulator) {
    Write-Host "Mode: EMULATOR | devUrl=http://127.0.0.1:5210 (via adb reverse) | WebView=http://tauri.localhost"
} else {
    Write-Host "Mode: PHYSICAL DEVICE | devUrl=PC LAN IP (e.g. 192.168.x.x) — phone and PC must be same Wi-Fi"
}
Write-Host "Wait for: optimized dependencies (~1-2 min first run). Do not edit vite.config.ts while running."
Write-Host "API: local.yaml LAN IP; emulator auto -> http://10.0.2.2:18760/api"
Write-Host ""

# Do NOT pass adb serial to Tauri (e.g. emulator-5554): CLI matches device *name*, fails, then defaults to
# aarch64-linux-android and prints "Opening Android Studio". ANDROID_SERIAL above is enough.
$tauriArgs = @("android", "dev", "--host", "127.0.0.1")
if ($args.Count -gt 0) {
    $tauriArgs += $args
}
Write-Host "Tauri: pnpm tauri $($tauriArgs -join ' ')  (ANDROID_SERIAL=$env:ANDROID_SERIAL)"
pnpm tauri @tauriArgs

& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")
& (Join-Path $PSScriptRoot "patch-android-mainactivity.ps1")
