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
trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 .
./mvnw -B -ntp org.owasp:dependency-check-maven:check

docker build -t wafer-inspection:local .
trivy image --format json --output trivy-image.json wafer-inspection:local
trivy image --format cyclonedx --output image-sbom.cdx.json wafer-inspection:local
trivy image --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 wafer-inspection:local
```

Trivy filesystem scanning is the always-on dependency gate. OWASP Dependency-Check also runs in CI when the `NVD_API_KEY` repository secret is configured; without a key, importing the complete NVD feed can take over an hour and is deliberately skipped rather than making every delivery nondeterministic.

## Vulnerability-to-remediation story

The vulnerable baseline is commit `1a03836`: it deliberately pins `commons-text:1.9`. Maven selects that direct dependency over `1.11.0` from Commons Configuration:

```text
+- commons-configuration2:2.10.1
|  \- (commons-text:1.11.0 - omitted for conflict with 1.9)
\- commons-text:1.9
```

Trivy—and OWASP Dependency-Check when an NVD key is configured—detects CVE-2022-42889 and stops the pipeline before a container can be published. The remediation commit upgrades Commons Configuration to 2.15.1, manages Commons Text at 1.15.0, and enables Maven Enforcer's dependency-convergence rule. Use `git show vulnerable-demo` and `git show remediated-demo` to compare the two states.

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
