FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src
COPY files ./files
RUN ./mvnw clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/files ./files
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]