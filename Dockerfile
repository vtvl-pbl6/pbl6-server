### Java Dockerfile ###

## Build stage ##
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn install -DskipTests=true

## Run stage ##
FROM alpine:3.19
RUN adduser -D pbl6
RUN apk add openjdk21-jre # add jdk
WORKDIR /run
COPY --from=build /app/target/*.jar /run/pbl6/app.jar
RUN chown -R pbl6:pbl6 /run
USER pbl6
EXPOSE 8080
ENTRYPOINT java -jar /run/pbl6/app.jar