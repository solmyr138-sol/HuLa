# Run ONCE to fix Gradle memory (32GB host). Safe to re-run.
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "ensure-console-encoding.ps1")

Write-Host "=== HuLa Android one-time setup ==="

$gradleDir = Join-Path $env:USERPROFILE ".gradle"
$props = Join-Path $gradleDir "gradle.properties"
if (-not (Test-Path $gradleDir)) { New-Item -ItemType Directory -Path $gradleDir -Force | Out-Null }
if (Test-Path $props) {
    Copy-Item $props (Join-Path $gradleDir "gradle.properties.bak-hula") -Force
}

$java21 = "C:/Users/P1/scoop/apps/openjdk21/current"
$javaHomeLine = if (Test-Path ($java21 -replace '/','\')) {
    "org.gradle.java.home=$java21"
} else { "" }

$lines = @(
    "# HuLa Android - 32GB host"
    $javaHomeLine
    "org.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=2048m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
    "kotlin.daemon.jvmargs=-Xmx3072m -XX:MaxMetaspaceSize=1536m -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
    "android.javaCompile.suppressSourceTargetDeprecationWarning=true"
    "kotlin.compiler.execution.strategy=daemon"
    "org.gradle.parallel=true"
    "org.gradle.workers.max=4"
    "org.gradle.daemon=true"
    "org.gradle.caching=true"
) | Where-Object { $_ }
Set-Content -LiteralPath $props -Value $lines -Encoding utf8
Write-Host "[OK] $props"

$projGradle = Join-Path $PSScriptRoot "..\src-tauri\gen\android\gradle.properties"
@(
    "org.gradle.daemon=true"
    "org.gradle.jvmargs=-Xmx8192m -XX:MaxMetaspaceSize=2048m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
    "kotlin.daemon.jvmargs=-Xmx3072m -XX:MaxMetaspaceSize=1536m -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
    "android.javaCompile.suppressSourceTargetDeprecationWarning=true"
    "kotlin.compiler.execution.strategy=daemon"
    "org.gradle.parallel=true"
    "org.gradle.workers.max=4"
    "org.gradle.caching=true"
    "android.useAndroidX=true"
    "kotlin.code.style=official"
    "android.nonTransitiveRClass=true"
    "android.nonFinalResIds=false"
) | Set-Content -LiteralPath $projGradle -Encoding utf8
Write-Host "[OK] $projGradle"

$gradlew = Resolve-Path (Join-Path $PSScriptRoot "..\src-tauri\gen\android\gradlew.bat")
$prevEap = $ErrorActionPreference
$ErrorActionPreference = "Continue"
& $gradlew --stop 2>&1 | ForEach-Object { Write-Host $_ }
$ErrorActionPreference = $prevEap
Write-Host "[OK] Gradle daemons stopped"

& (Join-Path $PSScriptRoot "patch-android-rust-kt.ps1")
& (Join-Path $PSScriptRoot "sync-android-devurl.ps1")

Write-Host ""
Write-Host "Done. Next: .\scripts\android-dev.ps1"
