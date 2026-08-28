# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

`reactividad` is a study project: a small reactive food-menu REST API built with **Spring Boot 4.1 / Spring WebFlux**, **Java 17**, **R2DBC** against PostgreSQL, and **Resilience4j** (via Spring Cloud Circuit Breaker) for fault tolerance. Package base: `demo.reactividad`.

## Commands

- Build: `./mvnw compile` (or `mvnw.cmd compile` on Windows cmd)
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=MenuServiceImplTest`
- Run a single test method: `./mvnw test -Dtest=MenuServiceImplTest#deleteMenu_whenMenuExists_deletesSuccessfully`
- Run the app: `./mvnw spring-boot:run`
- Package: `./mvnw package`

Integration tests (`MenuRepositoryTest`, `MenuServiceTest`) extend `AbstractIntegrationTest`, which runs against an in-memory H2 database (via `r2dbc-h2`) — no Docker/Testcontainers needed. `MenuServiceImplTest` is a pure Mockito unit test and needs no external services.

## Testing policy

All tests under `src/test/` must pass with zero failures and zero errors (`./mvnw test`) before a change is considered complete — this is a hard requirement, not a nice-to-have. The machine's default `JAVA_HOME` points at JDK 8; point it at a JDK 17 install before running Maven (e.g. `JAVA_HOME=/c/Users/oscar.vega/.jdks/jbr-17.0.14 ./mvnw test` in Git Bash), or `compile`/`test` will fail with unrelated parser errors that have nothing to do with the change being tested.

## Architecture

The app uses **functional (router-based) WebFlux**, not `@RestController` annotations. Request flow:

```
RouterConfig (RouterFunction beans)
  -> MenuRequestHandler (parses/validates the ServerRequest, delegates to the service)
    -> MenuService / MenuServiceImpl (business logic, @Transactional, @CircuitBreaker)
      -> MenuRepository / FoodTypeRepository (Spring Data R2DBC, reactive)
  -> GlobalExceptionHandler (registered per-exception via .onError() in RouterConfig, not @RestControllerAdvice)
```

- **`RouterConfig`** wires HTTP routes for `/api/v1/menu` (`GET /{menuId}`, `POST /`, `DELETE /`) and registers `onError` mappings from domain exceptions to `GlobalExceptionHandler` methods. Any new domain exception needs both a handler method in `GlobalExceptionHandler` and an `onError(...)` registration here — WebFlux won't dispatch it otherwise.
- **`MenuException`** is the shared base (carries an `errorCode` from the `MenuCodeException` enum) for all domain exceptions under `exception/menu/`: `MenuNotFoundException` (404), `MenuUnavailableException` (409), `MenuBadRequestException` (400, used for both Bean Validation failures and malformed UUID path variables).
- **`MenuServiceImpl.getMenu`** is guarded by `@CircuitBreaker(name = "menu-service-reactivo", fallbackMethod = "fallbackGetMenu")`. Resilience4j fallback methods must return the *same* type as the guarded method and may accept the triggering `Throwable` as the last parameter — keep `fallbackGetMenu(UUID, Throwable)` signature-compatible with `getMenu(UUID)` if either changes.
- **Auth is hand-rolled**, not Spring Security: `AuthenticationWebFilter` (`@Order(1)`) checks the `auth-token` header against tokens from `security.auth.tokens.standard`/`.prime` (env-overridable, default `secret123`/`secret456`) and stores an `AuthenticationCategory` (`STANDARD`/`PRIME`) as an exchange attribute. `AuthorizationWebFilter` (`@Order(2)`) reads that attribute and denies (403) if it's absent — `STANDARD` may only `GET`, `PRIME` can do anything. When adding new filters or routes, preserve this fail-closed default.
- **Mappers** (`MenuMapper`, `FoodTypeMapper`) are plain manual `@Component` classes (no MapStruct) converting between entities and DTOs (DTOs are Java records).
- **`FoodTypeRepository.findByMenuId`** is a custom `@Query` joining through `tbl_menu_food_type` — despite the name it does not look up by food-type id.

## Database

Schema lives in `src/main/resources/db/schema.sql` (three tables: `tbl_menu`, `tbl_food_type`, `tbl_menu_food_type` join table; uses `pgcrypto`'s `gen_random_uuid()`). It's executed on startup via `spring.sql.init.data-locations` in `application.properties`. Connection URL/credentials come from `spring.r2dbc.*`, with `DB_USERNAME`/`DB_PASSWORD` env vars overriding the local defaults (`postgres`/`postgres`).

Integration tests don't hit a manually-seeded external database — `AbstractIntegrationTest` points each `@SpringBootTest` context at its own uniquely-named in-memory H2 database (`r2dbc:h2:mem:///testdb-<random-uuid>;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE`, one per context to avoid schema/data collisions between differently-configured test classes) and applies both `src/test/resources/db/schema-h2.sql` and `src/test/resources/db/test-data.sql` before tests run. `schema-h2.sql` is a hand-kept H2-compatible mirror of `src/main/resources/db/schema.sql` — no `pgcrypto`/`CREATE EXTENSION`, and `gen_random_uuid()` becomes H2's `RANDOM_UUID()`. If you change the Postgres schema, update `schema-h2.sql` to match, or integration tests will drift from production DDL.
