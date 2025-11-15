# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven configuration and source code
COPY pom.xml .
COPY src ./src

# Install Maven, build the app, then remove Maven to save space
RUN apt-get update && apt-get install -y maven \
    && mvn clean package -DskipTests \
    && apt-get remove -y maven \
    && rm -rf /var/lib/apt/lists/*

# Expose the port Spring Boot runs on
EXPOSE 8080

# Command to run your backend
CMD ["java", "-jar", "target/IntegrationProjectBackend-0.0.1-SNAPSHOT.jar"]
