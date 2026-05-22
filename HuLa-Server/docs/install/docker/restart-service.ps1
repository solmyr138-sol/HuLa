param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('gateway', 'oauth', 'base', 'system', 'im', 'ws', 'all')]
    [string]$Service
)

$ProfileName = "dev"
$HostIp = "192.168.1.154"
$env:JAVA_HOME = "C:\Users\P1\scoop\apps\openjdk21\current"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$CloudRoot = "e:\code\HuLa\HuLa-Server\luohuo-cloud"
$LogsDir = Join-Path $CloudRoot "logs"
New-Item -ItemType Directory -Force -Path $LogsDir | Out-Null

$map = @{
    gateway = @("luohuo-gateway-server", "luohuo-gateway", "-Xms512m -Xmx1024m")
    oauth   = @("luohuo-oauth-server", "luohuo-oauth", "-Xms512m -Xmx1024m")
    base    = @("luohuo-base-server", "luohuo-base", "-Xms512m -Xmx1024m")
    system  = @("luohuo-system-server", "luohuo-system", "-Xms512m -Xmx1024m")
    im      = @("luohuo-im-server", "luohuo-im", "-Xms768m -Xmx1536m")
    ws      = @("luohuo-ws-server", "luohuo-ws", "-Xms512m -Xmx1024m")
}

function Stop-LuohuoService([string]$jarName) {
    Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -like "*$jarName*" } |
        ForEach-Object {
            Write-Host "[stop] PID $($_.ProcessId) $jarName"
            Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
        }
}

function Start-LuohuoService($svc) {
    $name = $svc[0]
    $dir = $svc[1]
    $mem = $svc[2]
    $jar = Join-Path $CloudRoot "$dir\$name\target\$name.jar"
    if (-not (Test-Path $jar)) {
        throw "Missing jar: $jar"
    }
    $log = Join-Path $LogsDir "$name.log"
    $errLog = Join-Path $LogsDir "$name.err.log"
    Write-Host "[start] $name"
    $argList = @(
        $mem.Split(" ")
        "-Dspring.profiles.active=$ProfileName"
        "-DNACOS_LOCAL_IP=$HostIp"
        "-jar", $jar
    )
    Start-Process java -ArgumentList $argList -WorkingDirectory $CloudRoot `
        -RedirectStandardOutput $log -RedirectStandardError $errLog -WindowStyle Hidden
}

$targets = if ($Service -eq 'all') { @('gateway', 'oauth', 'base', 'system', 'im', 'ws') } else { @($Service) }

foreach ($key in $targets) {
    Stop-LuohuoService $map[$key][0]
}
Start-Sleep -Seconds 3

foreach ($key in $targets) {
    Start-LuohuoService $map[$key]
    Start-Sleep -Seconds 12
}

Write-Host "Restarted: $($targets -join ', ')"
