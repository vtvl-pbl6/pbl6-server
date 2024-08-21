# Description: Run server by maven command (required maven 3.6.1 version and java 21 or higher)
# Usage: sh scripts/server.shs

# Required a application-dev.yml file in resources folder
mvn clean
mvn package -DskipTests=true
mvn test -Dspring.profiles.active=dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev