# Start all HuLa Java microservices on Windows
$ProfileName = "dev"
$HostIp = "192.168.1.154"

$env:JAVA_HOME = "C:\Users\P1\scoop\apps\openjdk21\current"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$CloudRoot = "e:\code\HuLa\HuLa-Server\luohuo-cloud"
$LogsDir = Join-Path $CloudRoot "logs"
New-Item -ItemType Directory -Force -Path $LogsDir | Out-Null

$services = @(
    @("luohuo-gateway-server", "luohuo-gateway", "-Xms512m -Xmx1024m"),
    @("luohuo-oauth-server", "luohuo-oauth", "-Xms512m -Xmx1024m"),
    @("luohuo-base-server", "luohuo-base", "-Xms512m -Xmx1024m"),
    @("luohuo-system-server", "luohuo-system", "-Xms512m -Xmx1024m"),
    @("luohuo-im-server", "luohuo-im", "-Xms768m -Xmx1536m"),
    @("luohuo-ws-server", "luohuo-ws", "-Xms512m -Xmx1024m")
)

foreach ($svc in $services) {
    $name = $svc[0]
    $dir = $svc[1]
    $mem = $svc[2]
    $jar = Join-Path $CloudRoot "$dir\$name\target\$name.jar"
    if (-not (Test-Path $jar)) {
        Write-Error "Missing jar: $jar"
        continue
    }
    $running = Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -like "*$name*" }
    if ($running) {
        Write-Host "[skip] $name already running"
        continue
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
    Start-Sleep -Seconds 15
}

Write-Host "Done. Logs: $LogsDir"
Write-Host "API: http://127.0.0.1:18760/api"
Write-Host "WS:  ws://127.0.0.1:18760/api/ws/ws"
Write-Host "Android emulator: http://10.0.2.2:18760/api"
