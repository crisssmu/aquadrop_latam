#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Script de despliegue automático de AquaDrop LATAM en local
    
.DESCRIPTION
    Levanta infraestructura, compila y ejecuta todos los servicios con monitoreo
    
.EXAMPLE
    .\deploy-local.ps1
    
.NOTES
    Requiere: Docker Desktop en ejecución, Java 21+, Maven
#>

param(
    [ValidateSet("up", "down", "rebuild")]
    [string]$Action = "up"
)

# Colores para output
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

function Write-Step {
    param([string]$Text, [string]$Type = "info")
    $symbol = @{
        info = "➜"
        success = "✓"
        error = "✗"
        warning = "⚠"
    }[$Type]
    
    Write-Host "$symbol " -ForegroundColor $colors[$Type] -NoNewline
    Write-Host $Text -ForegroundColor $colors[$Type]
}

function Check-Docker {
    Write-Step "Verificando Docker..." "info"
    
    try {
        $dockerVersion = docker --version
        Write-Step "Docker instalado: $dockerVersion" "success"
        
        # Verificar si está corriendo
        docker ps >$null 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Step "ERROR: Docker no está corriendo. Abre Docker Desktop." "error"
            exit 1
        }
        Write-Step "Docker está corriendo" "success"
    }
    catch {
        Write-Step "ERROR: Docker no está instalado" "error"
        exit 1
    }
}

function Check-Java {
    Write-Step "Verificando Java..." "info"
    
    try {
        $javaVersion = java -version 2>&1 | Select-String "version"
        Write-Step "Java instalado: $javaVersion" "success"
    }
    catch {
        Write-Step "ERROR: Java no está instalado" "error"
        exit 1
    }
}

function Check-Maven {
    Write-Step "Verificando Maven..." "info"
    
    # Usar mvnw.cmd de los proyectos (Maven Wrapper)
    $mvnwPath = Get-ChildItem -Path "." -Filter "mvnw.cmd" -Recurse | Select-Object -First 1
    if ($mvnwPath) {
        Write-Step "Maven Wrapper encontrado: $($mvnwPath.Directory)" "success"
        return $true
    }
    
    Write-Step "Maven Wrapper no encontrado. Intentando descargar Maven..." "warning"
    
    # Intentar descargar Maven
    $mavenHome = Install-Maven
    if ($mavenHome) {
        $env:MAVEN_HOME = $mavenHome
        $env:PATH = "$mavenHome\bin;$env:PATH"
        Write-Step "Maven instalado: $mavenHome" "success"
        return $true
    }
    
    Write-Step "ERROR: No se pudo instalar Maven" "error"
    return $false
}

function Install-Maven {
    Write-Step "Descargando Apache Maven 3.9.11..." "info"
    
    $mavenVersion = "3.9.11"
    $mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/apache-maven-$mavenVersion-bin.zip"
    $installDir = Join-Path $env:TEMP "maven-$mavenVersion"
    $zipPath = "$installDir.zip"
    
    # Crear directorio temporal si no existe
    if (-Not (Test-Path $installDir)) {
        New-Item -ItemType Directory -Path $installDir -Force | Out-Null
    }
    
    try {
        # Descargar Maven
        Write-Step "Descargando desde: $mavenUrl" "info"
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $mavenUrl -OutFile $zipPath -UseBasicParsing -ErrorAction Stop
        
        # Extraer ZIP
        Write-Step "Extrayendo Maven..." "info"
        Expand-Archive -Path $zipPath -DestinationPath $installDir -Force
        
        # Encontrar la carpeta de Maven
        $mavenFolder = Get-ChildItem -Path $installDir -Directory -Filter "apache-maven-*" | Select-Object -First 1
        
        if ($mavenFolder) {
            $mavenPath = $mavenFolder.FullName
            Write-Step "Maven instalado en: $mavenPath" "success"
            
            # Limpiar archivo ZIP
            Remove-Item -Path $zipPath -Force -ErrorAction SilentlyContinue
            
            return $mavenPath
        }
        else {
            Write-Step "No se encontró la carpeta de Maven después de extraer" "error"
            return $null
        }
    }
    catch {
        Write-Step "Error al descargar Maven: $_" "error"
        return $null
    }
}


