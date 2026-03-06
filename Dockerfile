# syntax=docker/dockerfile:1.6

########## Stage 1: Build jar ##########
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy POM trước để cache dependency
COPY pom.xml .
# Tải dependency (dùng cache BuildKit cho nhanh)
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests dependency:go-offline

# Copy source và build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests clean package

########## Stage 2: Runtime (JRE) ##########
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy jar vừa build (ăn mọi tên SNAPSHOT)
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-Xms512m","-Xmx2048m","-jar","app.jar"]
