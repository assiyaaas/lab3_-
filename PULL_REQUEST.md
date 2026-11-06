Title: CSS-?? REST contract and Spring Boot platform

<!-- Replace CSS-?? with the real story ID from your Jira board -->

## What
- Week 9: `POST /api/v1/bank-cases` creates one case through the Lab 3 `register`; `GET /api/v1/bank-cases/{id}` reads it by UUID (`CaseService.require`, read only)
- `handler`: `CaseController`, `CreateCase` (`@NotBlank`, `@Size(max = 120)`), `CaseResponse`, `ApiError`, `ApiErrors` (`@RestControllerAdvice`)
- `domain`: `CaseView` (what the service returns instead of the entity), `MissingCase`
- 201 + `Location`, 400 `VALIDATION`, 404 `NOT_FOUND`, 409 `DUPLICATE`, 415 `UNSUPPORTED`, 500 `INTERNAL`; `X-Correlation-Id` echoed or minted
- springdoc 2.8.17: create lists 201, 400, 409; get lists 200, 400, 404
- Week 10: `application.yaml` + `application-dev.yaml` + `application-prod.yaml`, `spring.profiles.default: dev`
- `app.downstream` (base URL + `2s` timeout) bound by `@ConfigurationProperties` into `DownstreamProperties`, injected into `LoggingCaseNotifier`
- Actuator: only `health` and `info`; health details hidden
- Tests: `CaseControllerWebTest` (`@WebMvcTest`), `ApiIntegrationTest` (HTTP on PostgreSQL), `DownstreamPropertiesTest` (dev profile)
- On a running app the second POST of the same business key is 409 and `SELECT count(*)` for that key is still 1 (also asserted in `ApiIntegrationTest`)

## PR checklist
- [ ] Story IDs in the title
- [ ] Same product as Labs 1–3
- [ ] `POST` and `GET` under `/api/v1/bank-cases`. Path id is a UUID.
- [ ] 201 + `Location`, 400 `VALIDATION` (field name), 404 `NOT_FOUND`, 409 `DUPLICATE`
- [ ] Second POST of the same business key is 409 and the row count stays 1
- [ ] Error JSON is only `code`, `message`, `correlationId`. Stack traces stay off the wire
- [ ] Response type is not the entity. Domain has no Spring and no `jakarta.validation`
- [ ] `/swagger-ui.html` shows 201, 400, 409 on create and 200, 404 on get
- [ ] `@WebMvcTest` covers 201, 400, 404, 409, and the correlation header
- [ ] Datasource and `app.downstream` (base URL + timeout) come from yaml
- [ ] `dev` profile is localhost `css`/`css`. `prod` uses env vars. No cloud password in git
- [ ] `GET /actuator/health` is 200. `env` and `heapdump` are 404
- [ ] Lab 3 tests still green. `mvn -q verify` is green
- [ ] No Spring Security, no Dockerfile, no `target/` in git