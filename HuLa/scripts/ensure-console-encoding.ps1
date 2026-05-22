# Force UTF-8 for PowerShell + Gradle/Java child processes (fixes GBK mojibake on Chinese Windows)
if ($PSVersionTable.PSVersion.Major -ge 6) {
    try {
        [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
        [Console]::InputEncoding = [System.Text.UTF8Encoding]::new($false)
        $OutputEncoding = [System.Text.UTF8Encoding]::new($false)
    } catch {
        # Windows PowerShell 5.x may reject; chcp below still helps gradlew.bat
    }
}

try {
    chcp 65001 | Out-Null
} catch {}

$env:PYTHONIOENCODING = 'utf-8'

$javaEnc = '-Dfile.encoding=UTF-8'
if (-not $env:JAVA_TOOL_OPTIONS) {
    $env:JAVA_TOOL_OPTIONS = $javaEnc
} elseif ($env:JAVA_TOOL_OPTIONS -notmatch 'file\.encoding') {
    $env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS $javaEnc"
}

# Gradle daemon JVM (merged into android-dev.ps1 GRADLE_OPTS when present)
$gradleEnc = '-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8'
if (-not $env:GRADLE_OPTS) {
    $env:GRADLE_OPTS = $gradleEnc
} elseif ($env:GRADLE_OPTS -notmatch 'file\.encoding') {
    $env:GRADLE_OPTS = "$env:GRADLE_OPTS $gradleEnc"
}
