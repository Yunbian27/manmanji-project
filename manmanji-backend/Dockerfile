FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY app.jar app.jar
RUN mkdir -p /data
VOLUME /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
