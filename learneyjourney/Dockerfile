# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# Maven:
RUN chmod +x mvnw \
    && ./mvnw -Dmaven.test.skip=true package
# Gradle (instead): RUN ./gradlew bootJar

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]