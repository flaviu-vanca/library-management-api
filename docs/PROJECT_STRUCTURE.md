# 🏗️ Project Structure

This document describes package layout and layer responsibilities only.

For setup and endpoint usage, use `README.md`.
For concrete error payloads, use `docs/ERROR_RESPONSE_EXAMPLES.md`.

## 🧩 Domain Model

- `Library` (parent) has many `Book` (child)
- `Book.library_id` references `Library.id`
- Delete on library cascades to books

## 📁 Directory Layout

```text
library-management-api/
├── Dockerfile
├── Jenkinsfile
├── compose.yaml
├── pom.xml
├── README.md
├── docs/
│   ├── PROJECT_STRUCTURE.md
│   ├── ERROR_RESPONSE_EXAMPLES.md
│   ├── LOMBOK_SETUP.md
│   ├── architecture-diagram.drawio
│   ├── architecture-diagram.drawio.png
│   ├── erd-diagram.drawio
│   └── erd-diagram.drawio.png
├── scripts/
│   ├── start-cicd-stack.ps1
│   ├── stop-cicd-stack.ps1
│   ├── run-screencast-demo.ps1
│   └── open-test-reports.ps1
├── src/
│   ├── main/
│   │   ├── java/com/library/api/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── mapper/
│   │   │   ├── exception/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql
│   │       └── data.sql
│   └── test/
│       └── java/com/library/api/
```

## 🏛️ Layer Responsibilities

### Controller Layer (`controller/`)

- Exposes HTTP endpoints
- Parses query/path/body input
- Delegates to services
- Returns typed response DTOs

### Service Layer (`service/`)

- Implements business rules
- Coordinates repositories and mappers
- Throws domain exceptions for invalid operations

### Repository Layer (`repository/`)

- Encapsulates database access with Spring Data JPA
- Provides CRUD and query methods

### Model Layer (`model/`)

- Defines JPA entities and relationships
- Maps to database schema

### DTO Layer (`dto/request`, `dto/response`)

- Separates API contracts from persistence entities
- Applies request validation constraints

### Mapper Layer (`mapper/`)

- Converts entity to DTO and DTO to entity
- Keeps controller/service code clean and explicit

### Exception Layer (`exception/`)

- Defines custom exception types
- Centralizes error response shaping via `GlobalExceptionHandler`

### Utility Layer (`util/`)

- Shared helpers, currently pagination construction logic

## 🔑 Key Files

- App entry point: `src/main/java/com/library/api/LibraryManagementApplication.java`
- API controllers: `src/main/java/com/library/api/controller/`
- Exception handler: `src/main/java/com/library/api/exception/GlobalExceptionHandler.java`
- Schema and seed data: `src/main/resources/schema.sql`, `src/main/resources/data.sql`
- CI/CD pipeline: `Jenkinsfile`
- Local CI/CD support services: `compose.yaml`
- Assignment helper scripts: `scripts/`

## 🧹 Maintenance Rule

When package names or directories change, update this file only.
Do not duplicate endpoint details or error payload examples here.
