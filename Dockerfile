FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle* settings.gradle* gradle.properties* ./

RUN chmod +x ./gradlew

COPY src src
RUN ./gradlew shadowJar --no-daemon

FROM gcr.io/distroless/java25-debian13@sha256:227c14f972231e8db2ff9bfe0fe5c91fc80b689b35bafdce3f8d6b1204087a30
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
