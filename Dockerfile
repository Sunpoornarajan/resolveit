# Stage 1: Build application with Maven and Eclipse Temurin JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy POM and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable jar
COPY src ./src
RUN mvn clean package -DskipTests=false

# Stage 2: Runtime image with lightweight JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user for security
RUN groupadd -r resolveit && useradd -r -g resolveit resolveit

# Copy jar from build stage
COPY --from=build /app/target/resolveit-1.0.0.jar app.jar

# Configuration environment variables with safe defaults
ENV PORT=8080 \
    DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=resolveit_db \
    DB_USERNAME=root \
    DB_PASSWORD=""

USER resolveit
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
