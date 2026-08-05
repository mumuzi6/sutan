# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY sutan-backend/pom.xml sutan-backend/
COPY sutan-mcp-server/pom.xml sutan-mcp-server/
COPY sutan-backend/src sutan-backend/src
COPY sutan-mcp-server/src sutan-mcp-server/src
RUN mvn -pl sutan-backend -am clean package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/sutan-backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
