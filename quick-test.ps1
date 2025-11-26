#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Test rápido de la Saga: Booking → Payment → Fleet → Confirmation
    
.DESCRIPTION
    Ejecuta requests para verificar el flujo Saga completo en AquaDrop LATAM
    
.EXAMPLE
    .\quick-test.ps1
    
.NOTES
    Requiere que todos los servicios estén corriendo en localhost
#>

param(
    [ValidateSet("happy", "compensation", "both")]
    [string]$TestType = "both"
)

# Colores
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

function Write-Result {
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

function Test-ServiceHealth {
    Write-Title "🔍 Verificando Servicios"
    
    $services = @(
        @{ Name = "API Gateway"; URL = "http://localhost:8080/actuator/health" },
        @{ Name = "Booking Service"; URL = "http://localhost:8085/actuator/health" },
        @{ Name = "Payment Service"; URL = "http://localhost:8082/actuator/health" },
        @{ Name = "Fleet Service"; URL = "http://localhost:8083/actuator/health" }
    )
    
    foreach ($service in $services) {
        try {
            $response = Invoke-RestMethod -Uri $service.URL -Method Get -ErrorAction Stop
            if ($response.status -eq "UP") {
                Write-Result "$($service.Name) ✓ Activo" "success"
            } else {
                Write-Result "$($service.Name) ⚠ Estado: $($response.status)" "warning"
            }
        }
        catch {
            Write-Result "$($service.Name) ✗ No disponible" "error"
            Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
            exit 1
        }
    }
}

function Test-HappyPath {
    Write-Title "🎯 TEST 1: HAPPY PATH (Booking exitoso)"
    
    $bookingId = $null
    $correlationId = [guid]::NewGuid().ToString()
    
    Write-Result "Creando nueva reserva..." "info"
    
    $payload = @{
        zone = "Bogota"
        latitude = 4.7110
        longitude = -74.0055
        volumeLiters = 50
        userSub = 1
        priorityTag = 1
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" `
            -Method Post `
            -ContentType "application/json" `
            -Headers @{ 
                "Idempotency-Key" = $correlationId
                "X-Correlation-Id" = $correlationId
            } `
            -Body $payload
        
        $bookingId = $response.id
        Write-Result "✓ Booking creado: $bookingId" "success"
        Write-Result "  Correlation-ID: $correlationId" "info"
        
        # Esperar a que Saga procese
        Write-Result "Esperando procesamiento de Saga (10s)..." "warning"
        Start-Sleep -Seconds 10
        
        # Obtener estado
        $booking = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/$bookingId" `
            -Method Get
        
        Write-Result "Estado final: $($booking.status)" "success"
        
        if ($booking.status -eq "CONFIRMED") {
            Write-Result "✓ SAGA EXITOSA: Reserva confirmada" "success"
        }
        else {
            Write-Result "⚠ Estado inesperado: $($booking.status)" "warning"
        }
    }
    catch {
        Write-Result "✗ Error: $($_.Exception.Message)" "error"
    }
}

function Test-Compensation {
    Write-Title "🔄 TEST 2: COMPENSACIÓN (Falla de asignación)"
    
    $correlationId = [guid]::NewGuid().ToString()
    
    Write-Result "Este test requiere que NO haya tanqueros disponibles..." "warning"
    Write-Result "Si hay tanqueros, la Saga completará normalmente." "warning"
    
    Write-Result "Creando reserva (espera compensación)..." "info"
    
    $payload = @{
        zone = "Medellin"
        latitude = 6.2442
        longitude = -75.5812
        volumeLiters = 200
        userSub = 2
        priorityTag = 2
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" `
            -Method Post `
            -ContentType "application/json" `
            -Headers @{ 
                "Idempotency-Key" = $correlationId
                "X-Correlation-Id" = $correlationId
            } `
            -Body $payload
        
        $bookingId = $response.id
        Write-Result "✓ Booking creado: $bookingId" "success"
        
        # Esperar compensación
        Write-Result "Esperando compensación (15s)..." "warning"
        Start-Sleep -Seconds 15
        
        # Obtener estado
        $booking = Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/$bookingId" `
            -Method Get
        
        Write-Result "Estado final: $($booking.status)" "success"
        
        if ($booking.status -eq "REFUNDED" -or $booking.status -eq "CANCELLED") {
            Write-Result "✓ COMPENSACIÓN EJECUTADA: Booking reembolsado" "success"
        }
    }
    catch {
        Write-Result "✗ Error: $($_.Exception.Message)" "error"
    }
}

function Show-Tracing {
    Write-Title "🔗 VER TRAZAS EN ZIPKIN"
    
    Write-Host @"
Abre Zipkin en tu navegador:
  → http://localhost:9411

Pasos:
  1. Busca por el Service: "booking-service"
  2. Haz clic en "Find Traces"
  3. Verás el flujo completo:
     ✓ booking-service → payment-service → fleet-service → booking-service

Emojis en logs:
  📨 = Evento recibido
  ✅ = Evento procesado exitosamente
  ❌ = Error
  🔄 = Compensación iniciada
  📤 = Evento emitido
"@ -ForegroundColor Green
}

# MAIN
try {
    switch ($TestType) {
        "happy" {
            Test-ServiceHealth
            Test-HappyPath
            Show-Tracing
        }
        "compensation" {
            Test-ServiceHealth
            Test-Compensation
            Show-Tracing
        }
        "both" {
            Test-ServiceHealth
            Test-HappyPath
            Write-Result "Esperando 5 segundos entre tests..." "warning"
            Start-Sleep -Seconds 5
            Test-Compensation
            Show-Tracing
        }
    }
    
    Write-Host "`n✓ Tests completados`n" -ForegroundColor Green
}
catch {
    Write-Result "ERROR FATAL: $_" "error"
    exit 1
}
