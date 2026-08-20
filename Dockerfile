FROM eclipse-temurin:25-jdk-jammy@sha256:c0e0be82836e51fc78c06db5ab2b4d440f1ce1672f4c4589c9c9ec2f3f432477 AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle* settings.gradle* gradle.properties* ./

RUN chmod +x ./gradlew

COPY src src
RUN ./gradlew shadowJar --no-daemon

FROM gcr.io/distroless/java25-debian13:latest@sha256:34f47c4e018bf205c771ad12a1c8c06d0801a69c0a5566677f0e31f166c89793
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
