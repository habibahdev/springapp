FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace/backend

COPY backend/pom.xml pom.xml

COPY backend/src src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package


FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --create-home spring

COPY --from=build /workspace/backend/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
