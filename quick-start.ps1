#!/usr/bin/env pwsh
#.SYNOPSIS
# Quick start script - launches pre-compiled services without rebuilding

# Kill existing Java
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep 2

$JAVAPATH = "C:\Program Files\Java\jdk-21\bin\java.exe"
$WORKSPACE = "D:\uni\Microservicios\aquadrop_latam"

Write-Host "`n=== AQUADROP QUICK START ===" -ForegroundColor Cyan
Write-Host "Using Java: $JAVAPATH"
Write-Host "Workspace: $WORKSPACE`n"

# Ensure Docker infrastructure is up
Write-Host "Verificando Docker..." -ForegroundColor Yellow
docker-compose -f "$WORKSPACE\docker-compose.yml" ps --quiet | Measure-Object | Where-Object { $_.Count -eq 0 } | ForEach-Object {
  Write-Host "Iniciando Docker..." -ForegroundColor Yellow
  docker-compose -f "$WORKSPACE\docker-compose.yml" up -d
  Start-Sleep 15
}

# Start services in background
Write-Host "`nIniciando servicios..." -ForegroundColor Cyan

$services = @(
  @{
    name = "Eureka Server"
    port = "8761"
    jar = "$WORKSPACE\eureka-server\target\eureka-server-0.0.1-SNAPSHOT.jar"
  },
  @{
    name = "API Gateway"
    port = "8080"
    jar = "$WORKSPACE\api-gateway\api-gateway\target\api-gateway-0.0.1-SNAPSHOT.jar"
  },
  @{
    name = "Booking Service"
    port = "8085"
    jar = "$WORKSPACE\booking-service\target\bookingService-0.0.1-SNAPSHOT.jar"
  },
  @{
    name = "Payment Service"
    port = "8082"
    jar = "$WORKSPACE\payment-service\target\payment-service-0.0.1-SNAPSHOT.jar"
  },
  @{
    name = "Fleet Service"
    port = "8083"
    jar = "$WORKSPACE\fleet-service\target\fleet-service-0.0.1-SNAPSHOT.jar"
  },
  @{
    name = "Notification Service"
    port = "8084"
    jar = "$WORKSPACE\notification-service\target\notification-service-0.0.1-SNAPSHOT.jar"
  }
)

foreach ($service in $services) {
  if (Test-Path $service.jar) {
    Write-Host "▸ $($service.name) @ :$($service.port)"
    Start-Process $JAVAPATH -ArgumentList "-jar $($service.jar)" -WindowStyle Hidden
  } else {
    Write-Host "✗ $($service.name) - JAR no existe: $($service.jar)" -ForegroundColor Red
  }
}

Write-Host "`nEsperando a que servicios inicien..." -ForegroundColor Yellow
Start-Sleep 20

Write-Host "`n=== STATUS ===" -ForegroundColor Green
$services | ForEach-Object {
  try {
    $resp = Invoke-WebRequest -Uri "http://localhost:$($_.port)/actuator/health" -TimeoutSec 2 -ErrorAction SilentlyContinue
    Write-Host "✓ $($_.name) RUNNING"
  } catch {
    Write-Host "⚠ $($_.name) - respuesta lenta o no disponible"
  }
}

Write-Host "`nPara ver logs: Get-Process java | Select Name, Id" -ForegroundColor Gray
Write-Host "Para detener: Get-Process java | Stop-Process -Force" -ForegroundColor Gray
