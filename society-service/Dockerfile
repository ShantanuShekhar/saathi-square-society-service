# Step 1: Build JAR using Maven + JDK 17 (builder stage)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the project source and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Lightweight runtime image
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/society-service.jar app.jar

# Expose the Spring Boot port
EXPOSE 1011

ENV SPRING_PROFILES_ACTIVE=docker


# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
