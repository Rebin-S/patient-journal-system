
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven descriptor to cache dependencies separately
COPY pom.xml .

# Download dependencies
RUN mvn -q -DskipTests dependency:go-offline

# Copy source code into the container
COPY src ./src

# Build the application JAR file
RUN mvn -q -DskipTests package

# Runtime stage
# Uses a lightweight Java runtime to run the built application

FROM eclipse-temurin:21-jre

# Set working directory for runtime container
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*jar /app/app.jar

# Set the active Spring profile to "docker"
ENV SPRING_PROFILES_ACTIVE=docker

# Expose backend port
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
