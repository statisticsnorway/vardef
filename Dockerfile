FROM gcr.io/distroless/java25-debian13:latest@sha256:19c68cb513e9210500b4e9312582d4d0928b2ca8949e25c9ae0ca28065d732b8
WORKDIR /app
COPY build/libs/*-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
