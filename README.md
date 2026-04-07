# 📚 Library Management REST API

<p align="center">
  <a href="https://github.com/flaviu-vanca/library-management-api">
    <img alt="GitHub repo" src="https://img.shields.io/badge/repo-library--management--api-24292e?logo=github&logoColor=white" />
  </a>
  <img alt="Java" src="https://img.shields.io/badge/Java-25-007396?logo=openjdk&logoColor=white" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.7-6DB33F?logo=springboot&logoColor=white" />
  <img alt="Maven" src="https://img.shields.io/badge/Maven-3.9+-C71A36?logo=apachemaven&logoColor=white" />
  <img alt="Database" src="https://img.shields.io/badge/DB-H2-1f6feb?logo=databricks&logoColor=white" />
  <img alt="Tests" src="https://img.shields.io/badge/Tests-JUnit%205%20%7C%20Mockito%20%7C%20Karate-0B7285" />
  <img alt="Coverage" src="https://img.shields.io/badge/Coverage-JaCoCo-BD1E59" />
</p>

<p align="center">
  <b>RESTful API for library management built with Spring Boot.</b><br/>
  Entity relationships • DTO-based contracts • Pagination • Date filtering • Centralized error handling
</p>

<p align="center">
  <a href="#-project-scope">🎯 Scope</a> ·
  <a href="#-technology-stack">🛠️ Stack</a> ·
  <a href="#-quick-start">🚀 Quick Start</a> ·
  <a href="#-api-overview">🔌 API</a> ·
  <a href="#-cicd-with-jenkins">🤖 CI/CD</a> ·
  <a href="#-testing">🧪 Testing</a>
</p>

---

## 🎯 Project Scope

This project demonstrates common backend API patterns and best practices:

- **Domain model & relationships:** one-to-many model (**one Library → many Books**)
- **Clean API contracts:** request/response **DTOs** separated from persistence entities
- **CRUD operations:** libraries and books
- **Pagination & sorting:** for list endpoints
- **Date-based filtering:** publication and acquisition date filters
- **Consistent error responses:** centralized exception handling with structured JSON payloads

---

## 🛠️ Technology Stack

- **Java 25**
- **Spring Boot 3.5.7**
- Spring Web, Spring Data JPA
- H2 in-memory database
- Maven
- Lombok
- Testing: JUnit 5, Mockito, Karate
- Quality: JaCoCo

---

## 🚀 Quick Start

### 📋 Prerequisites

- **Java 25** (default development and test runs)
- **Java 21** (required to generate the JaCoCo HTML report)
- **Maven 3.9+**
- **Docker Desktop** (for SonarQube, the webhook relay, and local container deployment)

### ▶️ Run Locally

```bash
mvn clean install
mvn spring-boot:run
```

---

## 🤖 CI/CD with Jenkins

This repository is designed to work with a local CI/CD setup:

- Jenkins: `http://localhost:8080`
- SonarQube: `http://localhost:9000`
- Deployed application: `http://localhost:8082`

**Why the app runs on `8082` in CI/CD mode**

- Jenkins uses `http://localhost:8080`
- the deployed app container publishes to **8082** to avoid a port conflict
- the app still listens on **8080** inside its container

### 📦 Stack files

- `compose.yaml`: SonarQube, PostgreSQL, webhook relay
- `Jenkinsfile`: pipeline definition (for Jenkins on `8080`)
- `Dockerfile`: runtime image for the Spring Boot application
- `scripts/start-cicd-stack.ps1`: start SonarQube and the webhook relay
- `scripts/stop-cicd-stack.ps1`: stop SonarQube and the webhook relay

### 🧰 Start supporting services

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-cicd-stack.ps1
```

This starts:

- SonarQube
- PostgreSQL (for SonarQube)
- optional webhook relay

> Jenkins is expected to already be running locally.

The stack expects **Docker Desktop** to be running in **Linux containers** mode.

### ⚙️ Configure the Jenkins job

Create a Jenkins **Pipeline** job in your existing Jenkins instance:

1. Open `http://localhost:8080`
2. Create a new item of type **Pipeline**
3. In the job configuration:
   - choose **Pipeline script from SCM**
   - set **SCM** to **Git**
   - set repository URL to `https://github.com/flaviu-vanca/library-management-api.git`
   - set the branch to `*/master`
   - set **Script Path** to `Jenkinsfile`
4. Save
5. Run once manually so Jenkins loads the `Jenkinsfile` and registers the `githubPush()` trigger

