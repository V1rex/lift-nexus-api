# 🏗️ System Architecture: Lift Nexus API

This document outlines the architectural boundaries, domain models, and strategic technical decisions behind the **Lift Nexus API** ecosystem.

---

## Table of Contents

1. [Architectural Style & Philosophy](#1-architectural-style--philosophy)
2. [Technology Stack](#2-technology-stack)
3. [Component Architecture](#3-component-architecture)
4. [Layer Architecture Within Each Context](#4-layer-architecture-within-each-context)
5. [Exception Architecture](#5-exception-architecture)
6. [Constraint System Design (VRP Engine)](#6-constraint-system-design-the-vrp-engine)
7. [Cross-Domain Communication Rules](#7-cross-domain-communication-rules)
8. [Fat Service, Slim Controller Pattern](#8-fat-service-slim-controller-pattern)
9. [Database Schema](#9-database-schema)
10. [Testing Strategy](#10-testing-strategy)
11. [Evolution to Sector Coupling](#11-evolution-to-sector-coupling-milestones-2--3)
12. [Known Limitations & Planned Refactoring](#12-known-limitations--planned-refactoring)

---

## 1. Architectural Style & Philosophy

Lift Nexus is built as a **Modular Monolith** applying **Domain-Driven Design (DDD)** principles using a **Feature-as-Package (Screaming Architecture)** directory structure.

Instead of organizing code by technical layers (e.g., `controllers/`, `services/`), the codebase is partitioned by business capabilities. This ensures high cohesion, strict domain encapsulation, and provides a clear extraction path into microservices as the system scales towards event-driven Sector Coupling.

```text
src/main/java/com/v1rex/liftnexus/
├── common/                      ← Shared kernel (exceptions, error handling)
├── forklift/                    ← Fleet management bounded context
├── loadunit/                    ← Physical inventory bounded context
├── storagebin/                  ← Warehouse topology bounded context
├── transportorder/              ← Logistics bounded context
└── planning/                    ← Core constraint-solver and orchestration
```

---

## 2. Technology Stack

| Category | Technology | Version |
|---|---|---|
| **Language** | Java | 21 (LTS) |
| **Framework** | Spring Boot | 4.0.5 |
| **Solver** | Timefold Solver | 2.0.0-beta-2 |
| **Database** | PostgreSQL | 16 |
| **Migrations** | Flyway (with PostgreSQL module) | — |
| **ORM** | Spring Data JPA (Hibernate) | — |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) | 3.0.2 |
| **Build** | Maven | 3.9+ |
| **Containerization** | Docker + Docker Compose | — |
| **CI** | GitHub Actions | — |

| Testing | Technology |
|---|---|
| **Test Framework** | JUnit 5 |
| **Assertions** | AssertJ |
| **Mocking** | Mockito |
| **Integration** | Testcontainers (PostgreSQL 1.21.4) |
| **Coverage** | JaCoCo 0.8.12 |

| Code Quality | Technology |
|---|---|
| **Formatter** | Spotless (Google Java Format 1.19.2) |
| **Linter** | Checkstyle (Sun conventions) |

---

## 3. Component Architecture (C4 Level)

The application enforces a strict layer structure within each bounded context. Inter-domain communication is strictly routed through **Service** interfaces to prevent database-level tight coupling.

```
┌─────────────────────────────────────────────────────────────────┐
│                        REST API Layer                           │
└────┬─────────────────────────────┬──────────────────────────────┘
     │                             │
┌────▼─────────────┐   ┌───────────▼───────┐   ┌──────────────────┐
│  Forklift Domain │   │  Logistics Domain │   │ Topology Domain  │
│  ├─ Controller   │   │  ├─ Controller    │   │ ├─ Controller    │
│  ├─ Service      │◀──┼──┤  Service       ├───▶ ├─ Service       │
│  ├─ Repository   │   │  ├─ Repository    │   │ ├─ Repository    │
│  └─ Entity       │   │  └─ Entity        │   │ └─ Entity        │
└──────────────────┘   └───────────────────┘   └──────────────────┘
            ▲                                         ▲
            │       ┌─────────────────────────┐       │
            └───────┤     Planning Domain     ├───────┘
                    │ (Timefold Orchestrator) │
                    └─────────────────────────┘
```

---

## 4. Layer Architecture Within Each Context

Each bounded context follows a **4-Layer Stack**:

```
┌──────────────────────────────────────────┐
│  Controller  ← HTTP routing, DTO mapping │
├──────────────────────────────────────────┤
│  Service     ← @Transactional, business  │
│                rules, cross-domain calls  │
├──────────────────────────────────────────┤
│  Repository  ← Data access (Spring Data  │
│                JPA interfaces)            │
├──────────────────────────────────────────┤
│  Entity      ← JPA-mapped domain model   │
└──────────────────────────────────────────┘
```

**Supporting layers shared across contexts:**

```
┌──────────────────────────────────────────┐
│  Exceptions  ← Per-domain sealed class   │
│                hierarchy + ErrorCodes     │  ← common/exception
├──────────────────────────────────────────┤
│  DTOs        ← Request/Response records  │
│                (Java records)             │
└──────────────────────────────────────────┘
```

**Key Boundary Rules:**

- **Slim Controllers:** Handle HTTP routing, payload validation (`@Valid`), and DTO translations only.
- **Fat Services:** Handle transaction boundaries (`@Transactional`), business rules, and cross-domain lookups.
- **Anti-Corruption Rule:** A Service in Domain A may never inject a Repository from Domain B. It must call the Service of Domain B.

---

## 5. Exception Architecture

Lift Nexus implements a **hierarchical, domain-isolated exception model** using Java 21's `sealed` classes and RFC 9457 (`ProblemDetail`) for consistent API error responses.

### Design Goals

- ✅ Each bounded context owns its own errors — no leaking domain details across boundaries
- ✅ Every error carries a machine-readable `errorCode` for client-side handling
- ✅ Sealed class hierarchies prevent ad-hoc, undocumented exceptions
- ✅ RFC 9457 compliance ensures standardized error response format

### Architecture Layers

```
┌──────────────────────────────────────────────────────────────────┐
│                    GlobalExceptionHandler                        │
│              (@RestControllerAdvice, fallback)                    │
│                                                                  │
│  Catches: DomainException, MethodArgumentNotValidException,      │
│           DataIntegrityViolationException, Exception, etc.       │
│                                                                  │
│  Delegates to: ProblemDetailFactory                              │
└────────────────────────┬─────────────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────────────┐
│                    ProblemDetailFactory                          │
│                                                                  │
│  Builds RFC 9457 ProblemDetail with:                             │
│    • type:    urn:liftnexus:problem:{errorCode}                  │
│    • title:   Human-readable error title                         │
│    • status:  HTTP status code                                   │
│    • detail:  Specific error message                             │
│    • instance: Request URI                                       │
│    • errorCode: Machine-readable code                            │
│    • timestamp: ISO 8601 instant                                 │
│    • errors:   Field-level validation details (when applicable)  │
└──────────────────────────────────────────────────────────────────┘
```

### Common (Shared Kernel)

The `common.exception` package defines the contract:

```java
// ErrorCode.java — Interface all error enums implement
public interface ErrorCode {
    String getCode();          // e.g., "forklift_not_found"
    String getDefaultTitle();  // e.g., "Forklift Not Found"
    HttpStatus getStatus();    // e.g., 404 NOT_FOUND
}

// DomainException.java — Abstract base for all domain exceptions
@Getter
public abstract class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
}

// GlobalErrorCode.java — Framework/system-level errors
public enum GlobalErrorCode implements ErrorCode {
    VALIDATION_FAILED("validation_failed", "Validation failed", BAD_REQUEST),
    DATABASE_CONFLICT("database_state_conflict", "Database state conflict", CONFLICT),
    INTERNAL_SERVER_ERROR("internal_server_error", "Internal server error", INTERNAL_SERVER_ERROR),
    // ... 12 total framework error codes
}
```

### Per-Domain Pattern (Example: Forklift)

Each domain replicates the same 3-file pattern:

```java
// 1. ErrorCode enum — catalog of all possible errors in this domain
public enum ForkliftErrorCode implements ErrorCode {
    FORKLIFT_NOT_FOUND("forklift_not_found", "Forklift Not Found", NOT_FOUND),
    FORKLIFT_FLEET_NUMBER_EXISTS("forklift_fleet_number_already_exists",
        "Forklift Fleet Number already exists", CONFLICT),
    FORKLIFT_TYPE_NOT_FOUND("forklift_type_not_found",
        "Forklift Type Not Found", NOT_FOUND),
    FORKLIFT_TYPE_NAME_EXISTS("forklift_type_name_already_exists",
        "Forklift Type Name already exists", CONFLICT);
}

// 2. Sealed abstract domain exception — restricts which concrete exceptions can exist
public abstract sealed class ForkliftDomainException extends DomainException
    permits ForkliftFleetNumberExistsException,
            ForkliftNotFoundException,
            ForkliftTypeNameExistsException,
            ForkliftTypeNotFoundException { }

// 3. Concrete exceptions — final, with descriptive constructors
public final class ForkliftNotFoundException extends ForkliftDomainException {
    public ForkliftNotFoundException(Long id) {
        super(ForkliftErrorCode.FORKLIFT_NOT_FOUND,
            "Forklift with ID " + id + " does not exist.");
    }
}
```

### Error Code Catalog (All 5 Domains)

| Domain | Error Codes | HTTP Statuses |
|---|---|---|
| **Common (Global)** | 12 codes — validation, type mismatch, missing params, payload too large, method not allowed, unsupported media type, not acceptable, malformed body, serialization, database conflict, internal server error | 400, 413, 405, 415, 406, 409, 500 |
| **Forklift** | 4 codes — not found, fleet number exists, type not found, type name exists | 404, 409 |
| **LoadUnit** | 2 codes — not found, tracking code exists | 404, 409 |
| **StorageBin** | 3 codes — not found, code exists, domain exception | 404, 409 |
| **TransportOrder** | 3 codes — not found, invalid state, domain exception | 404, 409 |
| **Planning** | 2 codes — dispatch job not found, invalid state | 404, 409 |

### Error Response Example

```json
{
  "type": "urn:liftnexus:problem:forklift_not_found",
  "title": "Forklift Not Found",
  "status": 404,
  "detail": "Forklift with ID 42 does not exist.",
  "instance": "/api/v1/forklifts/42",
  "errorCode": "forklift_not_found",
  "timestamp": "2025-06-01T12:00:00Z"
}
```

### Why `sealed` classes?

Java 21's `sealed` keyword ensures the compiler enforces that **only** the explicitly permitted subclasses can extend `ForkliftDomainException`. This prevents developers from creating ad-hoc, undocumented exceptions — every possible error in a domain must be declared in its sealed hierarchy. The `GlobalExceptionHandler` catches `DomainException` (the common ancestor) in a single handler, and the `ErrorCode` embedded in each exception drives the HTTP response.

---

## 6. Constraint System Design (The VRP Engine)

To prevent the Timefold `ConstraintProvider` from becoming an unmaintainable monolith, the optimization engine utilizes a **Modular Constraint Design**.

Each business constraint is isolated into its own testable class, making it trivial to toggle specific mathematical penalties (e.g., for A/B testing or future dynamic energy constraints).

```java
// planning/constraints/WarehouseConstraintProvider.java
public class WarehouseConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
            ForkliftCapacityConstraint.enforceCapacity(factory),
            ForkliftTravelDistanceConstraint.minimizeDeadheading(factory),
            EquipmentRequirementConstraint.matchForkliftType(factory)
        };
    }
}
```

**Why this design:**
- ✅ One file = one constraint = one concern (Single Responsibility Principle)
- ✅ Easy to understand the business rule at a glance
- ✅ Testable: Mock ConstraintFactory, verify behavior
- ✅ Versioning: Can create CapacityConstraintV2 alongside V1
- ✅ A/B Testing: Wrap constraints in conditionals

---

## 7. Cross-Domain Communication Rules

### Rule 1: Services Only (Never Inject Repositories Across Domains)

```java
// ✅ Correct
class WarehouseDispatcherService {
    private final ForkliftService forkliftService;      // Cross-domain via Service
    private final TransportOrderService orderService;   // ✅
}

// ❌ Wrong
class WarehouseDispatcherService {
    private final ForkliftRepository repo;  // ❌ Direct repository access!
}
```

### Rule 2: DTOs at Package Boundary

Never expose entities across domain boundaries.

```java
// ✅ Correct
public ResponseEntity<ForkliftResponse> getForklift(@PathVariable Long id) {
    return ResponseEntity.ok(forkliftService.findById(id));  // DTO
}

// ❌ Wrong
public ResponseEntity<Forklift> getForklift(@PathVariable Long id) {
    return ResponseEntity.ok(forkliftService.findById(id));  // Entity exposed!
}
```

---

## 8. Fat Service, Slim Controller Pattern

### Slim Controllers — HTTP Translation Only

Controllers handle ONLY:
- HTTP status codes (201 Created, 400 Bad Request, etc.)
- Request/response DTO translation
- URL routing & header management
- Input validation (delegated to framework via `@Valid`)

### Fat Services — Business Logic & Orchestration

Services handle:
- Transaction boundaries (`@Transactional`)
- Business rule validation
- Inter-domain orchestration (calls other Services, never Repositories)
- Domain exception throwing (using the sealed exception hierarchy)
- Entity lifecycle management

---

## 9. Database Schema

The database is managed via Flyway migrations. Five core entity tables exist, one per bounded context, plus a dispatch job table in the planning domain.

```
┌──────────────────────┐       ┌──────────────────────┐
│     storage_bins     │       │     load_units       │
├──────────────────────┤       ├──────────────────────┤
│ id (PK)              │◄──────│ storage_bin_id (FK)  │
│ bin_code (UNIQUE)    │       │ id (PK)              │
│ aisle                │       │ tracking_code (UQ)   │
│ shelf                │       │ weight_kg            │
│ level                │       │ length_cm            │
│ max_capacity_kg      │       │ width_cm             │
│ current_capacity_kg  │       │ height_cm            │
└──────────┬───────────┘       └──────────────────────┘
           │
           │◄─────────────────────────────────────────┐
           │                                          │
┌──────────▼───────────┐       ┌──────────────────────┤
│      forklifts       │       │   transport_orders   │
├──────────────────────┤       ├──────────────────────┤
│ id (PK)              │       │ id (PK)              │
│ fleet_number (UQ)    │       │ source_bin_id (FK)   │──┐
│ model                │       │ target_bin_id (FK)   │──┤
│ capacity_kg          │       │ priority             │  │
│ equipment_type       │       │ required_equipment   │  │
│ current_bin_id (FK)  │───────│ status               │  │
│ status               │       │ assigned_forklift_id │──┼──► forklifts.id
│ forklift_type_id (FK)│       └──────────────────────┘  │
└──────────────────────┘                                  │
                                                          │
┌──────────────────────┐                                  │
│   forklift_types     │                                  │
├──────────────────────┤                                  │
│ id (PK)              │                                  │
│ type_name (UQ)       │                                  │
│ description          │                                  │
└──────────────────────┘                                  │
                                                          │
┌──────────────────────┐                                  │
│   dispatch_jobs      │                                  │
├──────────────────────┤                                  │
│ id (PK)              │                                  │
│ status               │                                  │
│ solver_duration_ms   │                                  │
│ solution_json        │                                  │
│ created_at           │                                  │
│ completed_at         │                                  │
└──────────────────────┘
```

**Key relationships:**
- `forklifts.current_bin_id` → `storage_bins.id` (where the forklift is currently located)
- `load_units.storage_bin_id` → `storage_bins.id` (where the load unit is stored)
- `transport_orders.source_bin_id` / `target_bin_id` → `storage_bins.id` (pickup and drop-off locations)
- `transport_orders.assigned_forklift_id` → `forklifts.id` (assigned by solver)
- `forklifts.forklift_type_id` → `forklift_types.id` (equipment type reference)

---

## 10. Testing Strategy

### Test Pyramid

```
         ┌─────────┐
         │   E2E   │  ← Future: solver end-to-end tests
         ├─────────┤
         │  Int.   │  ← @DataJpaTest + Testcontainers (real PostgreSQL)
         ├─────────┤
         │  Unit   │  ← Pure Mockito + AssertJ, no Spring context
         └─────────┘
```

### Unit Tests

- **Scope:** Services, domain logic, constraint classes
- **Tools:** JUnit 5, Mockito, AssertJ
- **Profile:** No `@ActiveProfiles` needed — pure mock tests
- **Pattern:** `@ExtendWith(MockitoExtension.class)`, inject mocks, verify behavior

### Integration Tests

- **Scope:** Repository layer, database interactions, Flyway migrations
- **Tools:** `@DataJpaTest`, Testcontainers, `SharedPostgresContainer`
- **Approach:** Real PostgreSQL container (not H2/in-memory). Each repository test runs against a live database with Flyway migrations applied.

### Shared Container Strategy

```java
// SharedPostgresContainer.java — Singleton container reused across all test classes
public abstract class SharedPostgresContainer {
    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16");

    static {
        container.start();
        // On local dev: withReuse(true) keeps container alive between test runs
        // On CI: reuse is disabled, Ryuk handles cleanup after pipeline finishes
    }
}
```

This avoids starting a new container per test class — all `@DataJpaTest` classes share a single PostgreSQL instance, dramatically reducing test suite runtime.

### Coverage

- **Target:** 75%+ line coverage (JaCoCo)
- **Excluded from coverage:** Config classes, exception definitions (purely structural)
- **Gap areas:** Solver end-to-end tests (SolverManager currently mocked), `DispatchJobRepositoryTest` (not yet written)

---

## 11. Evolution to Sector Coupling (Milestones 2 & 3)

The Lift Nexus architecture is intentionally designed to evolve from a centralized monolith into a distributed, event-driven Demand-Response ecosystem.

### Milestone 2: Real-Time Reactive Optimization

The synchronous dispatch endpoint transitions to an event-driven model:

```
┌──────────┐   POST /dispatch   ┌──────────────┐   event    ┌──────────┐
│  Client  │ ──────────────────►│ Dispatch API │───────────►│  Message │
└──────────┘                    └──────────────┘            │  Queue   │
                                                            │ (RabbitMQ│
                                                            │  / Kafka)│
                                                            └────┬─────┘
                                                                 │
┌──────────┐   SSE/WebSocket    ┌──────────────┐   consume   ┌───▼──────┐
│  Client  │ ◄──────────────────│ Notification │◄────────────│  Solver  │
└──────────┘                    │   Service    │             │  Worker  │
                                └──────────────┘             └──────────┘
```

**Key changes:**
- Non-blocking solver execution via message queue workers
- Real-time dispatch progress via Server-Sent Events (SSE) or WebSocket
- Independent scaling of solver workers
- Event store for audit trail and replay capability

### Milestone 3: External Energy Market Integration

To achieve true sector coupling, the Java core will interface with a standalone Python service (`electron-nexus-api`):

```
┌──────────────────────────────────┐
│       Lift Nexus (Java 21)       │
│   Warehouse dispatch optimizer   │
└──────────────┬───────────────────┘
               │ REST / gRPC
               │
┌──────────────▼───────────────────┐
│   Electron Nexus (Python)        │
│   ┌───────────────────────────┐  │
│   │ Energy price forecaster   │  │  ← External market feeds
│   │ Charging schedule planner │  │
│   │ Grid load balancer        │  │
│   └───────────────────────────┘  │
└──────────────────────────────────┘
```

**Integration points:**
- Lift Nexus sends fleet state → Electron Nexus computes optimal charging windows
- Electron Nexus sends energy cost vector → Lift Nexus factors into dispatch scoring
- Combined optimization: minimize both travel distance AND energy cost

---

## 12. Known Limitations & Planned Refactoring

### Issue 1: The Contaminated Entity Problem (JPA vs. Optimization Model)

**Observed Issue:**
Currently, core domain classes (like `Forklift`) serve a dual purpose. They carry relational database annotations (`@Entity`, `@Table`) as well as optimization metadata (`@PlanningEntity`, `@PlanningVariable`). This couples the mathematical VRP solver logic directly to the database schema.

**Risk:**
- Solver changes force database migrations
- Difficult to version the optimization model independently
- Hard to test pure domain logic without JPA overhead

**Target Solution (Post-M1):**
Migration to **Hexagonal Architecture (Ports & Adapters)**. The database entity will be split:
- `ForkliftEntity` (Infrastructure Layer) — Contains JPA annotations only
- `Forklift` (Domain Core) — Pure Java model for Timefold, zero infrastructure coupling
- `ForkliftMapper` (Port) — Translates between layers at persistence boundary

### Issue 2: Service Layer Bloat

**Observed Issue:**
Adhering to the "Slim Controller, Fat Service" pattern risks creating oversized Service classes as more complex business logic is added across multiple domains and transactions.

**Risk:**
- Single methods handling 10+ responsibilities
- Difficult unit testing due to high coupling
- Performance bottlenecks when write operations block read queries

**Target Solution (Milestone 2+):**
Introduction of **CQRS (Command Query Responsibility Segregation)** pattern:
- **Write Path (Commands):** `ExecuteDispatchCommand` → `DispatchCommandHandler` → Updates state asynchronously via event streams
- **Read Path (Queries):** `GetFleetStatusQuery` → `FleetStatusQueryHandler` → Returns lightweight DTO immediately

---

**Last Updated:** 2025-06-02
**Updated By:** Amine Bahij
**Status:** Milestone 1 (Static Dispatching MVP)
**Next Evolution:** Milestone 2 (Real-Time Event-Driven)
