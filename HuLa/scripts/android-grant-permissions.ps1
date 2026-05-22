# Grant HuLa runtime permissions on emulator/device (skip blocking system dialogs during dev)
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "select-adb-device.ps1")
if (-not $env:ANDROID_SERIAL) {
    Select-HuLaAdbDevice -PreferEmulator | Out-Null
}

$pkg = "com.hula.app"
$perms = @(
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO",
    "android.permission.READ_MEDIA_IMAGES",
    "android.permission.READ_MEDIA_VIDEO",
    "android.permission.READ_MEDIA_AUDIO",
    "android.permission.MODIFY_AUDIO_SETTINGS"
)
$prevEap = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
foreach ($p in $perms) {
    adb shell pm grant $pkg $p 2>&1 | ForEach-Object { Write-Host $_ }
}
$ErrorActionPreference = $prevEap
Write-Host "Done. Restart app: adb shell am force-stop $pkg; adb shell am start -n $pkg/.MainActivity"
