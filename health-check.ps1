#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Script de verificación de salud de todos los microservicios de AquaDrop LATAM
    
.DESCRIPTION
    Verifica el estado de salud (health) de todos los servicios usando sus endpoints /actuator/health
    
.EXAMPLE
    .\health-check.ps1
    
.NOTES
    Requiere: PowerShell 5.0+, curl o Invoke-WebRequest
#>

# Colores para output
$colors = @{
    success = "Green"
    warning = "Yellow"
    error = "Red"
    info = "Cyan"
    debug = "Gray"
}

function Write-Title {
    param([string]$Text)
    Write-Host "`n$('=' * 70)" -ForegroundColor $colors.info
    Write-Host "  $Text" -ForegroundColor $colors.info
    Write-Host "$('=' * 70)`n" -ForegroundColor $colors.info
}

function Write-Status {
    param(
        [string]$Service,
        [string]$Status,
        [string]$Details = ""
    )
    
    $statusColor = if ($Status -eq "UP") { $colors.success } else { $colors.error }
    $statusSymbol = if ($Status -eq "UP") { "✓" } else { "✗" }
    
    Write-Host "$statusSymbol " -ForegroundColor $statusColor -NoNewline
    Write-Host "$Service " -ForegroundColor White -NoNewline
    Write-Host "... " -ForegroundColor $colors.debug -NoNewline
    Write-Host "$Status" -ForegroundColor $statusColor
    
    if ($Details) {
        Write-Host "  └─ $Details" -ForegroundColor $colors.debug
    }
}

function Check-Service {
    param(
        [string]$ServiceName,
        [string]$Url,
        [int]$TimeoutSeconds = 5
    )
    
    try {
        $response = Invoke-WebRequest -Uri $Url `
            -Method GET `
            -TimeoutSec $TimeoutSeconds `
            -SkipHttpErrorCheck
        
        # Manejar la respuesta como texto
        if ($response.Content -is [byte[]]) {
            $jsonText = [System.Text.Encoding]::UTF8.GetString($response.Content)
        } else {
            $jsonText = $response.Content
        }
        
        $content = $jsonText | ConvertFrom-Json
        $status = $content.status
        
        $details = ""
        if ($content.components) {
            $dbStatus = $content.components.db.status
            $rabbitStatus = $content.components.rabbit.status
            
            $detailParts = @()
            if ($dbStatus) { $detailParts += "DB: $dbStatus" }
            if ($rabbitStatus) { $detailParts += "RabbitMQ: $rabbitStatus" }
            
            if ($detailParts.Count -gt 0) {
                $details = $detailParts -join " | "
            }
        }
        
        Write-Status -Service $ServiceName -Status $status -Details $details
        return @{ Status = $status; Healthy = ($status -eq "UP") }
    }
    catch {
        Write-Status -Service $ServiceName -Status "DOWN" -Details "Error: $($_.Exception.Message)"
        return @{ Status = "DOWN"; Healthy = $false }
    }
}

function Get-ServiceSummary {
    param([hashtable[]]$HealthResults)
    
    $healthy = @($HealthResults | Where-Object { $_.Healthy -eq $true }).Count
    $total = $HealthResults.Count
    $percentage = if ($total -gt 0) { [math]::Round(($healthy / $total) * 100, 0) } else { 0 }
    
    return @{
        Total = $total
        Healthy = $healthy
        Unhealthy = $total - $healthy
        Percentage = $percentage
    }
}

# Definir los servicios a verificar
$services = @(
    @{ Name = "🔍 Eureka Server"; Url = "http://localhost:8761/actuator/health"; Port = 8761 },
    @{ Name = "🚪 API Gateway"; Url = "http://localhost:8080/actuator/health"; Port = 8080 },
    @{ Name = "📦 Booking Service"; Url = "http://localhost:8085/actuator/health"; Port = 8085 },
    @{ Name = "💳 Payment Service"; Url = "http://localhost:8082/actuator/health"; Port = 8082 },
    @{ Name = "🚗 Fleet Service"; Url = "http://localhost:8083/actuator/health"; Port = 8083 },
    @{ Name = "📧 Notification Service"; Url = "http://localhost:8084/actuator/health"; Port = 8084 }
)

# Verificar si hay servicios ejecutándose en los puertos
Write-Title "🏥 VERIFICACIÓN DE SALUD - AquaDrop LATAM"

$healthResults = @()

foreach ($service in $services) {
    $result = Check-Service -ServiceName $service.Name -Url $service.Url -TimeoutSeconds 15
    $healthResults += $result
}

# Resumen
Write-Title "📊 RESUMEN DE SALUD"

$summary = Get-ServiceSummary -HealthResults $healthResults

Write-Host "Servicios Saludables: " -ForegroundColor White -NoNewline
Write-Host "$($summary.Healthy)/$($summary.Total)" -ForegroundColor $colors.success

Write-Host "Servicios No Disponibles: " -ForegroundColor White -NoNewline
Write-Host "$($summary.Unhealthy)/$($summary.Total)" -ForegroundColor $(if ($summary.Unhealthy -gt 0) { $colors.error } else { $colors.success })

Write-Host "Porcentaje de Disponibilidad: " -ForegroundColor White -NoNewline
$percentageColor = if ($summary.Percentage -eq 100) { $colors.success } elseif ($summary.Percentage -ge 75) { $colors.warning } else { $colors.error }
Write-Host "$($summary.Percentage)%" -ForegroundColor $percentageColor

Write-Host ""

# Recomendaciones
Write-Title "💡 RECOMENDACIONES"

if ($summary.Unhealthy -eq 0) {
    Write-Host "✓ Todos los servicios están saludables y funcionando correctamente." -ForegroundColor $colors.success
}
else {
    Write-Host "⚠ Se detectaron servicios no disponibles:" -ForegroundColor $colors.warning
    Write-Host ""
    
    $unhealthyServices = $healthResults | Where-Object { $_.Healthy -eq $false }
    foreach ($service in $unhealthyServices) {
        Write-Host "  • Verifica que el servicio está corriendo en el puerto configurado" -ForegroundColor $colors.warning
        Write-Host "  • Revisa los logs del servicio para más información" -ForegroundColor $colors.warning
    }
    
    Write-Host ""
    Write-Host "Para relanzar los servicios, ejecuta:" -ForegroundColor $colors.info
    Write-Host "  .\deploy-local.ps1 up" -ForegroundColor $colors.debug
}

Write-Host ""

# Endpoint adicional de información
Write-Title "ℹ️  ENDPOINTS ADICIONALES"

Write-Host "Eureka Dashboard:" -ForegroundColor $colors.info
Write-Host "  http://localhost:8761" -ForegroundColor $colors.debug

Write-Host ""
Write-Host "Grafana Monitoring:" -ForegroundColor $colors.info
Write-Host "  http://localhost:3000 (admin/admin)" -ForegroundColor $colors.debug

Write-Host ""
Write-Host "Prometheus Metrics:" -ForegroundColor $colors.info
Write-Host "  http://localhost:9090" -ForegroundColor $colors.debug

Write-Host ""
Write-Host "Zipkin Tracing:" -ForegroundColor $colors.info
Write-Host "  http://localhost:9411" -ForegroundColor $colors.debug

Write-Host ""
Write-Host "RabbitMQ Management:" -ForegroundColor $colors.info
Write-Host "  http://localhost:15672 (guest/guest)" -ForegroundColor $colors.debug

Write-Host ""

# Códigos de salida
if ($summary.Percentage -eq 100) {
    exit 0
}
elseif ($summary.Percentage -ge 75) {
    exit 1
}
else {
    exit 2
}
