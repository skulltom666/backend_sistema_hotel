# Etapa de construcción con Maven y JDK 17
FROM maven:3.9.8-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Etapa de ejecución con JRE 17
FROM openjdk:17
COPY --from=build /target/<sistemahotelero>-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]