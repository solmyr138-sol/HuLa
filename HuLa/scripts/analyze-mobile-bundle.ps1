# P0: Analyze mobile frontend bundle size (run after build)
$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

$env:TAURI_ENV_PLATFORM = "android"
Write-Host ">>> Building mobile frontend (android mode)..."
pnpm exec vite build --mode android

$dist = Join-Path $root "dist\static\js"
if (-not (Test-Path $dist)) {
    Write-Error "dist/static/js not found — build failed?"
}

Write-Host ""
Write-Host "=== JS chunks (largest first) ==="
Get-ChildItem $dist -Filter "*.js" | Sort-Object Length -Descending | Select-Object -First 25 |
    ForEach-Object { "{0,8:N0} KB  {1}" -f ($_.Length / 1KB), $_.Name }

Write-Host ""
Write-Host ">>> WebView profiling: Chrome -> edge://inspect -> inspect WebView"
Write-Host ">>> Compare release APK: .\scripts\build-android-release.ps1"
