# Paso 1: Compilar la aplicación usando Maven y Java 17 (Imagen oficial actualizada)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Movemos el contenido de 'main/src' a la raíz 'src' por tu estructura de carpetas
RUN mv main/src ./src || true

RUN mvn clean package -DskipTests

# Paso 2: Ejecutar la aplicación usando Eclipse Temurin 17 (Ligera y disponible)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]