# Re-apply WebView perf tweaks after `tauri android init` regenerates MainActivity.kt
$path = Join-Path $PSScriptRoot "..\src-tauri\gen\android\app\src\main\java\com\hula\app\MainActivity.kt"
if (-not (Test-Path -LiteralPath $path)) {
    Write-Host "patch-android-mainactivity: MainActivity.kt not found, skip"
    exit 0
}
$content = Get-Content -Raw -LiteralPath $path
$import = 'import android.webkit.WebSettings'
if ($content -notmatch 'WebSettings') {
    $content = $content -replace 'import android\.webkit\.WebView', "import android.webkit.WebSettings`nimport android.webkit.WebView"
}
$perf = @'
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
'@
if ($content -notmatch 'LAYER_TYPE_HARDWARE') {
    $content = $content -replace 'currentWebView = webView\r?\n', "currentWebView = webView`n$perf`n"
    Set-Content -LiteralPath $path -Value $content -NoNewline
    Write-Host "patch-android-mainactivity: applied hardware acceleration"
} else {
    Write-Host "patch-android-mainactivity: already patched"
}
