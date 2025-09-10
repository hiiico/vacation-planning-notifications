# Use Java ... as base image
FROM openjdk:17
#FROM ubuntu:latest
LABEL authors="hiiico"

# Copy the Spring Boot JAR file into the container // name urlaubsplanung
#COPY target/vacation-planning-notifications-*.jar app.jar
COPY target/vacation-planning-notifications-*.jar urlaubsplanung-notifications.jar

# define how to run the application
#ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "urlaubsplanung-notifications.jar"]

COPY init-wrapper.sh /docker-entrypoint-initdb.d/
RUN chmod +x /docker-entrypoint-initdb.d/init-wrapper.sh