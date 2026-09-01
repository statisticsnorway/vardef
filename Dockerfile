FROM eclipse-temurin:25-jdk-jammy@sha256:89565961a318534f01c971c7b1d030e60713c66995b887c94010cef938dbc53e AS builder
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
