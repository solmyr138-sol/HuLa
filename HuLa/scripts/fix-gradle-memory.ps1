# 修复用户级 ~/.gradle/gradle.properties 过小导致 Metaspace / Kotlin 编译失败
$ErrorActionPreference = "Stop"
$gradleDir = Join-Path $env:USERPROFILE ".gradle"
$props = Join-Path $gradleDir "gradle.properties"
$backup = Join-Path $gradleDir "gradle.properties.bak-hula"

if (-not (Test-Path $gradleDir)) {
    New-Item -ItemType Directory -Path $gradleDir -Force | Out-Null
}

if (Test-Path $props) {
    Copy-Item $props $backup -Force
    Write-Host "Backed up to $backup"
}

$content = @"
# Patched by HuLa scripts/fix-gradle-memory.ps1 (32GB host)
org.gradle.java.home=D:/Program Files/Android/Android Studio/jbr
org.gradle.jvmargs=-Xmx6144m -XX:MaxMetaspaceSize=1536m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
kotlin.daemon.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=1024m -Dfile.encoding=UTF-8
kotlin.compiler.execution.strategy=daemon
org.gradle.parallel=true
org.gradle.workers.max=4
org.gradle.daemon=true
"@

Set-Content -LiteralPath $props -Value $content -Encoding UTF8
Write-Host "Updated $props"
Write-Host "Stopping old Gradle daemons..."
& (Join-Path (Resolve-Path (Join-Path $PSScriptRoot "..\src-tauri\gen\android")).Path "gradlew.bat") --stop 2>&1 | Out-Null
