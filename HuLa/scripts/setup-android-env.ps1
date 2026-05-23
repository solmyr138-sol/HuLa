# HuLa Android build environment. Dot-source before: pnpm tauri android dev
. (Join-Path $PSScriptRoot "ensure-console-encoding.ps1")

$sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
# Match Tauri CLI (symlinks libc++_shared from this NDK into jniLibs)
$ndk = Join-Path $sdk "ndk\29.0.14206865"
$ndkBin = Join-Path $ndk "toolchains\llvm\prebuilt\windows-x86_64\bin"

if (-not (Test-Path -LiteralPath $sdk)) {
    Write-Error "Android SDK not found: $sdk"
    exit 1
}
if (-not (Test-Path -LiteralPath $ndkBin)) {
    Write-Error "NDK not found: $ndk. Run: sdkmanager `"ndk;29.0.14206865`""
    exit 1
}

$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk
$env:NDK_HOME = $ndk

if (-not $env:JAVA_HOME) {
    $java21 = "C:\Users\P1\scoop\apps\openjdk21\current"
    if (Test-Path -LiteralPath $java21) { $env:JAVA_HOME = $java21 }
}

function Add-ToPathOnce([string]$dir) {
    if (-not $dir -or -not (Test-Path -LiteralPath $dir)) { return }
    $parts = $env:Path -split ';' | Where-Object { $_ }
    if ($parts -notcontains $dir) {
        $env:Path = "$dir;" + ($parts -join ';')
    }
}
Add-ToPathOnce $ndkBin
if ($env:JAVA_HOME) { Add-ToPathOnce "$env:JAVA_HOME\bin" }
Add-ToPathOnce "$sdk\platform-tools"
Add-ToPathOnce "$sdk\cmdline-tools\latest\bin"

# cc-rs / native deps
$env:CC_x86_64_linux_android = "x86_64-linux-android26-clang"
$env:CXX_x86_64_linux_android = "x86_64-linux-android26-clang++"
$env:AR_x86_64_linux_android = "llvm-ar"
$env:CC_aarch64_linux_android = "aarch64-linux-android26-clang"
$env:CXX_aarch64_linux_android = "aarch64-linux-android26-clang++"
$env:AR_aarch64_linux_android = "llvm-ar"
$env:CC_armv7_linux_androideabi = "armv7a-linux-androideabi26-clang"
$env:CXX_armv7_linux_androideabi = "armv7a-linux-androideabi26-clang++"
$env:CC_i686_linux_android = "i686-linux-android26-clang"
$env:CXX_i686_linux_android = "i686-linux-android26-clang++"

# cargo build --target aarch64-linux-android 需要完整 linker 路径（与 Tauri CLI 一致）
$clang = Join-Path $ndkBin "aarch64-linux-android26-clang.cmd"
if (Test-Path -LiteralPath $clang) {
    $env:CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER = $clang
}

# libc++ 链接写在 src-tauri/build.rs + .cargo/config.toml，不要在这里设 CARGO_TARGET_*_RUSTFLAGS
# （与 Tauri 注入的 flags 冲突会导致每次启动全量重编）

Write-Host "ANDROID_HOME=$env:ANDROID_HOME"
Write-Host "NDK_HOME=$env:NDK_HOME"
Write-Host "Android NDK toolchain added to PATH"