function Start-Infrastructure {
    Write-Title "🐳 Iniciando Infraestructura Docker"
    
    Write-Step "Levantando servicios: PostgreSQL, RabbitMQ, Redis, Prometheus, Grafana, Zipkin..." "info"
    
    if (-Not (Test-Path "docker-compose.yml")) {
        Write-Step "ERROR: docker-compose.yml no encontrado" "error"
        exit 1
    }
    
    # No hacer build, solo levantar servicios existentes
    docker-compose up -d --no-build 2>$null
    
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Intentando sin --no-build..." "warning"
        docker-compose down 2>$null
        docker-compose up -d 2>&1 | Where-Object { $_ -notmatch "COPY target" }
    }
    
    Write-Step "Esperando 15 segundos para que servicios estén listos..." "warning"
    Start-Sleep -Seconds 15
    
    # Inicializar bases de datos
    Initialize-Databases
    
    Write-Step "✓ Infraestructura lista" "success"
}

function Initialize-Databases {
    Write-Step "Inicializando bases de datos PostgreSQL..." "info"
    
    # Esperar a que PostgreSQL esté listo
    $retries = 0
    while ($retries -lt 10) {
        try {
            $result = docker exec aquadrop-postgres pg_isready -U postgres 2>&1
            if ($result -match "accepting") {
                break
            }
        }
        catch { }
        $retries++
        Start-Sleep -Seconds 2
    }
    
    if ($retries -eq 10) {
        Write-Step "PostgreSQL no está listo después de 20 segundos" "warning"
        return
    }
    
    # Crear bases de datos y usuario
    $sqlCreateDbs = @"
CREATE DATABASE aquadrop_bookings;
CREATE DATABASE aquadrop_payments;
CREATE DATABASE aquadrop_fleet;
CREATE DATABASE aquadrop_notifications;
CREATE USER aquadrop WITH PASSWORD 'aquadrop' CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE aquadrop_bookings TO aquadrop;
GRANT ALL PRIVILEGES ON DATABASE aquadrop_payments TO aquadrop;
GRANT ALL PRIVILEGES ON DATABASE aquadrop_fleet TO aquadrop;
GRANT ALL PRIVILEGES ON DATABASE aquadrop_notifications TO aquadrop;
"@
    
    # Ejecutar SQL para crear bases de datos
    $sqlCreateDbs | docker exec -i aquadrop-postgres psql -U postgres 2>&1 | Select-String "CREATE|ERROR" | Where-Object { $_ -notmatch "already exists" }
    
    # Crear tablas y datos para booking-service
    $sqlBookingTables = @"
-- Dar permisos en el schema public
GRANT ALL ON SCHEMA public TO aquadrop;
GRANT CREATE ON SCHEMA public TO aquadrop;
ALTER SCHEMA public OWNER TO aquadrop;

-- Crear tablas para booking-service
CREATE TABLE IF NOT EXISTS priority_tag (
    id SERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    score INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS addresses (
    id SERIAL PRIMARY KEY,
    address VARCHAR(255),
    zone VARCHAR(100),
    user_sub VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS bookings (
    id SERIAL PRIMARY KEY,
    user_sub INTEGER,
    volume_liters FLOAT,
    status VARCHAR(50),
    price_estimate FLOAT,
    amount FLOAT,
    address_id INTEGER REFERENCES addresses(id),
    priority_tag_id INTEGER REFERENCES priority_tag(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar datos iniciales de priority_tag
INSERT INTO priority_tag (id, type, score) VALUES (1, 'DEFAULT', 1) ON CONFLICT (id) DO NOTHING;
INSERT INTO priority_tag (id, type, score) VALUES (2, 'HOSPITAL', 3) ON CONFLICT (id) DO NOTHING;
INSERT INTO priority_tag (id, type, score) VALUES (3, 'ESCUELA', 2) ON CONFLICT (id) DO NOTHING;
INSERT INTO priority_tag (id, type, score) VALUES (4, 'VULNERABLE', 4) ON CONFLICT (id) DO NOTHING;

-- Dar permisos sobre las tablas creadas
GRANT ALL ON ALL TABLES IN SCHEMA public TO aquadrop;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO aquadrop;
"@
    
    # Ejecutar SQL para crear tablas en aquadrop_bookings
    $sqlBookingTables | docker exec -i aquadrop-postgres psql -U postgres -d aquadrop_bookings 2>&1 | Out-Null
    
    Write-Step "✓ Bases de datos creadas" "success"
}

function Ensure-MavenWrapper {
    # Asegurar que todos los servicios tengan la carpeta .mvn
    $services = @(
        "eureka-server",
        "api-gateway/api-gateway",
        "booking-service",
        "payment-service",
        "fleet-service",
        "notification-service"
    )
    
    # Encontrar un servicio que tenga .mvn
    $sourceMvn = $null
    foreach ($service in $services) {
        if (Test-Path "$service\.mvn") {
            $sourceMvn = "$service\.mvn"
            break
        }
    }
    
    if ($null -eq $sourceMvn) {
        Write-Step "ERROR: No se encontró carpeta .mvn en ningún servicio" "error"
        return $false
    }
    
    # Copiar a servicios que no la tengan
    foreach ($service in $services) {
        if (-Not (Test-Path "$service\.mvn")) {
            Copy-Item -Path $sourceMvn -Destination "$service\.mvn" -Recurse -Force -ErrorAction SilentlyContinue
            if ($?) {
                Write-Step ".mvn copiado a: $service" "info"
            }
        }
    }
    
    return $true
}

function Build-Services {
    Write-Title "🔨 Compilando Servicios"
    
    # Asegurar que .mvn exista en todos los servicios
    if (-Not (Ensure-MavenWrapper)) {
        Write-Step "ERROR: No se pudo copiar .mvn a los servicios" "error"
        exit 1
    }
    
    # Establecer JAVA_HOME correctamente
    if (-Not $env:JAVA_HOME -or -Not (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
        # Buscar JDK en ubicaciones conocidas
        $javaPaths = @(
            "C:\Program Files\Java\jdk-21",
            "C:\Program Files\Java\openjdk-21",
            "${env:ProgramFiles}\Java\jdk-21",
            "${env:JAVA_HOME}"
        )
        
        $javaHome = $null
        foreach ($path in $javaPaths) {
            if (Test-Path "$path\bin\javac.exe") {
                $javaHome = $path
                break
            }
        }
        
        if ($javaHome) {
            $env:JAVA_HOME = $javaHome
            Write-Step "JAVA_HOME establecido: $env:JAVA_HOME" "success"
        } else {
            Write-Step "ERROR: No se encontró JDK válido" "error"
            exit 1
        }
    }
    
    $services = @(
        "eureka-server",
        "api-gateway/api-gateway",
        "booking-service",
        "payment-service",
        "fleet-service",
        "notification-service"
    )
    
    foreach ($service in $services) {
        Write-Step "Compilando: $service" "info"
        
        Push-Location $service -ErrorAction SilentlyContinue
        
        # Intentar compilar con mvn o mvnw
        $compiled = $false
        
        # Primero intentar con mvn del sistema
        if (Get-Command mvn -ErrorAction SilentlyContinue) {
            Write-Step "  Usando: mvn (sistema)" "info"
            & mvn clean package -DskipTests -q 2>&1 | Out-Null
            if ($LASTEXITCODE -eq 0) {
                $compiled = $true
            }
        }
        
        # Si falla, intentar con mvnw.cmd
        if (-Not $compiled -and (Test-Path "mvnw.cmd")) {
            Write-Step "  Usando: mvnw.cmd (wrapper)" "info"
            & .\mvnw.cmd clean package -DskipTests -q 2>&1 | Out-Null
            if ($LASTEXITCODE -eq 0) {
                $compiled = $true
            }
        }
        
        # Si falla, intentar con mvnw
        if (-Not $compiled -and (Test-Path "mvnw")) {
            Write-Step "  Usando: mvnw (wrapper)" "info"
            & .\mvnw clean package -DskipTests -q 2>&1 | Out-Null
            if ($LASTEXITCODE -eq 0) {
                $compiled = $true
            }
        }
        
        # Si sigue fallando y Maven está en PATH, intentar nuevamente
        if (-Not $compiled -and (Get-Command mvn -ErrorAction SilentlyContinue)) {
            Write-Step "  Reintentando con Maven (modo verbose)..." "warning"
            & mvn clean package -DskipTests 2>&1 | Select-Object -Last 20
        }
        
        if ($compiled) {
            Write-Step "✓ $service compilado" "success"
        }
        else {
            Write-Step "⚠ Saltando $service (compilación falló)" "warning"
        }
        
        Pop-Location
    }
}

function Start-EurekaServer {
    Write-Title "🔍 Iniciando Eureka Server (8761)"
    
    Write-Step "Abriendo nueva terminal..." "info"
    
    $jarPath = Join-Path $PSScriptRoot "eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar"
    
    if (-Not (Test-Path $jarPath)) {
        Write-Step "JAR no encontrado: $jarPath" "error"
        return
    }
    
    $script = @"
`$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
Write-Host '🔍 Eureka Server iniciándose...' -ForegroundColor Cyan
java -jar '$jarPath'
"@
    
    Start-Process pwsh -ArgumentList @(
        "-NoExit",
        "-Command",
        $script
    ) -WindowStyle Normal
    
    Write-Step "Esperando a que Eureka esté disponible..." "warning"
    Start-Sleep -Seconds 10
}

function Start-ApiGateway {
    Write-Title "🚪 Iniciando API Gateway (8080)"
    
    Write-Step "Abriendo nueva terminal..." "info"
    
    $jarPath = Join-Path $PSScriptRoot "api-gateway/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar"
    
    if (-Not (Test-Path $jarPath)) {
        Write-Step "JAR no encontrado: $jarPath" "error"
        return
    }
    
    $script = @"
`$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
Write-Host '🚪 API Gateway iniciándose...' -ForegroundColor Cyan
java -jar '$jarPath'
"@
    
    Start-Process pwsh -ArgumentList @(
        "-NoExit",
        "-Command",
        $script
    ) -WindowStyle Normal
    
    Write-Step "Esperando a que API Gateway esté disponible..." "warning"
    Start-Sleep -Seconds 10
}

function Start-Microservices {
    Write-Title "⚙️  Iniciando Microservicios en Background"
    
    $services = @(
        @{ Name = "BookingService"; Jar = "booking-service/target/bookingService-0.0.1-SNAPSHOT.jar"; Port = "8085" },
        @{ Name = "PaymentService"; Jar = "payment-service/target/payment-service-0.0.1-SNAPSHOT.jar"; Port = "8082" },
        @{ Name = "FleetService"; Jar = "fleet-service/target/fleet-service-0.0.1-SNAPSHOT.jar"; Port = "8083" },
        @{ Name = "NotificationService"; Jar = "notification-service/target/notification-service-0.0.1-SNAPSHOT.jar"; Port = "8084" }
    )
    
    foreach ($service in $services) {
        Write-Step "Iniciando $($service.Name) ($($service.Port))..." "info"
        
        $jarPath = Join-Path $PSScriptRoot $service.Jar
        
        if (-Not (Test-Path $jarPath)) {
            Write-Step "JAR no encontrado: $jarPath" "warning"
            continue
        }
        
        $script = @"
`$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
`$env:SERVER_PORT = '$($service.Port)'
Write-Host '⚙️  $($service.Name) iniciándose en puerto $($service.Port)...' -ForegroundColor Green
java -jar '$jarPath'
"@
        
        Start-Process pwsh -ArgumentList @(
            "-NoExit",
            "-Command",
            $script
        ) -WindowStyle Normal
        
        Write-Step "✓ $($service.Name) en background" "success"
        Start-Sleep -Seconds 5
    }
}

function Show-Summary {
    Write-Title "✅ DESPLIEGUE COMPLETADO"
    
    Write-Host @"
📍 SERVICIOS DISPONIBLES:

  🚪 API Gateway:     http://localhost:8080
  📦 Booking Service: http://localhost:8085
  💳 Payment Service: http://localhost:8082
  🚗 Fleet Service:   http://localhost:8083
  📧 Notification Svc:http://localhost:8084
  🔍 Eureka Server:   http://localhost:8761

📊 MONITOREO:

  📈 Grafana:         http://localhost:3000 (admin/admin)
  🔍 Prometheus:      http://localhost:9090
  🔗 Zipkin Tracing:  http://localhost:9411
  📨 RabbitMQ:        http://localhost:15672 (guest/guest)

🧪 PRUEBAS:

  Crear booking (ejemplo):
    curl -X POST http://localhost:8080/api/bookings `
      -H "Content-Type: application/json" `
      -H "Idempotency-Key: test-123" `
      -d '{
        "zone": "Bogota",
        "latitude": 4.7110,
        "longitude": -74.0055,
        "volumeLiters": 100
      }'

🛑 PARA DETENER:
  - Cierra todas las terminales (Ctrl+C)
  - docker-compose down -v

📚 DOCUMENTACIÓN:
  - Lee: README.md
  - Ver logs en: docker-compose logs -f
"@ -ForegroundColor Green
    
    Write-Host ""
}

function Stop-Environment {
    Write-Title "🛑 Deteniendo AquaDrop LATAM"
    
    Write-Step "Deteniendo infraestructura Docker..." "warning"
    docker-compose down -v
    
    Write-Step "✓ Ambiente detenido" "success"
    Write-Step "Cierra las terminales de servicios manualmente (Ctrl+C)" "warning"
}

# MAIN FLOW
try {
    switch ($Action) {
        "up" {
            Check-Docker
            Check-Java
            Check-Maven
            Start-Infrastructure
            Build-Services
            Start-EurekaServer
            Start-Sleep -Seconds 5
            Start-ApiGateway
            Start-Sleep -Seconds 5
            Start-Microservices
            Show-Summary
        }
        "down" {
            Stop-Environment
        }
        "rebuild" {
            Write-Title "🔨 Recompilando sin levantar servicios"
            Build-Services
            Write-Step "✓ Todos los servicios compilados" "success"
        }
    }
}
catch {
    Write-Step "ERROR: $_" "error"
    exit 1
}
