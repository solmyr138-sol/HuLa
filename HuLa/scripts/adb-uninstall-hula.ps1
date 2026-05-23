# 签名不一致 (INSTALL_FAILED_UPDATE_INCOMPATIBLE) 时必须先卸载再装。
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "select-adb-device.ps1")
$null = Select-HuLaAdbDevice -PreferEmulator:$false

Write-Host "Uninstalling com.hula.app ..."
adb uninstall com.hula.app
if ($LASTEXITCODE -ne 0) {
    Write-Host "Note: package may not be installed (exit $LASTEXITCODE)"
}
Write-Host "Done. Reinstall with tauri android dev / adb install <apk>."
