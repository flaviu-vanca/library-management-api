# Comparaison SonarQube vs Semgrep — à compléter après scans
| Critère | SonarQube Community | Semgrep |
|---------|---------------------|---------|
| Install | Docker 2Go | pip 50Mo |
| Temps | 30-60s | 5-10s |
| SQLi | ✅ | ✅ |
| XSS JSON | ⚠️ Non (faux négatif) | ✅ (mais faux positif si JSON) |
| Secrets | ✅ Hotspot | ✅ |
| Path Trav | ✅ | ✅ |
| Weak Random | ✅ | ✅ |

## Résultats réels du 20/10/2026

| CWE | SonarQube | Semgrep | Commentaire |
|-----|-----------|---------|-------------|
| CWE-89 SQLi | ✅ java:S2077 trouvée | ✅ trouvée | Les 2 font Taint Analysis (source q → sink createNativeQuery) |
| CWE-79 XSS | ❌ Non trouvée (JSON) | ✅ trouvée | SonarQube attend du HTML, Semgrep flag tout concat HTML → faux positif Semgrep en JSON |
| CWE-22 Path Traversal | ✅ | ✅ | |
| CWE-798 Secret | ✅ Hotspot | ✅ | SonarQube met en Hotspot à valider manuellement |
| CWE-338 Weak Random | ✅ | ✅ | |
| **Faux positifs** | H2 password="" flaggé à tort | XSS en JSON flaggé à tort | À documenter |

## Limites & Complémentarités
- SonarQube : meilleur pour Quality Gate, historique, coverage. Lourd (Docker), règles fermées.
- Semgrep : rapide, règles custom faciles (`--config=p/java` éditable), mais pas de taint analysis profonde en community.
- **Ils sont complémentaires :** SonarQube en CI (bloquant), Semgrep en local/IDE (rapide).
