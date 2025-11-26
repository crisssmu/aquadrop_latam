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
