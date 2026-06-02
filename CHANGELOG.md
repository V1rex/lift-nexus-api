# Changelog

All notable changes to the Lift Nexus API are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.0.1-SNAPSHOT] — Unreleased (Milestone 1: Static Dispatching MVP)

### Added

#### Core Domain
- **Forklift management** — CRUD operations for forklift fleet with fleet number, model, capacity (kg), equipment type, and current storage bin location
- **Load Unit management** — CRUD for physical inventory items with weight, dimensions, and storage bin assignment
- **Storage Bin management** — Warehouse topology modeling with coordinate system (`Coordinate3D`: aisle, shelf, level) and capacity tracking
- **Transport Order management** — Logistics orders with source/destination bins, priority levels, required equipment type, and status lifecycle (`PENDING` → `IN_PROGRESS` → `COMPLETED`)

#### Optimization Engine
- **Timefold constraint solver** — Vehicle Routing Problem (VRP) with capacity constraints
  - `ForkliftCapacityConstraint` — enforces weight limits per forklift
  - `ForkliftTravelDistanceConstraint` — minimizes deadheading via Manhattan distance
  - `EquipmentRequirementConstraint` — matches forklift type to transport order requirements
- **Async dispatch jobs** — Non-blocking solver execution with job status tracking (`SUBMITTED` → `RUNNING` → `COMPLETED` / `FAILED`)
- **Dispatch job repository** — Persist and query solver results

#### API & Documentation
- **REST API** — Full CRUD endpoints for all 5 bounded contexts
- **OpenAPI/Swagger** — Interactive API documentation at `/swagger-ui.html` (SpringDoc 3.0.2)
- **Health check** — Actuator endpoint at `/actuator/health`

#### Database
- **PostgreSQL 16** — Production-grade relational database
- **Flyway migrations** — Versioned schema evolution (`V1__initial_schema.sql`)
- **Spring Data JPA** — Repository layer with derived query methods

#### DevOps & CI
- **Docker Compose** — One-command local development environment (PostgreSQL + app)
- **Multi-stage Dockerfile** — Optimized production image (~200MB JRE Alpine)
- **GitHub Actions CI** — Automated build, test, Spotless, Checkstyle, and JaCoCo on every push
- **Dynamic badges** — Build status, test coverage, and doc coverage via Gist endpoint

#### Testing
- **JUnit 5 + AssertJ** — Modern testing stack with fluent assertions
- **Testcontainers** — Real PostgreSQL integration tests (no H2/in-memory mocking)
- **SharedPostgresContainer** — Reusable container singleton for fast test execution
- **75%+ line coverage** — 31 test files across unit and integration layers

#### Code Quality
- **Spotless** — Google Java Format 1.19.2 (auto-formatter)
- **Checkstyle** — Sun Java conventions (linter, service-layer focused)
- **JaCoCo** — Code coverage reports at `target/site/jacoco/index.html`

### Known Limitations

| Area | Limitation | Target Fix |
|---|---|---|
| Solver | Stateless — no incremental or real-time solving | Milestone 2 |
| API | No authentication/authorization layer | Milestone 2 |
| API | No real-time dispatch updates | Milestone 2 |
| Database | Single PostgreSQL instance (no read replicas) | Milestone 2 |
| Architecture | JPA entities double as Timefold planning entities (contaminated entity problem) | Post-M1 refactor |
| Architecture | Service classes may bloat under "Fat Service" pattern | Milestone 2 (CQRS) |

| Code Style | CRLF line endings on some files (Spotless fix pending) | Milestone 1 cleanup |
