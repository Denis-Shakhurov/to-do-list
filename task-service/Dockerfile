FROM bellsoft/liberica-openjre-alpine:21 AS layers
WORKDIR /application
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]