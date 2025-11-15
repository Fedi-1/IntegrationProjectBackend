# Use official Eclipse Temurin JDK 21 image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory inside container
WORKDIR /app

# Copy Maven-built JAR into the container
COPY target/IntegrationProjectBackend-0.0.1-SNAPSHOT.jar app.jar

# Expose port your Spring Boot app runs on (default 8080)
EXPOSE 5069

# Run the Spring Boot JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
