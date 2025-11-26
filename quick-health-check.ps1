#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Script rápido de verificación de salud con timeout reducido
    
.DESCRIPTION
    Verifica rápidamente el estado de todos los servicios (timeout de 1 segundo)
    
.EXAMPLE
    .\quick-health-check.ps1
#>

$services = @(
    @{ Name = "🔍 Eureka"; Port = 8761 },
    @{ Name = "🚪 API Gateway"; Port = 8080 },
    @{ Name = "📦 Booking"; Port = 8085 },
    @{ Name = "💳 Payment"; Port = 8082 },
    @{ Name = "🚗 Fleet"; Port = 8083 },
    @{ Name = "📧 Notification"; Port = 8084 }
)

Write-Host "`n🏥 Verificando servicios...`n" -ForegroundColor Cyan

$healthy = 0
$total = $services.Count

foreach ($service in $services) {
    $url = "http://localhost:$($service.Port)/actuator/health"
    
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 1 -UseBasicParsing -ErrorAction Stop
        $status = ($response.Content | ConvertFrom-Json).status
        
        if ($status -eq "UP") {
            Write-Host "✓ $($service.Name) - UP" -ForegroundColor Green
            $healthy++
        } else {
            Write-Host "✗ $($service.Name) - $status" -ForegroundColor Red
        }
    }
    catch {
        Write-Host "✗ $($service.Name) - DOWN" -ForegroundColor Red
    }
}

Write-Host "`n📊 Resultado: $healthy/$total servicios saludables`n" -ForegroundColor Cyan
