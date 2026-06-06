# Testing Strategy

Lift Nexus API uses automated tests to keep the current MVP stable while the domain model, REST API, persistence layer, and optimization logic evolve.

The goal is not only to increase the coverage percentage. The more important goal is to test the behavior that matters for the current warehouse dispatching workflow.

!!! info "MVP scope"
    The test suite is still evolving. Current tests focus on the most important backend, persistence, API, and solver behavior for Milestone 1.

---

## Test structure

The test source tree follows the main project modules.

Current test areas include:

- application and configuration tests
- forklift tests
- load unit tests
- planning tests 
- storage bin tests
- transport order tests

This mirrors the modular structure of the application and makes it easier to understand which part of the system a test belongs to.

---

## What is tested?

The current test suite covers several layers of the application:

- application context startup
- controller behavior
- service behavior
- mapper behavior
- repository and persistence behavior
- OpenAPI generation
- Timefold solver constraint behavior

The goal is to test the project as a backend system, not only as isolated Java classes.

---

## Repository and persistence tests

Repository tests are used to verify persistence behavior against the database layer.

These tests are important because Lift Nexus API uses PostgreSQL and Flyway migrations. The persistence layer is part of the real backend behavior and should not only be tested with mocks.

The project uses PostgreSQL/Testcontainers test infrastructure so persistence tests can run against a database setup that is closer to the real application environment than an in-memory database.

!!! note "Why this matters"
    Database behavior is part of the system. Testing repositories with PostgreSQL helps catch persistence and migration issues earlier.

---

## Controller and service tests

Controller and service tests check important application behavior around the domain modules.

These tests help verify that:

- API endpoints behave as expected
- service methods apply the intended business logic
- errors and edge cases are handled consistently
- module behavior does not break during refactoring

For the current MVP, these tests are useful because the domain model is still evolving.

---

## Mapper tests

Mapper tests verify that data is translated correctly between domain objects and DTOs.

This matters because the API layer should not expose internal domain objects directly. Mapper tests help protect the boundary between the REST API and the internal model.

---

## Solver constraint tests

Planning constraints are tested directly.

The current solver-related tests cover constraints such as:

- forklift capacity limit
- forklift travel distance
- transport order equipment requirement
- constraint provider behavior

This is one of the most important parts of the test suite because the optimization model is a core part of the project.

Testing constraints directly makes it easier to verify that the solver rules behave as intended before they are used inside a larger dispatching workflow.

---

## Coverage reports

The CI pipeline generates a JaCoCo coverage report.

[View coverage report](../coverage/)

Coverage is useful as a visibility tool, but it is not the only measure of test quality.

A high coverage number does not automatically mean the most important behavior is tested. For this project, meaningful tests around dispatching, persistence, constraints, and domain rules matter more than only increasing the percentage.

---

## Test reports

The CI pipeline also publishes test reports.

These reports make it easier to inspect test results after a pipeline run.

[View test results](../tests/)

---

## Current testing focus

The current test focus is:

- keeping the application context stable
- testing repository behavior with PostgreSQL/Testcontainers
- testing controller and service behavior
- checking mapper behavior
- testing Timefold constraints directly
- generating OpenAPI and CI reports

This is enough for the current static dispatching MVP.

---

## Future testing improvements

Future milestones should add stronger tests around:

- full dispatch job workflows
- solver failure handling
- edge cases with no available forklifts
- edge cases with incompatible transport orders
- larger dispatching scenarios
- dynamic dispatching and replanning behavior
- benchmark scenarios for optimization quality

The goal is to grow the test suite together with the project instead of adding unnecessary complexity too early.

---

## Testing philosophy

The testing strategy follows the same philosophy as the project architecture:

> Keep the system understandable first, then improve it step by step.

For Milestone 1, the test suite should protect the current static dispatching MVP and make future refactoring safer.

---