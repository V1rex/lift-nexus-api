# Lift Nexus API Documentation

Lift Nexus API is a Spring Boot backend MVP for warehouse dispatch optimization.

It models a simplified warehouse scenario with forklifts, load units, storage bins, and transport orders. The current MVP focuses on static dispatching: creating a warehouse state, starting a one-shot optimization job, and assigning transport orders to forklifts using Timefold Solver.

!!! info "Project status"
    This is a portfolio / learning project, not a production warehouse management system yet.

## What this documentation covers

<div class="grid cards" markdown>

-   :material-map-marker-path:{ .lg .middle } **Project Overview**

    ---

    Why the project exists, what problem it models, and what is currently implemented.

    [:octicons-arrow-right-24: Read overview](project-overview.md)

-   :material-sitemap:{ .lg .middle } **Architecture**

    ---

    Current architecture decisions, module boundaries, and why the project uses a modular monolith.

    [:octicons-arrow-right-24: Read architecture](architecture.md)

-   :material-package-variant:{ .lg .middle } **Domain Model**

    ---

    Explanation of forklifts, load units, storage bins, transport orders, and dispatch jobs.

    [:octicons-arrow-right-24: Read domain model](domain-model.md)

-   :material-brain:{ .lg .middle } **Optimization Approach**

    ---

    How Timefold is used, what gets optimized, and how hard and soft constraints are structured.

    [:octicons-arrow-right-24: Read optimization approach](optimization.md)

-   :material-api:{ .lg .middle } **API Usage**

    ---

    How to run the application, create data, start a dispatch job, and inspect the result.

    [:octicons-arrow-right-24: Read API usage](api-usage.md)

-   :material-test-tube:{ .lg .middle } **Testing Strategy**

    ---

    Unit tests, integration tests with PostgreSQL/Testcontainers, coverage, and code quality checks.

    [:octicons-arrow-right-24: Read testing strategy](testing.md)

-   :material-alert-circle-outline:{ .lg .middle } **Known Limitations**

    ---

    Current MVP limitations, simplifications, and technical areas that still need improvement.

    [:octicons-arrow-right-24: Read limitations](limitations.md)

-   :material-map-clock:{ .lg .middle } **Roadmap**

    ---

    Milestones from static dispatching toward dynamic dispatching, stronger constraints, and deployment readiness.

    [:octicons-arrow-right-24: Read roadmap](roadmap.md)

</div>

## Generated reports

These reports are generated from the build pipeline:

- [Interactive API Reference](../api.html)
- [Test Coverage](../coverage/)
- [Javadoc](../javadoc/)
- [Test Results](../tests/)

## Repository

[View the project on GitHub](https://github.com/v1rex/lift-nexus-api)