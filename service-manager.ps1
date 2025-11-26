#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Herramienta para gestionar el ciclo de vida de los servicios
    
.DESCRIPTION
    Pausar, resumir, limpiar y verificar estado de infraestructura
    
.EXAMPLE
    .\service-manager.ps1 -Action status
    .\service-manager.ps1 -Action logs -Service payment-service
    .\service-manager.ps1 -Action clean
#>

param(
    [ValidateSet("status", "logs", "clean", "restart", "rebuild")]
    [string]$Action = "status",
    
    [ValidateSet("all", "postgres", "rabbitmq", "redis", "prometheus", "grafana", "zipkin", 
                 "booking-service", "payment-service", "fleet-service")]
    [string]$Service = "all"
)

$colors = @{
    success = "Green"
    warning = "Yellow"
    error = "Red"
    info = "Cyan"
}

function Write-Title {
    param([string]$Text)
    Write-Host "`n$('=' * 70)" -ForegroundColor $colors.info
    Write-Host "  $Text" -ForegroundColor $colors.info
    Write-Host "$('=' * 70)`n" -ForegroundColor $colors.info
}

function Write-Status {
    param([string]$Text, [string]$Type = "info")
    $symbol = @{
        success = "✓"
        error = "✗"
        info = "➜"
        warning = "⚠"
    }[$Type]
    
    Write-Host "$symbol " -ForegroundColor $colors[$Type] -NoNewline
    Write-Host $Text -ForegroundColor $colors[$Type]
}

function Show-InfrastructureStatus {
    Write-Title "🔍 Estado de Infraestructura"
    
    $containers = docker ps -a --format "table {{.Names}}\t{{.Status}}"
    
    if ($containers) {
        Write-Host $containers -ForegroundColor Gray
    }
    else {
        Write-Status "No hay contenedores activos" "warning"
    }
    
    Write-Status "Verificando servicios..." "info"
    
    $checks = @(
        @{ Name = "PostgreSQL"; Port = 5432 },
        @{ Name = "RabbitMQ"; Port = 5672 },
        @{ Name = "Redis"; Port = 6379 },
        @{ Name = "Prometheus"; Port = 9090 },
        @{ Name = "Grafana"; Port = 3000 },
        @{ Name = "Zipkin"; Port = 9411 }
    )
    
    foreach ($check in $checks) {
        try {
            $socket = New-Object System.Net.Sockets.TcpClient
            $socket.Connect("localhost", $check.Port)
            if ($socket.Connected) {
                Write-Status "$($check.Name) ✓ Accesible (puerto $($check.Port))" "success"
            }
            $socket.Close()
        }
        catch {
            Write-Status "$($check.Name) ✗ No responde (puerto $($check.Port))" "error"
        }
    }
}

function Show-Logs {
    param([string]$ServiceName)
    
    Write-Title "📋 Logs de $ServiceName"
    
    if ($ServiceName -eq "all") {
        docker-compose logs -f --tail=50
    }
    elseif ($ServiceName.Contains("-service")) {
        Write-Status "Los microservicios se ejecutan en terminales separadas" "warning"
        Write-Status "Usa Zipkin (http://localhost:9411) para trazas distribuidas" "info"
    }
    else {
        docker-compose logs -f --tail=50 $ServiceName
    }
}

function Clean-Environment {
    Write-Title "🧹 Limpiando Ambiente"
    
    Write-Status "Deteniendo y eliminando contenedores..." "warning"
    docker-compose down -v
    
    if ($LASTEXITCODE -eq 0) {
        Write-Status "✓ Infraestructura limpiada" "success"
    }
    else {
        Write-Status "✗ Error durante limpieza" "error"
    }
    
    Write-Status "Limpiando archivos compilados..." "info"
    $services = @("booking-service", "payment-service", "fleet-service", "eureka-server")
    
    foreach ($svc in $services) {
        if (Test-Path "$svc/target") {
            Remove-Item -Path "$svc/target" -Recurse -Force -ErrorAction SilentlyContinue
            Write-Status "  ✓ $svc/target" "success"
        }
    }
}

function Restart-Infrastructure {
    Write-Title "🔄 Reiniciando Infraestructura"
    
    Write-Status "Deteniendo servicios..." "warning"
    docker-compose down
    
    Write-Status "Iniciando servicios..." "info"
    docker-compose up -d
    
    Write-Status "Esperando 10 segundos..." "warning"
    Start-Sleep -Seconds 10
    
    Write-Status "✓ Infraestructura reiniciada" "success"
}

function Rebuild-Services {
    Write-Title "🔨 Recompilando Servicios"
    
    $services = @(
        "eureka-server",
        "api-gateway/api-gateway",
        "booking-service",
        "payment-service",
        "fleet-service"
    )
    
    foreach ($svc in $services) {
        Write-Status "Compilando $svc..." "info"
        
        Push-Location $svc
        
        if (Test-Path "mvnw.cmd") {
            & .\mvnw.cmd clean package -DskipTests -q
        }
        else {
            & .\mvnw clean package -DskipTests -q
        }
        
        if ($LASTEXITCODE -eq 0) {
            Write-Status "  ✓ $svc compilado" "success"
        }
        else {
            Write-Status "  ✗ Error en $svc" "error"
        }
        
        Pop-Location
    }
}

function Show-Menu {
    Write-Host @"

Comandos disponibles:

  Status:
    .\service-manager.ps1 -Action status                      # Ver estado general
    
  Logs:
    .\service-manager.ps1 -Action logs -Service all           # Todos los logs
    .\service-manager.ps1 -Action logs -Service rabbitmq      # Solo RabbitMQ
    .\service-manager.ps1 -Action logs -Service postgres      # Solo PostgreSQL
    
  Limpiar:
    .\service-manager.ps1 -Action clean                       # Eliminar contenedores
    
  Reiniciar:
    .\service-manager.ps1 -Action restart                     # Reiniciar docker-compose
    
  Recompilar:
    .\service-manager.ps1 -Action rebuild                     # Compilar todos los servicios

@ -ForegroundColor Cyan
}

# MAIN
try {
    switch ($Action) {
        "status" {
            Show-InfrastructureStatus
        }
        "logs" {
            Show-Logs -ServiceName $Service
        }
        "clean" {
            Clean-Environment
        }
        "restart" {
            Restart-Infrastructure
        }
        "rebuild" {
            Rebuild-Services
        }
        default {
            Show-Menu
        }
    }
}
catch {
    Write-Status "ERROR: $_" "error"
    exit 1
}
