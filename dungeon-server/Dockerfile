# Multi-stage build: compile with JDK, run with JRE
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml ./
RUN apk add --no-cache maven && mvn dependency:resolve
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 4010
ENTRYPOINT ["java", "-jar", "app.jar"]
