FROM amazoncorretto:21-al2023-headless

COPY ./build/libs/vardef-all.jar /usr/share/vardef/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/usr/share/vardef/vardef-all.jar"]