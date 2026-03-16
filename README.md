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

## 🚀 Quick Start

### 📋 Prerequisites

- Java 25
- Maven 3.9+

### ▶️ Run Locally

```bash
mvn clean install
mvn spring-boot:run
```

### ✅ Test Commands

Default test run on Java 25:

```bash
mvn test
```

Coverage run for the assignment screencast:

```powershell
$env:JAVA_HOME='C:\Users\skety\.sdkman\candidates\java\21.0.9-oracle'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn clean -Pcoverage-java21 test
```

Coverage report output:

```text
target/site/jacoco/index.html
```

### 🌐 Local URLs

- API base URL: `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:librarydb`
  - Username: `sa`
  - Password: (blank)

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

For full request examples, use the Postman collection at `postman/Library_API_Collection.json`.

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

Additional artifacts:
- `docs/architecture-diagram.drawio` and `docs/architecture-diagram.drawio.png`
- `docs/erd-diagram.drawio` and `docs/erd-diagram.drawio.png`

## 📝 Development Notes

- H2 runs in-memory; data resets on restart.
- Schema and seed data live in `src/main/resources/schema.sql` and `src/main/resources/data.sql`.
- Postman collection: `postman/Library_API_Collection.json`.

## 📄 License

Educational project for Microservices Architecture coursework.
