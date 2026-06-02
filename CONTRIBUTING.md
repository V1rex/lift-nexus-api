# 🤝 Contributing to Lift Nexus API

Thank you for your interest in contributing! This document outlines the process for setting up your development environment, code standards, and pull request guidelines.

---

## 📋 Table of Contents

- [Development Setup](#development-setup)
- [Prerequisites](#prerequisites)
- [Running Locally](#running-locally)
- [Code Style Guidelines](#code-style-guidelines)
- [Testing Requirements](#testing-requirements)
- [Commit Message Conventions](#commit-message-conventions)
- [Pull Request Process](#pull-request-process)
- [Reporting Issues](#reporting-issues)

---

## Development Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/v1rex/lift-nexus-api.git
cd lift-nexus-api
```

### Step 2: Set Up Your Branch

```bash
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name
```

**Branch Naming Convention:**
- `feature/add-xyz` – New feature
- `fix/issue-xyz` – Bug fix
- `docs/update-readme` – Documentation
- `refactor/improve-xyz` – Code improvement

---

## Prerequisites

Ensure you have the following installed:

| Tool | Version | Command to Verify |
|------|---------|-------------------|
| **Java** | 21+ | `java -version` |
| **Maven** | 3.8+ | `mvn -version` |
| **Docker** | Latest | `docker --version` |
| **Docker Compose** | Latest | `docker-compose --version` |

---

## Running Locally

### 1. Start PostgreSQL (Docker)

```bash
docker-compose up -d
```

**Verify it's running:**
```bash
docker-compose ps
```

### 2. Build the Application

```bash
./mvnw clean install
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

**Expected Output:**
```
Started LiftNexusApplication in XX seconds
Tomcat is running on http://localhost:8080
```

### 4. Access the API

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Health Check:** 
  ```bash
  curl http://localhost:8080/actuator/health
  ```

### 5. Stop the Environment

```bash
docker-compose down
```

---

## Code Style Guidelines

### Google Java Format with Spotless

This project enforces **Google Java Format** via the Spotless Maven plugin.

### Auto-Format Your Code

```bash
./mvnw spotless:apply
```

### Check Formatting (without changing)

```bash
./mvnw spotless:check
```

**Best Practice:** Run `spotless:apply` before committing to ensure compliance.

### Key Style Rules

- 4-space indentation
- Line width: 100 characters (soft limit)
- No wildcard imports (`import java.util.*`)
- No trailing whitespace
- One blank line between methods

---

## Testing Requirements

### Minimum Coverage: 90%+

This project targets **90% code coverage** as measured by JaCoCo. All new code must maintain or improve this threshold.

### Run Unit Tests

```bash
./mvnw clean test
```

### Run Full Test Suite (including integration tests)

```bash
./mvnw clean verify
```

This includes:
- Unit tests (JUnit 5)
- Integration tests (Testcontainers + real PostgreSQL)
- Code coverage report

### View Coverage Report

```bash
./mvnw clean verify
open target/site/jacoco/api.html
```

### Writing Tests

- **Unit Tests:** Mock external dependencies
- **Integration Tests:** Use `@SpringBootTest` with `@Testcontainers`
- **Test Naming:** `shouldReturnForkliftById_WhenIdExists()`

---

## Commit Message Conventions

Follow **[Conventional Commits](https://www.conventionalcommits.org/)** for clear, semantic commit messages.

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Examples

```bash
git commit -m "feat(planning): add static dispatcher service"
git commit -m "fix(forklift): correct battery percentage calculation"
git commit -m "docs(readme): update quick start guide"
git commit -m "refactor(constraints): extract modular constraint classes"
git commit -m "test(service): add integration tests for dispatcher"
```

### Commit Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `refactor` | Code refactoring (no feature/fix) |
| `test` | Test additions/updates |
| `perf` | Performance improvements |
| `chore` | Build, CI/CD, dependency updates |

### Scope Examples

- `planning` – Planning domain
- `forklift` – Forklift domain
- `constraints` – Constraint system (Timefold)
- `api` – REST API changes
- `db` – Database schema changes

---

## Pull Request Process

### 1. Create Your Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Your Changes

- Implement the feature
- Add/update tests
- Run `./mvnw spotless:apply` to format code
- Ensure tests pass: `./mvnw clean verify`

### 3. Commit Your Changes

Use Conventional Commits:
```bash
git commit -m "feat(domain): descriptive message"
```

### 4. Push to Your Fork

```bash
git push origin feature/your-feature-name
```

### 5. Open a Pull Request

- **Target:** `develop` branch (not `main`)
- **Title:** Follow commit convention (e.g., "feat(planning): add new constraint")
- **Description:** Explain the change and **why** it's needed
- **Link Issues:** Reference related issues (e.g., "Closes #42")

### 6. Code Review & CI/CD

- Automated tests must pass
- Code coverage must not decrease
- At least one approver review required
- Address feedback and push updates

### 7. Merge

Once approved:
- Squash merge is preferred: `Squash and merge`
- Keep commit history clean

---

## Reporting Issues

### Use Issue Templates

When reporting bugs or requesting features, use the provided templates:

- **Bug Report:** `.github/ISSUE_TEMPLATE/bug_report.md`
- **Feature Request:** `.github/ISSUE_TEMPLATE/feature_request.md`
- **Technical Debt:** `.github/ISSUE_TEMPLATE/tech_debt.md`

### Minimal Reproducible Example (MRE)

For bugs, always include:

```markdown
### Steps to Reproduce
1. Start the application
2. Make a request to `/api/v1/forklifts`
3. Observe the error

### Expected Behavior
Should return a list of forklifts

### Actual Behavior
Returns 500 Internal Server Error

### Environment
- Java Version: 21
- OS: macOS Ventura
```

---

## Questions?

- 📚 See [ARCHITECTURE.md](./ARCHITECTURE.md) for design questions
- 🚀 See [README.md](./README.md) for project overview
- 🔗 Check existing [GitHub Issues](https://github.com/v1rex/lift-nexus-api/issues)

---

**Thank you for contributing to Lift Nexus! 🙏**

Last Updated: 2026-05-31  
Status: Active Development  
Milestone: 1 (Static Dispatching)

