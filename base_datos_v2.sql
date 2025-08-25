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
-----------------------------------------
-- Tabla: estado_producto
CREATE TABLE estado_producto (
    id_estado_producto SERIAL PRIMARY KEY,
    estado_producto VARCHAR(50) NOT NULL
);

-- Tabla: estado_pedido
CREATE TABLE estado_pedido (
    id_estado_pedido SERIAL PRIMARY KEY,
    estado_pedido VARCHAR(50) NOT NULL
);

-- Tabla: categoria
CREATE TABLE categoria (
    id_categoria SERIAL PRIMARY KEY,
    categoria VARCHAR(100) NOT NULL
);


-- Tabla: cliente
CREATE TABLE cliente (
    id_cliente SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
	telefono VARCHAR(20),
    direccion TEXT,
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);


-- Tabla: producto
CREATE TABLE producto (
    id_producto SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio NUMERIC(10,2) NOT NULL,
    stock INT NOT NULL,
    categoria INT NOT NULL,
    fecha_agregado DATE DEFAULT CURRENT_DATE,
    caducidad DATE,
    estado_producto INT NOT NULL,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria) REFERENCES categoria(id_categoria),
    CONSTRAINT fk_producto_estado FOREIGN KEY (estado_producto) REFERENCES estado_producto(id_estado_producto)
);

-- Tabla: pedido
CREATE TABLE pedido (
    id_pedido SERIAL PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado_pedido INT NOT NULL,
    tipo_entrega VARCHAR(50),
    metodo_pago VARCHAR(50),
    codigo VARCHAR(50) UNIQUE,
    total NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_pedido_estado FOREIGN KEY (estado_pedido) REFERENCES estado_pedido(id_estado_pedido)
);

-- Tabla: detalle_pedido
CREATE TABLE detalle_pedido (
    id_detalle SERIAL PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario NUMERIC(10,2) NOT NULL,
    subtotal NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);



CREATE USER programador WITH PASSWORD 'admin123';

GRANT ALL PRIVILEGES ON DATABASE tienda_db TO programador;


SELECT rolname, rolcanlogin, rolsuper
FROM pg_roles;


-- Permitir conexión
GRANT CONNECT ON DATABASE tienda_db TO programador;

-- Dar permisos sobre el esquema público
\c tienda_db
GRANT USAGE ON SCHEMA public TO programador;
GRANT CREATE ON SCHEMA public TO programador;

-- Dar permisos para trabajar con tablas existentes y futuras
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO programador;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO programador;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO programador;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO programador;


