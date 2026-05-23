# HuLa Android dev on a physical phone (arm64). Same Wi-Fi as PC; local.yaml must use PC WLAN IP.
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "setup-android-env.ps1")

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

& (Join-Path $PSScriptRoot "free-dev-port.ps1")
& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")
& (Join-Path $PSScriptRoot "patch-android-mainactivity.ps1")

. (Join-Path $PSScriptRoot "select-adb-device.ps1")
$serial = Select-HuLaAdbDevice -PreferEmulator:$false
if ($serial -match '^emulator-') {
    Write-Host "ERROR: Physical phone expected. Unplug emulator or set ANDROID_SERIAL to your phone."
    exit 1
}

$wlanIp = (
    Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
    Where-Object {
        $_.InterfaceAlias -match 'WLAN|Wi-?Fi|无线' -and
        $_.IPAddress -notmatch '^169\.254\.' -and
        $_.PrefixOrigin -ne 'WellKnown'
    } |
    Select-Object -First 1 -ExpandProperty IPAddress
)
if (-not $wlanIp) {
    Write-Host "WARN: No WLAN IPv4 found; pass IP: .\scripts\android-dev-phone.ps1 192.168.x.x"
    if ($args.Count -gt 0) { $wlanIp = $args[0] } else { throw "Set PC LAN IP in local.yaml and pass as argument." }
}

$gradleProps = Join-Path $root "src-tauri\gen\android\gradle.properties"
if (Test-Path $gradleProps) {
    $gpLines = @(Get-Content $gradleProps | Where-Object { $_ -notmatch '^(abiList|archList|targetList)=' })
    $gpLines += "abiList=arm64-v8a", "archList=arm64", "targetList=aarch64"
    Set-Content -Path $gradleProps -Value $gpLines -Encoding UTF8
}

$env:GRADLE_OPTS = "-Dorg.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=2048m -Dfile.encoding=UTF-8"
$env:TAURI_ENV_PLATFORM = "android"
$env:ANDROID_SERIAL = $serial

Write-Host ""
Write-Host "Phone dev | ANDROID_SERIAL=$serial | --host $wlanIp | API from local.yaml"
Write-Host "Ensure backend http://${wlanIp}:18760/api is reachable on phone browser."
Write-Host "If install fails with INSTALL_FAILED_UPDATE_INCOMPATIBLE, run: .\scripts\adb-uninstall-hula.ps1"
Write-Host ""

# Do NOT pass adb serial as [DEVICE]: Tauri matches device *name*, fails, then opens Android Studio.
$hostIp = $wlanIp
if ($args.Count -gt 0 -and $args[0] -match '^\d+\.\d+\.\d+\.\d+$') {
    $hostIp = $args[0]
}
$tauriArgs = @("android", "dev", "--host", $hostIp)
if ($args.Count -gt 0 -and $args[0] -notmatch '^\d+\.\d+\.\d+\.\d+$') {
    $tauriArgs += $args
}
Write-Host "Tauri: pnpm tauri $($tauriArgs -join ' ')  (ANDROID_SERIAL=$env:ANDROID_SERIAL)"
pnpm tauri @tauriArgs

& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")
& (Join-Path $PSScriptRoot "patch-android-mainactivity.ps1")
