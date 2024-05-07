# Build 
FROM openjdk:21-jdk-alpine as builder
WORKDIR gradle/src
COPY . .
RUN ./gradlew build

# Run
FROM openjdk:21-jre-alpine
WORKDIR /src
COPY --from=builder gradle/src/build/libs/*.jar ./app.jar

RUN apk add --update --no-cache fontconfig ttf-dejavu && rm -rf /var/cache/apk/*

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
