# Strip libc++_shared preload from Tauri-generated Rust.kt (static libc++ in libhula_app_lib.so).
$rustKt = Join-Path $PSScriptRoot "..\src-tauri\gen\android\app\src\main\java\com\hula\app\generated\Rust.kt"
if (-not (Test-Path -LiteralPath $rustKt)) {
    Write-Host "patch-android-rust-kt: Rust.kt not found, skip"
    exit 0
}
$content = Get-Content -Raw -LiteralPath $rustKt
$fixed = $content -replace '\s*System\.loadLibrary\("c\+\+_shared"\)\s*;?\r?\n', "`n"
if ($content -eq $fixed) {
    Write-Host "patch-android-rust-kt: no c++_shared preload (ok)"
} else {
    Set-Content -LiteralPath $rustKt -Value $fixed -NoNewline
    Write-Host "patch-android-rust-kt: removed c++_shared preload from Rust.kt"
}
