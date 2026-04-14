# ============================================================
# STAGE 1 : BUILD
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
COPY src/ ./src/

RUN mvn clean package -DskipTests

# ============================================================
# STAGE 2 : RUNTIME
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/najma-server.jar ./server.jar

EXPOSE 12345

ENTRYPOINT ["java", "-jar", "server.jar"]