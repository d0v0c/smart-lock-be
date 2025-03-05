FROM eclipse-temurin:17.0.14_7-jre-alpine-3.21
WORKDIR /app
COPY ./target/smart-door-lock-be-0.0.1-SNAPSHOT.jar 0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "0.0.1-SNAPSHOT.jar"]