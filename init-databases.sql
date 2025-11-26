-- AquaDrop LATAM Microservices - Database Initialization
-- Este script crea todas las bases de datos necesarias para el sistema

-- Conectar a PostgreSQL como superuser
\c postgres

-- Crear bases de datos para cada microservicio
CREATE DATABASE aquadrop_bookings
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

CREATE DATABASE aquadrop_payments
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

CREATE DATABASE aquadrop_fleet
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

CREATE DATABASE aquadrop_notifications
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

CREATE DATABASE aquadrop_keycloak
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TEMPLATE = template0;

-- Crear usuario para aplicaciones
CREATE USER aquadrop WITH PASSWORD 'aquadrop' CREATEDB;

-- Otorgar permisos en todas las bases de datos
GRANT ALL PRIVILEGES ON DATABASE aquadrop_bookings TO aquadrop;
GRANT ALL PRIVILEGES ON DATABASE aquadrop_payments TO aquadrop;
GRANT ALL PRIVILEGES ON DATABASE aquadrop_fleet TO aquadrop;
GRANT ALL PRIVILEGES ON DATABASE aquadrop_notifications TO aquadrop;
GRANT ALL PRIVILEGES ON DATABASE aquadrop_keycloak TO aquadrop;

-- Conectar a cada base de datos y crear schemas
\c aquadrop_bookings
CREATE SCHEMA IF NOT EXISTS public;
ALTER SCHEMA public OWNER TO aquadrop;
GRANT ALL ON SCHEMA public TO aquadrop;
GRANT ALL ON ALL TABLES IN SCHEMA public TO aquadrop;
GRANT CREATE ON SCHEMA public TO aquadrop;

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

\c aquadrop_payments
CREATE SCHEMA IF NOT EXISTS public;
ALTER SCHEMA public OWNER TO aquadrop;
GRANT ALL ON SCHEMA public TO aquadrop;
GRANT ALL ON ALL TABLES IN SCHEMA public TO aquadrop;
GRANT CREATE ON SCHEMA public TO aquadrop;

-- Crear tablas para payment-service
CREATE TABLE IF NOT EXISTS payment_intents (
    id VARCHAR(255) PRIMARY KEY,
    booking_id VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    zone VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    customer_id VARCHAR(255),
    idempotency_key VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS charges (
    id VARCHAR(255) PRIMARY KEY,
    payment_intent_id VARCHAR(255) NOT NULL REFERENCES payment_intents(id),
    provider VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    provider_reference VARCHAR(255) NOT NULL,
    authorized_at TIMESTAMP,
    captured_at TIMESTAMP,
    authorized_amount DECIMAL(10,2) NOT NULL,
    captured_amount DECIMAL(10,2),
    failure_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subsidies (
    id VARCHAR(255) PRIMARY KEY,
    zone VARCHAR(100) NOT NULL,
    rule VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    is_percentage BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    max_uses INTEGER,
    uses_count INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS refunds (
    id VARCHAR(255) PRIMARY KEY,
    charge_id VARCHAR(255) NOT NULL REFERENCES charges(id),
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason VARCHAR(500),
    provider_reference VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Dar permisos sobre las tablas creadas
GRANT ALL ON ALL TABLES IN SCHEMA public TO aquadrop;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO aquadrop;

\c aquadrop_fleet
CREATE SCHEMA IF NOT EXISTS public;
ALTER SCHEMA public OWNER TO aquadrop;
GRANT ALL ON SCHEMA public TO aquadrop;
GRANT ALL ON ALL TABLES IN SCHEMA public TO aquadrop;
GRANT CREATE ON SCHEMA public TO aquadrop;

\c aquadrop_notifications
CREATE SCHEMA IF NOT EXISTS public;
ALTER SCHEMA public OWNER TO aquadrop;
GRANT ALL ON SCHEMA public TO aquadrop;
GRANT ALL ON ALL TABLES IN SCHEMA public TO aquadrop;
GRANT CREATE ON SCHEMA public TO aquadrop;

\c aquadrop_keycloak
CREATE SCHEMA IF NOT EXISTS public;
ALTER SCHEMA public OWNER TO aquadrop;
GRANT ALL ON SCHEMA public TO aquadrop;
GRANT ALL ON ALL TABLES IN SCHEMA public TO aquadrop;
GRANT CREATE ON SCHEMA public TO aquadrop;
