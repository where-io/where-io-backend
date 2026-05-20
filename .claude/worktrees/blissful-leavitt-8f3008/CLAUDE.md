# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=LocalControllerTest

# Skip tests during build
./mvnw clean install -DskipTests
```

## Architecture Overview

This is a **Spring Boot 4 / Java 17** REST API following **Hexagonal Architecture (Ports & Adapters)**. All business logic lives in the `application` layer and never depends on infrastructure.

### Layer structure

```
adapters/in/web/          → REST controllers + DTOs + converters (input adapters)
adapters/out/persistence/ → MongoDB repositories + entities + mappers (output adapters)
adapters/out/external/    → Google Geocoding API integration (output adapter)
application/model/        → Domain models (Local, Visita, Endereco, Coordenadas)
application/ports/in/     → Use case interfaces (one interface per use case)
application/ports/out/    → Repository and external service port interfaces
application/service/      → Use case implementations
exceptions/               → BusinessException + GlobalExceptionHandler
config/                   → Spring beans (WebClient, RestTemplate, CORS, MongoDB)
```

### Key domain concepts

- **Local** — A place (restaurant, bar, etc.) with an address (`Endereco`), coordinates (`Coordenadas`), and a list of visits.
- **Visita** — A visit record linked to a `Local` by `idLocal`, with a date, rating (`RestauranteAvaliacao` enum: OTIMO=1 … PESSIMO=5), and comment.

### Data flow for creating a Local

1. `POST /api/local` → `LocalController` → calls `CadastrarLocalUsecaseImpl`
2. Use case checks for duplicate name via `LocalRepositoryPort`
3. If coordinates are missing, calls `LatitudeLongitudeInterfacePort` → `GoogleGeocodingAdapter` (Google Geocoding API via `WebClient`)
4. Persists via `LocalRepositoryPort` → `LocalRepositoryAdapter` → MongoDB (`local_table` collection)

### REST API endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/local` | Create a local (returns its ID) |
| PUT | `/api/local/{id}` | Update a local |
| DELETE | `/api/local/{id}` | Remove a local |
| GET | `/api/local/all` | List all locals |
| GET | `/api/local/buscar-local` | Search via Google Places autocomplete (body: `inputText`, `sessionToken`) |
| POST | `/api/visita` | Create a visit |
| PUT | `/api/visita/{id}` | Update a visit |
| DELETE | `/api/visita/{id}` | Remove a visit |
| GET | `/api/visita/{idLocal}` | List visits for a local |

### Conventions

- One use case interface per operation under `ports/in/` (e.g., `CadastrarLocalUsecase`, `RemoverLocalUsecase`).
- Converters (`LocalConverter`, `VisitaConverter`) handle DTO ↔ domain mapping in the web layer.
- Persistence mappers (`LocalPersistenceMapper`, `VisitaPersistanceMapper`) use MapStruct for entity ↔ domain mapping.
- `LocalServiceMapper` in `application/mapper/` is a MapStruct mapper used inside the application layer (entity ↔ domain).
- `BusinessException` carries an `HttpStatus`; `GlobalExceptionHandler` maps it to the response.
- The Google API key is loaded from `application.properties` as `google.map.api.key` using `dotenv-java`.

### Logging

Services use SLF4J (`LoggerFactory.getLogger(...)`) with MDC for structured logging. `MdcLoggingFilter` (runs at `@Order(1)`) automatically injects `requestId`, `httpMethod`, `requestUri`, and `clientIp` into MDC for every request. Use case implementations add `operation` and `entityId` manually via `MDC.put(...)` inside a try/finally block that calls `MDC.remove(...)` on exit.

### Testing

Tests use **JUnit 5 + Mockito** (`@ExtendWith(MockitoExtension.class)`). Test classes are organized with `@Nested` inner classes and `@DisplayName` labels following the pattern `"METHOD /path - methodName"` for the outer class and plain descriptions for each `@Test`. Coverage exists at all three layers:

- `adapters/in/web/` — controller tests (mock all use cases + converter)
- `application/service/` — use case impl tests (mock ports)
- `adapters/out/persistence/impl/` — repository adapter tests (mock Spring Data repositories)

### External dependencies

- **MongoDB** — primary persistence (Spring Data MongoDB, `local_table` and visita collections)
- **H2** — present as a runtime dependency but MongoDB is the active store
- **Google Geocoding API** — geocodes addresses to lat/lng via `WebClient`
- **MapStruct 1.5.5** — compile-time mapper generation (requires both `lombok` and `mapstruct-processor` in `annotationProcessorPaths`)
