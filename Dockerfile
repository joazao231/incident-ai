FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /workspace/target/incident-ai-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseSerialGC","-XX:MaxRAMPercentage=40.0","-XX:InitialRAMPercentage=10.0","-XX:MaxMetaspaceSize=128m","-XX:ReservedCodeCacheSize=48m","-XX:TieredStopAtLevel=1","-Xss512k","-Dspring.main.lazy-initialization=true","-Dspring.jmx.enabled=false","-jar","app.jar"]

