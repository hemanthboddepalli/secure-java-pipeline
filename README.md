# Secure Java Delivery Pipeline

A small ASML-themed Spring Boot service that demonstrates shift-left security from commit to a hardened local Kubernetes deployment.

```text
Commit -> Compile + JUnit + coverage + SAST -> SCA -> Docker build
       -> Container scan + SBOM -> GHCR publication -> Helm deployment
```

## API

```bash
./mvnw spring-boot:run

curl -i http://localhost:8080/api/v1/inspections \
  -H 'Content-Type: application/json' \
  -d '{"waferId":"WAFER-42","defectCount":7}'

curl http://localhost:8080/api/v1/inspections
curl http://localhost:8080/actuator/health
```

The default policy is PASS for 0-5 defects, REVIEW for 6-20, and REJECT above 20. Override it with `INSPECTION_PASS_MAX` and `INSPECTION_REVIEW_MAX`.

## Local security pipeline

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp dependency:tree -Dincludes=org.apache.commons:commons-text
./mvnw -B -ntp org.owasp:dependency-check-maven:check

docker build -t wafer-inspection:local .
trivy image --format json --output trivy-image.json wafer-inspection:local
trivy image --format cyclonedx --output image-sbom.cdx.json wafer-inspection:local
trivy image --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 wafer-inspection:local
```

`NVD_API_KEY` is optional but strongly recommended because OWASP Dependency-Check downloads substantially faster with one.

## Vulnerability-to-remediation story

The first commit deliberately pins `commons-text:1.9`. Maven selects that direct dependency over the newer transitive version from Commons Configuration. OWASP Dependency-Check detects CVE-2022-42889 and stops the pipeline before a container can be published. The next commit upgrades and converges the graph; its hashes and GitHub run links are recorded after both runs complete.

## Local Kubernetes with Helm

```bash
kind create cluster --name secure-java-pipeline
kind load docker-image wafer-inspection:local --name secure-java-pipeline
helm lint charts/wafer-inspection
helm template wafer charts/wafer-inspection >/dev/null
helm upgrade --install wafer charts/wafer-inspection \
  --set image.repository=wafer-inspection \
  --set image.tag=local \
  --set image.pullPolicy=Never
kubectl rollout status deployment/wafer-wafer-inspection --timeout=120s
helm test wafer
kubectl port-forward service/wafer-wafer-inspection 8080:8080
```

Upgrade or roll back with `helm upgrade wafer charts/wafer-inspection ...` and `helm rollback wafer`. Troubleshoot with `kubectl get pods`, `kubectl describe pod`, and `kubectl logs deployment/wafer-wafer-inspection`. Clean up with `helm uninstall wafer` and `kind delete cluster --name secure-java-pipeline`.

## GitHub setup

The workflow needs only the repository `GITHUB_TOKEN`; add `NVD_API_KEY` as an Actions secret when available. Pull requests build and scan without publishing. Passing pushes to `main` publish the exact scanned image as `sha-<commit>` and `latest` under `ghcr.io/<owner>/secure-java-pipeline` and attach provenance.

