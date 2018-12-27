FROM maven:3.6.0-jdk-8-alpine AS DownloadDependencies
LABEL author="Anderson Marques"
WORKDIR /app
COPY pom.xml .
RUN [ "mvn", "clean", "validate", "dependency:resolve"]

FROM DownloadDependencies AS TestAndBuild
WORKDIR /app
COPY src src
RUN [ "mvn", "test", "package", "dependency:resolve" ]

FROM java:openjdk-8-alpine AS Distribution
WORKDIR /app
COPY --from=TestAndBuild app/target/app.jar  /app

FROM Distribution AS Application
CMD [ "java", "-jar", "app.jar" ]
