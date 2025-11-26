FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/payment-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
