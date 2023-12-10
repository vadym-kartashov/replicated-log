# Use a base image with Java (adjust the version as needed)
FROM openjdk:17-slim

# Copy the built JAR file into the image
COPY target/*.jar app.jar

# Command to run the application
ENTRYPOINT ["java","-jar","/app.jar"]