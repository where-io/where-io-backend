FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Xms256m","-Xmx512m","-Xss256k","-XX:+UseG1GC","-XX:MaxMetaspaceSize=128m","-XX:+UseStringDeduplication","-XX:+ExitOnOutOfMemoryError","-jar","app.jar"]
