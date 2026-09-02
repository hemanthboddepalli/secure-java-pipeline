FROM eclipse-temurin:17-jdk-jammy@sha256:400014962ad7224461f945bb1cc3d7d5a1927ce15b8245b72d9cedcda554cd2a AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -ntp dependency:go-offline
COPY src/ src/
COPY docker/HealthCheck.java /healthcheck/HealthCheck.java
RUN ./mvnw -B -ntp package -DskipTests && javac -d /healthcheck /healthcheck/HealthCheck.java

FROM gcr.io/distroless/java17-debian13:nonroot@sha256:cc22e72aa8540b9b87f056a30319c62fa6ebd98d6463f364b162ef9723e1e695
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
