---
name: write-unit-tests
description: Write unit and integration tests for a new or changed reactividad API endpoint (router route, handler, service method, or repository query), following this repo's existing Mockito/StepVerifier/WebTestClient/Testcontainers conventions. Use after adding or modifying anything under RouterConfig, MenuRequestHandler, MenuServiceImpl, MenuRepository, or FoodTypeRepository.
---

# Writing tests for a new reactividad API

When a route/handler/service method/repository query is added or changed, write tests at
whichever of these layers were touched, following the existing style exactly rather than
inventing a new one. Reference examples:

- `src/test/java/demo/reactividad/service/impl/MenuServiceImplTest.java`
- `src/test/java/demo/reactividad/service/MenuServiceTest.java`
- `src/test/java/demo/reactividad/repository/MenuRepositoryTest.java`
- `src/test/java/demo/reactividad/AbstractIntegrationTest.java`

## 1. Service-layer unit test (pure Mockito, no Spring context, no Docker)

Mirror `MenuServiceImplTest`:

- `@ExtendWith(MockitoExtension.class)`, `@Mock` each repository/mapper dependency,
  `@InjectMocks` the `*ServiceImpl` under test.
- Every reactive assertion goes through `StepVerifier`
  (`expectNext`/`expectError`/`verifyComplete`) — never `.block()`.
- Test naming: `methodName_condition_expectedResult`
  (e.g. `deleteMenu_whenMenuExists_deletesSuccessfully`).
- Cover: the happy path, the not-found/domain-exception path, and — if the method is
  `@CircuitBreaker`-guarded — its fallback method
  (see `fallbackGetMenu_returnsMenuUnavailableException`).
- On the error path, assert side effects didn't happen with
  `verify(mock, never())...` (see `deleteMenu_whenMenuDoesNotExist...`).

## 2. Router/HTTP integration test

`extends AbstractIntegrationTest`, `@SpringBootTest`, `@AutoConfigureWebTestClient`. Mirror
`MenuServiceTest`:

- Drive the autowired `WebTestClient` against the real route path under `/api/v1/...`.
- Always include an auth case, per the hand-rolled `AuthenticationWebFilter` /
  `AuthorizationWebFilter`:
  - no `auth-token` header -> `expectStatus().isUnauthorized()`
  - a mutating request (POST/DELETE) with the `STANDARD` token (`secret123`) ->
    `expectStatus().isForbidden()`
  - a mutating request needs the `PRIME` token (`secret456`) to succeed
- Assert status, `Content-Type: application/json`, and relevant `jsonPath(...)` fields on
  the body.
- If the endpoint can raise a new domain exception, add a case for it and assert the JSON
  error body matches what `GlobalExceptionHandler` produces. Remember: a new domain
  exception needs both a handler method in `GlobalExceptionHandler` *and* an `onError(...)`
  registration in `RouterConfig`, or WebFlux won't dispatch it.

## 3. Repository integration test (only if a new/changed R2DBC query is involved)

`extends AbstractIntegrationTest`, `@SpringBootTest`, autowire the repository. Mirror
`MenuRepositoryTest`:

- Drive assertions through `StepVerifier` (`expectNextCount`, `assertNext` + JUnit
  `Assertions`) — never block.
- If the query needs fixture rows that don't already exist, add them to
  `src/test/resources/db/test-data.sql` (loaded automatically by
  `AbstractIntegrationTest`'s `DynamicPropertySource`).

## Reminders

- Integration tests (`MenuServiceTest`, `MenuRepositoryTest`, and any new ones extending
  `AbstractIntegrationTest`) run against an in-memory H2 database — no Docker required.
  Each `@SpringBootTest` context gets its own isolated H2 instance.
- `*ServiceImplTest` classes need no external services.
- If a repository test needs new fixture rows, add them to
  `src/test/resources/db/test-data.sql`; if a schema change is needed, mirror it in both
  `src/main/resources/db/schema.sql` (Postgres) and `src/test/resources/db/schema-h2.sql`
  (H2 — no `pgcrypto`, `gen_random_uuid()` becomes `RANDOM_UUID()`).
- Run a single class: `./mvnw test -Dtest=<ClassName>`
- Run a single method: `./mvnw test -Dtest=<ClassName>#<methodName>`
