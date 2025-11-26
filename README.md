# 🌊 AquaDrop LATAM - Plataforma de Distribución de Agua

Plataforma de microservicios para distribución y entrega de agua potable en Latinoamérica con patrón Saga orquestado y compensación automática.

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────────┐
│                         API Gateway (8080)                           │
│                     (OAuth2, Rate Limiting, Routing)                │
└─────────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
        │  Booking     │ │  Payment     │ │  Fleet       │
        │  Service     │ │  Service     │ │  Service     │
        │  (8081)      │ │  (8082)      │ │  (8083)      │
        └──────────────┘ └──────────────┘ └──────────────┘
              │               │               │
              └───────────────┼───────────────┘
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
                PostgreSQL          RabbitMQ
                (Datos)         (Eventos)
```

## 📊 Flujo Saga (Happy Path + Compensación)

```
CASO EXITOSO:
1️⃣  POST /api/bookings
    └─ BookingService: BookingRequestedEvent

2️⃣  PaymentService (recibe evento)
    └─ PaymentAuthorizedEvent

3️⃣  FleetService (recibe evento)
    └─ TankerAssignedEvent

4️⃣  BookingService confirma
    └─ ✅ Booking CONFIRMADO

COMPENSACIÓN (si falla asignación):
3️⃣  FleetService: Sin disponibilidad
    └─ AssignmentFailedEvent

4️⃣  PaymentService: Procesa refund
    └─ RefundIssuedEvent

5️⃣  BookingService cancela
    └─ ✅ Booking CANCELADO + Reembolso
```

## 🚀 Despliegue Local - Opción Rápida

```powershell
# Ejecutar todo con UN COMANDO
.\deploy-local.ps1
```

**Esto levanta automáticamente:**
- ✅ Stack Docker (PostgreSQL, RabbitMQ, Redis, Prometheus, Grafana, Zipkin)
- ✅ Eureka Server (service discovery)
- ✅ API Gateway (routing)
- ✅ BookingService, PaymentService, FleetService
- ✅ Abre automáticamente los dashboards

## 📍 URLs de Acceso

| Servicio | URL |
|----------|-----|
| **API Gateway** | http://localhost:8080 |
| **Eureka Server** | http://localhost:8761 |
| **Grafana** (admin/admin) | http://localhost:3000 |
| **Prometheus** | http://localhost:9090 |
| **Zipkin** (Trazas) | http://localhost:9411 |
| **RabbitMQ** (guest/guest) | http://localhost:15672 |

## 🛠️ Scripts Útiles

| Script | Función |
|--------|---------|
| `deploy-local.ps1` | Levanta TODO con un comando |
| `quick-test.ps1` | Prueba el flujo Saga automáticamente |

## 🧪 Testear la Plataforma

### 1. Crear una Reserva (Happy Path)

```powershell
$body = @{
    zone = "Bogota"
    latitude = 4.7110
    longitude = -74.0055
    volumeLiters = 100
} | ConvertTo-Json

curl -X POST http://localhost:8080/api/bookings `
  -H "Content-Type: application/json" `
  -H "Idempotency-Key: $(New-Guid)" `
  -d $body
```

### 2. Ver el Saga en Acción

**En Zipkin:**
- Ve a http://localhost:9411
- Busca por el `X-Correlation-Id` del response
- Visualiza el flujo completo

**En Grafana:**
- Abre http://localhost:3000
- Dashboard muestra eventos procesados

### 3. Verificar Compensación

- Sin tanqueros disponibles → PaymentService devuelve dinero automáticamente
- Booking se cancela con estado REFUND_ISSUED

## 🔧 Despliegue Manual Paso a Paso

### 1. Levantar infraestructura
```powershell
docker-compose up -d
```

### 2. Compilar servicios
```powershell
.\build-all-services.ps1
```

### 3. Iniciar servicios (en terminales separadas)

**Eureka Server:**
```powershell
cd eureka-server
.\mvnw.cmd spring-boot:run
```

**API Gateway:**
```powershell
cd api-gateway\api-gateway
.\mvnw.cmd spring-boot:run
```

**BookingService, PaymentService, FleetService:**
```powershell
cd booking-service
.\mvnw.cmd spring-boot:run
```

## 📚 Documentación

| Archivo | Descripción |
|---------|------------|
| **README.md** | Este archivo - Visión general |
| **SCRIPTS_GUIDE.md** | Guía completa de todos los scripts |
| **DEPLOYMENT_CHECKLIST.md** | Checklist paso a paso |
| **QUICK_START.md** | Ejemplos de requests rápidas |

## 🛑 Detener Todo

```powershell
# Servicios (Ctrl+C en cada terminal)

# Infraestructura
docker-compose down -v
```

## 🌐 Tecnologías

- Java 21, Spring Boot 3.4+
- PostgreSQL, RabbitMQ, Redis
- Prometheus, Grafana, Zipkin
- Eureka, Keycloak

## 🔧 Troubleshooting

**❌ Error: Docker no está corriendo**
```powershell
# Abre Docker Desktop desde Windows
# O verifica con:
docker ps
```

**❌ Puerto ya está en uso**
```powershell
# Ver qué proceso ocupa el puerto 8080:
netstat -ano | findstr :8080

# Detener el contenedor:
docker-compose down -v
```

**❌ Servicio "connection refused"**
```powershell
# Espera a que Docker esté listo (15-30 segundos)
# O reinicia la infraestructura:
.\service-manager.ps1 -Action restart
```

**❌ Error: mvnw no encontrado**
```powershell
# Usa Maven del sistema o descarga mvnw
# O usa el script de compilación:
.\build-all-services.ps1
```

**❌ Saga no procesa eventos**
```powershell
# Verifica RabbitMQ:
http://localhost:15672 (guest/guest)

# Ve a Connections y Channels para ver conexiones activas
```

## 📞 Soporte

Para reportar problemas:
1. Copia los logs: `docker-compose logs > logs.txt`
2. Ve las trazas en Zipkin: http://localhost:9411
3. Revisa Grafana dashboard para métricas
