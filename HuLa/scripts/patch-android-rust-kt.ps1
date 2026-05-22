# Tauri regenerates Rust.kt without loading libc++ first — re-apply fix after each android build/init
$rustKt = Join-Path $PSScriptRoot "..\src-tauri\gen\android\app\src\main\java\com\hula\app\generated\Rust.kt"
if (-not (Test-Path -LiteralPath $rustKt)) {
    Write-Host "patch-android-rust-kt: Rust.kt not found, skip"
    exit 0
}
$content = Get-Content -Raw -LiteralPath $rustKt
$fixed = $content -replace 'init \{\s*System\.loadLibrary\("hula_app_lib"\)', @'
init {
        System.loadLibrary("c++_shared")
        System.loadLibrary("hula_app_lib")
'@
if ($content -eq $fixed) {
    Write-Host "patch-android-rust-kt: already patched"
} else {
    Set-Content -LiteralPath $rustKt -Value $fixed -NoNewline
    Write-Host "patch-android-rust-kt: patched $rustKt"
}
