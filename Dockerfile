FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
COPY src/main/resources/application.yml application.yml
ENTRYPOINT ["java", "-jar", "app.jar"]