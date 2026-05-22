# Pick one adb target when multiple devices are connected. Sets $env:ANDROID_SERIAL for child tools (tauri, gradle, adb).

function Invoke-HuLaAdb {
    param([Parameter(ValueFromRemainingArguments)][string[]]$AdbArgs)
    $prevEap = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    $out = & adb @AdbArgs 2>&1
    $ErrorActionPreference = $prevEap
    return $out
}

function Get-ConnectedAdbSerials {
    $list = @()
    Invoke-HuLaAdb devices | ForEach-Object {
        $line = "$_".Trim()
        if ($line -match '^(\S+)\s+device\s*$') {
            $list += $Matches[1]
        }
    }
    return $list
}

function Select-HuLaAdbDevice {
    param(
        [switch]$PreferEmulator = $true
    )

    $serials = @(Get-ConnectedAdbSerials)
    if ($serials.Count -eq 0) {
        Write-Host "ERROR: No adb device. Start Pixel 8 Pro AVD (Play) in Device Manager."
        Invoke-HuLaAdb devices -l | ForEach-Object { Write-Host $_ }
        exit 1
    }

    if ($env:ANDROID_SERIAL -and ($serials -contains $env:ANDROID_SERIAL)) {
        Write-Host "ADB target: $env:ANDROID_SERIAL (ANDROID_SERIAL)"
        return $env:ANDROID_SERIAL
    }

    if ($PreferEmulator) {
        $emu = @($serials | Where-Object { $_ -match '^emulator-' } | Select-Object -First 1)
        if ($emu.Count -gt 0) {
            $env:ANDROID_SERIAL = $emu[0]
            $others = @($serials | Where-Object { $_ -ne $env:ANDROID_SERIAL })
            if ($others.Count -gt 0) {
                Write-Host "ADB target: $($env:ANDROID_SERIAL) (emulator ONLY)"
                Write-Host "Ignored other adb device(s): $($others -join ', ') — unplug Huawei USB to avoid Tauri picking arm64 phone."
            } else {
                Write-Host "ADB target: $($env:ANDROID_SERIAL) (emulator)"
            }
            return $env:ANDROID_SERIAL
        }
        Write-Host "ERROR: No emulator in 'adb devices'. Start Pixel 8 Pro AVD first (Device Manager -> Play)."
        Write-Host "If only Huawei RTE-AL00 is listed, builds target aarch64 phone — NOT the emulator."
        Invoke-HuLaAdb devices -l | ForEach-Object { Write-Host $_ }
        exit 1
    }

    if ($serials.Count -eq 1) {
        $env:ANDROID_SERIAL = $serials[0]
        if ($env:ANDROID_SERIAL -match '^emulator-') {
            Write-Host "ADB target: $($env:ANDROID_SERIAL) (emulator)"
        } else {
            Write-Host "ADB target: $($env:ANDROID_SERIAL) (physical phone — dev server needs PC LAN IP)"
        }
        return $env:ANDROID_SERIAL
    }

    Write-Host "Multiple adb devices connected. Unplug a phone or set ANDROID_SERIAL:"
    Write-Host ""
    Invoke-HuLaAdb devices -l | ForEach-Object { Write-Host $_ }
    Write-Host ""
    Write-Host '  $env:ANDROID_SERIAL = "emulator-5554"'
    Write-Host "  .\scripts\android-dev.ps1"
    exit 1
}
