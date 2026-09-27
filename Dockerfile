FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline
COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 docucatalog
COPY --from=build /workspace/target/docucatalog-1.0.0.jar app.jar
USER docucatalog
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
