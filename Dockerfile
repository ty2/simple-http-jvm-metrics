#
# Build stage
#
FROM maven:3.9.4-eclipse-temurin-21 AS build
COPY src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package

#
# Package stage
#
FROM eclipse-temurin:21_35-jre-jammy
ARG JAR_FILE=/app/target/java-simple-http-1.0-SNAPSHOT.jar
COPY --from=build $JAR_FILE /app/runner.jar
EXPOSE 9999
ENTRYPOINT java -jar /app/runner.jar