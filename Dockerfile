# Build stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached layer)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests

# Run stage (smaller image)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy only the JAR from build stage
COPY --from=build /app/target/IntegrationProjectBackend-0.0.1-SNAPSHOT.jar app.jar

# Expose port your Spring Boot app runs on
EXPOSE 5069

# Run the Spring Boot JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
