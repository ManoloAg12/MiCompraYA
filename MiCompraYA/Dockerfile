# Etapa de compilación
FROM maven:3.8.5-openjdk-17 AS build
COPY . .

# --- CAMBIO IMPORTANTE AQUÍ ---
# Entramos a la carpeta donde está el pom.xml
WORKDIR /MiCompraYA

RUN mvn clean package -DskipTests -B

# Etapa de ejecución
FROM openjdk:17.0.1-jdk-slim
# Ajustamos la ruta para buscar el jar dentro de la carpeta compilada
COPY --from=build /MiCompraYA/target/MiCompraYA-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
