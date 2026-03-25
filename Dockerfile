# build/Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x gradlew
# show (and download if it has not been done before) dependencies (will be cached by Docker)
RUN ./gradlew dependencies --no-daemon

COPY src ./src

#download (if necessary) dependencies and build jar with all these dependencies and compiled sources
# -x test means to avoid run tests
RUN ./gradlew bootJar --no-daemon -x test

#only JRE, without compiler => less image size
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]