FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle* settings.gradle* gradle.properties* ./

RUN chmod +x ./gradlew

COPY src src
RUN ./gradlew shadowJar --no-daemon

FROM gcr.io/distroless/java25-debian13@sha256:3777aeaf78927b8417e2a17f1d6dd20cdb454ea69c7d02fa3f12143d13c0c13a
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
