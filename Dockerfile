# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
# Копируем только pom.xml для кеширования зависимостей
COPY pom.xml .
RUN mvn -q dependency:go-offline
# Теперь копируем исходники и собираем
COPY src ./src
RUN mvn -q -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/testing-system.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
