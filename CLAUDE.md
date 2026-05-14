## Repo: service-cp-caseadmin-case-urn-mapper

Spring Boot service that maps a Case URN to a Case ID by proxying the CP backend, with an in-memory cache to reduce backend load on repeated lookups.

**Pattern**: Stateless proxy with in-memory cache
**Spring Boot version**: 4.0.3 (target 4.0.6+ per upgrade cycle)
**Implements**: `api-cp-caseadmin-case-urn-mapper`

## Infrastructure

| Component | Technology | Purpose |
|---|---|---|
| CP Backend | External HTTP | Source of URN-to-ID mappings |
| WireMock | 3.6.0 (docker) | Backend stub for local dev and API tests |
| In-memory cache | Spring Cache (`CachingConfig`) | Avoids repeat backend calls for same URN |

## Source Structure

```
uk.gov.hmcts.cp/
  Application.java                          @SpringBootApplication
  client/
    CaseUrnMapperClient                     RestTemplate calls to CP backend; sets CJSCPPUID header
    UrnMapperResponse                       Internal DTO mapping backend response
  config/
    AppConfig                               @Bean RestTemplate
    AppPropertiesBackend                    @Value CP_BACKEND_URL
    CachingConfig                           @EnableCaching; configures in-memory cache
  controllers/
    CaseUrnMapperController                 Implements generated API; delegates to service
    GlobalExceptionHandler                  @RestControllerAdvice; maps exceptions to HTTP codes
    RootController                          Returns 200 on GET /
  filters/
    TracingFilter                           Reads/generates X-Correlation-Id; propagates via MDC
  repositories/
    CaseUrnMapperRepository                 @FunctionalInterface; abstraction over cache+backend
    InMemoryCaseUrnMapperRepositoryImpl     In-memory implementation
    CaseUrnCacheService                     Cache read/write; TTL managed by Spring Cache
  services/
    CaseUrnMapperService                    Orchestrates cache lookup → backend call → cache write
  utils/
    EncodeDecodeUtils                       URL-encodes case URNs before forwarding to backend
```

## Environment Variables

| Variable | Purpose | Default |
|---|---|---|
| `CP_BACKEND_URL` | Base URL of the CP backend | `http://localhost` |
| `CJSCPPUID` | User UUID sent as header on all backend calls | `00000000-0000-0000-0000-000000000000` |
| `rpe.AppInsightsInstrumentationKey` | Azure Application Insights key | `00000000-0000-0000-0000-000000000000` |

## Repo-Specific Architecture Rules

- **Cache-aside pattern**: `CaseUrnCacheService` checks cache first; on miss, `CaseUrnMapperClient` calls backend and writes to cache.
- **URN encoding**: `EncodeDecodeUtils` must be applied before passing URN to `CaseUrnMapperClient` — URNs contain slashes and colons that break URL routing.
- **`refresh` query parameter**: When `refresh=true`, skip the cache and force a backend call; client passes this through unchanged.
- **CJSCPPUID header**: `CaseUrnMapperClient` sets `CJSCPPUID` on every backend request. Never remove this header.

## Debugging

| Symptom | Cause / Fix |
|---|---|
| 404 from backend | Check `CP_BACKEND_URL` is set and reachable; verify the case URN is URL-encoded |
| Stale cache returning wrong ID | Force refresh with `?refresh=true`; if persists, restart the service to clear in-memory cache |
| Missing `CJSCPPUID` in backend logs | Check the `CJSCPPUID` env var is set and `CaseUrnMapperClient` is not bypassed |

## Repo-Specific Notes

- `ci-build-publish.yml` is present (in addition to `ci-draft.yml` / `ci-released.yml`) — handles combined build-and-publish in a single workflow run.
- No database; all state is in-memory cache (does not survive restart).
