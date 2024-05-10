FROM amazoncorretto:21-al2023-headless

COPY vardef-0.1.jar /usr/share/vardef/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/usr/share/vardef/vardef-0.1.jar"]