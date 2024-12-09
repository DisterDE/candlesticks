# Use the Gradle image with JDK 21 to build the project
FROM gradle:jdk21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project into the container
COPY . .

# Build the project using the Gradle wrapper
RUN gradle build

# Use a slimmer JDK image for deploying the application
FROM openjdk:21-jdk-slim

# Set the working directory for the application in the container
WORKDIR /opt/app

# Copy the jar from the build stage
COPY --from=build /app/build/libs/candlesticks-all.jar .

# Expose the application’s port
EXPOSE 9000

# Command to run the application
CMD ["java", "-jar", "candlesticks-all.jar"]