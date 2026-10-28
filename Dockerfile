FROM eclipse-temurin:22-jdk-jammy@sha256:d8e6ba486df17bf758888d2b1b608133d1eedca8daf69d3fc6bf78d8be81e07e AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -ntp dependency:go-offline
COPY src/ src/
COPY docker/HealthCheck.java /healthcheck/HealthCheck.java
RUN ./mvnw -B -ntp package -DskipTests && javac -d /healthcheck /healthcheck/HealthCheck.java

FROM gcr.io/distroless/java17-debian13:nonroot@sha256:2dcb2b21efab7e9250945985e0b0aab99ec7835db96a7bbc68edd1a0ad2e51ff
ARG VCS_URL="local"
ARG VCS_REF="unknown"
LABEL org.opencontainers.image.title="wafer-inspection-service" \
      org.opencontainers.image.description="Secure Java delivery pipeline demo" \
      org.opencontainers.image.source="${VCS_URL}" \
      org.opencontainers.image.revision="${VCS_REF}" \
      org.opencontainers.image.licenses="Apache-2.0"
WORKDIR /app
COPY --from=build --chown=nonroot:nonroot /workspace/target/wafer-inspection-service-*.jar /app/app.jar
COPY --from=build --chown=nonroot:nonroot /healthcheck /healthcheck
USER nonroot:nonroot
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD ["/usr/bin/java", "-cp", "/healthcheck", "HealthCheck"]
ENTRYPOINT ["/usr/bin/java", "-jar", "/app/app.jar"]
