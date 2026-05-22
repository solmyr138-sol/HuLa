param(
    [string]$NacosHost = "127.0.0.1",
    [int]$NacosPort = 8848,
    [string]$NamespaceId = "bfa0d426-e281-4da0-b830-c3962ed883d1",
    [string]$ConfigDir = "",
    [string]$HostIp = "127.0.0.1"
)

if (-not $ConfigDir) {
    $ConfigDir = Join-Path $PSScriptRoot "..\nacos\extracted\DEFAULT_GROUP"
}

$baseUrl = "http://${NacosHost}:${NacosPort}/nacos/v1/cs/configs"

Get-ChildItem -Path $ConfigDir -Filter "*.yml" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    $content = $content -replace '192\.168\.1\.37', $HostIp

    $params = @{
        dataId  = $_.Name
        group   = "DEFAULT_GROUP"
        content = $content
        type    = "yaml"
        tenant  = $NamespaceId
    }

    try {
        $resp = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $params -TimeoutSec 15
        Write-Host "Imported $($_.Name): $resp"
    }
    catch {
        Write-Warning "Failed $($_.Name): $_"
    }
}

Write-Host "Done."
