# Free Vite port 5210 (mobile dev) if a stale node process is still listening
. (Join-Path $PSScriptRoot "ensure-console-encoding.ps1")
$port = 5210
$listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if (-not $listeners) {
    Write-Host "Port $port is free."
    exit 0
}
foreach ($c in $listeners) {
    $procId = $c.OwningProcess
    $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
    $name = if ($proc) { $proc.ProcessName } else { "?" }
    Write-Host "Stopping PID $procId ($name) on port $port..."
    Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
}
Start-Sleep -Seconds 2
Write-Host "Port $port released."
