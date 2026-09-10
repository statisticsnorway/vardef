FROM gcr.io/distroless/java25-debian13:latest@sha256:34f47c4e018bf205c771ad12a1c8c06d0801a69c0a5566677f0e31f166c89793
WORKDIR /app
COPY build/libs/*-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
