FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
COPY pom.xml /build/
WORKDIR /build/
RUN mvn dependency:go-offline dependency:resolve-plugins
COPY /src /build/src
RUN mvn package -DskipTests

FROM eclipse-temurin:21.0.4_7-jre-alpine AS runtime
COPY --from=build /build/target/*.jar ./app-resource-proc.jar
EXPOSE 9292
CMD ["java", "-jar", "./app-resource-proc.jar"]