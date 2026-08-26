# Digests are pinned in the remediated commit after image verification.
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -ntp dependency:go-offline
COPY src/ src/
COPY docker/HealthCheck.java /healthcheck/HealthCheck.java
RUN ./mvnw -B -ntp package -DskipTests && javac -d /healthcheck /healthcheck/HealthCheck.java

FROM gcr.io/distroless/java17-debian12:nonroot
LABEL org.opencontainers.image.title="wafer-inspection-service" \
      org.opencontainers.image.description="Secure Java delivery pipeline demo" \
      org.opencontainers.image.source="https://github.com/hemanth/secure-java-pipeline"
WORKDIR /app
COPY --from=build --chown=nonroot:nonroot /workspace/target/wafer-inspection-service-*.jar /app/app.jar
COPY --from=build --chown=nonroot:nonroot /healthcheck /healthcheck
USER nonroot:nonroot
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD ["/usr/bin/java", "-cp", "/healthcheck", "HealthCheck"]
ENTRYPOINT ["/usr/bin/java", "-jar", "/app/app.jar"]

