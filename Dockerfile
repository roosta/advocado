FROM openjdk:8-alpine

COPY target/uberjar/advocado.jar /advocado/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/advocado/app.jar"]
