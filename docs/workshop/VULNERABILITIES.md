# Vulnérabilités pédagogiques — workshop/vuln-demo
> ⚠️ Branche isolée, ne jamais merger dans main.

| # | Fichier | Ligne | Problème | CWE | SonarQube | Semgrep | Gravité | Exploitable ? |
|---|---------|-------|----------|-----|-----------|---------|---------|---------------|
| 1 | VulnerableDemoController.java | 35 | SQLi concaténation | CWE-89 | java:S2077 | sqli | Critique | Oui (`q=' OR 1=1--`) |
| 2 | VulnerableDemoController.java | 50 | XSS HTML dans JSON | CWE-79 | java:S5131* | xss | Moyenne | **Non en JSON**, Oui si innerHTML |
| 3 | VulnerableDemoController.java | 64 | Path Traversal | CWE-22 | java:S2083 | path-traversal | Haute | Oui (`../../etc/passwd`) |
| 4 | VulnerableDemoController.java | 23 | Hardcoded secret | CWE-798 | java:S6418 | secrets | Haute | Oui |
| 5 | VulnerableDemoController.java | 74 | Weak Random | CWE-338 | java:S2245 | weak-random | Moyenne | Oui |
| 6 | VulnerableDemoController.java | 87 | Log Injection | CWE-117 | java:S5145 | log-injection | Basse | Oui |
