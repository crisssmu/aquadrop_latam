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
