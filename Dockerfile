# FILE: Dockerfile
# Etapa 1: compilamos el proyecto con Maven dentro de un contenedor temporal
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: imagen final, solo con Tomcat 10 (compatible con jakarta.servlet) y el .war ya compilado
FROM tomcat:10.1-jdk17
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/impostor-party.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]