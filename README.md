# 🛒 MiCompraYa - Sistema de Gestión Integral para Supermercados

**MiCompraYa** es una solución de software robusta diseñada para la administración completa de un supermercado mediano. El sistema centraliza tanto la experiencia de compra del cliente (e-commerce) como la gestión operativa del negocio (inventario, alertas y administración), garantizando la integridad de datos mediante **Spring Boot** y **Hibernate**.

## 🌟 Características Principales

### 👥 Módulo de Cliente
* **Gestión de Pedidos:** Realización de compras, visualización de historial y cancelación de pedidos en curso.
* **Validación de Entrega:** Sistema de seguridad mediante **Código Único** o **Escaneo QR** para confirmar la entrega de productos.
* **Notificaciones Transaccionales:** Envío automático de correos sobre el estado del pedido:
    * *Confirmado*
    * *Por vencer* (alerta de recogida)
    * *Vencido* (lógica automática de cancelación)

### 🏢 Módulo Administrativo e Inventario
* **Control de Stock en Tiempo Real:** Seguimiento detallado del flujo de productos.
* **Tipos de Movimientos de Inventario:** Auditoría precisa clasificando las salidas/entradas por:
    * ✅ Venta
    * 🔄 Devolución
    * ⚠️ Producto Dañado
    * 🍽️ Consumo Interno
    * 📦 Compra a Proveedores
* **Dashboard Analítico:** Visualización gráfica de métricas clave del negocio.
* **Sistema de Alertas Tempranas:**
    * Notificación automática por correo a Gerentes y Supervisores cuando un producto alcanza el stock mínimo o se agota.

## 🛠️ Stack Tecnológico

**Backend:**
* **Lenguaje:** Java 17+
* **Framework:** Spring Boot (MVC, Security, Data JPA, Mail)
* **ORM:** Hibernate (Manejo robusto de relaciones y validaciones)
* **Base de Datos:** PostgreSQL

**Frontend:**
* **Estilos:** Tailwind CSS
* **Motor de Plantillas:** Thymeleaf

## 📋 Prerrequisitos de Instalación

1.  **Java JDK 17** o superior.
2.  **PostgreSQL** instalado y ejecutándose.
3.  **Maven** (para gestión de dependencias).

## 🚀 Despliegue Local

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/ManoloAg12/MiCompraYA.git](https://github.com/ManoloAg12/MiCompraYA.git)
    cd MiCompraYA
    ```

2.  **Configuración de Base de Datos:**
    Crea una base de datos en PostgreSQL llamada `micopraya_db`.
    
    Edita el archivo `src/main/resources/application.properties` con tus credenciales:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/micopraya_db
    spring.datasource.username=tu_usuario
    spring.datasource.password=tu_contraseña
    
    # Configuración de Email (Gmail SMTP ejemplo)
    spring.mail.host=smtp.gmail.com
    spring.mail.username=tu_correo@gmail.com
    spring.mail.password=tu_app_password
    ```

3.  **Ejecutar la aplicación:**
    ```bash
    ./mvnw spring-boot:run
    ```

4.  **Acceso:**
    Navega a `http://localhost:8080`.

## 🛡️ Seguridad y Validaciones

El sistema implementa validaciones estrictas tanto a nivel de controlador como de entidad (Hibernate Validator) para asegurar que:
* No se generen pedidos con stock insuficiente.
* Los estados de los pedidos sigan un flujo lógico (no se puede cancelar un pedido ya entregado).
* Los correos electrónicos y datos de usuario tengan el formato correcto.
