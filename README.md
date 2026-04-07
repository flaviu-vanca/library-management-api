# Library Management API

> RESTful API for managing libraries and books — built with Spring Boot, containerised with Docker, and validated by a Jenkins CI/CD pipeline.

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-25-orange?logo=openjdk" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?logo=springboot" />
  <img alt="Build" src="https://img.shields.io/badge/Build-Maven-blue?logo=apachemaven" />
  <img alt="Docker" src="https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker" />
  <img alt="CI" src="https://img.shields.io/badge/CI-Jenkins-D24939?logo=jenkins&logoColor=white" />
  <img alt="Quality" src="https://img.shields.io/badge/Quality-SonarQube-4E9BCD?logo=sonarqube&logoColor=white" />
  <img alt="Coverage" src="https://img.shields.io/badge/Coverage-JaCoCo-BD1E59" />
  <img alt="Testing" src="https://img.shields.io/badge/Tests-JUnit%205%20%7C%20Karate-0B7285" />
  <img alt="Database" src="https://img.shields.io/badge/DB-H2%20In--Memory-1f6feb" />
  <img alt="License" src="https://img.shields.io/badge/License-Educational-lightgrey" />
</p>

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running Locally](#running-locally)
- [API Reference](#api-reference)
- [Project Structure](#project-structure)
- [CI/CD Pipeline](#cicd-pipeline)
- [Testing](#testing)
- [Error Handling](#error-handling)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

Library Management API is a Spring Boot REST API that demonstrates production-grade backend patterns: layered architecture (Controller → Service → Repository), DTO-based request/response contracts, pagination and sorting, date-range filtering, centralised exception handling, and a full CI/CD pipeline driven by Jenkins, SonarQube, and Docker.

The domain model is a classic **one-to-many** relationship: one `Library` contains many `Book` records.

---

## Features

- Full CRUD for both `Library` and `Book` resources
- One-to-many entity relationship with cascading deletes
- Request/response DTOs decoupled from JPA entities
- Pagination and sorting on list endpoints
- Date-based filtering (`by-publication-date`, `by-acquisition-date`)
- Bean validation on all incoming requests
- Centralised error responses via `GlobalExceptionHandler`
- H2 in-memory database with schema and seed data on startup
- Dockerised runtime image (Eclipse Temurin 25 JRE)
- Jenkins declarative pipeline: build → test → coverage → SonarQube → package → deploy
- JaCoCo code-coverage reports (skipped by default on Java 25; enable with `-Djacoco.skip=false` on Java 21)
- Karate end-to-end API tests executed as part of the Maven build

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.2 (Web, Data JPA, Validation) |
| Build | Maven 3.9+ |
| Database | H2 (in-memory) |
| Persistence | Spring Data JPA / Hibernate |
| Boilerplate reduction | Lombok |
| Unit / Integration tests | JUnit 5, Mockito |
| API tests | Karate (io.karatelabs 1.5.1) |
| Coverage | JaCoCo 0.8.12 |
| Static analysis | SonarQube (community edition) |
| Containerisation | Docker (Eclipse Temurin 25 JRE base image) |
| CI/CD | Jenkins declarative pipeline |

---

## Getting Started

### Prerequisites

| Tool | Version / Notes |
|---|---|
| Java | **25** — default build, test, and package stages |
| Java | **21** — required only to generate JaCoCo HTML coverage report (`-Djacoco.skip=false`) |
| Maven | 3.9+ |
| Docker Desktop | Required for SonarQube, the webhook relay, and local container deployment. Must run in **Linux containers** mode. |

### Installation

```bash
git clone https://github.com/flaviu-vanca/library-management-api.git
cd library-management-api
mvn clean install
```

### Running Locally

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080`.  
The H2 console is available at `http://localhost:8080/h2-console`:

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:librarydb` |
| Username | `sa` |
| Password | *(blank)* |

---

## API Reference

### Libraries

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/libraries` | List all libraries (paginated) |
| `GET` | `/api/libraries/{id}` | Get a library by ID |
| `POST` | `/api/libraries` | Create a library |
| `PUT` | `/api/libraries/{id}` | Update a library |
| `DELETE` | `/api/libraries/{id}` | Delete a library (cascades to books) |
| `GET` | `/api/libraries/{id}/books` | List books belonging to a library |

### Books

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/books` | List all books (paginated) |
| `GET` | `/api/books/{id}` | Get a book by ID |
| `POST` | `/api/books` | Create a book |
| `PUT` | `/api/books/{id}` | Update a book |
| `DELETE` | `/api/books/{id}` | Delete a book |
| `GET` | `/api/books/by-publication-date` | Filter books by publication date range |
| `GET` | `/api/books/by-acquisition-date` | Filter books by acquisition date range |

> Error payload examples are documented in [`docs/ERROR_RESPONSE_EXAMPLES.md`](docs/ERROR_RESPONSE_EXAMPLES.md).

---

## Project Structure

```text
library-management-api/
├── Dockerfile
├── Jenkinsfile
├── compose.yaml                          # SonarQube + PostgreSQL + webhook relay
├── pom.xml
├── docs/
│   ├── PROJECT_STRUCTURE.md
│   ├── ERROR_RESPONSE_EXAMPLES.md
│   ├── LOMBOK_SETUP.md
│   ├── architecture-diagram.drawio(.png)
│   └── erd-diagram.drawio(.png)
├── scripts/
│   ├── start-cicd-stack.ps1              # Start SonarQube & webhook relay
│   ├── stop-cicd-stack.ps1               # Stop the CI/CD support stack
│   ├── run-screencast-demo.ps1           # Coverage run + log output
│   └── open-test-reports.ps1            # Open Karate & JaCoCo reports
└── src/
    ├── main/
    │   ├── java/com/library/api/
    │   │   ├── controller/
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── model/
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   └── response/
    │   │   ├── mapper/
    │   │   ├── exception/
    │   │   └── util/
    │   └── resources/
    │       ├── application.properties
    │       ├── schema.sql
    │       └── data.sql
    └── test/
        └── java/com/library/api/
```

> See [`docs/PROJECT_STRUCTURE.md`](docs/PROJECT_STRUCTURE.md) for package and layer responsibilities.

---

## CI/CD Pipeline

The project ships with a Jenkins declarative pipeline (`Jenkinsfile`) designed for a local CI/CD stack.

### Pipeline stages

| Stage | JDK | Description |
|---|---|---|
| Preflight Validation | — | Validates required tools, credentials, and plugins before any work begins |
| Verify Tooling | 25 | Confirms Maven and JDK resolution |
| Build and Test | 25 | `mvn clean verify` — compiles, runs JUnit + Karate tests, archives results |
| Coverage + SonarQube | 21 | `mvn -Djacoco.skip=false clean verify sonar:sonar` — generates JaCoCo report and pushes to SonarQube |
| Package Artifact | 25 | `mvn -DskipTests clean package` — produces the deployable JAR |
| Build Docker Image | — | Builds and tags the Docker image |
| Deploy Locally | — | Runs the container on port `8082` (master branch only), health-checks with retry |

### Local URLs

| Service | URL |
|---|---|
| Jenkins | `http://localhost:8080` |
| SonarQube | `http://localhost:9000` |
| Deployed app (CI/CD) | `http://localhost:8082` |

> The app container exposes port `8082` on the host to avoid a conflict with Jenkins on `8080`.

### Starting the support stack

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-cicd-stack.ps1
```

This starts SonarQube, its PostgreSQL backend, and (optionally) the webhook relay via Docker Compose.

### Configuring the Jenkins job

1. Open `http://localhost:8080` → **New Item** → **Pipeline**
2. Set **Pipeline script from SCM**, SCM = **Git**, repository URL = `https://github.com/flaviu-vanca/library-management-api.git`, branch = `*/master`, script path = `Jenkinsfile`
3. Save and run once to register the `githubPush()` trigger

**Required Jenkins global tools** (Manage Jenkins → Tools):

| Name | Purpose |
|---|---|
| `JDK25` | Build, test, and package stages |
| `JDK21` | Coverage + SonarQube stage |
| `Maven3` | All Maven stages |

**Required credential** (Manage Jenkins → Credentials): `sonarqube-token` (Secret text)

**Required plugins:** Git, Pipeline, GitHub, JUnit, Maven Integration, Credentials Binding, Docker Pipeline, HTTP Request

### GitHub webhook relay

To trigger builds on push, expose your local Jenkins with [ngrok](https://ngrok.com/) or [smee.io](https://smee.io/) and register the forwarded URL as a GitHub webhook (`{relay-url}/github-webhook/`).

---

## Testing

Tests are executed in two modes because JaCoCo 0.8.12 does not fully support Java 25 class files; JaCoCo is therefore skipped by default.

### Default test run (Java 25)

```bash
mvn test
```

### Coverage run (Java 21)

```powershell
$env:JAVA_HOME = 'C:\Path\To\Your\JDK-21'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
mvn clean -Djacoco.skip=false test
```

Generated outputs:

```
target/surefire-reports/
target/karate-reports/
target/site/jacoco/index.html
```

Open the HTML reports after the coverage run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\open-test-reports.ps1
```

---

## Error Handling

All errors are returned as structured JSON payloads by `GlobalExceptionHandler`:

| HTTP Status | Trigger |
|---|---|
| `400 Bad Request` | Validation failure, `BadRequestException`, `IllegalArgumentException` |
| `404 Not Found` | `ResourceNotFoundException` |
| `500 Internal Server Error` | Unhandled exceptions (fallback) |

See [`docs/ERROR_RESPONSE_EXAMPLES.md`](docs/ERROR_RESPONSE_EXAMPLES.md) for example payloads.

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'feat: add your feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a pull request against `master`

Please ensure `mvn clean verify` passes before submitting a pull request.

---

## License

Educational project for Microservices Architecture coursework.
