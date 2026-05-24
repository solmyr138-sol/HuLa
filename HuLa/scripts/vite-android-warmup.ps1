# Pre-warm Vite deps before tauri android dev opens the WebView (avoids optimize+reload white screen).
param(
    [Parameter(Mandatory = $true)]
    [string]$HostIp
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

$env:TAURI_ENV_PLATFORM = "android"
$env:TAURI_DEV_HOST = $HostIp

Write-Host "[vite-android-warmup] TAURI_DEV_HOST=$HostIp"

$pnpmCmd = Get-Command pnpm.cmd -ErrorAction SilentlyContinue
if ($pnpmCmd) {
    $pnpm = $pnpmCmd.Source
} else {
    $pnpm = (Get-Command pnpm -ErrorAction Stop).Source
}
$viteProc = Start-Process -FilePath $pnpm `
    -ArgumentList @("exec", "vite", "--mode", "android") `
    -PassThru -NoNewWindow -WorkingDirectory $root

try {
    $deadline = (Get-Date).AddMinutes(3)
    $ready = $false
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Seconds 2
        try {
            $base = "http://${HostIp}:5210"
            $index = Invoke-WebRequest -Uri "$base/" -UseBasicParsing -TimeoutSec 5
            if ($index.StatusCode -ne 200) { continue }
            $null = Invoke-WebRequest -Uri "$base/src/main.ts" -UseBasicParsing -TimeoutSec 120
            Start-Sleep -Seconds 3
            $null = Invoke-WebRequest -Uri "$base/src/main.ts" -UseBasicParsing -TimeoutSec 120
            $ready = $true
            break
        } catch {
            # Vite still optimizing or port not listening yet
        }
    }
    if (-not $ready) {
        throw "Vite did not become ready on http://${HostIp}:5210 within 3 minutes"
    }
    Write-Host "[vite-android-warmup] dependency cache ready"
} finally {
    if ($viteProc -and -not $viteProc.HasExited) {
        Stop-Process -Id $viteProc.Id -Force -ErrorAction SilentlyContinue
    }
    & (Join-Path $PSScriptRoot "free-dev-port.ps1") | Out-Null
}