### ✅ Required Jenkins configuration (preflight-validated)

The `Preflight Validation` stage in `Jenkinsfile` fails early with clear messages if anything is missing.

#### 🧱 Global tools

Configure these in **Manage Jenkins → Tools** with the exact names below:

- JDK named `JDK25` (default build/test/package stages)
- JDK named `JDK21` (coverage + Sonar stage)
- Maven named `Maven3`

#### 🔐 Credentials

Configure in **Manage Jenkins → Credentials**:

- Secret text credential with ID `sonarqube-token`

#### 🧩 Plugins used by the pipeline

Install/enable before running the job:

- Git plugin
- Pipeline plugin
- GitHub plugin
- JUnit plugin
- Maven Integration plugin
- Credentials Binding plugin
- Docker Pipeline plugin
- HTTP Request plugin

> The pipeline archives JaCoCo and Karate HTML outputs as build artifacts (no extra report publisher plugins required).

### 🔁 Real GitHub push trigger

To demonstrate push-triggered builds from GitHub to local Jenkins, run a webhook relay.

#### Option 1: ngrok (current setup)

1. Install ngrok from `https://ngrok.com/`
2. Start a tunnel to Jenkins:
   ```bash
   ngrok http 8080
   ```
3. Copy the forwarding URL from ngrok output (example: `https://<your-id>.ngrok-free.dev`)
4. In GitHub repository settings → **Webhooks**, add a webhook:
   - Payload URL: `{ngrok-url}/github-webhook/`
   - Content type: `application/json`
   - Events: Push events
5. Ensure the job is configured and the GitHub webhook trigger is enabled
6. Push a commit

**Important**

- Keep the ngrok terminal running
- Free tier URLs change on restart; update your GitHub webhook if ngrok restarts

#### Option 2: smee.io (alternative)

1. Create a channel at `https://smee.io/`
2. Start the relay:
   ```bash
   npm install -g smee-client
   smee -u https://smee.io/{your-channel} -t http://localhost:8080/github-webhook/
   ```
3. In GitHub repository settings → **Webhooks**, add a webhook pointing to your smee channel
4. Keep the relay running

---

## 🧪 Testing

The automated suite is executed in two different modes:

1. **Default verification run (Java 25)**
2. **Coverage run (Java 21)** — required because **JaCoCo 0.8.12** does not fully support Java 25 class files.

### 1) Default test run (Java 25)

```bash
mvn test
```

### 2) Coverage run (Java 21)

```powershell
$env:JAVA_HOME='C:\Path\To\Your\JDK-21'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn clean -Pcoverage-java21 test
```

Generated outputs:

```text
target/surefire-reports/
target/karate-reports/
target/site/jacoco/index.html
```

### 3) Screencast helper script

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-screencast-demo.ps1
```

### 4) Open the generated HTML reports

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\open-test-reports.ps1
```

---

## 📜 Helper Scripts

| Script | Purpose |
|--------|---------|
| `scripts/run-screencast-demo.ps1` | Locates a Java 21 installation, runs the coverage-compatible test suite, writes console output to `coverage-test-run.log`, and regenerates `target/site/jacoco/index.html`. |
| `scripts/open-test-reports.ps1` | Opens the Karate summary report and the JaCoCo coverage report in the browser after the coverage run finishes. |

---

## 🌐 Local URLs

- Development API base URL: `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console`
- CI/CD deployed app URL: `http://localhost:8082`
  - JDBC URL: `jdbc:h2:mem:librarydb`
  - Username: `sa`
  - Password: (blank)

---

## 🔌 API Overview

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

---

## ⚠️ Error Handling

The API returns structured error payloads via `GlobalExceptionHandler`, including:

- **404 Not Found** (`ResourceNotFoundException`)
- **400 Bad Request** (`BadRequestException`, `MethodArgumentNotValidException`, `IllegalArgumentException`)
- **500 Internal Server Error** (fallback)

Examples are documented in `docs/ERROR_RESPONSE_EXAMPLES.md`.

---

## 🗺️ Documentation Map

To avoid duplication, each documentation file has a single responsibility:

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

---

## 📝 Development Notes

- H2 runs in-memory; data resets on restart.
- Schema and seed data live in `src/main/resources/schema.sql` and `src/main/resources/data.sql`.
- `target/` is build output only and is regenerated by the latest Maven run.

---

## 📄 License

Educational project for Microservices Architecture coursework.