FROM maven:3.9-sapmachine-17 AS build
WORKDIR /app
COPY . /app/
RUN mvn clean package


FROM openjdk:17
LABEL authors="pro"
# EXPOSE 8080
VOLUME /tmp
ARG JAR_FILE=target/SASS-CAPSTONE1-0.0.1-SNAPSHOT.jar
COPY --from=build /app/${JAR_FILE} /app/sass-capstone1-server.jar
#COPY .env /app/.env
ENTRYPOINT ["java", "-jar", "/app/sass-capstone1-server.jar"]