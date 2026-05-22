# HuLa-Server 本地 Docker 中间件一键启动（Windows）
$ErrorActionPreference = "Stop"
$DockerDir = $PSScriptRoot

Set-Location $DockerDir

Write-Host ">>> 启动 Docker 中间件..."
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d mysql redis rocketmq-namesrv rocketmq-broker rocketmq-proxy minio minio-mc nacos

Write-Host ">>> 等待 MySQL 就绪..."
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    $health = docker inspect mysql --format '{{.State.Health.Status}}' 2>$null
    if ($health -eq "healthy") { $ready = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $ready) { throw "MySQL 未在预期时间内变为 healthy" }

Write-Host ">>> 初始化 Nacos 库表（已存在则跳过报错可忽略）..."
docker cp "$DockerDir\..\mysql-schema.sql" mysql:/tmp/mysql-schema.sql
docker exec mysql sh -c "mysql -uroot -p123456 nacos < /tmp/mysql-schema.sql" 2>$null

Write-Host ">>> 初始化业务库..."
docker cp "$DockerDir\init-databases.sql" mysql:/tmp/init-databases.sql
docker exec mysql sh -c "mysql -uroot -p123456 < /tmp/init-databases.sql"

$devImported = docker exec mysql sh -c "mysql -uroot -p123456 -N -e \"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='luohuo_dev';\"" 2>$null
if ([int]$devImported -lt 10) {
    Write-Host ">>> 导入 luohuo_dev / luohuo_im_01（首次较慢）..."
    docker cp "$DockerDir\..\sql\luohuo_dev.sql" mysql:/tmp/luohuo_dev.sql
    docker cp "$DockerDir\..\sql\luohuo_im_01.sql" mysql:/tmp/luohuo_im_01.sql
    docker exec mysql sh -c "mysql -uroot -p123456 luohuo_dev < /tmp/luohuo_dev.sql"
    docker exec mysql sh -c "mysql -uroot -p123456 luohuo_im_01 < /tmp/luohuo_im_01.sql"
}

Write-Host ">>> 初始化 Nacos 命名空间与配置..."
docker cp "$DockerDir\init-nacos-namespace.sql" mysql:/tmp/init-nacos-namespace.sql
docker exec mysql sh -c "mysql -uroot -p123456 nacos < /tmp/init-nacos-namespace.sql" 2>$null
docker restart nacos-standalone-mysql | Out-Null
Start-Sleep -Seconds 15
powershell -NoProfile -File "$DockerDir\import-nacos-config.ps1" -HostIp "127.0.0.1"

Write-Host ""
Write-Host "=== 中间件已启动 ==="
Write-Host "MySQL      : 127.0.0.1:13306  (root/123456, luohuo_dev/5Dez5Yi7nAmpyh1C)"
Write-Host "Redis      : 127.0.0.1:16379  (密码 luo123456)"
Write-Host "Nacos      : http://127.0.0.1:8080  API :8848  (nacos/nacos)"
Write-Host "RocketMQ   : 127.0.0.1:9876"
Write-Host "MinIO      : http://127.0.0.1:9001  (luohuo/mini.123huo)"
Write-Host ""
Write-Host "Java 微服务需在宿主机编译启动，见 docs/install/服务端部署文档.md"
