FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/bookingService-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085

ENV JAVA_OPTS="-Xmx256m -Xms128m"

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
