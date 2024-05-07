# Build 
FROM openjdk:21-jdk-alpine as builder
WORKDIR gradle/src
COPY . .
RUN ./gradlew build

FROM openjdk:21-jre-alpine
WORKDIR gradle/src
COPY --from=builder gradle/src/build/libs/*.jar ./app.jar

ENV PORT=8080
EXPOSE $PORT
WORKDIR app
CMD ["app.jar"]
