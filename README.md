# 📚 Library Management REST API

REST API for managing libraries and books with one-to-many relationships, DTO-based API contracts, pagination, filtering, and centralized exception handling.

## 🎯 Project Scope

This project demonstrates:
- One-to-many data model: one library has many books
- CRUD endpoints for libraries and books
- Pagination and sorting on list endpoints
- Date filtering by publication and acquisition dates
- Request/response DTO separation
- Consistent JSON error responses

## 🛠️ Technology Stack

- Java 25
- Spring Boot 3.5.7
- Spring Web + Spring Data JPA
- H2 in-memory database
- Maven
- Lombok
- JUnit 5 + Mockito
- Karate
- JaCoCo

## 🚀 Quick Start

### 📋 Prerequisites

- Java 25 for normal development and default test runs
- Java 21 if you want to generate the JaCoCo coverage report
- Maven 3.9+

### ▶️ Run Locally

```bash
mvn clean install
mvn spring-boot:run
```

### ✅ Testing Procedure

This project uses the same automated suite in two different runs:

- a normal verification run under Java 25
- a coverage-generation run under Java 21

Why two runs are needed:

- the default project configuration targets Java 25
- JaCoCo `0.8.12` does not fully support Java 25 class files
- the `coverage-java21` Maven profile recompiles and runs the same suite under Java 21 so coverage HTML can be generated

#### 1. Default test run on Java 25

```bash
mvn test
```

Use this for the standard automated test pass.

#### 2. Coverage run on Java 21

Use this when you need coverage output for the assignment report or screencast:

```powershell
$env:JAVA_HOME='C:\Path\To\Your\JDK-21'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn clean -Pcoverage-java21 test
```

If you have multiple Java versions installed, update `JAVA_HOME` to your Java 21 installation before running this command. This makes Maven compile and run the suite with Java 21 so JaCoCo can generate the coverage report successfully.

Generated outputs:

```text
target/surefire-reports/
target/karate-reports/
target/site/jacoco/index.html
```

Notes:

- `target/` always reflects the most recent Maven run
- `mvn clean` removes the previous `target/` output before regenerating it

#### 3. Screencast helper script

For the assignment screencast, use the prepared Windows script:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-screencast-demo.ps1
```

This script:

- switches to a Java 21 environment for coverage compatibility
- runs `mvn clean -Pcoverage-java21 test`
- writes the terminal output to `coverage-test-run.log`
- regenerates `target/site/jacoco/index.html`

#### 4. Open the generated HTML reports

After the coverage run, open the HTML reports with:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\open-test-reports.ps1
```

This opens:

- `target/karate-reports/karate-summary.html`
- `target/karate-reports/karate.books.html`
- `target/karate-reports/karate.libraries.html`
- `target/site/jacoco/index.html`

### 🌐 Local URLs

- API base URL: `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:librarydb`
  - Username: `sa`
  - Password: (blank)

## 🧪 Test Suite Overview

The automated suite is organized by layer:

- Unit tests: services, mappers, utilities, and base service helpers
- Repository tests: JPA slice tests with H2
- Controller tests: HTTP-layer tests with Spring MVC test support
- Integration tests: end-to-end Spring Boot lifecycle flow
- Karate tests: API-level feature scenarios with HTML reporting

Main test locations:

- `src/test/java/com/library/api/service`
- `src/test/java/com/library/api/repository`
- `src/test/java/com/library/api/controller`
- `src/test/java/com/library/api/integration`
- `src/test/java/com/library/api/karate`
- `src/test/resources/karate`

## 🔌 API Summary

### 🏛️ Library endpoints

- `GET /api/libraries`
- `GET /api/libraries/{id}`
- `POST /api/libraries`
- `PUT /api/libraries/{id}`
- `DELETE /api/libraries/{id}`
- `GET /api/libraries/{id}/books`

### 📖 Book endpoints

- `GET /api/books`
- `GET /api/books/{id}`
- `POST /api/books`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`
- `GET /api/books/by-publication-date`
- `GET /api/books/by-acquisition-date`

## ⚠️ Error Handling

The API returns structured error payloads via `GlobalExceptionHandler` for:
- `404 Not Found` (`ResourceNotFoundException`)
- `400 Bad Request` (`BadRequestException`, `MethodArgumentNotValidException`, `IllegalArgumentException`)
- `500 Internal Server Error` (fallback)

Detailed examples are documented in `docs/ERROR_RESPONSE_EXAMPLES.md`.

## 🗺️ Documentation Map

To avoid duplicate documentation, each file has a single responsibility:

- `README.md`: project overview, quick start, high-level API map
- `docs/PROJECT_STRUCTURE.md`: package layout and layer responsibilities
- `docs/ERROR_RESPONSE_EXAMPLES.md`: canonical error payload examples
- `docs/LOMBOK_SETUP.md`: IDE/Lombok setup and troubleshooting
- `docs/SCREENCAST_ASSIGNMENT_SCRIPT.md`: assignment-aligned screencast flow
- `docs/SCREENCAST_READ_ALOUD.md`: read-aloud narration for the screencast
- `docs/SCREENCAST_SHOW_GUIDE.md`: optional presentation order for test evidence

Additional artifacts:
- `docs/architecture-diagram.drawio` and `docs/architecture-diagram.drawio.png`
- `docs/erd-diagram.drawio` and `docs/erd-diagram.drawio.png`

## 📝 Development Notes

- H2 runs in-memory; data resets on restart.
- Schema and seed data live in `src/main/resources/schema.sql` and `src/main/resources/data.sql`.
- `target/` is build output only and is regenerated by the latest Maven run.

## 📄 License

Educational project for Microservices Architecture coursework.

