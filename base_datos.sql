CREATE DATABASE tienda_db;

-- Tabla de estados
CREATE TABLE estados (
    id_estado SERIAL PRIMARY KEY,
    estado VARCHAR(50) NOT NULL
);

-- Tabla de roles
CREATE TABLE roles (
    id_rol SERIAL PRIMARY KEY,
    rol VARCHAR(50) NOT NULL
);

-- Tabla de usuarios
CREATE TABLE usuarios (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    fecha_registro DATE DEFAULT CURRENT_DATE,
    id_estado INT NOT NULL,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES roles (id_rol),
    CONSTRAINT fk_usuario_estado FOREIGN KEY (id_estado) REFERENCES estados (id_estado)
);

CREATE USER programador WITH PASSWORD 'admin123';

GRANT ALL PRIVILEGES ON DATABASE tienda_db TO programador;




