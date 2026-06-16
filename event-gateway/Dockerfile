FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
ARG JAR_FILE=target/event-gateway-1.0.0.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
