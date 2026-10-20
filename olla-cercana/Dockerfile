# ------------------------------------------------------------------------------
# ETAPA 1: Construcción con Maven y JDK 21
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Aprovechar caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilar código fuente sin volver a correr tests (los tests ya corren en el pipeline)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# ETAPA 2: Imagen final liviana de ejecución con JRE 21 Alpine
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no privilegiado por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el jar compilado desde la etapa anterior
COPY --from=builder /app/target/olla-cercana-*.jar app.jar

# Puerto donde corre Spring Boot
EXPOSE 8080

# Variables de entorno JVM recomendadas para contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]