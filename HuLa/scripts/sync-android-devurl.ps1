# Manual patch: emulator devUrl in APK assets (normally done by Gradle patchAndroidDevUrl)
# Keep bundled tauri.conf.json devUrl on 10.0.2.2 (emulator -> host)
$devUrl = "http://127.0.0.1:5210/"
$assets = Join-Path $PSScriptRoot "..\src-tauri\gen\android\app\src\main\assets\tauri.conf.json"
if (-not (Test-Path -LiteralPath $assets)) {
    Write-Host "sync-android-devurl: assets not found yet, skip"
    exit 0
}
$content = Get-Content -Raw -LiteralPath $assets
$patched = [regex]::Replace($content, '"devUrl"\s*:\s*"[^"]*"', "`"devUrl`":`"$devUrl`"")
# Stale Vite may have baked LAN IP into assets; emulator only reaches 10.0.2.2
$patched = [regex]::Replace($patched, 'http://(?:192\.168\.\d+\.\d+|10\.0\.2\.2):5210', 'http://127.0.0.1:5210')
if ($content -ne $patched) {
    Set-Content -LiteralPath $assets -Value $patched -NoNewline
    Write-Host "sync-android-devurl: set devUrl=$devUrl"
}
