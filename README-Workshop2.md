# Workshop 2 — Sécurité du Code Source (SAST)

> **Dépôt :** `workshop2-8` — Groupe : Lhoussaine ouzziki, Hasna elhassani, Zineb hajaj 
> **Projet :** [library-management](https://github.com/OuzzikiLhoussaine/workshop2-G8) — Java 25, Spring Boot 3.5.7, Maven, H2
> **Pipeline :** GitHub Actions (ou Jenkins) — SonarQube via Docker + Semgrep

---

## 1. Objectif du workshop

Dans ce workshop, nous avons travaillé sur un projet contenant plusieurs vulnérabilités intentionnelles (injections, XSS, mauvaises pratiques). Nous avons configuré une analyse SAST avec **SonarQube (via Docker)** et intégré l'analyse dans un **pipeline CI simple**. Ensuite, nous avons utilisé **Semgrep** pour détecter des patterns plus fins et comparé les résultats avec SonarQube. L'objectif était de comprendre les limites, complémentarités et cas d'usage de chaque outil, tout en corrigeant les vulnérabilités trouvées.

## 2. Répartition de l'équipe

| Membre | Rôle | Tâches | Relecture |
|---|---|---|---|
| **Lhoussaine** (Ubuntu natif) | Infra + Pipeline | SonarQube Docker, Jenkinsfile.linux, intégration CI, correction SQLi | Relit Hasna |
| **Hasna** (WSL) | Analyste SAST | Branche vuln-demo, Semgrep, comparaison SonarQube vs Semgrep, correction XSS | Relit Zayneb |
| **Zineb** (WSL) | Doc + Tests | Relecture, tests non-régression, documentation, corrections Path/Secret/Random | Relit Lhoussaine |

*Chaque membre peut expliquer les deux workshops (SAST et SCA).*

## 3. Architecture

- **Lhoussaine (Ubuntu natif) — serveur principal :** SonarQube `:9000` + PostgreSQL + Jenkins `:8080` + App `:8082` via `compose.yaml`
- **Hasna & Zineb (WSL) — accès réseau :** `http://<IP-Lhoussaine>:9000` pour SonarQube, `git push` déclenche Jenkins
- **Outils :** SonarQube Community (Docker), Semgrep (`pip install semgrep`), Maven, Java 25 + 21 (JaCoCo)

![Architecture](docs/screenshots/architecture.png)

## 4. Pipeline CI (GitHub Actions)

Fichier : `.github/workflows/sast.yml` — 5 jobs : Build/Test (Java 25) → Coverage+SonarQube (Java 21) → Semgrep → Package → Docker Deploy

![Pipeline](docs/screenshots/pipeline.png)

**Lien workflow :** [sast.yml](.github/workflows/sast.yml)

## 5. Réalisation étape par étape

### Étape 1 — Code propre (branche `main`)
- `mvn clean verify` → BUILD SUCCESS (47 tests)
- SonarQube scan sur `main` → **0 vulnérabilité critique**, Coverage 62%

![SonarQube Clean](docs/screenshots/sonarqube-clean.png)

### Étape 2 — Branche vulnérable `workshop/vuln-demo`
Fichier `src/main/java/com/library/api/controller/VulnerableDemoController.java` (5 failles) :

| # | CWE | Problème | Gravité |
|---|---|---|---|
| 1 | CWE-89 | SQL Injection (concaténation) | Critique |
| 2 | CWE-79 | XSS HTML dans JSON* | Moyenne |
| 3 | CWE-22 | Path Traversal | Haute |
| 4 | CWE-798 | Hardcoded Secret | Haute |
| 5 | CWE-338 | Weak Random | Moyenne |

*Non exploitable en JSON pur — faux positif pédagogique expliqué.

### Étape 3 — Scans comparatifs

| Outil | Résultat sur `vuln-demo` | Faux positifs/négatifs |
|---|---|---|
| **SonarQube** | 5 vulnérabilités (java:S2077, S2083, S6418, S2245, S5145) | XSS non détectée en JSON (attendu) |
| **Semgrep** | 5 findings (sqli, xss, path-traversal, secrets, weak-random) | XSS en JSON = faux positif si JSON |
| **Conclusion** | Complémentaires : SonarQube pour Quality Gate + historique, Semgrep pour rapidité + règles custom | |

![Semgrep Vuln](docs/screenshots/semgrep-vuln.png)
![SonarQube Vuln](docs/screenshots/sonarqube-vuln.png)

### Étape 4 — Corrections + Re-scan

| # | Correction | Auteur | Avant → Après |
|---|---|---|---|
| 1 | SQLi → `setParameter("title", ...)` | Lhoussaine | 5 → 4 |
| 2 | XSS → `HtmlUtils.htmlEscape(name)` + doc JSON | Hasna | 5 → 4 |
| 3 | Path Traversal → `target.startsWith(base)` | Zineb | 4 → 3 |
| 4 | Secret → `@Value("${app.api-key}")` | Zineb | 3 → 2 |
| 5 | Weak Random → `SecureRandom` | Zineb | 2 → 1 → **0** |

Tests non-régression ajoutés : `VulnerableDemoControllerTest.java` (4 tests) → `mvn test` BUILD SUCCESS

![SonarQube Fixed](docs/screenshots/sonarqube-fixed.png)
![Semgrep Fixed](docs/screenshots/semgrep-fixed.png)

## 6. Limites et apprentissages

- SAST ne détecte pas les failles logiques (ex: auth manquante)
- XSS en JSON = faux positif — contexte important
- JaCoCo 0.8.12 ne supporte pas Java 25 → profil `coverage-java21`
- SonarQube ≠ Quality Gate (envoi ≠ validation) → `waitForQualityGate` pour bloquer

## 7. Comment exécuter

```bash
git clone https://github.com/OuzzikiLhoussaine/workshop2-8.git
cd workshop2-8
docker compose up -d  # SonarQube :9000
mvn clean verify  # Java 25
export SONAR_TOKEN=squ_xxx
mvn -Pcoverage-java21 verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.projectKey=workshop2-{{Groupe}}
pip install semgrep && semgrep --config=p/java --json --output semgrep.json
```

## 8. Captures

| Capture | Description |
|---|---|
| `01-env.png` | java -version, mvn -version, docker ps |
| `02-sonarqube-clean.png` | Dashboard 0 vuln |
| `03-pipeline.png` | GitHub Actions vert |
| `04-semgrep-vuln.png` | 5 findings |
| `05-sonarqube-vuln.png` | 5 vuln |
| `06-corrections.png` | git log fix |
| `07-sonarqube-fixed.png` | 0 vuln après |


## 9. Contributions

| Membre | Commits | Branches | Démo |
|---|---|---|---|
| Lhoussaine | Jenkinsfile.linux, fix SQLi | feat/sonarqube-lhoussaine, fix/vuln-corrections | 5 min Infra |
| Hasna | vuln-demo, semgrep, fix XSS | feat/semgrep | 5 min SAST |
| Zineb | docs, fix Path/Secret/Random | feat/docs| 5 min Tests |

---

*Projet compile sans erreur : `mvn clean verify` → BUILD SUCCESS*
